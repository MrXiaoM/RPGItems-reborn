package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;


public class Drop extends Trigger<PlayerDropItemEvent, PowerPlain, Void, Void> {
    public Drop() {
        this("DROP");
    }

    Drop(String name) {
        super(name, PlayerDropItemEvent.class, PowerPlain.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(PowerPlain power, Player player, ItemStack i, PlayerDropItemEvent event) {
        event.setCancelled(true);
        return power.fire(player, i);
    }

    public static class Sneak extends Drop {
        public Sneak() {
            super("DROP_SNEAK");
        }
    }
}
