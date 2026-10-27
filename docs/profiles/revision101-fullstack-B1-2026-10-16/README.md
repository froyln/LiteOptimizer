# Revision 101 full-stack B1 compatibility smoke

The exact revision-101 artifact (SHA-256 `93bcdac9797fa11abbafcd9af3e3216a91201846041e8a3f09df0b0168d8de6e`) loaded with the installed technical mod stack and joined the disposable `B1-rev101-compat-smoke` world. The world was copied from `B1-storage-smoke`; the source world was not opened or changed.

Carpet Client's pause-menu entry, main options screen, and client-options screen opened. The piston visualizer preference toggled from false to true and was restored to false. The UI showed Carpet server version `null` because this was singleplayer; this is UI compatibility evidence only. It does not verify Carpet rule synchronization, piston preview rendering, or interaction with a Carpet server.

MiniHUD's player spawnable-chunk overlay was enabled in the disposable profile, but no clear overlay was visible in the B1 capture. That check is inconclusive and remains open. No B1 blocks were intentionally changed.

Tweakeroo’s Configs screen opened in this exact full-stack session. `Chunk Render On Main Thread` toggled OFF→ON→OFF; the final clone config value is `false`. This proves the config UI and live toggle coexist with the stack. No chunk rebuild was triggered in this check; the exact-revision dirty-section/F3+A evidence remains revision 97. Captures: `tweakeroo-main-thread-off.png`, `tweakeroo-main-thread-on.png`, and `tweakeroo-main-thread-restored.png`. The disposable B1 world was saved and quit cleanly; no blocks were intentionally changed.
