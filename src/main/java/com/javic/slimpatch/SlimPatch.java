package com.javic.slimpatch;

import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.item.ModCreativeTabs;
import com.javic.slimpatch.item.ModItems;
import com.javic.slimpatch.commands.TestSoundCommand;
import com.javic.slimpatch.commands.TestIllagerSoundCommand;
import com.javic.slimpatch.commands.SpawnHumanTraderCommand;
import com.javic.slimpatch.commands.SlimPatchDebugCommand;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import com.javic.slimpatch.sounds.HumanIllagerSounds;
import com.javic.slimpatch.sounds.HumanZombieVillagerSounds;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.ServerCooldownTracker;
import com.javic.slimpatch.config.GiftPoolConfig;
import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.config.VillagerNameConfig;
import com.javic.slimpatch.quests.objectives.QuestInitializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

@Mod(SlimPatch.MODID)
public class SlimPatch {

    public static final String MODID = "slimpatch";
    private static final long REPLACED_WANDERING_TRADER_TTL_TICKS = 200L;
    private static final Map<UUID, ReplacedWanderingTraderEntry> REPLACED_WANDERING_TRADERS = new HashMap<>();
    public SlimPatch(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        HumanVillagerSounds.register(modEventBus);
        HumanIllagerSounds.register(modEventBus);
        HumanZombieVillagerSounds.register(modEventBus);

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TestSoundCommand::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TestIllagerSoundCommand::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(SpawnHumanTraderCommand::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(SlimPatchDebugCommand::register);

        modEventBus.addListener(this::addItemsToCreativeTabs);
        modEventBus.addListener(this::registerAttributes);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::replaceVanillaEntities);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.javic.slimpatch.events.ZombieVillagerDataHandler.class);

        ModNetworking.register(modEventBus);
        ServerCooldownTracker.init();

