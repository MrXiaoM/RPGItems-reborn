package think.rpgitems.power.trigger;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;


public class Armor extends Trigger<PlayerArmorChangeEvent, PowerPlain, Void, Void> {
    public Armor() {
        this("ARMOR");
    }

    Armor(String name) {
        super(name, PlayerArmorChangeEvent.class, PowerPlain.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(PowerPlain power, Player player, ItemStack i, PlayerArmorChangeEvent event) {
        return power.fire(player, i);
    }

    public static class Update extends Armor {
        public Update() {
            super("ARMOR_UPDATE");
        }
    }
}
