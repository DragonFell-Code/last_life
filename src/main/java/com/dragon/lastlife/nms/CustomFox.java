package com.dragon.lastlife.nms;

import com.dragon.lastlife.utils.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * A custom NMS Fox with fully overridden AI. It ignores all default goals and
 * performs a very simple behavior: walk to a set target position, then stop.
 */
public class CustomFox extends Fox implements NmsEntity {
    // ---- Persistent Data (Bukkit PDC) helpers ----
    public static final NamespacedKey KEY_CUSTOM_FOX_MARKER = new NamespacedKey(Utils.initializer(), "is_custom_fox");
    public static final NamespacedKey KEY_STATE = new NamespacedKey(Utils.initializer(), "fox_state");
    public static final NamespacedKey KEY_TARGET_X = new NamespacedKey(Utils.initializer(), "fox_target_x");
    public static final NamespacedKey KEY_TARGET_Y = new NamespacedKey(Utils.initializer(), "fox_target_y");
    public static final NamespacedKey KEY_TARGET_Z = new NamespacedKey(Utils.initializer(), "fox_target_z");

    private Vec3 target;
    State state;
    PersistentDataContainer persistentData;

    /*
      TODO:
         - [ ] Foxes can be captured in boats / minecarts
         - [ ] They can spawn in a pit and never be able to reach their goal
         - [ ] Players can trap them in pits / fences
         - [ ] Foxes can die to the void
         - [x] Leads: Foxes currently cant be led, but need to ensure it stays that way when we restrict click actions to owner only
         - [ ] Foxes drift around when trying to stop lol
    */

    public CustomFox(Level level) {
        super(EntityType.FOX, level);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
        persistentData = this.getBukkitEntity().getPersistentDataContainer();
        persistentData.set(KEY_CUSTOM_FOX_MARKER, PersistentDataType.BYTE, (byte) 1);
        ItemStack item = Items.CYAN_BUNDLE.getDefaultInstance();

        this.setItemSlot(EquipmentSlot.MAINHAND, item);
        this.setTarget(this, EntityTargetEvent.TargetReason.CUSTOM); // Prevent fox to eat item if edible
    }

    @Override
    public void load(@NotNull ValueInput input) {
        super.load(input);

        // Restore target/state from PDC
        Double tx = persistentData.get(KEY_TARGET_X, PersistentDataType.DOUBLE);
        Double ty = persistentData.get(KEY_TARGET_Y, PersistentDataType.DOUBLE);
        Double tz = persistentData.get(KEY_TARGET_Z, PersistentDataType.DOUBLE);
        String state = persistentData.get(KEY_STATE, PersistentDataType.STRING);

        if (tx != null && ty != null && tz != null) {
            this.target = new Vec3(tx, ty, tz);
        }
        if (state != null) {
            try {
                this.state = CustomFox.State.valueOf(state);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Do not register any default goals to completely override AI.
     */
    @Override
    protected void registerGoals() {
        // We need to call the super method, cause it sets some private fields that needs to be set to restore Fox after reload
        super.registerGoals();

        // Intentionally remove all goals
        // This effectively disables vanilla fox AI.
        // We rely on the tick() method for very simple behavior.
        this.goalSelector.removeAllGoals((goal) -> true);
        this.targetSelector.removeAllGoals((goal) -> true);
    }

    @Override
    public boolean canHoldItem(@NotNull ItemStack stack) {
        return false; // Prevent Fox from picking up food and dropping custom loot
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        // when reading the state, it will set some target goals, clearing them
        this.targetSelector.removeAllGoals((goal) -> true);
    }

    /**
     * Extremely simple navigation loop. Server-side only.
     * If a target is set, the fox will path toward it. Once close enough,
     * it stops navigating and marks as arrived.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (this.target == null) return;
        switch (this.state) {
            case DELIVERING -> {
                if (!this.getNavigation().isInProgress()) {
                    this.getNavigation().moveTo(target.x, target.y, target.z, 1);
                }

                if (this.reachedTarget()) {
                    this.getNavigation().stop();
                    // Prevent residual motion causing the fox to drift past the target
                    // and flip back into DELIVERING. Zero the entity velocity explicitly.
                    this.setDeltaMovement(new Vec3(0, 0, 0));
                    this.setState(State.WAITING);
                    this.setSitting(true);
                }
            }
            case WAITING -> {
                if (!this.reachedTarget()) {
                    this.setState(State.DELIVERING);
                    this.setSitting(false);
                }
            }
        }
    }

    public boolean reachedTarget() {
        if (this.target == null) {
            return true; // Or false ? IDK what makes sense
        }
        double distSq = this.position().distanceToSqr(this.target);

        return distSq <= 2.56; // within ~1.6 blocks
    }

    public void setTarget(Vec3 target) {
        this.target = target;

        if (target == null) {
            persistentData.remove(KEY_TARGET_Y);
            persistentData.remove(KEY_TARGET_Z);
            persistentData.remove(KEY_TARGET_X);
        } else {
            persistentData.set(KEY_TARGET_Y, PersistentDataType.DOUBLE, target.y);
            persistentData.set(KEY_TARGET_Z, PersistentDataType.DOUBLE, target.z);
            persistentData.set(KEY_TARGET_X, PersistentDataType.DOUBLE, target.x);
        }
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
        persistentData.set(KEY_STATE, PersistentDataType.STRING, state.name());
    }

    @Override
    public void setPos(Location pos) {
        setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public void deliverTo(Vec3 target) {
        setTarget(target);
        setState(State.DELIVERING);
    }

    public enum State {
        DELIVERING,
        WAITING,
        DROPPING_OFF
    }
}
