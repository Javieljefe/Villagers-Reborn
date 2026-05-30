package com.javic.slimpatch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.npc.Villager;

public class MountedVillagerRenderer extends VillagerRenderer {
    private final float horseMountedOffset;
    private final float camelMountedOffset;

    public MountedVillagerRenderer(EntityRendererProvider.Context context, float horseMountedOffset, float camelMountedOffset) {
        super(context);
        this.horseMountedOffset = horseMountedOffset;
        this.camelMountedOffset = camelMountedOffset;
    }

    @Override
    public void render(Villager entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.getVehicle() instanceof Horse) {
            poseStack.translate(0.0F, this.horseMountedOffset, 0.0F);
        } else if (entity.getVehicle() instanceof Camel) {
            poseStack.translate(0.0F, this.camelMountedOffset, 0.0F);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
