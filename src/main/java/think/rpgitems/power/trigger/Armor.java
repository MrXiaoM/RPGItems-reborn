package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;


public class Armor extends Trigger<PlayerEvent, PowerPlain, Void, Void> {
    public Armor() {
        super(PlayerEvent.class, PowerPlain.class, Void.class, Void.class, "ARMOR");
    }

    Armor(String name) {
        super(name, "ARMOR", PlayerEvent.class, PowerPlain.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerPlain power, Player player, ItemStack i, PlayerEvent event) {
        return power.fire(player, item, i);
    }

    public static class Update extends Armor {
        public Update() {
            super("ARMOR_UPDATE");
            register(this);
        }
    }
}
