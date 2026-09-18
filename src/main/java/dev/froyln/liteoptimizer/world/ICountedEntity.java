package dev.froyln.liteoptimizer.world;

/** Implemented on Entity by the entity_type_counts mixin: is the entity in a world's counters right now. */
public interface ICountedEntity {
    boolean liteoptimizer$isCounted();

    void liteoptimizer$setCounted(boolean counted);
}
