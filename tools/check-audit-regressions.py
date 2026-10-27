#!/usr/bin/env python3
"""Check cache behavior and the worker wait protocol without Minecraft/OpenGL.

Minecraft boundaries are stubbed. Helpers/mixin bodies are real; only the test
copy of EntityLivingMixin's constructor is made protected for a concrete entity.
The worker harness compiles the actual getNextJob method in a minimal executor.
"""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
src = root / 'src/main/java'
java = Path('/usr/lib/jvm/java-8-openjdk/bin')
cache = Path.home() / '.gradle/caches/modules-2/files-2.1'
classpath = ':'.join(str(next((cache / path).glob('*/*.jar'))) for path in (
    'org.spongepowered/mixin/0.7.4-SNAPSHOT', 'it.unimi.dsi/fastutil/7.1.0'))


def method(source, signature):
    start = source.index(signature)
    brace = source.index('{', start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


builder = (src / 'me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java').read_text()
wait_method = method(builder, 'private WrappedTask<T> getNextJob()').replace('WrappedTask<T>', 'Object').replace('ChunkBuilder.this', 'WorkerCheck.this')
thread_method = method(builder, 'private static int getThreadCount()').replace('SodiumClientMod.options().performance.chunkBuilderThreads', 'requestedThreads')
stubs = {
    'net/minecraft/block/properties/IProperty.java': 'package net.minecraft.block.properties; public interface IProperty<T extends Comparable<T>> { java.util.Collection<T> getAllowedValues(); }',
    'net/minecraft/block/state/IBlockState.java': 'package net.minecraft.block.state; public interface IBlockState {}',
    'net/minecraft/world/World.java': '''package net.minecraft.world; import dev.froyln.liteoptimizer.world.*;
public class World implements ICountingWorld { public final EntityTypeCounts counts=new EntityTypeCounts(); public EntityTypeCounts liteoptimizer$counts(){return counts;} }''',
    'net/minecraft/entity/Entity.java': 'package net.minecraft.entity; public class Entity {}',
    'net/minecraft/entity/EntityLivingBase.java': 'package net.minecraft.entity; public class EntityLivingBase extends Entity { public net.minecraft.world.World world; public EntityLivingBase(net.minecraft.world.World w){world=w;} }',
    'net/minecraft/entity/item/EntityItem.java': 'package net.minecraft.entity.item; public class EntityItem extends net.minecraft.entity.Entity {}',
    'net/minecraft/entity/EntityLiving.java': '''package net.minecraft.entity;
import java.lang.reflect.*; import dev.froyln.liteoptimizer.world.*; import dev.froyln.liteoptimizer.mixin.world.entity_type_counts.EntityLivingMixin;
public class EntityLiving extends EntityLivingMixin implements ICountedEntity {
 public boolean counted;
 public EntityLiving(net.minecraft.world.World w){super(w);}
 public boolean liteoptimizer$isCounted(){return counted;} public void liteoptimizer$setCounted(boolean c){counted=c;}
 public boolean isNoDespawnRequired(){try{Field f=EntityLivingMixin.class.getDeclaredField("persistenceRequired");f.setAccessible(true);return f.getBoolean(this);}catch(Exception e){throw new RuntimeException(e);}}
 public void readPersistence(boolean value) throws Exception {
  for(Method m:EntityLivingMixin.class.getDeclaredMethods()) {
   org.spongepowered.asm.mixin.injection.Redirect a=m.getAnnotation(org.spongepowered.asm.mixin.injection.Redirect.class);
   if(a!=null && java.util.Arrays.asList(a.method()).contains("readEntityFromNBT")){m.setAccessible(true);m.invoke(this,this,value);return;}
  }
  Field f=EntityLivingMixin.class.getDeclaredField("persistenceRequired");f.setAccessible(true);f.setBoolean(this,value);
 }
}''',
    'net/minecraft/item/Item.java': 'package net.minecraft.item; public class Item {}',
    'net/minecraft/item/ItemStack.java': '''package net.minecraft.item; public class ItemStack {
 public static final ItemStack EMPTY=new ItemStack(null,0); private final Item item; private final int meta;
 public ItemStack(Item i,int m){item=i;meta=m;} public Item getItem(){return item;} public int getMetadata(){return meta;} public boolean isEmpty(){return item==null;}
}''',
    'net/minecraft/item/crafting/FurnaceRecipes.java': 'package net.minecraft.item.crafting; public class FurnaceRecipes {}',
    'CacheCheck.java': r'''
import java.util.*; import java.lang.reflect.*;
import net.minecraft.entity.*; import net.minecraft.world.World;
import net.minecraft.block.properties.IProperty; import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*; import dev.froyln.liteoptimizer.memory.StateTable;
import dev.froyln.liteoptimizer.mixin.world.furnace_recipe_cache.FurnaceRecipesMixin;
import org.spongepowered.asm.mixin.injection.callback.*;
public class CacheCheck {
 static class Property implements IProperty<Integer> {
  public Collection<Integer> getAllowedValues(){return Arrays.asList(0,1);}
  public boolean equals(Object o){return o instanceof Property;} public int hashCode(){return 1;}
 }
 static class State implements IBlockState {}
 static void states() {
  Property p=new Property(); Map<Map<IProperty<?>,Comparable<?>>,IBlockState> all=new HashMap<>();
  Map<IProperty<?>,Comparable<?>> zero=Collections.<IProperty<?>,Comparable<?>>singletonMap(p,0);
  State a=new State(),b=new State(); all.put(zero,a);all.put(Collections.<IProperty<?>,Comparable<?>>singletonMap(p,1),b);
  StateTable table=StateTable.forContainer(all,Arrays.<IProperty<?>>asList(p)); int index=table.indexOf(zero);
  assert table.with(index,p,1)==b;
  assert table.with(index,new Property(),1)==b : "equal property must find neighbor";
  assert table.with(index,new Property(),2)==null;
 }
 static void persistence() throws Exception {
  World w=new World();EntityLiving mob=new EntityLiving(w);List<Entity> loaded=new ArrayList<>();loaded.add(mob);mob.counted=true;
  assert w.counts.count(EntityLiving.class,loaded)==1;
  mob.readPersistence(true); assert w.counts.count(EntityLiving.class,loaded)==0 : "persistent mob still counted";
  mob.readPersistence(true); assert w.counts.count(EntityLiving.class,loaded)==0;
  mob.readPersistence(false); assert w.counts.count(EntityLiving.class,loaded)==1 : "nonpersistent mob not counted";
  mob.readPersistence(false); assert w.counts.count(EntityLiving.class,loaded)==1;
  w.counts.removed(mob);loaded.clear();mob.counted=false;
  assert w.counts.count(EntityLiving.class,loaded)==0;
  mob.readPersistence(true);mob.readPersistence(false);assert w.counts.count(EntityLiving.class,loaded)==0;
 }
 static class Recipes extends FurnaceRecipesMixin {
  final Map<ItemStack,ItemStack> map=new LinkedHashMap<>();
  void call(String name,Object... args) throws Exception {
   for(Method m:FurnaceRecipesMixin.class.getDeclaredMethods())if(m.getName().equals(name)){m.setAccessible(true);m.invoke(this,args);return;}
   throw new AssertionError(name);
  }
  Map<ItemStack,ItemStack> expose() throws Exception {call("liteoptimizer$invalidateOnExpose",new CallbackInfoReturnable<Map<ItemStack,ItemStack>>("expose",true,map));return map;}
  ItemStack lookup(ItemStack key) throws Exception {
   CallbackInfoReturnable<ItemStack> hit=new CallbackInfoReturnable<>("hit",true);call("liteoptimizer$memoHit",key,hit);
   ItemStack result=hit.isCancelled()?hit.getReturnValue():map.get(key);if(result==null)result=ItemStack.EMPTY;
   call("liteoptimizer$memoStore",key,new CallbackInfoReturnable<ItemStack>("store",true,result));return result;
  }
 }
 static void recipes() throws Exception {
  Recipes r=new Recipes();ItemStack key=new ItemStack(new Item(),0),out=new ItemStack(new Item(),0);r.map.put(key,out);
  assert r.lookup(key)==out;assert r.lookup(key)==out;
  Map<ItemStack,ItemStack> live=r.expose();assert r.lookup(key)==out;
  live.remove(key);assert r.lookup(key)==ItemStack.EMPTY : "removed recipe still cached";
  live.put(key,out);assert r.lookup(key)==out : "negative result still cached";
  live.clear();assert r.lookup(key)==ItemStack.EMPTY;
 }
 public static void main(String[] args) throws Exception {
  if(args[0].equals("states"))states();else if(args[0].equals("persistence"))persistence();else recipes();
  System.out.println(args[0]+" check passed");
 }
}
''',
    'WorkerCheck.java': r'''
import java.util.concurrent.*; import java.util.concurrent.atomic.*;
public class WorkerCheck {
 final Object jobNotifier=new Object();final AtomicBoolean running=new AtomicBoolean(true);
 final CountDownLatch polled=new CountDownLatch(1),resume=new CountDownLatch(1),notified=new CountDownLatch(1);
 final ConcurrentLinkedDeque<Object> buildQueue=new ConcurrentLinkedDeque<Object>() {
  boolean first=true;
  public Object poll(){Object job=super.poll();if(first){first=false;polled.countDown();try{if(!resume.await(2,TimeUnit.SECONDS))throw new AssertionError("poll stalled");}catch(InterruptedException e){throw new AssertionError(e);}}return job;}
 };
 class Worker {
 final AtomicBoolean running=WorkerCheck.this.running;
 WAIT_METHOD
 }
 void check(boolean stop) throws Exception {
  AtomicReference<Object> result=new AtomicReference<>();Object task=new Object();
  Thread worker=new Thread(()->result.set(new Worker().getNextJob()));worker.setDaemon(true);worker.start();
  assert polled.await(2,TimeUnit.SECONDS);
  Thread notifier=new Thread(()->{if(stop)running.set(false);else buildQueue.add(task);synchronized(jobNotifier){jobNotifier.notifyAll();}notified.countDown();});
  notifier.setDaemon(true);notifier.start();
  // Old code allows notification to finish while the worker is between poll and wait.
  notified.await(100,TimeUnit.MILLISECONDS);resume.countDown();
  notifier.join(2000);worker.join(2000);
  boolean stuck=worker.isAlive();
  if(stuck){synchronized(jobNotifier){jobNotifier.notifyAll();}worker.join(2000);}
  assert !stuck : "worker missed "+(stop?"shutdown":"task")+" notification";
  assert stop || result.get()==task : "queued task not returned";
 }
 public static void main(String[] args) throws Exception {new WorkerCheck().check(args[0].equals("stop"));System.out.println("worker "+args[0]+" check passed");}
}
'''.replace('WAIT_METHOD', wait_method),
    'ThreadCountCheck.java': '''public class ThreadCountCheck {
 static int requestedThreads;
 static int getOptimalThreadCount(){return 2;} static int getMaxThreadCount(){return 4;}
 THREAD_METHOD
 public static void main(String[] args){
  requestedThreads=-1;assert getThreadCount()==2 : "negative worker count must use automatic setting";
  requestedThreads=0;assert getThreadCount()==2;
  requestedThreads=1;assert getThreadCount()==1;
  requestedThreads=100;assert getThreadCount()==4;
  System.out.println("worker count check passed");
 }
}'''.replace('THREAD_METHOD', thread_method),
}

with tempfile.TemporaryDirectory(prefix='audit-regressions-') as d:
    scratch = Path(d)
    sources = []
    for name, content in stubs.items():
        path = scratch / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        sources.append(str(path))
    for name in ('memory/StateTable.java', 'world/EntityTypeCounts.java', 'world/ICountedEntity.java',
                 'world/ICountingWorld.java', 'mixin/world/furnace_recipe_cache/FurnaceRecipesMixin.java',
                 'mixin/world/entity_type_counts/EntityLivingMixin.java'):
        path = src / 'dev/froyln/liteoptimizer' / name
        if name.endswith('EntityLivingMixin.java'):
            copy = scratch / path.name
            copy.write_text(path.read_text().replace('private EntityLivingMixin(', 'protected EntityLivingMixin('))
            path = copy
        sources.append(str(path))
    subprocess.run([str(java / 'javac'), '-proc:none', '-cp', classpath, '-d', d, *sources], check=True)
    failed = []
    for main, case in [('CacheCheck', 'states'), ('CacheCheck', 'persistence'), ('CacheCheck', 'recipes'),
                       ('WorkerCheck', 'task'), ('WorkerCheck', 'stop'), ('ThreadCountCheck', 'threads')]:
        result = subprocess.run([str(java / 'java'), '-ea', '-cp', d + ':' + classpath, main, case], timeout=15)
        if result.returncode:
            failed.append(case)
    if failed:
        raise SystemExit('Failed checks: ' + ', '.join(failed))
