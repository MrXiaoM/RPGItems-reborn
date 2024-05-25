package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerLeftClick;
import think.rpgitems.power.PowerResult;


public class ClickBlock extends Trigger<PlayerInteractEvent, PowerLeftClick, Void, Void> {
    public ClickBlock() {
        this("CLICK_BLOCK");
    }

    ClickBlock(String name) {
        super(name, PlayerInteractEvent.class, PowerLeftClick.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(PowerLeftClick power, Player player, ItemStack item, PlayerInteractEvent event) {
        return power.leftClick(player, item, event);
    }
}
