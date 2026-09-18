# Baseline profiles

Status: **not recorded yet** — needs a person in the game. Everything below is the
procedure; the numbers get appended per world when they exist.

## Benchmark worlds → existing saves

The instance already has real technical worlds in
`~/.local/share/PrismLauncher/instances/1.12.2 test ai/minecraft/saves/`. Copy (never play
the originals) into `~/Dev/OptimizationMod-1.12.2-worlds/<id>/` and record the fixed pose in
this file:

| id | save to copy | pose (`/tp x y z yaw pitch`) | notes |
|----|--------------|-------------------------------|-------|
| B1 base | `RedBlaze908 Storage System 1_12 V2` (or `main sturuge`) | _fill_ | item-frame/chest walls, many TEs |
| B2 items | `cobble farm for 1_11` or `48.7kcobble` pasted, running | _fill_ | let items pile up ~5k before recording |
| B3 redstone | `8_5` / `22_Hopper_8_Itemsslice…` hopper halls, `50 furnace Array` | _fill_ | sorters + clocks running |
| B4 mobs | `Heinz' Witchfarm`, `flush_mobfarms_creative`, iron farm | _fill_ | let the farm fill |
| B5 tnt | `3-dir TNT quarry` / `4D TNT Quarry` | _fill_ | quarry running |
| B6 flight | `seed 7480190280633614536` | elytra loop, RD 16 | the only non-static one |

Settings for every recording: render distance 16, VSync off, max FPS unlimited, fullscreen
off (fixed window size), `doDaylightCycle false` + `/time set 6000`, particles All, smooth
lighting on, same mod set as the instance (no LiteOptimizer for the baseline).

## Recording

```bash
# once: instance.cfg → JavaPath=/usr/lib/jvm/java-8-openjdk/bin/java  (JFR built in)
tools/test-in-game.sh                 # or launch the instance normally
# in game: load world, /tp to the pose, wait 30 s
tools/profile.sh B1-baseline 60       # → docs/profiles/B1-baseline.{jfr,top.txt}
# in game, same window: /debug start … 60 s … /debug stop  → <instance>/minecraft/debug/profile-results-*.txt (copy here as B1-baseline.debug.txt)
# shift+F3 screenshot → B1-baseline.pie.png
/usr/lib/jvm/java-8-openjdk/bin/jcmd <pid> GC.run; jcmd <pid> GC.heap_info > B1-baseline.heap.txt
/usr/lib/jvm/java-8-openjdk/bin/jcmd <pid> GC.class_histogram | head -40 > B1-baseline.histo.txt
```

`.jfr` files are gitignored; the `.top.txt` summaries are committed.

## Results

(append one `## B<n>` section per world: pose, date, top-10 self frames, top-10 inclusive
frames, MSPT avg/max from the debug profile, heap after GC.)
