package think.rpgitems.power;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when hit some LivingEntity
 */
public interface PowerHit extends Pimpl {
    /**
     * Calls when {@code player} using {@code stack} hits an {@code entity} with {@code damage}
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  ItemStack of this RPGItem
     * @param entity LivingEntity being hit
     * @param damage Damage from this event
     * @param event  Event that triggered this power
     * @return PowerResult with proposed damage
     */
    @CheckReturnValue
    PowerResult<Double> hit(Player player, RPGItem item, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event);
}
