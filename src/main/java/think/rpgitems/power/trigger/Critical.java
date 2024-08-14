package think.rpgitems.power.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerHit;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;

import java.util.Optional;

import static think.rpgitems.power.Utils.maxWithCancel;

public class Critical extends Trigger<EntityDamageByEntityEvent, PowerHit, Double, Optional<Double>> {

    @Property
    public double minDamage = Double.NEGATIVE_INFINITY;

    @Property
    public double maxDamage = Double.POSITIVE_INFINITY;

    Critical() {
        super(EntityDamageByEntityEvent.class, PowerHit.class, Double.class, Optional.class, "CRITICAL");
    }

    public Critical(String name, int ignored){
        super(EntityDamageByEntityEvent.class, PowerHit.class, Double.class, Optional.class, name);
    }

    public Critical(String name) {
        super(name, "CRITICAL", EntityDamageByEntityEvent.class, PowerHit.class, Double.class, Optional.class);
    }

    @Override
    public Optional<Double> def(Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> next(Optional<Double> a, PowerResult<Double> b) {
        return b.isOK() ? Optional.ofNullable(maxWithCancel(a.orElse(null), b.data())) : a;
    }

    @Override
    public PowerResult<Double> warpResult(PowerResult<Void> overrideResult, PowerHit power, Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return overrideResult.with(event.getDamage());
    }

    @Override
    public PowerResult<Double> run(RPGItem item, PowerHit power, Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return power.hit(player, item, i, (LivingEntity) event.getEntity(), event.getDamage(), event);
    }

    @Override
    public boolean check(Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return event.getDamage() > minDamage && event.getDamage() < maxDamage;
    }
}
