package com.javic.slimpatch.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HumanVillagerSounds {

    // 🔹 Registro diferido para sonidos
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "slimpatch");

    // ========================================================
    // Clicks básicos
    // ========================================================
    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_CLICK =
            SOUND_EVENTS.register("male_click",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_click")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_CLICK =
            SOUND_EVENTS.register("female_click",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_click")));

    // ========================================================
    // Reacciones (masculino)
    // ========================================================
    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_REACTION_POSITIVE =
            SOUND_EVENTS.register("villager.male.reaction.positive",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.male.reaction.positive")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_REACTION_NEGATIVE =
            SOUND_EVENTS.register("villager.male.reaction.negative",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.male.reaction.negative")));

    // ========================================================
    // Reacciones (femenino)
    // ========================================================
    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_REACTION_POSITIVE =
            SOUND_EVENTS.register("villager.female.reaction.positive",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.female.reaction.positive")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_REACTION_NEGATIVE =
            SOUND_EVENTS.register("villager.female.reaction.negative",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.female.reaction.negative")));

    // ========================================================
    // Sonidos de daño y muerte
    // ========================================================
    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_HURT =
            SOUND_EVENTS.register("male_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT =
            SOUND_EVENTS.register("female_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_DEATH =
            SOUND_EVENTS.register("male_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_DEATH =
            SOUND_EVENTS.register("female_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_death")));

    // ========================================================
    // Helpers
    // ========================================================
    public static SoundEvent maleClick() {
        return MALE_CLICK.get();
    }

    public static SoundEvent femaleClick() {
        return FEMALE_CLICK.get();
    }

    public static SoundEvent maleReactionPositive() {
        return MALE_REACTION_POSITIVE.get();
    }

    public static SoundEvent maleReactionNegative() {
        return MALE_REACTION_NEGATIVE.get();
    }

    public static SoundEvent femaleReactionPositive() {
        return FEMALE_REACTION_POSITIVE.get();
    }

    public static SoundEvent femaleReactionNegative() {
        return FEMALE_REACTION_NEGATIVE.get();
    }

    public static SoundEvent maleHurt() {
        return MALE_HURT.get();
    }

    public static SoundEvent femaleHurt() {
        return FEMALE_HURT.get();
    }

    public static SoundEvent maleDeath() {
        return MALE_DEATH.get();
    }

    public static SoundEvent femaleDeath() {
        return FEMALE_DEATH.get();
    }

    // 🔹 Llamada desde SlimPatch.java
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}