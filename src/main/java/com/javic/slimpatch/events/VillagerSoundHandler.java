package com.javic.slimpatch.events;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = "slimpatch")
public class VillagerSoundHandler {

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (event.getLevel().isClientSide()) return;
        if (!Config.CUSTOM_VILLAGER_SOUNDS.get()) return;

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
                } else {
                    event.getLevel().playSound(null, villager.blockPosition(),
                            HumanVillagerSounds.femaleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            });
        }

        if (entity instanceof WanderingTrader trader) {
            event.getLevel().getServer().execute(() -> {
                boolean isFemale = false;

                if (trader instanceof HumanWanderingTraderEntity humanTrader) {
                    isFemale = humanTrader.isFemale();
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
                } else {
                    event.getLevel().playSound(null, trader.blockPosition(),
                            HumanVillagerSounds.maleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            });
        }
    }
}
