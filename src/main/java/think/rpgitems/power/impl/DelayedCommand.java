package think.rpgitems.power.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Meta;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;

import static think.rpgitems.power.Utils.checkAndSetCooldown;

/**
 * Power delayedcommand.
 * <p>
 * The item will run {@link #getCommand()} on click with a {@link #delay}
 * giving the permission {@link #getPermission()} just for the use of the command.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
@Meta(defaultTrigger = "RIGHT_CLICK", implClass = DelayedCommand.Impl.class)
public class DelayedCommand extends Command {
    @Property(order = 0)
    public int delay = 20;

    @Property
    public boolean cmdInPlace = false;

    /**
     * Delay before executing command
     */
    public int getDelay() {
        return delay;
    }

    public boolean isCmdInPlace() {
        return cmdInPlace;
    }

    @Override
    public String getName() {
        return "delayedcommand";
    }

    public class Impl extends Command.Impl {
        @Override
        public PowerResult<Void> leftClick(final Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Void> fire(Player target, RPGItem item, ItemStack stack) {
            int uid = item.getUid();
            if (!checkAndSetCooldown(item, getPower(), target, getCooldown(), true, false, uid + "." + getCommand()))
                return PowerResult.cd();
            if (!item.consumeDurability(target, stack, getCost())) return PowerResult.cost();
            String cmd;
            if (!cmdInPlace) {
                cmd = handlePlayerPlaceHolder(target, getCommand());
            }else {
                cmd = null;
            }
            (new BukkitRunnable() {
                @Override
                public void run() {
                    if (cmd == null){
                        executeCommand(target);
                    }else {
                        executeCommand(target, cmd);
                    }
                }
            }).runTaskLater(RPGItems.plugin, getDelay());
            return PowerResult.ok();
        }

        @Override
        public Power getPower() {
            return DelayedCommand.this;
        }

        @Override
        public PowerResult<Void> sneak(Player player, RPGItem item, ItemStack stack, PlayerToggleSneakEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Void> sprint(Player player, RPGItem item, ItemStack stack, PlayerToggleSprintEvent event) {
            return fire(player, item, stack);
        }

        @Override
        public PowerResult<Void> hurt(Player target, RPGItem item, ItemStack stack, EntityDamageEvent event) {
            return fire(target, item, stack);
        }

        @Override
        public PowerResult<Void> rightClick(final Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, item, stack);
        }
    }
}
