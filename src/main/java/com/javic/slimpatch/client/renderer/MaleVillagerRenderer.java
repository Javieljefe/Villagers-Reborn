package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.CustomVillagerModelMale;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer específico para MaleVillagerEntity.
 * Usa el modelo masculino y selecciona skin sincronizada según el tema del mundo.
 * 🔹 Se ha añadido un microajuste visual para suavizar posibles fugas de textura en las piernas.
 */
public class MaleVillagerRenderer extends MobRenderer<MaleVillagerEntity, HumanoidModel<MaleVillagerEntity>> {

    public MaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomVillagerModelMale<>(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MaleVillagerEntity entity) {
        // ✅ Delegamos al entity, que sabe si usar modern o fantasy
        return entity.getSkinTexture();
    }

    @Override
    public void render(MaleVillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // ============================================================
        // 🔹 Microajuste visual para suavizar glitches de textura
        // ============================================================
        // No modifica el tamaño real ni la proporción del modelo.
        // Simplemente aplana un 0.5 % el render en el eje Z (frontal)
        // para evitar pequeños errores de UV en las piernas.
        poseStack.scale(1.0F, 1.0F, 0.995F);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}