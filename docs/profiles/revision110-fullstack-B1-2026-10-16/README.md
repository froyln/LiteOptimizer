# Revision 110 full-stack B1 smoke — 2026-10-16

The exact revision-110 artifact loaded in the disposable full-stack Prism clone with the 17-mod technical stack and opened the copied `B1-rev110-compat-smoke` scene. The storage build rendered in-game; the post-launch client log contains no `FATAL` or `NoClassDefFoundError`. It does repeat Model Gap Fix's `mixins.modelgapfix.json` selection NPE; the rev97 no-LiteOptimizer baseline recorded the same failure, so it remains a baseline compatibility issue, not evidence of an optimizer regression. See `b1-rendered.png` and `client.log`. The source `B1-storage-smoke` save was copied before launch and was not opened or changed.

This proves exact-build startup, world loading, and a B1 Sodium render smoke only. It does not complete the individual feature checks in `docs/compat/checklist.md`.
