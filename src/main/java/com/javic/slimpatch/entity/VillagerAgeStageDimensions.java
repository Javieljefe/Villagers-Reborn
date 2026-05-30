package com.javic.slimpatch.entity;

import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;

final class VillagerAgeStageDimensions {

    private static final float TEEN_WIDTH = 0.56F;
    private static final float TEEN_HEIGHT = 1.80F;
    private static final float TEEN_NAME_TAG_Y = 1.92F;
    private static final float CHILD_WIDTH = 0.52F;
    private static final float CHILD_HEIGHT = 1.55F;
    private static final float CHILD_NAME_TAG_Y = 1.88F;
    private static final float TODDLER_WIDTH = 0.48F;
    private static final float TODDLER_HEIGHT = 1.18F;
    private static final float TODDLER_NAME_TAG_Y = 1.80F;

    private VillagerAgeStageDimensions() {
    }

    public static EntityDimensions forStage(VillagerAgeStage ageStage, EntityDimensions adultDimensions) {
        if (ageStage == null || ageStage == VillagerAgeStage.ADULT) {
            return adultDimensions;
        }

        return switch (ageStage) {
            case TEEN -> scale(adultDimensions, TEEN_WIDTH, TEEN_HEIGHT, TEEN_NAME_TAG_Y);
            case CHILD -> scale(adultDimensions, CHILD_WIDTH, CHILD_HEIGHT, CHILD_NAME_TAG_Y);
            case TODDLER -> scale(adultDimensions, TODDLER_WIDTH, TODDLER_HEIGHT, TODDLER_NAME_TAG_Y);
            case ADULT -> adultDimensions;
        };
    }

    private static EntityDimensions scale(EntityDimensions adultDimensions, float width, float height, float nameTagY) {
        return adultDimensions.scale(width / adultDimensions.width(), height / adultDimensions.height())
                .withEyeHeight(height * 0.85F)
                .withAttachments(EntityAttachments.builder().attach(EntityAttachment.NAME_TAG, 0.0F, nameTagY, 0.0F));
    }
}