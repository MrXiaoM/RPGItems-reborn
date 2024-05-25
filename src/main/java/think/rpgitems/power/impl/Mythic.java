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

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.power.*;
import think.rpgitems.support.MythicSupport;

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "RIGHT_CLICK", generalInterface = PowerPlain.class, implClass = Mythic.Impl.class)
public class Mythic extends BasePower {
    @Property(order = 1)
    public int cooldown = 0;

    @Property
    public int cost = 0;

    @Property(required = true)
    public String skill;

    /**
     * Cooldown time of this power
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * Cost of this power
     */
    public int getCost() {
        return cost;
    }

    @Override
    public String getName() {
        return "mythic";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.mythic", skill);
    }

    public String getSkill() {
        return skill;
    }

    public class Impl implements PowerPlain, PowerRightClick, PowerLeftClick {
        @Override
        public PowerResult<Void> rightClick(final Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        public PowerResult<Void> fire(final Player player, ItemStack s) {
            if (!checkCooldown(getPower(), player, getCooldown(), false, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(player, s, getCost())) return PowerResult.cost();

            boolean result = MythicSupport.castSkill(player, getSkill());

            return result ? PowerResult.ok() : PowerResult.fail();
        }

        @Override
        public Power getPower() {
            return Mythic.this;
        }

        @Override
        public PowerResult<Void> leftClick(final Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }
    }
}
