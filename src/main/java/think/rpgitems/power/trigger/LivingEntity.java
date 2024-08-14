package think.rpgitems.power.trigger;

import think.rpgitems.item.RPGItem;
import think.rpgitems.utils.nyaacore.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerLivingEntity;
import think.rpgitems.power.PowerResult;

class LivingEntity extends Trigger<Event, PowerLivingEntity, Void, Void> {
    LivingEntity() {
        super(Event.class, PowerLivingEntity.class, Void.class, Void.class, "LIVINGENTITY");
    }
    public LivingEntity(String name) {
        super(name, "LIVINGENTITY", Event.class, PowerLivingEntity.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerLivingEntity power, Player player, ItemStack i, Event event) {
        throw new IllegalStateException();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public PowerResult<Void> run(RPGItem item, PowerLivingEntity power, Player player, ItemStack i, Event event, Object data) {
        Pair<org.bukkit.entity.LivingEntity, Double> pair = (Pair<org.bukkit.entity.LivingEntity, Double>) data;
        return power.fire(player, item, i, pair.getKey(), pair.getValue());
    }
}
