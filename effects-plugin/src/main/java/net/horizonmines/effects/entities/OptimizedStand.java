package net.horizonmines.effects.entities;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
// CraftBukkit end

public class OptimizedStand extends LivingEntity {
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
    private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0F, 0.0F, -1.0F);
    private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0F, 0.0F, 1.0F);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5F);
    private final NonNullList<ItemStack> handItems;
    private final NonNullList<ItemStack> armorItems;
    private boolean invisible;
    public Rotations headPose;
    public Rotations bodyPose;
    public Rotations leftArmPose;
    public Rotations rightArmPose;
    public Rotations leftLegPose;
    public Rotations rightLegPose;
    private boolean noTickPoseDirty = false;
    private boolean noTickEquipmentDirty = false;

    public OptimizedStand(EntityType<? extends ArmorStand> type, Level world) {
        super(type, world);
        this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        this.armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        this.headPose = OptimizedStand.DEFAULT_HEAD_POSE;
        this.bodyPose = OptimizedStand.DEFAULT_BODY_POSE;
        this.leftArmPose = OptimizedStand.DEFAULT_LEFT_ARM_POSE;
        this.rightArmPose = OptimizedStand.DEFAULT_RIGHT_ARM_POSE;
        this.leftLegPose = OptimizedStand.DEFAULT_LEFT_LEG_POSE;
        this.rightLegPose = OptimizedStand.DEFAULT_RIGHT_LEG_POSE;
        this.maxUpStep = 0.0F;
    }

    public OptimizedStand(Level world, double x, double y, double z) {
        this(EntityType.ARMOR_STAND, world);
        this.setPos(x, y, z);
    }

    // CraftBukkit start - SPIGOT-3607, SPIGOT-3637
    @Override
    public float getBukkitYaw() {
        return this.getYRot();
    }
    // CraftBukkit end

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();

        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    private boolean hasPhysics() {
        return !this.isNoGravity();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && this.hasPhysics();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ArmorStand.DATA_CLIENT_FLAGS, (byte) 0);
        this.entityData.define(ArmorStand.DATA_HEAD_POSE, OptimizedStand.DEFAULT_HEAD_POSE);
        this.entityData.define(ArmorStand.DATA_BODY_POSE, OptimizedStand.DEFAULT_BODY_POSE);
        this.entityData.define(ArmorStand.DATA_LEFT_ARM_POSE, OptimizedStand.DEFAULT_LEFT_ARM_POSE);
        this.entityData.define(ArmorStand.DATA_RIGHT_ARM_POSE, OptimizedStand.DEFAULT_RIGHT_ARM_POSE);
        this.entityData.define(ArmorStand.DATA_LEFT_LEG_POSE, OptimizedStand.DEFAULT_LEFT_LEG_POSE);
        this.entityData.define(ArmorStand.DATA_RIGHT_LEG_POSE, OptimizedStand.DEFAULT_RIGHT_LEG_POSE);
    }

    @Override
    public @NotNull Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public @NotNull ItemStack getItemBySlot(net.minecraft.world.entity.EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.handItems.get(slot.getIndex());
            case ARMOR -> this.armorItems.get(slot.getIndex());
        };
    }

    @Override
    public void setItemSlot(net.minecraft.world.entity.@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        // CraftBukkit start
        this.setItemSlot(slot, stack, false);
    }

    @Override
    public void setItemSlot(net.minecraft.world.entity.EquipmentSlot equipmentSlot, @NotNull ItemStack itemstack, boolean silent) {
        // CraftBukkit end
        this.verifyEquippedItem(itemstack);
        switch (equipmentSlot.getType()) {
            case HAND:
                this.onEquipItem(equipmentSlot, this.handItems.set(equipmentSlot.getIndex(), itemstack), itemstack, silent); // CraftBukkit
                break;
            case ARMOR:
                this.onEquipItem(equipmentSlot, this.armorItems.set(equipmentSlot.getIndex(), itemstack), itemstack, silent); // CraftBukkit
        }

        this.noTickEquipmentDirty = true; // Paper - Allow equipment to be updated even when tick disabled
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        ListTag tagList = new ListTag();

        CompoundTag nbttagcompound1;

        for (Iterator<ItemStack> iterator = this.armorItems.iterator(); iterator.hasNext(); tagList.add(nbttagcompound1)) {
            ItemStack itemstack = iterator.next();

            nbttagcompound1 = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(nbttagcompound1);
            }
        }

        nbt.put("ArmorItems", tagList);
        ListTag tagList2 = new ListTag();

        CompoundTag nbttagcompound2;

        for (Iterator<ItemStack> iterator1 = this.handItems.iterator(); iterator1.hasNext(); tagList2.add(nbttagcompound2)) {
            ItemStack itemStack = iterator1.next();

            nbttagcompound2 = new CompoundTag();
            if (!itemStack.isEmpty()) {
                itemStack.save(nbttagcompound2);
            }
        }

        nbt.put("HandItems", tagList2);
        nbt.putBoolean("Invisible", this.isInvisible());
        nbt.putBoolean("Small", this.isSmall());
        nbt.putBoolean("ShowArms", this.isShowArms());
        nbt.putBoolean("NoBasePlate", this.isNoBasePlate());
        nbt.put("Pose", this.writePose());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        ListTag nbttaglist;
        int i;

        if (nbt.contains("ArmorItems", 9)) {
            nbttaglist = nbt.getList("ArmorItems", 10);

            for (i = 0; i < this.armorItems.size(); ++i) {
                this.armorItems.set(i, ItemStack.of(nbttaglist.getCompound(i)));
            }
        }

        if (nbt.contains("HandItems", 9)) {
            nbttaglist = nbt.getList("HandItems", 10);

            for (i = 0; i < this.handItems.size(); ++i) {
                this.handItems.set(i, ItemStack.of(nbttaglist.getCompound(i)));
            }
        }

        this.setInvisible(nbt.getBoolean("Invisible"));
        this.setSmall(nbt.getBoolean("Small"));
        this.setShowArms(nbt.getBoolean("ShowArms"));
        this.setNoBasePlate(nbt.getBoolean("NoBasePlate"));
        this.noPhysics = !this.hasPhysics();
        // Paper end
        CompoundTag nbttagcompound1 = nbt.getCompound("Pose");

        this.readPose(nbttagcompound1);
    }

    private void readPose(CompoundTag nbt) {
        ListTag nbttaglist = nbt.getList("Head", 5);

        this.setHeadPose(nbttaglist.isEmpty() ? OptimizedStand.DEFAULT_HEAD_POSE : new Rotations(nbttaglist));
        ListTag nbttaglist1 = nbt.getList("Body", 5);

        this.setBodyPose(nbttaglist1.isEmpty() ? OptimizedStand.DEFAULT_BODY_POSE : new Rotations(nbttaglist1));
        ListTag nbttaglist2 = nbt.getList("LeftArm", 5);

        this.setLeftArmPose(nbttaglist2.isEmpty() ? OptimizedStand.DEFAULT_LEFT_ARM_POSE : new Rotations(nbttaglist2));
        ListTag nbttaglist3 = nbt.getList("RightArm", 5);

        this.setRightArmPose(nbttaglist3.isEmpty() ? OptimizedStand.DEFAULT_RIGHT_ARM_POSE : new Rotations(nbttaglist3));
        ListTag nbttaglist4 = nbt.getList("LeftLeg", 5);

        this.setLeftLegPose(nbttaglist4.isEmpty() ? OptimizedStand.DEFAULT_LEFT_LEG_POSE : new Rotations(nbttaglist4));
        ListTag nbttaglist5 = nbt.getList("RightLeg", 5);

        this.setRightLegPose(nbttaglist5.isEmpty() ? OptimizedStand.DEFAULT_RIGHT_LEG_POSE : new Rotations(nbttaglist5));
    }

    private CompoundTag writePose() {
        CompoundTag nbttagcompound = new CompoundTag();

        if (!OptimizedStand.DEFAULT_HEAD_POSE.equals(this.headPose)) {
            nbttagcompound.put("Head", this.headPose.save());
        }

        if (!OptimizedStand.DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            nbttagcompound.put("Body", this.bodyPose.save());
        }

        if (!OptimizedStand.DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            nbttagcompound.put("LeftArm", this.leftArmPose.save());
        }

        if (!OptimizedStand.DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            nbttagcompound.put("RightArm", this.rightArmPose.save());
        }

        if (!OptimizedStand.DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
            nbttagcompound.put("LeftLeg", this.leftLegPose.save());
        }

        if (!OptimizedStand.DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
            nbttagcompound.put("RightLeg", this.rightLegPose.save());
        }

        return nbttagcompound;
    }

    @Override
    public boolean isCollidable(boolean ignoreClimbing) { // Paper
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity entity) {}

    @Override
    protected void pushEntities() {
    }

    @Override
    public @NotNull InteractionResult interactAt(net.minecraft.world.entity.player.@NotNull Player player, @NotNull Vec3 hitPos, @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status != 32) {
            super.handleEntityEvent(status);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d1 = this.getBoundingBox().getSize() * 4.0D;

        if (Double.isNaN(d1) || d1 == 0.0D) {
            d1 = 4.0D;
        }

        d1 *= 64.0D;
        return distance < d1 * d1;
    }

    @Override
    protected float tickHeadTurn(float bodyRotation, float headRotation) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
        return 0.0F;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height * (this.isBaby() ? 0.5F : 0.9F);
    }

    @Override
    public double getMyRidingOffset() {
        return 0.10000000149011612D;
    }

    @Override
    public void travel(@NotNull Vec3 movementInput) {
        if (this.hasPhysics()) {
            super.travel(movementInput);
        }
    }

    @Override
    public void setYBodyRot(float bodyYaw) {
        this.yBodyRotO = this.yRotO = bodyYaw;
        this.yHeadRotO = this.yHeadRot = bodyYaw;
    }

    @Override
    public void setYHeadRot(float headYaw) {
        this.yBodyRotO = this.yRotO = headYaw;
        this.yHeadRotO = this.yHeadRot = headYaw;
    }

    @Override
    public void tick() {
        if (this.noTickPoseDirty) {
            this.noTickPoseDirty = false;
            this.updatePose();
        }

        if (this.noTickEquipmentDirty) {
            this.noTickEquipmentDirty = false;
            this.detectEquipmentUpdates();
        }
    }

    public void updatePose() {
        // Paper end
        Rotations vector3f = this.entityData.get(ArmorStand.DATA_HEAD_POSE);

        if (!this.headPose.equals(vector3f)) {
            this.setHeadPose(vector3f);
        }

        Rotations vector3f1 = this.entityData.get(ArmorStand.DATA_BODY_POSE);

        if (!this.bodyPose.equals(vector3f1)) {
            this.setBodyPose(vector3f1);
        }

        Rotations vector3f2 = this.entityData.get(ArmorStand.DATA_LEFT_ARM_POSE);

        if (!this.leftArmPose.equals(vector3f2)) {
            this.setLeftArmPose(vector3f2);
        }

        Rotations vector3f3 = this.entityData.get(ArmorStand.DATA_RIGHT_ARM_POSE);

        if (!this.rightArmPose.equals(vector3f3)) {
            this.setRightArmPose(vector3f3);
        }

        Rotations vector3f4 = this.entityData.get(ArmorStand.DATA_LEFT_LEG_POSE);

        if (!this.leftLegPose.equals(vector3f4)) {
            this.setLeftLegPose(vector3f4);
        }

        Rotations vector3f5 = this.entityData.get(ArmorStand.DATA_RIGHT_LEG_POSE);

        if (!this.rightLegPose.equals(vector3f5)) {
            this.setRightLegPose(vector3f5);
        }

    }

    @Override
    protected void updateInvisibilityStatus() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    // CraftBukkit start
    @Override
    public boolean shouldDropExperience() {
        return true; // MC-157395, SPIGOT-5193 even baby (small) armor stands should drop
    }
    // CraftBukkit end

    @Override
    public void kill() {
        org.bukkit.event.entity.EntityDeathEvent event = org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory.callEntityDeathEvent(this, drops); // CraftBukkit - call event // Paper - make cancellable
        if (event.isCancelled()) return; // Paper - make cancellable
        this.remove(RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isInvisible();
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public void setSmall(boolean small) {
        this.entityData.set(ArmorStand.DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS), 1, small));
    }

    public boolean isSmall() {
        return (this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS) & 1) != 0;
    }

    public void setShowArms(boolean showArms) {
        this.entityData.set(ArmorStand.DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS), 4, showArms));
    }

    public boolean isShowArms() {
        return (this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS) & 4) != 0;
    }

    public void setNoBasePlate(boolean hideBasePlate) {
        this.entityData.set(ArmorStand.DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS), 8, hideBasePlate));
    }

    public boolean isNoBasePlate() {
        return (this.entityData.get(ArmorStand.DATA_CLIENT_FLAGS) & 8) != 0;
    }

    private byte setBit(byte value, int bitField, boolean set) {
        if (set) {
            value = (byte) (value | bitField);
        } else {
            value = (byte) (value & ~bitField);
        }

        return value;
    }

    public void setHeadPose(Rotations angle) {
        this.headPose = angle;
        this.entityData.set(ArmorStand.DATA_HEAD_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public void setBodyPose(Rotations angle) {
        this.bodyPose = angle;
        this.entityData.set(ArmorStand.DATA_BODY_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public void setLeftArmPose(Rotations angle) {
        this.leftArmPose = angle;
        this.entityData.set(ArmorStand.DATA_LEFT_ARM_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public void setRightArmPose(Rotations angle) {
        this.rightArmPose = angle;
        this.entityData.set(ArmorStand.DATA_RIGHT_ARM_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public void setLeftLegPose(Rotations angle) {
        this.leftLegPose = angle;
        this.entityData.set(ArmorStand.DATA_LEFT_LEG_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public void setRightLegPose(Rotations angle) {
        this.rightLegPose = angle;
        this.entityData.set(ArmorStand.DATA_RIGHT_LEG_POSE, angle);
        this.noTickPoseDirty = true; // Paper - Allow updates when not ticking
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return this.leftLegPose;
    }

    public Rotations getRightLegPose() {
        return this.rightLegPose;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable();
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity attacker) {
        return true;
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public @NotNull Fallsounds getFallSounds() {
        return new Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Nullable
    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(@NotNull ServerLevel world, @NotNull LightningBolt lightning) {}

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> data) {
        if (ArmorStand.DATA_CLIENT_FLAGS.equals(data)) {
            this.refreshDimensions();
            this.blocksBuilding = true;
        }

        super.onSyncedDataUpdated(data);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return (this.isBaby() ? OptimizedStand.BABY_DIMENSIONS : this.getType().getDimensions());
    }

    @Override
    public @NotNull Vec3 getLightProbePosition(float tickDelta) {
        return super.getLightProbePosition(tickDelta);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !this.isInvisible();
    }

    @Override
    public void move(net.minecraft.world.entity.@NotNull MoverType type, @NotNull Vec3 movement) {
        super.move(type, movement);
    }

    @Override
    public boolean canBreatheUnderwater() { // Skips a bit of damage handling code, probably a micro-optimization
        return true;
    }
}
