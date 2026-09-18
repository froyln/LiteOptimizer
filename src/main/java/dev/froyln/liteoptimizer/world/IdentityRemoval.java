package dev.froyln.liteoptimizer.world;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * {@code list.removeAll(toRemove)} for lists of objects that use identity equality (entities,
 * tile entities): one pass over the list against an identity set instead of ArrayList's
 * O(list × toRemove) {@code contains} scan. Survivors keep their order, exactly as removeAll.
 */
public final class IdentityRemoval {
    private IdentityRemoval() {
    }

    public static <T> boolean removeAll(List<T> list, Collection<?> toRemove) {
        if (toRemove.isEmpty() || list.isEmpty()) {
            return false;
        }
        if (toRemove.size() == 1) {
            // ArrayList.remove(Object) is a single O(n) pass — no set needed
            return list.remove(toRemove.iterator().next());
        }
        final Set<Object> set = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>(toRemove.size() * 2));
        set.addAll(toRemove);
        int write = 0;
        int size = list.size();
        for (int read = 0; read < size; read++) {
            T item = list.get(read);
            if (!set.contains(item)) {
                if (write != read) {
                    list.set(write, item);
                }
                write++;
            }
        }
        if (write == size) {
            return false;
        }
        list.subList(write, size).clear();
        return true;
    }

    /** Self-check: {@code java -ea -cp build/classes/java/main dev.froyln.liteoptimizer.world.IdentityRemoval}. */
    public static void main(String[] args) {
        Object a = new Object(), b = new Object(), c = new Object(), d = new Object();
        List<Object> list = new java.util.ArrayList<Object>(java.util.Arrays.asList(a, b, c, d, b, a));
        List<Object> ref = new java.util.ArrayList<Object>(list);
        List<Object> rm = java.util.Arrays.asList(b, d, new Object());
        boolean r1 = removeAll(list, rm);
        boolean r2 = ref.removeAll(rm);
        assert r1 == r2 && list.equals(ref) : list + " vs " + ref;
        assert list.size() == 3 && list.get(0) == a && list.get(1) == c && list.get(2) == a;
        assert !removeAll(list, java.util.Arrays.asList(new Object()));
        assert removeAll(list, java.util.Arrays.asList(c)) && list.size() == 2;
        assert !removeAll(new java.util.ArrayList<Object>(), rm);
        System.out.println("IdentityRemoval self-check OK");
    }
}
