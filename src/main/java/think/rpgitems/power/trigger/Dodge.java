package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerHurt;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;

class Dodge extends Trigger<EntityDamageEvent, PowerHurt, Void, Void> {

    @Property
    public double minDamage = Double.NEGATIVE_INFINITY;

    @Property
    public double maxDamage = Double.POSITIVE_INFINITY;

    Dodge() {
        super(EntityDamageEvent.class, PowerHurt.class, Void.class, Void.class, "DODGE");
    }

    public Dodge(String name) {
        super(name, "DODGE", EntityDamageEvent.class, PowerHurt.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerHurt power, Player player, ItemStack i, EntityDamageEvent event) {
        return power.hurt(player, item, i, event);
    }

    @Override
    public boolean check(Player player, ItemStack i, EntityDamageEvent event) {
        return event.getDamage() > minDamage && event.getDamage() < maxDamage;
    }
}
