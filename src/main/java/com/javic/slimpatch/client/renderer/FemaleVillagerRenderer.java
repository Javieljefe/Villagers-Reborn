package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.FemaleVillagerModel;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer específico para FemaleVillagerEntity.
 * Usa el modelo femenino y selecciona skin según el tema del mundo.
 * 🔹 Ahora también aplica un ligero escalado para que las female sean un poco más bajas y slim.
 */
public class FemaleVillagerRenderer extends MobRenderer<FemaleVillagerEntity, HumanoidModel<FemaleVillagerEntity>> {

    public FemaleVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new FemaleVillagerModel<>(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FemaleVillagerEntity entity) {
        // ✅ Delegamos en la entidad para elegir la skin (modern/fantasy)
        return entity.getSkinTexture();
    }

    @Override
    public void render(FemaleVillagerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // ============================================================
        // 🔹 Escalado combinado: más baja + ligeramente más slim
        // ============================================================
        poseStack.scale(0.95F, 0.95F, 0.95F); // 🔸 mantiene la altura reducida
        poseStack.scale(0.93F, 1.0F, 0.93F); // 🔸 reduce grosor (X/Z) sin afectar la altura

        // 💡 Niveles de “slimness” opcionales para probar:
        //  - 0.95F → muy sutil
        //  - 0.92F → equilibrado (recomendado)
        //  - 0.90F → más notorio

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}