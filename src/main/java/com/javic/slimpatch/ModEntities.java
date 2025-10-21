package com.javic.slimpatch;

import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.entity.HumanPillagerEntity;
import com.javic.slimpatch.entity.HumanVindicatorEntity;
import com.javic.slimpatch.entity.HumanEvokerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, SlimPatch.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MaleVillagerEntity>> MALE_VILLAGER =
            ENTITIES.register("male_villager", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] Registrando entidad slimpatch:male_villager");
                return EntityType.Builder.<MaleVillagerEntity>of(MaleVillagerEntity::new, MobCategory.CREATURE)
                        .sized(0.6f, 1.95f)
                        .build(ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "male_villager").toString());
            });

    public static final DeferredHolder<EntityType<?>, EntityType<FemaleVillagerEntity>> FEMALE_VILLAGER =
            ENTITIES.register("female_villager", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] Registrando entidad slimpatch:female_villager");
                return EntityType.Builder.<FemaleVillagerEntity>of(FemaleVillagerEntity::new, MobCategory.CREATURE)
                        .sized(0.6f, 1.95f)
                        .build(ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "female_villager").toString());
            });

    public static final DeferredHolder<EntityType<?>, EntityType<HumanWanderingTraderEntity>> HUMAN_WANDERING_TRADER =
            ENTITIES.register("wandering_trader", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] 🔄 Reemplazando entidad vanilla: minecraft:wandering_trader");
                return EntityType.Builder.<HumanWanderingTraderEntity>of(HumanWanderingTraderEntity::new, MobCategory.CREATURE)
                        .sized(0.6f, 1.95f)
                        .build("minecraft:wandering_trader");
            });

    public static final DeferredHolder<EntityType<?>, EntityType<HumanWanderingTraderEntity>> HUMAN_TRADER_NATURAL =
            ENTITIES.register("human_trader", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] Registrando entidad natural: slimpatch:human_trader");
                return EntityType.Builder.<HumanWanderingTraderEntity>of(HumanWanderingTraderEntity::new, MobCategory.CREATURE)
                        .sized(0.6f, 1.95f)
                        .build(ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "human_trader").toString());
            });

    public static final DeferredHolder<EntityType<?>, EntityType<HumanPillagerEntity>> HUMAN_PILLAGER =
            ENTITIES.register("pillager", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] 🔄 Reemplazando entidad vanilla: minecraft:pillager");
                return EntityType.Builder.<HumanPillagerEntity>of(HumanPillagerEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .build("minecraft:pillager");
            });

    public static final DeferredHolder<EntityType<?>, EntityType<HumanVindicatorEntity>> HUMAN_VINDICATOR =
            ENTITIES.register("vindicator", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] 🔄 Reemplazando entidad vanilla: minecraft:vindicator");
                return EntityType.Builder.<HumanVindicatorEntity>of(HumanVindicatorEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .build("minecraft:vindicator");
            });

    public static final DeferredHolder<EntityType<?>, EntityType<HumanEvokerEntity>> HUMAN_EVOKER =
            ENTITIES.register("evoker", () -> {
                SlimPatch.LOGGER.info("[SlimPatch] 🔄 Reemplazando entidad vanilla: minecraft:evoker");
                return EntityType.Builder.<HumanEvokerEntity>of(HumanEvokerEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .build("minecraft:evoker");
            });
}