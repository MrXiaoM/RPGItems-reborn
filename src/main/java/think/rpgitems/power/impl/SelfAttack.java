package think.rpgitems.power.impl;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

@Meta(defaultTrigger = "HIT", implClass = SelfAttack.Impl.class)
public class SelfAttack extends BasePower {
    @Override
    public String getName() {
        return "selfattack";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.selfattack");
    }

    public class Impl implements PowerHit {
        @Override
        public PowerResult<Double> hit(final Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            if(!player.equals(entity)) {
                event.setDamage(0);
                event.setCancelled(true);
                player.damage(damage);
            }
            return PowerResult.ok(damage);
        }

        @Override
        public Power getPower() {
            return SelfAttack.this;
        }
    }
}
