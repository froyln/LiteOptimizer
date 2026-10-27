# Revision 101 stable-save parity

The exact reobfuscated revision-101 artifact (SHA-256
`93bcdac9797fa11abbafcd9af3e3216a91201846041e8a3f09df0b0168d8de6e`) passed 6,000 integrated-
server ticks on the stable `test` save at render distance 2 in the disposable full-stack Prism
clone. The clone contained the same artifact hash. Both the all-options-OFF/OFF determinism
control and the direct OFF/ON comparison produced the same total state hash:
`9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`.

`off-control-second.txt` is the second dump from the OFF/OFF control. The later OFF leg and ON
dump are `off-on-off.txt` and `on.txt`; all three files are byte-identical (SHA-256
`f8d1b98fdea615beb6e9cbab4bcd703bc48145b8d530bab095698ef1ea189e74`). Prism launched through
the isolated Sway/WayVNC desktop. The harness removed its temporary save and run properties, and
the clone's render distance was restored to 10. The source `test` save was not modified.

This clears cumulative integrated-server parity for revision 101 under the fixed-distance
fixture. The separate render-distance-10 OFF/OFF outlier remains a fixture limitation, not an
optimization mismatch. It does not substitute for the current-artifact vanilla-server smoke or
the remaining per-mod feature checklist.
