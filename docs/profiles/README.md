# Baseline profiles

Status: **not recorded yet** — needs a person in the game. Everything below is the
procedure; the numbers get appended per world when they exist.

## Benchmark worlds → existing saves

The instance already has real technical worlds in
`~/.local/share/PrismLauncher/instances/1.12.2 test ai/minecraft/saves/`. Copy (never play
the originals) into `~/Dev/OptimizationMod-1.12.2-worlds/<id>/` and record the fixed pose in
this file:

| id | dir under `~/Dev/OptimizationMod-1.12.2-worlds/` | source save | pose (`pose.txt`: `x y z yaw pitch`) | notes |
|----|---|---|---|---|
| B1 base | `B1-storage` | `RedBlaze908 Storage System 1_12 V2` | _fill_ | item-frame/chest walls, many TEs |
| B2 items | `B2-items` | `cobble farm for 1_11` | _fill_ | let items pile up before recording |
| B3 redstone | `B3-furnace` (harness proven), `B3-hoppers` | `50 furnace Array`, `8_5` | _fill_ | furnaces / hopper halls |
| B4 mobs | `B4-mobs` | `Heinz' Witchfarm` | _fill_ | let the farm fill |
| B5 tnt | `B5-tnt` | `4D TNT Quarry` | _fill_ | quarry running |
| B6 flight | _todo_ | `seed 7480190280633614536` | elytra loop, RD 16 | the only non-static one |

Worlds are copies (the instance's originals are never played by the tools). `pose.txt` is
optional: without it the player stays where the save left them, which is fine for parity runs.

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
