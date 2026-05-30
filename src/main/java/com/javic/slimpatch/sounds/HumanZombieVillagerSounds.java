package com.javic.slimpatch.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HumanZombieVillagerSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "slimpatch");

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_AMBIENT =
            SOUND_EVENTS.register("zombie_villager.female.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "zombie_villager.female.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT =
            SOUND_EVENTS.register("zombie_villager.female.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "zombie_villager.female.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_DEATH =
            SOUND_EVENTS.register("zombie_villager.female.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "zombie_villager.female.death")));

    public static SoundEvent femaleAmbient() {
        return FEMALE_AMBIENT.get();
    }

    public static SoundEvent femaleHurt() {
        return FEMALE_HURT.get();
    }

    public static SoundEvent femaleDeath() {
        return FEMALE_DEATH.get();
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
