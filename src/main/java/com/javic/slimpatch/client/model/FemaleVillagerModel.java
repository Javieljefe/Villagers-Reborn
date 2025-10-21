package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FemaleVillagerModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "female_villager"),
                    "main"
            );

    public FemaleVillagerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        // Cabeza
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Cuerpo con cubos extra (pecho estilo Minecraft)
        // ============================================================
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 32)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        // 🔸 Añadimos los cubos extra con nombres únicos para controlarlos luego
        body.addOrReplaceChild("cube", CubeListBuilder.create()
                        .texOffs(20, 21)
                        .addBox(-3.0F, 2.0F, -3.0F,
                                3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        body.addOrReplaceChild("cube2", CubeListBuilder.create()
                        .texOffs(23, 21)
                        .addBox(0.0F, 2.0F, -3.0F,
                                3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Brazos
        // ============================================================
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 32)
                        .addBox(-2.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-1.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 48)
                        .addBox(-1.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Piernas
        // ============================================================
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // ============================================================
    // 🔹 Ocultar cubos extra si la entidad es un bebé
    // ============================================================
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean isChild = entity.isBaby();

        // Accedemos a los cubos "cube" y "cube2" definidos arriba
        ModelPart chestLeft = this.body.getChild("cube");
        ModelPart chestRight = this.body.getChild("cube2");

        if (chestLeft != null) chestLeft.visible = !isChild;
        if (chestRight != null) chestRight.visible = !isChild;
    }
}