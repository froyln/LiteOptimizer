# Six audit fixes, 2026-10-13

The pre-fix working tree was committed as `7c5e1ee`. These are corrections to existing
features, not new optimization candidates.

| finding | correction | focused proof |
|---|---|---|
| `/entitydata` persistence bypassed spawn counters | mirror all three vanilla persistence field writes; remove the old contribution and add the new one only for loaded entities | both transitions, repeated values, unloaded entities, removal after mutation |
| chunk workers could miss task/shutdown notifications | poll and check running inside the notifier lock, wait in a condition loop | pause between poll and wait while another thread queues work or stops the executor |
| equal block properties rejected | use vanilla property equality | distinct equal property selects the identical neighbor; invalid value still returns null |
| edits through a retained smelting map used stale results | clear and permanently bypass caching when the live map escapes | cache hit before exposure; remove, add after negative lookup, and clear after exposure |
| unknown/null enums reached the options screen | normalize each nullable enum to its declared default on load/save | real Gson with unknown enum names and explicit nulls |
| negative worker count created no workers | normalize saved negatives to automatic; builder also accepts any nonpositive value as automatic | negative/zero, valid positive, and upper-limit thread requests |

`python3 tools/check-audit-regressions.py` and `python3 tools/check-sodium-options.py`
pass. The same checks against source from `7c5e1ee` fail for the audited behavior.
The former compiles the real helpers and mixin bodies against small Minecraft stubs,
and the actual worker wait/thread-count methods in a minimal executor. It does not
apply Mixin transformations. The latter uses real Gson and the actual option class.
`python3 tools/check-bench-config.py` also passes. Reviewer found no blocking defect;
production refmap contains all three persistence method targets and the field target.

Runtime: installed the built litemod into the full technical mod instance and ran
`tools/parity.sh ~/Dev/OptimizationMod-1.12.2-worlds/B3-hoppers 600` inside the isolated
headless Sway desktop. All-off and all-on states match exactly:
`172d1bcc127863c6719d1572ddef014609e9a7221a4ff92fd34c4e2b7d8ee6a0`.
Startup/world load and normal shutdown passed with Sodium workers and the persistence
redirect enabled. `runtime.sha256` identifies the tested artifact; the log and dumps
are alongside this file. Instance config and LiteOptimizer switches were restored by
the parity runner; the fresh litemod remains installed.

Final `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 in 21 s
(`final-build.log`). The final archive has identical code/resources to the installed,
runtime-tested archive; only `litemod.json`'s automatic revision counter changes (3 → 4).
`payload-check.json` records that comparison.

The first launch attempt encountered an existing Prism process: its command returned
before Minecraft exited, so the runner removed the temporary save and startup failed.
Closing that isolated launcher and rerunning with a fresh launcher produced the pass.

Boundaries: no new performance claim, no new render pixel comparison, no full release
compatibility certification. The command-specific persistence/cache edge cases are
covered by focused checks; B3 is cumulative runtime regression evidence.
