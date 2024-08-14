package think.rpgitems.power.impl;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.*;

@Meta(immutableTrigger = true, defaultTrigger = "BOW_SHOOT", implClass = CancelBowArrow.Impl.class)
public class CancelBowArrow extends BasePower {

    @Property
    public boolean cancelArrow = true;

    @Override
    public String getName() {
        return "cancelbowarrow";
    }

    @Override
    public String displayText() {
        return null;
    }

    public boolean isCancelArrow() {
        return cancelArrow;
    }

    public class Impl implements PowerBowShoot {
        @Override
        public PowerResult<Float> bowShoot(Player player, RPGItem item, ItemStack itemStack, EntityShootBowEvent e) {
            if (isCancelArrow()) {
                Entity projectile = e.getProjectile();
                projectile.remove();
            } else {
                e.setCancelled(true);
            }
            return PowerResult.ok(e.getForce());
        }

        @Override
        public Power getPower() {
            return CancelBowArrow.this;
        }
    }
}
