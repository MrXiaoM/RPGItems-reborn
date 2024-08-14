package think.rpgitems.power.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.event.BeamEndEvent;
import think.rpgitems.event.BeamHitBlockEvent;
import think.rpgitems.event.BeamHitEntityEvent;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

import java.util.logging.Level;

import static think.rpgitems.RPGItems.plugin;
import static think.rpgitems.power.Utils.attachPermission;
import static think.rpgitems.power.Utils.checkAndSetCooldown;


/**
 * Power commandhit.
 * <p>
 * The item will run {@link #getCommand()} when player hits some {@link LivingEntity}
 * giving the permission {@link #getPermission()} just for the use of the command.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
@Meta(defaultTrigger = "HIT", generalInterface = PowerLivingEntity.class, implClass = CommandHit.Impl.class)
public class CommandHit extends Command {

    @Property
    public double minDamage = 0;

    public static String handleEntityPlaceHolder(LivingEntity e, String cmd) {
        cmd = cmd.replaceAll("\\{entity}", e.getName());
        cmd = cmd.replaceAll("\\{entity\\.uuid}", e.getUniqueId().toString());
        cmd = cmd.replaceAll("\\{entity\\.x}", Float.toString(e.getLocation().getBlockX()));
        cmd = cmd.replaceAll("\\{entity\\.y}", Float.toString(e.getLocation().getBlockY()));
        cmd = cmd.replaceAll("\\{entity\\.z}", Float.toString(e.getLocation().getBlockZ()));
        cmd = cmd.replaceAll("\\{entity\\.yaw}", Float.toString(90 + e.getEyeLocation().getYaw()));
        cmd = cmd.replaceAll("\\{entity\\.pitch}", Float.toString(-e.getEyeLocation().getPitch()));
        return cmd;
    }

    /**
     * Minimum damage to trigger
     */
    public double getMinDamage() {
        return minDamage;
    }

    @Override
    public String getName() {
        return "commandhit";
    }

    @Override
    public String displayText() {
        return ChatColor.GREEN + getDisplay();
    }

    public class Impl implements PowerHit, PowerLivingEntity, PowerBeamHit {
        @Override
        public PowerResult<Double> hit(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            return fire(player, item, stack, entity, damage).with(damage);
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack, LivingEntity entity, Double damage) {
            if (damage == null || damage < getMinDamage()) return PowerResult.noop();
            if (!checkAndSetCooldown(item, getPower(), player, getCooldown(), true, false, item.getUid() + "." + getCommand()))
                return PowerResult.cd();
            if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();

            return executeCommand(player, entity, damage);
        }

        @Override
        public Power getPower() {
            return CommandHit.this;
        }

        /**
         * Execute command
         *
         * @param player player
         * @param e      entity
         * @param damage damage
         * @return PowerResult with proposed damage
         */
        protected PowerResult<Void> executeCommand(Player player, LivingEntity e, double damage) {
            if (!player.isOnline()) return PowerResult.noop();
            String cmd = getCommand();
            cmd = handleEntityPlaceHolder(e, cmd);
            cmd = handlePlayerPlaceHolder(player, cmd);
            cmd = cmd.replaceAll("\\{damage}", String.valueOf(damage));

            if (getPermission().equals("console")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                boolean wasOp = player.isOp();
                attachPermission(player, getPermission());
                if (getPermission().equals("*")) {
                    try {
                        player.setOp(true);
                        player.performCommand(cmd);
                    } catch (Throwable t) {
                        plugin.getLogger().log(Level.WARNING, "Run OP command failed for player " + player.getName(), t);
                    } finally {
                        if (!wasOp) {
                            player.setOp(false);
                        }
                    }
                } else {
                    player.performCommand(cmd);
                }
            }
            return PowerResult.ok();
        }

        @Override
        public PowerResult<Double> hitEntity(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, BeamHitEntityEvent event) {
            return fire(player, item, stack, entity, damage).with(damage);
        }

        @Override
        public PowerResult<Void> hitBlock(Player player, RPGItem item, ItemStack stack, Location location, BeamHitBlockEvent event) {
            return PowerResult.noop();
        }

        @Override
        public PowerResult<Void> beamEnd(Player player, RPGItem item, ItemStack stack, Location location, BeamEndEvent event) {
            return PowerResult.noop();
        }
    }
}
