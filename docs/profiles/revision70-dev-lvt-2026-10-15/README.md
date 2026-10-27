# Revision 70 development-client LVT failure

Revision 70 builds successfully and is staged byte-for-byte in the disposable full-stack Prism
clone (`a1a5fdc009bef6770cc7491c04be17d3d40503121fb515082f18815b92829d6`). Its 728 archive entries
match the previously staged revision 61 except for `litemod.json`'s automatic revision field. The
release-tested revision 58 archive has the same code payload.

An isolated ForgeGradle `runClient` launch on 2026-10-15 reached LiteLoader, loaded LiteOptimizer,
then failed while applying `MixinRenderGlobal.renderEntities`. Mixin 0.7.4 reports these live locals
at the `renderInfos` field injection:

```text
Expected: [D, D, D, Entity, List, List, PooledMutableBlockPos]
Found:    [D, D, D, Entity, D, D, D, List, List, List, PooledMutableBlockPos]
```

A `@Surrogate` for the additional coordinates and loaded-entity list was compiled and retried. It
was not selected by Mixin 0.7.4. A second attempt captured only through the final needed list (as
recommended by Mixin's local-capture guidance), but it failed the same way:

```text
Expected: [D, D, D, Entity, List, List]
Found:    [D, D, D, Entity, D, D, D, List, List, List, PooledMutableBlockPos]
```

Both experiments were reverted. The current source therefore retains the original callback. The
packaged production mapping loaded in prior full-stack testing, but the development mapping remains
a real startup failure and must be resolved before release.

The disposable Prism clone has no account file, so exact revision 70 full-stack and vanilla-server
checks remain pending an authorized test account. No Microsoft code or credentials were entered.


## Superseded by revision 83 follow-up

The callback failure was resolved on 2026-10-15 by registering separate `CAPTURE_FAILSOFT`
callbacks for the MCP and production local-variable layouts. Revision 83 loaded LiteOptimizer,
joined a disposable world, and rendered terrain in an isolated dev client using Mixin 0.7.5; see
[`revision83-dev-smoke-2026-10-15`](../revision83-dev-smoke-2026-10-15/README.md). The earlier
failure remains valid for revision 70 source, but is no longer the current startup status. The full
installed-stack and exact-current-artifact server gates remain open.
