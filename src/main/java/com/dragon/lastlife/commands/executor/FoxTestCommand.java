package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

/**
 * Test command to reproduce fox delivery oscillation issues.
 * Usage: /foxtest spawn [distance] - spawns a fox that will deliver to a location that might cause oscillation
 * Usage: /foxtest precise <x> <y> <z> - spawns a fox to deliver to exact coordinates for precise testing
 */
public class FoxTestCommand extends CommandExecutor {
    
    public FoxTestCommand(Initializer initializer) {
        super(initializer, "foxtest");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .requires(source -> source.getSender().hasPermission("lastlife.admin.foxtest"))
                .executes(context -> showUsage(context, "lastlife.admin.foxtest"))
                .then(literal("spawn")
                        .executes(context -> {
                            // Default: spawn fox 10 blocks away, deliver to player location (likely to overshoot)
                            return spawnTestFox(context, 10.0);
                        })
                        .then(argument("distance", DoubleArgumentType.doubleArg(1.0, 50.0))
                                .executes(context -> {
                                    double distance = DoubleArgumentType.getDouble(context, "distance");
                                    return spawnTestFox(context, distance);
                                })))
                .then(literal("precise")
                        .then(argument("x", DoubleArgumentType.doubleArg())
                                .then(argument("y", DoubleArgumentType.doubleArg())
                                        .then(argument("z", DoubleArgumentType.doubleArg())
                                                .executes(context -> {
                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                    return spawnTestFoxPrecise(context, x, y, z);
                                                })))))
                .then(literal("cleanup")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                return logError(context, "This command can only be used by players.");
                            }
                            
                            // Remove all custom foxes in a 100 block radius
                            int removed = 0;
                            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 100, 100, 100)) {
                                if (entity instanceof org.bukkit.entity.Fox fox) {
                                    // Check if it's our custom fox via PDC
                                    if (fox.getPersistentDataContainer().has(CustomFox.KEY_CUSTOM_FOX_MARKER)) {
                                        entity.remove();
                                        removed++;
                                    }
                                }
                            }
                            
                            context.getSource().getSender().sendMessage("Removed " + removed + " test foxes.");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private int spawnTestFox(com.mojang.brigadier.context.CommandContext<io.papermc.paper.command.brigadier.CommandSourceStack> context, double distance) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            return logError(context, "This command can only be used by players.");
        }

        try {
            // Spawn fox at distance behind player (so it has to travel past the target)
            Location playerLoc = player.getLocation();
            Location spawnLoc = playerLoc.clone().subtract(playerLoc.getDirection().multiply(distance));
            spawnLoc.setY(playerLoc.getY() + 3); // Spawn above ground
            
            // Make sure spawn location has air
            while (spawnLoc.getBlock().getType().isSolid()) {
                spawnLoc.add(0, 1, 0);
            }
            
            // Target is player's current location - fox will likely overshoot
            Vec3 target = new Vec3(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());
            
            CustomFox fox = NmsEntityFactory.spawn(spawnLoc, CustomFox.class);
            fox.deliverTo(target);
            
            context.getSource().getSender().sendMessage(
                String.format("Spawned test fox at (%.1f, %.1f, %.1f) to deliver to (%.1f, %.1f, %.1f). Distance: %.1f blocks.",
                    spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(),
                    target.x, target.y, target.z,
                    distance));
            
            context.getSource().getSender().sendMessage(
                "Watch for oscillation between DELIVERING and WAITING states. Use /foxtest cleanup to remove test foxes.");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return logError(context, "Failed to spawn test fox: " + e.getMessage());
        }
    }

    private int spawnTestFoxPrecise(com.mojang.brigadier.context.CommandContext<io.papermc.paper.command.brigadier.CommandSourceStack> context, 
                                  double x, double y, double z) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            return logError(context, "This command can only be used by players.");
        }

        try {
            // Spawn fox at player location
            Location spawnLoc = player.getLocation().clone().add(0, 3, 5);
            
            // Make sure spawn location has air
            while (spawnLoc.getBlock().getType().isSolid()) {
                spawnLoc.add(0, 1, 0);
            }
            
            Vec3 target = new Vec3(x, y, z);
            
            CustomFox fox = NmsEntityFactory.spawn(spawnLoc, CustomFox.class);
            fox.deliverTo(target);
            
            context.getSource().getSender().sendMessage(
                String.format("Spawned test fox at (%.1f, %.1f, %.1f) to deliver to (%.1f, %.1f, %.1f).",
                    spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(),
                    target.x, target.y, target.z));
            
            context.getSource().getSender().sendMessage(
                "Watch for oscillation between DELIVERING and WAITING states. Use /foxtest cleanup to remove test foxes.");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return logError(context, "Failed to spawn test fox: " + e.getMessage());
        }
    }
}