        try {
            Class.forName("com.javic.slimpatch.network.VillagerCooldownsPacket");
            Class.forName("com.javic.slimpatch.network.RelationshipPacket");
        } catch (Throwable t) {
        }

        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, SlimPatchConfig.SERVER_SPEC, "villagersreborn-server.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "villagersreborn.toml");

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            initClient(modEventBus, modContainer);
        }
    }

    private static void initClient(IEventBus modEventBus, ModContainer modContainer) {
        try {
            Class<?> factoryClass = Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
            Class<?> screenClass = Class.forName("net.neoforged.neoforge.client.gui.ConfigurationScreen");
            Supplier<?> factorySupplier = () -> Proxy.newProxyInstance(
                    factoryClass.getClassLoader(),
                    new Class<?>[]{factoryClass},
                    (proxy, method, args) -> {
                        if (method.getDeclaringClass() == Object.class) {
                            return switch (method.getName()) {
                                case "toString" -> factoryClass.getName();
                                case "hashCode" -> System.identityHashCode(proxy);
                                case "equals" -> proxy == args[0];
                                default -> null;
                            };
                        }
                        return screenClass.getConstructor(ModContainer.class, Class.forName("net.minecraft.client.gui.screens.Screen"))
                                .newInstance(modContainer, args[1]);
                    }
            );
            modContainer.getClass()
                    .getMethod("registerExtensionPoint", Class.class, Supplier.class)
                    .invoke(modContainer, factoryClass, factorySupplier);

            Class<?> clientSetupClass = Class.forName("com.javic.slimpatch.client.ClientSetup");
            clientSetupClass.getMethod("init", IEventBus.class).invoke(null, modEventBus);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize client setup", e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GiftPoolConfig.ensureLoaded();
            VillagerNameConfig.ensureLoaded();
            QuestInitializer.registerAll();
        });
    }

    private void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.MALE_VILLAGER_SPAWN_EGG.get());
            event.accept(ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier villagerAttributes = Villager.createAttributes().add(Attributes.ATTACK_DAMAGE, 3.0D).build();
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

        purgeExpiredReplacedWanderingTraders(event);

        if (event.getEntity() instanceof MaleVillagerEntity || event.getEntity() instanceof FemaleVillagerEntity || event.getEntity() instanceof HumanWanderingTraderEntity) return;

        if (event.getEntity() instanceof Villager vanilla) {
            var data = vanilla.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            boolean spawnMale = level.getRandom().nextBoolean();
            var entityType = spawnMale ? ModEntities.MALE_VILLAGER.get() : ModEntities.FEMALE_VILLAGER.get();
            var newVillager = entityType.create(level);

            if (newVillager != null) {
                copyVanillaVillagerData(vanilla, newVillager, event.loadedFromDisk());

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newVillager.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanilla.blockPosition()), MobSpawnType.NATURAL, null);
                }

                if (level.addFreshEntity(newVillager)) {
                    vanilla.getPersistentData().putBoolean("slimpatch_replaced", true);
                    event.setCanceled(true);
                    vanilla.discard();
                }
            }
            return;
        }

        if (event.loadedFromDisk()) return;

        if (event.getEntity() instanceof WanderingTrader vanillaTrader) {
            var data = vanillaTrader.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_WANDERING_TRADER.get();
            var newTrader = humanType.create(level);

            if (newTrader != null) {
                if (level instanceof ServerLevelAccessor serverLevel) {
                    newTrader.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaTrader.blockPosition()), MobSpawnType.NATURAL, null);
                }

                copyVanillaWanderingTraderData(vanillaTrader, newTrader);
                if (!vanillaTrader.hasCustomName()) {
                    String name = newTrader.isFemale() ? "Wanderer" : "Traveler";
                    newTrader.setCustomName(net.minecraft.network.chat.Component.literal(name));
                    newTrader.setCustomNameVisible(true);
                }
                newTrader.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level.addFreshEntity(newTrader)) {
                    REPLACED_WANDERING_TRADERS.put(vanillaTrader.getUUID(), new ReplacedWanderingTraderEntry(newTrader.getUUID(), vanillaTrader.level().getGameTime() + REPLACED_WANDERING_TRADER_TTL_TICKS));
                    reattachTraderLlamas(vanillaTrader, newTrader);
                    event.setCanceled(true);
                    vanillaTrader.discard();
                }
            }
            return;
        }

        if (event.getEntity() instanceof TraderLlama traderLlama) {
            reattachTraderLlamaIfNeeded(traderLlama);
            return;
        }

        if (event.getEntity() instanceof Pillager vanillaPillager) {
            var data = vanillaPillager.getPersistentData();
            if (data.getBoolean("slimpatch_replaced") || data.getBoolean("slimpatch_forced")) return;

            var level = event.getLevel();
            var humanType = ModEntities.HUMAN_PILLAGER.get();
            var newPillager = humanType.create(level);

            if (newPillager != null) {
                newPillager.moveTo(vanillaPillager.getX(), vanillaPillager.getY(), vanillaPillager.getZ(), vanillaPillager.getYRot(), vanillaPillager.getXRot());
                newPillager.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newPillager.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaPillager.blockPosition()), MobSpawnType.NATURAL, null);
                }

                if (level.addFreshEntity(newPillager)) {
                    event.setCanceled(true);
                }
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
                newEvoker.moveTo(vanillaEvoker.getX(), vanillaEvoker.getY(), vanillaEvoker.getZ(), vanillaEvoker.getYRot(), vanillaEvoker.getXRot());
                newEvoker.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newEvoker.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaEvoker.blockPosition()), MobSpawnType.NATURAL, null);
                }

                if (level.addFreshEntity(newEvoker)) {
                    event.setCanceled(true);
                }
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
                newVindicator.moveTo(vanillaVindicator.getX(), vanillaVindicator.getY(), vanillaVindicator.getZ(), vanillaVindicator.getYRot(), vanillaVindicator.getXRot());
                newVindicator.getPersistentData().putBoolean("slimpatch_replaced", true);

                if (level instanceof ServerLevelAccessor serverLevel) {
                    newVindicator.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vanillaVindicator.blockPosition()), MobSpawnType.NATURAL, null);
                }

                if (level.addFreshEntity(newVindicator)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void copyVanillaVillagerData(Villager vanilla, Villager replacement, boolean loadedFromDisk) {
        replacement.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
        replacement.setYBodyRot(vanilla.yBodyRot);
        replacement.setYHeadRot(vanilla.getYHeadRot());
        replacement.setDeltaMovement(vanilla.getDeltaMovement());
        replacement.setVillagerData(vanilla.getVillagerData());
        replacement.setNoAi(vanilla.isNoAi());
        replacement.setSilent(vanilla.isSilent());
        replacement.setInvulnerable(vanilla.isInvulnerable());
        if (vanilla.isPersistenceRequired()) {
            replacement.setPersistenceRequired();
        }
        replacement.setHealth(Math.min(vanilla.getHealth(), replacement.getMaxHealth()));
        replacement.setAge(vanilla.getAge());

        if (vanilla.hasCustomName()) {
            replacement.setCustomName(vanilla.getCustomName());
        }
        replacement.setCustomNameVisible(vanilla.isCustomNameVisible());

        CompoundTag copiedData = vanilla.getPersistentData().copy();
        copiedData.remove("slimpatch_replaced");
        replacement.getPersistentData().merge(copiedData);
        replacement.getPersistentData().putBoolean("slimpatch_replaced", true);
        replacement.getPersistentData().putBoolean("slimpatch_vanilla_migrated", true);
        replacement.getPersistentData().putBoolean("slimpatch_vanilla_replacement_loaded_from_disk", loadedFromDisk);
    }

    private static void copyVanillaWanderingTraderData(WanderingTrader vanillaTrader, HumanWanderingTraderEntity replacement) {
        CompoundTag savedData = vanillaTrader.saveWithoutId(new CompoundTag());
        savedData.remove("UUID");
        replacement.load(savedData);
        replacement.setYBodyRot(vanillaTrader.yBodyRot);
        replacement.setYHeadRot(vanillaTrader.getYHeadRot());
        replacement.getPersistentData().putBoolean("slimpatch_vanilla_migrated", true);
    }

    private static void reattachTraderLlamas(WanderingTrader vanillaTrader, HumanWanderingTraderEntity replacement) {
        if (!(replacement.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        for (TraderLlama traderLlama : serverLevel.getEntitiesOfClass(TraderLlama.class, vanillaTrader.getBoundingBox().inflate(16.0D))) {
            if (traderLlama.getLeashHolder() == vanillaTrader) {
                traderLlama.setLeashedTo(replacement, true);
            }
        }
    }

    private static void reattachTraderLlamaIfNeeded(TraderLlama traderLlama) {
        if (traderLlama.level().isClientSide()) {
            return;
        }
        if (!(traderLlama.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        if (!(traderLlama.getLeashHolder() instanceof WanderingTrader wanderingTrader)) {
            return;
        }
        ReplacedWanderingTraderEntry replacementEntry = REPLACED_WANDERING_TRADERS.get(wanderingTrader.getUUID());
        if (replacementEntry == null) {
            return;
        }
        net.minecraft.world.entity.Entity replacementEntity = serverLevel.getEntity(replacementEntry.replacementUuid());
        if (replacementEntity instanceof HumanWanderingTraderEntity replacementTrader) {
            traderLlama.setLeashedTo(replacementTrader, true);
            REPLACED_WANDERING_TRADERS.remove(wanderingTrader.getUUID());
            return;
        }
        REPLACED_WANDERING_TRADERS.remove(wanderingTrader.getUUID());
    }

    private static void purgeExpiredReplacedWanderingTraders(EntityJoinLevelEvent event) {
        long gameTime = event.getLevel().getGameTime();
        REPLACED_WANDERING_TRADERS.entrySet().removeIf(entry -> entry.getValue().expiresAt() < gameTime);
    }

    private record ReplacedWanderingTraderEntry(UUID replacementUuid, long expiresAt) {
    }
}
