# Conservative 0.6.0-rc2 — 2026-10-13

Final built/installed SHA-256: `a26226f6ec8f96fa9174378feab1efd048cca0e2ae074cd2a3d0f11562e8519b`.
Runtime measurements below used pre-version-fix SHA-256
`843d887cd841188932474fc3b15d9d8a07a800b05fd40144a6130314dd04209e`.
`version-payload-check.json` proves the only changed payloads are the UTF-8 version constants
in the entry point and SodiumClientMod; every other jar entry is byte-identical. Gradle now
substitutes the runtime version from `mod_version`, matching metadata and filename.
Source: `091d4e1` plus the working-tree GUI/settings and culling-removal changes.
No release tag or publication. Tested pre-version-fix mod manifest: `mods-tested-payload.sha256`. Final candidate instance manifest: `mods.sha256`.

## Candidate scope

Entity graph culling and TE graph occlusion were removed: neither has an established isolated
benefit in B1, and ordinary TE occlusion is redundant. Generic TE frustum culling was removed
although it gains about 17–18% in the recovered B1 pose. Its fixed-box proof misses spawner
preview RNG initialization/unbounded labels, GL state restoration by shulker renderers, and
end-portal renderer RNG. No screenshot can excuse a demonstrated RNG/order violation.
Fixed-function terrain, item-frame model display lists, and existing logic/memory work remain.
Saved unsafe Sodium settings cannot reactivate excluded paths; their controls were removed.

## Verification

Clean Java 8 `./gradlew clean build --offline`: exit 0, 12 seconds.
Focused real-Gson settings and benchmark config checks pass. Packaging excludes removed
frustum/occlusion helper and mixin classes. Read-only source review found no material issues.

B3-hoppers cumulative all-off versus all-on state parity: **PASS**, 600 ticks, seed 1.
Total hash `172d1bcc127863c6719d1572ddef014609e9a7221a4ff92fd34c4e2b7d8ee6a0`.
Dumps and log are retained here. This includes game-state/RNG hash equality on this workload;
it is not universal proof or full release compatibility.

## Retained item-frame display lists

B1-storage original pose: `-1980.5841684379052 31.809877259292847 712.0017676442468 -101.02180480957031 42.53301239013672`.
`tools/bench.sh run/eval/rc2-model-B1 20 40 --compare model_display_lists --profile --no-entities`.
Both runs keep every other source option enabled. Only `model_display_lists` differs in the
saved config snapshots; moving entities are suppressed, item frames/paintings/TESRs retained.
MiniHUD info lines and particles are off. Tweakeroo glint uses an empty whitelist. Captures
are compared in full RGB with no masking/cropping/tolerance.

| metric | off | on |
|---|---:|---:|
| frame average | 6.26 ms | 5.14 ms (-17.8%) |
| slowest 1% average | 18.09 ms | 15.78 ms (-12.7%) |
| maximum frame | 344.2 ms | 342.0 ms |
| integrated-server MSPT average | 3.74 ms | 3.90 ms (+4.3%) |

The gain claim is rendering, not MSPT; long frame outliers remain. Exact off/on image difference:
**0 pixels**. Independent unchanged off/off control (20-second warmup, 10-second record):
**0 pixels**. PNGs, counts, CSVs, profiler text and config snapshots are retained here.
Instance settings and prior `_bench` save were restored. Same RC2 artifact throughout.

## Compatibility smoke on the full installed stack

Disposable B1 world on an official [Carpet server patch](https://github.com/gnembon/carpetmod112/releases/tag/v22_03_22),
loopback `127.0.0.1:25566`. Its EULA file was copied verbatim from the owner's existing accepted
server installation. Original server/world files were not edited. `carpet-server-smoke.log`
records the commands and responses; full candidate and no-LiteOptimizer logs are retained.

| exercised feature | observed result |
|---|---|
| full stack startup and B1 join | loads and joins with candidate; repeated after headless correction |
| LiteOptimizer Options | opens from Video Settings; retained Advanced controls visible; clouds Apply/save false then true verified on disk; Done returns |
| Carpet Client | detects server version v22_03_22; server rule list populated; console accurateBlockPlacement true/false synchronizes to GUI |
| Litematica + schematicpreview | browser and preview widget render; load/create placement; Layers All selected; moved-placement block overlay renders; material list totals91 |
| paste and verifier | paste reports93 setblock commands; aligned scan2/2 chunks,74 correct,4 wrong blocks,9 wrong states,6 missing; selected mismatch renders red in world |
| MiniHUD and Newlight | info lines visible; light-level overlay visible on test platform; torch addition shows10–14 and removal returns0 |
| Tweakeroo | free camera moves inside platform while console testfor confirms real player remains(-1969.5,100,687.5); chunk reload path works |
| connect_to_1_12_x | Direct Connect version selector visible; localhost:25566 join succeeds |

The crafting station changes after sequential paste due to active pistons/redstone. The aligned
verifier is a functional smoke check, **not** an exact structure/state parity claim.
The first paste attempt lacked OP on the mixed-case offline UUID and was kicked for chat spam;
OP was corrected after login, and the repeat completed. This was a disposable-server setup error.

Normalized startup conflict comparison: **7 distinct warnings both without and with LiteOptimizer**,
no added or removed warnings (`mixin-warning-comparison.json`). Generated handler suffixes and
repeat warnings were normalized. The baseline also logs malilib PacketSplitter empty-payload
exceptions on this Carpet stack; candidate logs contain those exceptions too. This is baseline
behavior, not a clean no-error certification. The direct-connect `ConfigOverrideUtils` NPE was
compared on 2026-10-14 with the same vanilla server and full mod stack after LiteOptimizer was
removed; it reproduced at the same MaLiLib line. See
[`vanilla-server-smoke-2026-10-14`](../vanilla-server-smoke-2026-10-14/README.md).

A moved overlay once needed F3+A during unreliable per-command VNC interaction. The control
was repeated with a persistent VNC connection in true headless Sway. On both no-LiteOptimizer
baseline and candidate, valid36→100 and out-of-height36100→100 moves render without F3+A.
Screenshots and baseline log are retained. The original cause was not proven; it was not
reproduced under the corrected interaction setup, and no speculative product patch was added.

This is a tested development candidate, **not** completion of the entire release checklist.
Remaining gates include the other table features in `docs/compat/checklist.md`: exact prepared
inert verification, translucent mode, other MiniHUD shapes/structures, piston preview, inventory
scroll, placement/restock/stacking, other installed-mod commands/outlines, and resource-reload
smoke. The vanilla-server smoke and a built revision 21 startup are recorded in
`docs/profiles/vanilla-server-smoke-2026-10-14/`; entity interaction is still unverified.
WorldEditCUI selection needs a suitable WorldEdit-capable fixture.


Desktop correction: the reused session was discovered to use `WLR_BACKENDS=wayland`,
not headless. Benchmark captures came from Minecraft itself, and compared exactly; manual VNC
frames could be stale until input produced damage. The launcher script was changed to
`WLR_BACKENDS=headless`, with `HEADLESS-1` and loopback WayVNC verified for subsequent GUI checks.

Final Java 8 `./gradlew build --offline` exited 0 in 15 seconds after version substitution. The final jar was installed and loaded by LiteLoader as `0.6.0-rc2` with no disabled options; the log records connection to the test server in `final-startup.log`. The built and installed jar hashes match. Shared common configs were restored from the pre-test backup after shutdown.
