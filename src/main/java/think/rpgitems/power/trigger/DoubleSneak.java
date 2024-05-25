package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.PowerSneak;


public class DoubleSneak extends Trigger<PlayerToggleSneakEvent, PowerSneak, Void, Void> {
    public DoubleSneak() {
        super("DOUBLE_SNEAK", PlayerToggleSneakEvent.class, PowerSneak.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(PowerSneak power, Player player, ItemStack i, PlayerToggleSneakEvent event) {
        return power.sneak(player, i, event);
    }
}
