# Revision 93 parity repeatability check

The exact revision-93 artifact (SHA-256 `bf6744c36e52f210b521b51e8a1f6b06effd34294d31b3844dfc62a05330a4e1`) ran the stable `test` save for 6,000 ticks per run. Repeated comparisons isolate inconsistent all-OFF output in chunk `(-120, 44)`:

- Initial OFF/ON: OFF total `53905eb3…`, ON total `9fa11a43…`; only chunk `(-120, 44)` differed.
- Repeat OFF/ON: OFF total `6958cf6b…`, ON total `9fa11a43…`; again only chunk `(-120, 44)` differed.
- A two-run OFF/OFF control passed with both totals `9fa11a43…`, matching the ON output.
- A second OFF/OFF control also passed with both totals `9fa11a43…`, matching the repeat ON output.
- A verbose OFF/OFF control passed with the same total. Its end-of-run entity and tile-entity
  records for chunk `(-120, 44)` are identical; only 11 per-tick detail lines differ, beginning at
  tick 890 where loaded-chunk/entity counts differ between runs. The changing chunk contains the
  player. This points toward chunk-load/update timing, but does not prove which block/light field
  caused the outlier.

At render distance 10, two ON results and four OFF control results agree at total `9fa11a43…`; two other OFF runs produced distinct hashes, each isolated to chunk `(-120, 44)`. Verbose data points toward chunk-load/update timing, but does not identify the exact changed block/light field. A follow-up used the same source save with client render distance fixed at 2: the OFF/OFF controls and direct OFF/ON comparison all passed with byte-identical dumps and total `9494ea25…`. That closes the cumulative parity gate for the controlled fixture; render-distance-10 chunk-loading variance remains a separate unresolved test limitation. See [revision 94 fixed-distance parity](../revision94-parity-render-distance-2/README.md).

Evidence: [initial OFF](all-off.txt), [initial ON](all-on.txt), [first OFF controls](off-control-a.txt), [second first controls](off-control-b.txt), [repeat OFF](off-repeat.txt), [repeat ON](on-repeat.txt), [repeat control A](off-control-repeat-a.txt), [repeat control B](off-control-repeat-b.txt), [verbose control A](off-verbose-control-a.txt), [verbose control B](off-verbose-control-b.txt), and [ON client log](on-client.log). `PARITY_VERBOSE=1` records tick and entity/TE details; `PARITY_TRACE=1` separately enables per-RNG stack traces. The harness restored the clone config and removed its temporary `_parity` world after every run.
