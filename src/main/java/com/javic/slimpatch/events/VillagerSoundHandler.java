package com.javic.slimpatch.events;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = "slimpatch")
public class VillagerSoundHandler {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        String name = event.getName();

        if (name.startsWith("entity.villager.") || name.startsWith("entity.wandering_trader.")) {

            if (name.equals("entity.villager.ambient") || name.equals("entity.villager.celebrate")
                    || name.equals("entity.wandering_trader.ambient") || name.equals("entity.wandering_trader.trade")) {
                SlimPatch.LOGGER.debug("[SlimPatch] Reemplazando sonido vanilla por custom (villager/trader).");
                event.setSound(null);
                return;
            }

            if (name.equals(SoundEvents.VILLAGER_NO.getLocation().getPath())
                    || name.equals(SoundEvents.VILLAGER_YES.getLocation().getPath())) {
                SlimPatch.LOGGER.debug("[SlimPatch] Bloqueado sonido vanilla duplicado: {}", name);
                event.setSound(null);
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (event.getLevel().isClientSide()) return;

        if (entity instanceof Villager villager
                && !(villager instanceof MaleVillagerEntity)
                && !(villager instanceof FemaleVillagerEntity)) {

            event.getLevel().getServer().execute(() -> {
                CompoundTag data = villager.getPersistentData();
                String gender = data.getString("slimpatch_gender");

                if (gender.isEmpty()) gender = event.getLevel().random.nextBoolean() ? "male" : "female";
                data.putString("slimpatch_gender", gender);

                if (gender.equals("male")) {
                    event.getLevel().playSound(null, villager.blockPosition(),
                            HumanVillagerSounds.maleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    SlimPatch.LOGGER.debug("[SlimPatch] Reproducido maleClick() en spawn de aldeano.");
                } else {
                    event.getLevel().playSound(null, villager.blockPosition(),
                            HumanVillagerSounds.femaleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    SlimPatch.LOGGER.debug("[SlimPatch] Reproducido femaleClick() en spawn de aldeano.");
                }
            });
        }

        if (entity instanceof WanderingTrader trader) {
            event.getLevel().getServer().execute(() -> {
                boolean isFemale = false;

                if (trader instanceof HumanWanderingTraderEntity humanTrader) {
                    isFemale = humanTrader.isFemale();
                    SlimPatch.LOGGER.debug("[SlimPatch] Detectado HumanWanderingTraderEntity (female={})", isFemale);
                } else {
                    CompoundTag data = trader.getPersistentData();
                    if (data.contains("hv_isFemale")) {
                        isFemale = data.getBoolean("hv_isFemale");
                    } else {
                        isFemale = event.getLevel().random.nextBoolean();
                        data.putBoolean("hv_isFemale", isFemale);
                    }
                }

                if (isFemale) {
                    event.getLevel().playSound(null, trader.blockPosition(),
                            HumanVillagerSounds.femaleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    SlimPatch.LOGGER.debug("[SlimPatch] Reproducido femaleClick() en spawn de wandering trader.");
                } else {
                    event.getLevel().playSound(null, trader.blockPosition(),
                            HumanVillagerSounds.maleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    SlimPatch.LOGGER.debug("[SlimPatch] Reproducido maleClick() en spawn de wandering trader.");
                }
            });
        }
    }
}