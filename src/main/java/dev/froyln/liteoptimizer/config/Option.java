package dev.froyln.liteoptimizer.config;

/**
 * Every switch the config file knows. Pillars ({@link #pillar} == null) are master switches; an
 * optimization is applied only when both its pillar and its own key are true. The key of an
 * optimization is the name of its mixin package: {@code dev.froyln.liteoptimizer.mixin.<pillar>.<key>}.
 */
public enum Option {
    // pillars
    RENDER("render", null, true, "Sodium pillar: terrain render pipeline, culling, textures"),
    WORLD("world", null, true, "Lithium pillar: world/tile-entity/entity logic"),
    AI("ai", null, true, "Lithium pillar: mob AI and pathfinding"),
    MEMORY("memory", null, true, "FerriteCore pillar: heap reductions"),

    // 0.3 Sodium I
    SODIUM("sodium", RENDER, true, "Sodium terrain renderer (ported Vintagium): chunk-graph culling, worker meshing, region VBOs, GL20/GL43 backends, particle culling"),

    // 0.2 Lithium I (keys exist before the code so the file is stable across versions)
    TILE_ENTITY_REMOVAL("tile_entity_removal", WORLD, true, "O(n) instead of O(n*m) tile entity list removal"),
    PENDING_TICKS_CHUNK_LOOKUP("pending_ticks_chunk_lookup", WORLD, true, "per-chunk index for scheduled ticks on chunk save/unload"),
    ITEMSTACK_IS_EMPTY("itemstack_is_empty", MEMORY, true, "ItemStack.isEmpty without a map lookup"),
    ENTITY_DATA_MANAGER("entity_data_manager", MEMORY, true, "array-backed, lock-free entity data manager"),
    ENTITY_FLUID_CHECKS("entity_fluid_checks", WORLD, true, "skip water/lava scans in sections without fluids"),
    BLOCK_COLLISIONS("block_collisions", WORLD, true, "per-chunk block collision box collection (masa)"),
    HARD_HITBOX_ENTITY_LIST("hard_hitbox_entity_list", WORLD, true, "per-chunk list of collidable entities (boats, shulkers)"),
    ITEM_MERGING("item_merging", WORLD, true, "skip merge search for full item stacks"),
    EQUIPMENT_CHANGE_CHECK("equipment_change_check", WORLD, true, "cheap equipment change detection"),
    CRAMMING_PREDICATE_CACHE("cramming_predicate_cache", WORLD, true, "cache team collision predicate and cramming rule"),
    ENTITY_TYPE_COUNTS("entity_type_counts", WORLD, true, "per-type entity counters for mob spawning"),
    FALLING_BLOCK_MOVEMENT("falling_block_movement", WORLD, true, "early-out movement for idle falling blocks / TNT"),
    FURNACE_RECIPE_CACHE("furnace_recipe_cache", WORLD, true, "cache furnace recipe lookups"),
    RAYTRACE_ALLOCATIONS("raytrace_allocations", WORLD, true, "allocation-free block ray tracing"),
    BLOCKPOS_GARBAGE("blockpos_garbage", WORLD, true, "mutable positions in hot vanilla loops"),
    NBT_TAG_POOLING("nbt_tag_pooling", MEMORY, true, "canonical instances for small NBT tags"),
    STRUCTURE_LOOKUP_INDEX("structure_lookup_index", WORLD, true, "per-chunk index for 'is this position inside a structure' (swamp hut / monument / fortress spawn checks)"),

    // 0.4 FerriteCore
    BAKED_QUAD_DEDUP("baked_quad_dedup", MEMORY, true, "share identical baked quads between models"),
    BLOCKSTATE_PROPERTY_STORAGE("blockstate_property_storage", MEMORY, true, "one neighbour table per block instead of one per blockstate"),

    // 0.5 Lithium II
    PROPERTY_HASHCODE_CACHE("property_hashcode_cache", WORLD, true, "cache IProperty.hashCode (hashed on every IBlockState.getValue)");

    public final String key;
    public final Option pillar;
    public final boolean defaultValue;
    public final String description;

    Option(String key, Option pillar, boolean defaultValue, String description) {
        this.key = key;
        this.pillar = pillar;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public static Option byKey(String key) {
        for (Option o : values()) {
            if (o.key.equals(key)) {
                return o;
            }
        }
        return null;
    }
}
