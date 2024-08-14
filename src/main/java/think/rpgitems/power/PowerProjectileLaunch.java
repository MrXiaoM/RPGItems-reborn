package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when RPG Projectile launches
 */
public interface PowerProjectileLaunch extends Pimpl {
    /**
     * Calls when {@code player} using {@code stack} launching a projectile {@code arrow}
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> projectileLaunch(Player player, RPGItem item, ItemStack stack, ProjectileLaunchEvent event);
}
