package think.rpgitems.power.impl;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.Events;
import think.rpgitems.I18n;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

import static think.rpgitems.power.Utils.checkCooldown;

/**
 * Power arrow.
 * <p>
 * The arrow power will fire an arrow on right click.
 * </p>
 */
@Meta(defaultTrigger = "RIGHT_CLICK", generalInterface = PowerPlain.class, implClass = Arrows.Impl.class)
public class Arrows extends BasePower {

    @Property(order = 0)
    public int cooldown = 0;
    @Property
    public int cost = 0;

    /**
     * Cost of this power
     */
    public int getCost() {
        return cost;
    }

    @Override
    public String getName() {
        return "arrow";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.arrow", (double) getCooldown() / 20d);
    }

    /**
     * Cooldown time of this power
     */
    public int getCooldown() {
        return cooldown;
    }

    public class Impl implements PowerRightClick, PowerLeftClick, PowerPlain {

        @Override
        public PowerResult<Void> rightClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            RPGItem item = ItemManager.toRPGItem(stack).orElse(null);
            if (item == null) return PowerResult.fail();
            if (!checkCooldown(item, getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
            Events.registerRPGProjectile(stack, player);
            Arrow arrow = player.launchProjectile(org.bukkit.entity.Arrow.class);
            arrow.setPickupStatus(org.bukkit.entity.Arrow.PickupStatus.DISALLOWED);
            Events.autoRemoveProjectile(arrow.getEntityId());
            arrow.setPersistent(false);
            return PowerResult.ok();
        }

        @Override
        public Power getPower() {
            return Arrows.this;
        }

        @Override
        public PowerResult<Void> leftClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }
    }
}
