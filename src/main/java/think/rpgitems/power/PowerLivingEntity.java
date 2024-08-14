package think.rpgitems.power;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public interface PowerLivingEntity extends Pimpl {

    /**
     * A trigger that fire a power with an entity and an double value (usually the damage)
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param entity Entity that involved in this trigger
     * @param value  Value that involved in this trigger
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack, LivingEntity entity, @Nullable Double value);
}
