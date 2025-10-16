package com.dragon.lastlife.nms;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.dragon.lastlife.utils.Utils;

/**
 * A custom NMS Fox with fully overridden AI. It ignores all default goals and
 * performs a very simple behavior: walk to a set target position, then stop.
 */
public class CustomFox extends Fox implements NmsEntity {

    private Vec3 target;
    State state;

    public CustomFox(Level level) {
        super(EntityType.FOX, level);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
        state = State.DELIVERING;
        ItemStack item = Items.CYAN_BUNDLE.getDefaultInstance();

        this.setItemSlot(EquipmentSlot.MAINHAND, item);
    }

    /**
     * Do not register any default goals to completely override AI.
     */
    @Override
    protected void registerGoals() {
        // Intentionally empty: no default goals
        // This effectively disables vanilla fox AI.
        // We rely on the tick() method for very simple behavior.
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

        if (target == null) return;
        switch(state){
            case DELIVERING -> {
                this.getNavigation().moveTo(target.x, target.y, target.z, 1);
                double distSq = this.position().distanceToSqr(target);
                if (distSq <= 2.25) { // within ~1.5 blocks
                    this.getNavigation().stop();
                    this.state = State.WAITING;
                }
            }
            case WAITING -> {
                ((org.bukkit.entity.Fox) this.getBukkitEntity()).setSitting(true);
            }
        }


    }

    public void setTarget(Vec3 target) {
        this.target = target;
        this.state = State.DELIVERING;
    }

    public State state() {
        return state;
    }

    public void state(State state) {
        this.state = state;
    }

    @Override
    public void pos(Location pos) {
        setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3 getDeliveryTarget() {
        return target;
    }

    // ---- Persistent Data (Bukkit PDC) helpers ----
    private static final NamespacedKey KEY_MARKER = new NamespacedKey(Utils.initializer(), "is_custom_fox");
    private static final NamespacedKey KEY_STATE = new NamespacedKey(Utils.initializer(), "fox_state");
    private static final NamespacedKey KEY_TX = new NamespacedKey(Utils.initializer(), "fox_target_x");
    private static final NamespacedKey KEY_TY = new NamespacedKey(Utils.initializer(), "fox_target_y");
    private static final NamespacedKey KEY_TZ = new NamespacedKey(Utils.initializer(), "fox_target_z");

    public static void writePersistentData(org.bukkit.entity.Fox fox, State state, Vec3 target) {
        PersistentDataContainer pdc = fox.getPersistentDataContainer();
        pdc.set(KEY_MARKER, PersistentDataType.BYTE, (byte) 1);
        if (state != null) pdc.set(KEY_STATE, PersistentDataType.STRING, state.name());
        if (target != null) {
            pdc.set(KEY_TX, PersistentDataType.DOUBLE, target.x);
            pdc.set(KEY_TY, PersistentDataType.DOUBLE, target.y);
            pdc.set(KEY_TZ, PersistentDataType.DOUBLE, target.z);
        }
    }

    public static Vec3 readTarget(org.bukkit.entity.Fox fox) {
        PersistentDataContainer pdc = fox.getPersistentDataContainer();
        Double tx = pdc.get(KEY_TX, PersistentDataType.DOUBLE);
        Double ty = pdc.get(KEY_TY, PersistentDataType.DOUBLE);
        Double tz = pdc.get(KEY_TZ, PersistentDataType.DOUBLE);
        if (tx == null || ty == null || tz == null) return null;
        return new Vec3(tx, ty, tz);
    }

    public enum State {
        DELIVERING,
        WAITING,
        DROPPING_OFF;
    }
}
