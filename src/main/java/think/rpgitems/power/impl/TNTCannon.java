package think.rpgitems.power.impl;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

import javax.annotation.Nullable;

import static think.rpgitems.power.Utils.checkCooldown;

/**
 * Power tntcannon.
 * <p>
 * The tntcannon power will fire active tnt on right click.
 * </p>
 */
@Meta(immutableTrigger = true, implClass = TNTCannon.Impl.class)
public class TNTCannon extends BasePower {

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
        return "tntcannon";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.tntcannon", (double) getCooldown() / 20d);
    }

    /**
     * Cooldown time of this power
     */
    public int getCooldown() {
        return cooldown;
    }

    public class Impl implements PowerRightClick, PowerLivingEntity {
        @Override
        public PowerResult<Void> rightClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event) {
            if (!checkCooldown(item, getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1.8, 0), TNTPrimed.class);
            tnt.setVelocity(player.getLocation().getDirection().multiply(2d));
            return PowerResult.ok();
        }

        @Override
        public Power getPower() {
            return TNTCannon.this;
        }

        @Override
        public PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack, LivingEntity entity, @Nullable Double value) {
            if (!checkCooldown(item, getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();
            player.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
            TNTPrimed tnt = player.getWorld().spawn(entity.getLocation().add(0, 1.8, 0), TNTPrimed.class);
            tnt.setVelocity(entity.getLocation().getDirection().multiply(2d));
            return PowerResult.ok();        }
    }
}
