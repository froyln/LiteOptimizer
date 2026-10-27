# GUI item display-list probe — 2026-10-24

## Runtime and performance result

The full technical-stack MultiMC client connected to the disposable vanilla 1.12.2 server at
`127.0.0.1:25566` with `gui_item_model_display_lists=true` via the focused benchmark override.
The creative inventory opened and rendered its item grid; the server stayed at 20 TPS. The captured
screen is `gui-item-display-lists-on-inventory-2026-10-24.png`. Startup logged the feature as enabled
(the loaded-options disabled list omitted it).

## Performance result

With the inventory already open for 60 seconds before each recording, the matched 30-second
OFF/ON capture produced:

| metric | off | on | change |
|---|---:|---:|---:|
| frames | 22,215 | 20,606 | — |
| average FPS | 740.5 | 686.8 | −7.2% |
| average frame time | 1.35 ms | 1.46 ms | +7.8% |
| slowest 1% average frame time | 4.35 ms | 5.23 ms | +20.3% |
| maximum frame | 11.0 ms | 15.1 ms | — |

Both runs used the same isolated MultiMC client, vanilla server, creative inventory, player pose,
and 30-second capture after a 60-second warmup. The server stayed around 20 TPS. Raw records are
`gui-item-display-lists-off-controlled-2026-10-24.csv` and
`gui-item-display-lists-on-controlled-2026-10-24.csv`; enabled screenshot is
`gui-item-display-lists-on-inventory-2026-10-24.png`.

Decision: reject and remove the GUI display-list branch. This one matched pair shows a regression,
not an optimization. Existing item-frame display lists remain unchanged. Font glyph caching stays
deferred because prior measurements were slower.
