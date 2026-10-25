# Rendering and network probes — 2026-10-24

## Entity culling

The client-only `entity_culling` option uses Sodium's section visibility graph to skip eligible
entity draws. It defaults off. Unknown or modded entity types, glowing or name-tagged entities,
entities outside world height, the render-view entity, ridden entities, and active outlines fail
open. The existing block-entity frustum option also now culls only the known vanilla TESR classes;
modded renderers remain visible.

On the full technical client connected to an unmodified vanilla 1.12.2 server, a sealed stone
section fixture changed the F3 entity count from `E: 2/3` with culling off to `E: 1/3` with it on,
at the same camera pose. Litematica, MiniHUD, selection boxes and outlines stayed visible, and
server TPS remained 20.0. This verifies suppression for the fixture, not all modded renderers or
all overlay states. No useful-workload performance gain has been measured, so the option remains
experimental and default-off. The prior open-scene pair showed no suppression and is excluded from
gain claims.

## GUI item display lists

A matched creative-inventory pair measured 22,215 frames off and 20,606 on over 30 seconds after a
60-second warmup. Mean frame time rose from 1.35 ms to 1.46 ms (+7.8%); slowest-1% mean rose from
4.35 ms to 5.23 ms (+20.3%). The GUI display-list candidate was removed after this regression.
Existing item-frame display lists are unchanged. Font glyph caching remains unimplemented.

## Network

The client logged in, received world and chunk data, moved and sent chat through a disposable
vanilla 1.12.2 server. A 60-second active-play JFR was dominated by raw input (5,747 self samples);
Sodium chunk graph iteration had 25 and chunk rendering 20. Netty and packet methods did not
appear as sampled hotspots. This bounded profile did not justify a packet optimization. No network
code, packet IDs, serialization, Netty pipeline, or player-action timing changed.

Reopen the network investigation only if a repeatable profile identifies packet processing as a
hot path. Any future candidate must preserve wire bytes and player-action timing.
