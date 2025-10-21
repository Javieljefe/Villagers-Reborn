package com.javic.slimpatch.client.renderer;

import com.javic.slimpatch.client.model.HumanWanderingTraderModelMale;
import com.javic.slimpatch.client.model.HumanWanderingTraderModelFemale;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import com.javic.slimpatch.util.SkinPathHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer híbrido para el Wandering Trader humano.
 * 🔹 Usa modelo masculino o femenino según el género sincronizado en la entidad.
 * 🔹 Selecciona la skin ya asignada por el servidor (sin aleatorizar nada en cliente).
 * 🔹 Mantiene escala coherente con villagers.
 */
@SuppressWarnings("rawtypes")
public class HumanWanderingTraderRenderer extends MobRenderer<HumanWanderingTraderEntity, HumanoidModel<HumanWanderingTraderEntity>> {

    private final HumanoidModel<HumanWanderingTraderEntity> maleModel;
    private final HumanoidModel<HumanWanderingTraderEntity> femaleModel;

    public HumanWanderingTraderRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanWanderingTraderModelMale<>(context.bakeLayer(HumanWanderingTraderModelMale.LAYER_LOCATION)), 0.5f);
        this.maleModel = this.getModel(); // reutiliza el modelo del super
        this.femaleModel = new HumanWanderingTraderModelFemale<>(context.bakeLayer(HumanWanderingTraderModelFemale.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(HumanWanderingTraderEntity entity) {
        boolean isFemale = entity.isFemale();
        int skinIndex = entity.getSkinIndex();

        // 🧩 Protección: evita texturas moradas si no se inicializó aún
        if (skinIndex <= 0) skinIndex = 1;

        String genderPath = isFemale ? "female" : "male";
        String path = String.format("textures/entity/human_trader/%s/skin_%02d.png", genderPath, skinIndex);

        return SkinPathHelper.from(path);
    }

    @Override
    public void render(HumanWanderingTraderEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // 🔹 Selecciona modelo según género
        this.model = entity.isFemale() ? femaleModel : maleModel;

        poseStack.pushPose();

        // 🔹 Escalado visual coherente con villagers
        if (entity.isFemale()) {
            poseStack.scale(0.94F, 0.94F, 0.94F);
        } else {
            poseStack.scale(0.98F, 0.98F, 0.98F);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}