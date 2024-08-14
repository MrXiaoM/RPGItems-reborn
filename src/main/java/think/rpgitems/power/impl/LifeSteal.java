/*
 *  This file is part of RPG Items.
 *
 *  RPG Items is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  RPG Items is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RPG Items.  If not, see <http://www.gnu.org/licenses/>.
 */
package think.rpgitems.power.impl;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

import java.util.Random;

/**
 * Power lifesteal.
 * <p>
 * The lifesteal power will steal enemy life
 * in a chance of 1/{@link #chance}
 * </p>
 */
@Meta(defaultTrigger = "HIT", generalInterface = PowerLivingEntity.class, implClass = LifeSteal.Impl.class)
public class LifeSteal extends BasePower {

    @Property(order = 0)
    public int chance = 20;
    @Property
    public int cost = 0;
    @Property
    public double factor = 1;

    private Random random = new Random();

    /**
     * Cost of this power
     */
    public int getCost() {
        return cost;
    }

    /**
     * Factor of life steal
     */
    public double getFactor() {
        return factor;
    }

    @Override
    public String getName() {
        return "lifesteal";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.lifesteal", getChance());
    }

    /**
     * Chance of triggering this power
     */
    public int getChance() {
        return chance;
    }

    public Random getRandom() {
        return random;
    }

    public class Impl implements PowerHit, PowerLivingEntity {

        @Override
        public PowerResult<Double> hit(Player player, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            return fire(player, stack, entity, damage).with(damage);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack, LivingEntity entity, Double damage) {
            if (getRandom().nextInt(getChance()) == 0 && damage != null) {
                RPGItem item = ItemManager.toRPGItem(stack).orElse(null);
                if (item == null) return PowerResult.fail();
                if (!item.consumeDurability(player, stack, getCost())) return PowerResult.cost();
                player.setHealth(Math.max(Math.min(player.getHealth() + damage * getFactor(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()), 0.01));
                return PowerResult.ok();
            }
            return PowerResult.noop();
        }

        @Override
        public Power getPower() {
            return LifeSteal.this;
        }
    }
}
