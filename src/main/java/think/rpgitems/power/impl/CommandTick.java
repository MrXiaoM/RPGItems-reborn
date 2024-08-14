package think.rpgitems.power.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

import java.util.logging.Level;

import static think.rpgitems.RPGItems.plugin;

@Meta(
        defaultTrigger = {"TICK"},
        implClass = CommandTick.Impl.class
)
public class CommandTick extends BasePower {
    @Property(
            order = 1,
            required = true
    )
    public String command;
    @Property(
            order = 3
    )
    public String display = "Runs command";
    @Property(
            order = 7
    )
    public String permission = "";
    @Property
    public int cost;
    @Property(
            order = 2
    )
    public int interval;

    public int getCost() {
        return this.cost;
    }

    public int getInterval() {
        return this.interval;
    }

    public String getName() {
        return "commandtick";
    }

    public String displayText() {
        return display;
    }

    public String getCommand() {
        return command;
    }

    public String getDisplay() {
        return display;
    }

    public String getPermission() {
        return permission;
    }

    public class Impl implements PowerTick, PowerSneaking {
        public Impl() {
        }

        @Override
        public PowerResult<Void> tick(Player player, RPGItem item, ItemStack stack) {
            return this.fire(player, item, stack);
        }

        private PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack) {
            if (!Utils.checkAndSetCooldown(item, getPower(), player, getInterval(), false, true, item.getUid() + ".commandtick." + getPowerId())) {
                return PowerResult.cd();
            } else if (!item.consumeDurability(player, stack, getCost())) {
                return PowerResult.cost();
            } else {
                String cmd = Command.handlePlayerPlaceHolder(player, getCommand());
                return executeCommand(player, cmd);
            }
        }

        protected PowerResult<Void> executeCommand(Player player, String cmd) {
            if (!player.isOnline()) {
                return PowerResult.noop();
            } else {
                if (getPermission().equals("console")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                } else {
                    boolean wasOp = player.isOp();
                    Utils.attachPermission(player, getPermission());
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
        }

        @Override
        public Power getPower() {
            return CommandTick.this;
        }

        @Override
        public PowerResult<Void> sneaking(Player player, RPGItem item, ItemStack stack) {
            return this.fire(player, item, stack);
        }
    }
}
