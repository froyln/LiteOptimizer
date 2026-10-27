# Revision 92 Tweakeroo dirty-section check attempt

The exact revision-92 artifact was loaded with the full mod stack in disposable `B1-storage-smoke`. Tweakeroo's `Chunk Render On Main Thread` setting was enabled in its GUI and persisted as `true` in the clone config.

The GUI showed the setting ON and the change persisted to the clone config ([capture](tweakeroo-main-thread-on.png)). The attempt to issue a block-edit command through isolated WayVNC failed: the key sequence entered ordinary chat text rather than a command. No block mutation or dirty-section rebuild was confirmed, so this is not a pass. The game process was closed after the failed input attempt; no block edit was confirmed. The disposable clone config is now restored to `tweakChunkRenderOnMainThread=false`. The B1 disposable copy was the only world involved.

Next useful path: use a prepared physical placement target and a verified input chord (or use a vanilla right-click placement) instead of command chat. Current blocker is only the VNC command-entry method; the check remains open.
