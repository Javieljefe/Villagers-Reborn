package com.javic.slimpatch.menu;

import com.javic.slimpatch.ModMenus;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.entity.VillagerEquipmentHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public class VillagerEquipmentMenu extends AbstractContainerMenu {

    private static final int EQUIPMENT_SLOT_COUNT = 6;
    private static final int STORAGE_SLOT_COUNT = 27;
    public static final int ARMOR_SLOT_X = 8;
    public static final int ARMOR_SLOT_START_Y = 8;
    public static final int ARMOR_SLOT_SPACING = 18;
    public static final int OFFHAND_SLOT_X = 77;
    public static final int OFFHAND_SLOT_Y = 62;
    public static final int MAINHAND_SLOT_X = 77;
    public static final int MAINHAND_SLOT_Y = 40;
    public static final int STORAGE_START_X = 108;
    public static final int STORAGE_START_Y = 18;
    public static final int PLAYER_INV_START_X = 108;
    public static final int PLAYER_INV_START_Y = 84;
    public static final int HOTBAR_START_X = 108;
    public static final int HOTBAR_Y = 142;
    private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    private final Container equipmentContainer;
    private final Container storageContainer;
    private final LivingEntity villager;
    private final int villagerId;
    private final CommandableVillager commandableVillager;
    private final VillagerCommandHandler.TemporaryCommandStateSnapshot temporaryCommandStateSnapshot;
    private final boolean temporaryStayApplied;

    public VillagerEquipmentMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainer(EQUIPMENT_SLOT_COUNT), new SimpleContainer(STORAGE_SLOT_COUNT));
    }

    public VillagerEquipmentMenu(int containerId, Inventory playerInventory, LivingEntity villager, Container storageContainer) {
        this(containerId, playerInventory, villager, new EquipmentContainer(villager), storageContainer);
    }

    private VillagerEquipmentMenu(int containerId, Inventory playerInventory, LivingEntity villager, Container equipmentContainer, Container storageContainer) {
        super(ModMenus.VILLAGER_EQUIPMENT.get(), containerId);
        this.villager = villager;
        this.villagerId = villager == null ? -1 : villager.getId();
        this.commandableVillager = villager instanceof CommandableVillager commandable ? commandable : null;
        this.equipmentContainer = equipmentContainer;
        this.storageContainer = storageContainer;
        if (this.commandableVillager != null && !playerInventory.player.level().isClientSide) {
            this.temporaryCommandStateSnapshot = VillagerCommandHandler.createTemporaryStaySnapshot(this.commandableVillager);
            this.temporaryStayApplied = VillagerCommandHandler.beginTemporaryStay((net.minecraft.world.entity.npc.Villager) villager, this.commandableVillager);
        } else {
            this.temporaryCommandStateSnapshot = null;
            this.temporaryStayApplied = false;
        }

        this.equipmentContainer.startOpen(playerInventory.player);
        this.storageContainer.startOpen(playerInventory.player);

        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 0, ARMOR_SLOT_X, ARMOR_SLOT_START_Y));
        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 1, ARMOR_SLOT_X, ARMOR_SLOT_START_Y + ARMOR_SLOT_SPACING));
        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 2, ARMOR_SLOT_X, ARMOR_SLOT_START_Y + ARMOR_SLOT_SPACING * 2));
        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 3, ARMOR_SLOT_X, ARMOR_SLOT_START_Y + ARMOR_SLOT_SPACING * 3));
        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 4, MAINHAND_SLOT_X, MAINHAND_SLOT_Y));
        this.addSlot(new EquipmentInventorySlot(this.equipmentContainer, 5, OFFHAND_SLOT_X, OFFHAND_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.storageContainer, col + row * 9, STORAGE_START_X + col * 18, STORAGE_START_Y + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_START_X + col * 18, PLAYER_INV_START_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, HOTBAR_START_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        int equipmentStart = 0;
        int equipmentEnd = EQUIPMENT_SLOT_COUNT;
        int storageStart = equipmentEnd;
        int storageEnd = storageStart + STORAGE_SLOT_COUNT;
        int playerStart = storageEnd;
        int playerEnd = this.slots.size();

        if (index < playerStart) {
            if (!this.moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            EquipmentSlot preferredSlot = getPreferredEquipmentSlot(stack);
            if (preferredSlot != null) {
                int equipmentIndex = getEquipmentIndex(preferredSlot);
                if (equipmentIndex >= equipmentStart && equipmentIndex < equipmentEnd && !this.slots.get(equipmentIndex).hasItem()) {
                    if (!this.moveItemStackTo(stack, equipmentIndex, equipmentIndex + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, storageStart, storageEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, storageStart, storageEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.villager == null) {
            return true;
        }
        return this.villager.isAlive() && player.distanceToSqr(this.villager) <= 64.0D;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && this.temporaryStayApplied && this.villager instanceof net.minecraft.world.entity.npc.Villager villager && this.commandableVillager != null) {
            VillagerCommandHandler.restoreTemporaryStay(villager, this.commandableVillager, this.temporaryCommandStateSnapshot);
        }
        this.equipmentContainer.stopOpen(player);
        this.storageContainer.stopOpen(player);
    }

    public int getEquipmentSlotCount() {
        return EQUIPMENT_SLOT_COUNT;
    }

    public int getVillagerId() {
        return this.villagerId;
    }

    public boolean isForVillager(int entityId) {
        return this.villagerId == entityId;
    }

    private static int getEquipmentIndex(EquipmentSlot slot) {
        for (int i = 0; i < EQUIPMENT_SLOT_ORDER.length; i++) {
            if (EQUIPMENT_SLOT_ORDER[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private class EquipmentInventorySlot extends Slot {
        private final int equipmentIndex;

        public EquipmentInventorySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.equipmentIndex = slot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            EquipmentSlot targetSlot = EQUIPMENT_SLOT_ORDER[this.equipmentIndex];
            if (targetSlot == EquipmentSlot.MAINHAND) {
                return true;
            }
            if (targetSlot == EquipmentSlot.OFFHAND) {
                return true;
            }
            return getPreferredEquipmentSlot(stack) == targetSlot;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot();
        }
        if (stack.getItem() instanceof ShieldItem) {
            return EquipmentSlot.OFFHAND;
        }
        if (stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof DiggerItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MaceItem) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }

    private static class EquipmentContainer implements Container {
        private static final String TAG_VILLAGERPATROLS_EVENT_ENTITY = "villagerpatrols_event_entity";
        private static final String TAG_VILLAGERPATROLS_GROUP_ID = "villagerpatrols_group_id";
        private static final String TAG_VILLAGERPATROLS_GROUP_TYPE = "villagerpatrols_group_type";
        private static final String TAG_VILLAGERPATROLS_MANUAL_MAINHAND_OVERRIDE = "villagerpatrols_manual_mainhand_override";
        private final LivingEntity entity;
        private final ItemStack[] stacks;

        private EquipmentContainer(LivingEntity entity) {
            this.entity = entity;
            this.stacks = new ItemStack[EQUIPMENT_SLOT_COUNT];

            if (this.entity instanceof VillagerEquipmentHolder holder
                    && holder.getPersistentMainHandItem().isEmpty()
                    && !this.entity.getMainHandItem().isEmpty()) {
                holder.setPersistentMainHandItem(this.entity.getMainHandItem().copy());
            }

            for (int i = 0; i < EQUIPMENT_SLOT_COUNT; i++) {
                this.stacks[i] = this.getEquipmentStack(EQUIPMENT_SLOT_ORDER[i]);
            }
        }

        private ItemStack getEquipmentStack(EquipmentSlot slot) {
            return switch (slot) {
                case MAINHAND -> this.entity instanceof VillagerEquipmentHolder holder
                        ? holder.getPersistentMainHandItem()
                        : this.entity.getMainHandItem().copy();
                case OFFHAND -> this.entity.getOffhandItem().copy();
                default -> this.entity.getItemBySlot(slot).copy();
            };
        }

        private void setEquipmentStack(EquipmentSlot slot, ItemStack stack) {
            ItemStack copy = stack.copy();
            switch (slot) {
                case MAINHAND -> {
                    this.markVillagerPatrolsMainHandOverride();
                    if (this.entity instanceof VillagerEquipmentHolder holder) {
                        holder.setPersistentMainHandItem(copy);
                        holder.syncPersistentMainHand();
                    } else {
                        this.entity.setItemInHand(InteractionHand.MAIN_HAND, copy);
                    }
                }
                case OFFHAND -> this.entity.setItemInHand(InteractionHand.OFF_HAND, copy);
                default -> this.entity.setItemSlot(slot, copy);
            }
        }

        private void markVillagerPatrolsMainHandOverride() {
            CompoundTag data = this.entity.getPersistentData();
            if (data.getBoolean(TAG_VILLAGERPATROLS_EVENT_ENTITY)
                    || data.contains(TAG_VILLAGERPATROLS_GROUP_ID)
                    || data.contains(TAG_VILLAGERPATROLS_GROUP_TYPE)) {
                data.putBoolean(TAG_VILLAGERPATROLS_MANUAL_MAINHAND_OVERRIDE, true);
            }
        }

        @Override
        public int getContainerSize() {
            return EQUIPMENT_SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : this.stacks) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return this.stacks[slot];
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack current = this.stacks[slot];
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack split = current.split(amount);

            if (current.isEmpty()) {
                this.stacks[slot] = ItemStack.EMPTY;
            }

            this.setEquipmentStack(EQUIPMENT_SLOT_ORDER[slot], this.stacks[slot]);
            return split;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack current = this.stacks[slot];
            this.stacks[slot] = ItemStack.EMPTY;
            this.setEquipmentStack(EQUIPMENT_SLOT_ORDER[slot], ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(copy.getCount(), this.getMaxStackSize()));
            this.stacks[slot] = copy;
            this.setEquipmentStack(EQUIPMENT_SLOT_ORDER[slot], copy);
        }

        @Override
        public void setChanged() {
            for (int i = 0; i < EQUIPMENT_SLOT_COUNT; i++) {
                this.setEquipmentStack(EQUIPMENT_SLOT_ORDER[i], this.stacks[i]);
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return this.entity.isAlive() && player.distanceToSqr(this.entity) <= 64.0D;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < EQUIPMENT_SLOT_COUNT; i++) {
                this.stacks[i] = ItemStack.EMPTY;
                this.setEquipmentStack(EQUIPMENT_SLOT_ORDER[i], ItemStack.EMPTY);
            }
        }
    }
}
