# TellMe / MaLiLib compatibility baseline

TellMe 0.6.3 is installed but disabled in the target Prism profile. To check its compatibility,
I enabled it in the disposable clone and launched revision 93; LiteOptimizer initialized, then
TellMe failed in `LiteModTellMe.init` with
`NoSuchMethodError: fi.dy.masa.malilib.util.FileUtils.getConfigDirectory()Ljava/io/File;`.
With LiteOptimizer disabled and TellMe still enabled, the same exception occurs at the same
TellMe init call. This identifies a pre-existing TellMe/MaLiLib API mismatch rather than a
LiteOptimizer regression. `javap` on the installed MaLiLib 0.54.0 confirms `FileUtils` provides
`getMinecraftDirectory(): Path` and no `getConfigDirectory(): File`.

- Revision-93 enabled log: [`with-liteoptimizer.log.gz`](with-liteoptimizer.log.gz)
- No-LiteOptimizer baseline log: [`without-liteoptimizer.log`](without-liteoptimizer.log)
- Runtime config restored after the check: LiteOptimizer enabled, TellMe disabled.

TellMe's dump commands remain unverified because this installed version pair cannot initialize.
Do not attribute the startup exception to LiteOptimizer; verify again only if the instance's
TellMe or MaLiLib version changes.
