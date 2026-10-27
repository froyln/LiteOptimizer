# Entity culling evaluation — 2026-10-24

## Status: entity suppression verified; gain remains unproven

The two OFF/ON B4-mobs pairs were launched through the existing PrismLauncher root while a
PrismLauncher process was already running. Although the benchmark requested the isolated Sway
desktop, Prism's single-instance handling meant the game window's desktop target was not proven.
Exclude these timings from all gain claims. One set of raw logs/configs is retained in
`pair-off-on/` for diagnosis; it is not a valid benchmark record.

The runtime check used the existing full-stack 1.12.2 MultiMC instance from the isolated Sway
desktop. With `entity_culling` enabled for the B4 visual pass and then disabled for comparison, the
world rendered and the Litematica/MiniHUD/selection/outline overlays remained visible. The
controlled `B4-mobs-culling-safe-20261024` pair used the same saved camera pose, but the open/glass
scene exposed every entity to Sodium's section graph: F3 stayed at `E: 63/64` in both runs. The
option therefore did not actually hide an entity in this fixture. Its frame average was 1.37 ms
off and 1.41 ms on (+2.8%); 1% low was 6.60 ms off and 6.93 ms on (+4.9%). Treat this as a
no-gain result for the open scene, not a measurement of an occluded scene. The raw pair is in
`run/bench/B4-mobs-culling-safe-20261024-{off,on}.*`.

A controlled sealed-section fixture then verified suppression. At the same camera pose
(1290.5, 5.0, 488.5, facing east), a full stone section wall at x=1320 separated the camera from
the pig in section (83,0,30). With the option off, F3 reported `E: 2/3`; with it on, `E: 1/3`.
The adjacent camera section remained visible, and the pig's section was outside the Sodium
visibility graph. Screenshots `entity-fixture-off.png` and `entity-fixture-on.png` record the pair.
This proves one entity was culled in the sealed fixture, not that every modded entity or every
visibility edge is covered. Server TPS remained 20.0. No useful-workload gain has been measured.

A separate vanilla-server smoke rendered the world and creative inventory. The Litematica/MiniHUD
and selection/outline overlays remained visible. This is not an exhaustive matrix for every TESR
or overlay state. The older PrismLauncher pair in `pair-off-on/` remains invalid because the
active window/pose could not be verified.

The vanilla-server client used the full technical stack except the two Bobby test mods, which
were temporarily removed because their test driver auto-loaded a world and stalled startup. The
files were backed up and restored after the smoke check. The server jar matched the Mojang 1.12.2
fixture SHA-256 recorded in the task notes. See `network-probe-vanilla-2026-10-24.md` for the
connection and network-profile record.

## Code state

`entity_culling` is implemented behind a config key whose release default is `false`. Entity
suppression passed the sealed-section smoke above, while a repeatable gain remains unproven.
`tile_entity_frustum` is the existing tested block-entity culling path; its limitations and older
measurements are documented in `../tile-entity-frustum-B1-2026-10-14/README.md`. The newer
`tile_entity_occlusion` dispatch hook remains a separate experimental candidate: it has not passed
a runtime suppression check or a gain check, and stays default-off. Custom/global renderers fail
open in that implementation. Keep entity culling default-off until useful-scene performance and
the full release compatibility checklist pass.
