package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

public interface PowerBowShoot extends Pimpl {

    @CheckReturnValue
    PowerResult<Float> bowShoot(Player player, RPGItem item, ItemStack stack, EntityShootBowEvent event);
}
