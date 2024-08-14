package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when RPG Projectile hits
 */
public interface PowerProjectileHit extends Pimpl {
    /**
     * Calls when {@code player} using {@code stack} has launched a projectile {@code arrow} and it hit something
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> projectileHit(Player player, RPGItem item, ItemStack stack, ProjectileHitEvent event);
}
