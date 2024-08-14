package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;


public class Drop extends Trigger<PlayerDropItemEvent, PowerPlain, Void, Void> {
    public Drop() {
        super(PlayerDropItemEvent.class, PowerPlain.class, Void.class, Void.class, "DROP");
    }

    public Drop(String name) {
        super(name, "DROP", PlayerDropItemEvent.class, PowerPlain.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerPlain power, Player player, ItemStack i, PlayerDropItemEvent event) {
        event.setCancelled(true);
        return power.fire(player, item, i);
    }

    public static class Sneak extends Drop {
        public Sneak() {
            super("DROP_SNEAK");
            register(this);
        }
    }
}
