package com.javic.slimpatch.client.cutscene;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class WeddingCutsceneMusicInstance extends AbstractTickableSoundInstance {

    public WeddingCutsceneMusicInstance(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.RECORDS, RandomSource.create());
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.looping = false;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    @Override
    public void tick() {
    }

    public void stopPlayback() {
        this.stop();
    }
}
