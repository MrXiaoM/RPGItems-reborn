package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.PowerSneak;


public class DoubleSneak extends Trigger<PlayerToggleSneakEvent, PowerSneak, Void, Void> {
    public DoubleSneak() {
        super(PlayerToggleSneakEvent.class, PowerSneak.class, Void.class, Void.class, "DOUBLE_SNEAK");
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerSneak power, Player player, ItemStack i, PlayerToggleSneakEvent event) {
        return power.sneak(player, item, i, event);
    }
}
