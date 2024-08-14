package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerLeftClick;
import think.rpgitems.power.PowerResult;


public class ClickBlock extends Trigger<PlayerInteractEvent, PowerLeftClick, Void, Void> {
    public ClickBlock() {
        super(PlayerInteractEvent.class, PowerLeftClick.class, Void.class, Void.class, "CLICK_BLOCK");
    }

    ClickBlock(String name) {
        super(name, "CLICK_BLOCK", PlayerInteractEvent.class, PowerLeftClick.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerLeftClick power, Player player, ItemStack stack, PlayerInteractEvent event) {
        return power.leftClick(player, item, stack, event);
    }
}
