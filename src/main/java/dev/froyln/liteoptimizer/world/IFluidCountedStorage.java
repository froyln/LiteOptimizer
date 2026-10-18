package dev.froyln.liteoptimizer.world;

/** Implemented on ExtendedBlockStorage by the entity_fluid_checks mixin: fluid block counts per section. */
public interface IFluidCountedStorage {
    int liteoptimizer$waterCount();

    int liteoptimizer$lavaCount();
}
