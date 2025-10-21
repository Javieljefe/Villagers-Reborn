package com.javic.slimpatch;

import com.javic.slimpatch.item.ModItems;
import com.javic.slimpatch.commands.TestSoundCommand;
import com.javic.slimpatch.commands.TestIllagerSoundCommand;
import com.javic.slimpatch.commands.SpawnHumanTraderCommand;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import com.javic.slimpatch.sounds.HumanIllagerSounds;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.ServerCooldownTracker;
import com.javic.slimpatch.config.SlimPatchConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

@Mod(SlimPatch.MODID)
public class SlimPatch {

    public static final String MODID = "slimpatch";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SlimPatch(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("SlimPatch inicializado correctamente.");

        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        HumanVillagerSounds.register(modEventBus);
        HumanIllagerSounds.register(modEventBus);

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TestSoundCommand::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TestIllagerSoundCommand::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(SpawnHumanTraderCommand::register);

        modEventBus.addListener(this::addItemsToCreativeTabs);
        modEventBus.addListener(this::registerAttributes);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::replaceVanillaEntities);

        ModNetworking.register(modEventBus);
        ServerCooldownTracker.init();

        try {
            Class.forName("com.javic.slimpatch.network.VillagerCooldownsPacket");
            Class.forName("com.javic.slimpatch.network.RelationshipPacket");
            LOGGER.info("[SlimPatch] Clases de red precargadas correctamente en servidor.");
        } catch (Throwable t) {
            LOGGER.error("[SlimPatch] Error precargando clases de red: ", t);
        }

        modEventBus.addListener(this::commonSetup);

        // 🔹 El config ahora se guarda globalmente en /config/, no en serverconfig/
        modContainer.registerConfig(ModConfig.Type.COMMON, SlimPatchConfig.SERVER_SPEC);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.javic.slimpatch.client.ClientSetup.init(modEventBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[SlimPatch] Common setup ejecutado correctamente.");
    }

    private void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.MALE_VILLAGER_SPAWN_EGG.get());
            event.accept(ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier villagerAttributes = Villager.createAttributes().build();
        event.put(ModEntities.MALE_VILLAGER.get(), villagerAttributes);
        event.put(ModEntities.FEMALE_VILLAGER.get(), villagerAttributes);
        event.put(ModEntities.HUMAN_WANDERING_TRADER.get(), villagerAttributes);

        AttributeSupplier illagerAttributes = Pillager.createAttributes().build();
        event.put(ModEntities.HUMAN_PILLAGER.get(), illagerAttributes);
        event.put(ModEntities.HUMAN_VINDICATOR.get(), illagerAttributes);
        event.put(ModEntities.HUMAN_EVOKER.get(), illagerAttributes);
    }

    private void replaceVanillaEntities(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Villager vanilla) {
            var data = vanilla.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            boolean spawnMale = level.getRandom().nextBoolean();
            var entityType = spawnMale ? ModEntities.MALE_VILLAGER.get() : ModEntities.FEMALE_VILLAGER.get();
            var newVillager = entityType.create(level);

            if (newVillager != null) {
                event.setCanceled(true);
                newVillager.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
                newVillager.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newVillager.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanilla.blockPosition()), MobSpawnType.NATURAL, null);
                }

                level.addFreshEntity(newVillager);
            }
            return;
        }

        if (event.getEntity() instanceof WanderingTrader vanillaTrader) {
            var data = vanillaTrader.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_WANDERING_TRADER.get();
            var newTrader = humanType.create(level);

            if (newTrader != null) {
                event.setCanceled(true);
                newTrader.moveTo(vanillaTrader.getX(), vanillaTrader.getY(), vanillaTrader.getZ(), vanillaTrader.getYRot(), vanillaTrader.getXRot());
                newTrader.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newTrader.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaTrader.blockPosition()), MobSpawnType.NATURAL, null);
                }

                level.addFreshEntity(newTrader);
                vanillaTrader.discard();
            }
            return;
        }

        if (event.getEntity() instanceof Pillager vanillaPillager) {
            var data = vanillaPillager.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_PILLAGER.get();
            var newPillager = humanType.create(level);

            if (newPillager != null) {
                event.setCanceled(true);
                newPillager.moveTo(vanillaPillager.getX(), vanillaPillager.getY(), vanillaPillager.getZ(), vanillaPillager.getYRot(), vanillaPillager.getXRot());
                newPillager.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newPillager.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaPillager.blockPosition()), MobSpawnType.NATURAL, null);
                }

                level.addFreshEntity(newPillager);
            }
            return;
        }

        if (event.getEntity() instanceof Evoker vanillaEvoker) {
            var data = vanillaEvoker.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_EVOKER.get();
            var newEvoker = humanType.create(level);

            if (newEvoker != null) {
                event.setCanceled(true);
                newEvoker.moveTo(vanillaEvoker.getX(), vanillaEvoker.getY(), vanillaEvoker.getZ(), vanillaEvoker.getYRot(), vanillaEvoker.getXRot());
                newEvoker.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newEvoker.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaEvoker.blockPosition()), MobSpawnType.NATURAL, null);
                }

                level.addFreshEntity(newEvoker);
            }
            return;
        }

        if (event.getEntity() instanceof Vindicator vanillaVindicator) {
            var data = vanillaVindicator.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_VINDICATOR.get();
            var newVindicator = humanType.create(level);

            if (newVindicator != null) {
                event.setCanceled(true);
                newVindicator.moveTo(vanillaVindicator.getX(), vanillaVindicator.getY(), vanillaVindicator.getZ(), vanillaVindicator.getYRot(), vanillaVindicator.getXRot());
                newVindicator.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newVindicator.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaVindicator.blockPosition()), MobSpawnType.NATURAL, null);
                }

                level.addFreshEntity(newVindicator);
            }
        }
    }
}