package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

import java.util.Map;
import java.util.TreeMap;

@Getter @Setter
public class RPGPowerTriggerPreEvent<TEvent extends Event, TPower extends Pimpl, TResult, TReturn> extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled = false;
    private final RPGItem rpg;
    private final ItemStack item;
    private final Event event;
    private final Trigger<TEvent, TPower, TResult, TReturn> trigger;
    private final Object context;
    private final Map<String, PowerResult<TResult>> skippingPowersById = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public RPGPowerTriggerPreEvent(@NotNull Player player, RPGItem rpg, ItemStack item, Event event, Trigger<TEvent, TPower, TResult, TReturn> trigger, Object context) {
        super(player);
        this.rpg = rpg;
        this.item = item;
        this.event = event;
        this.trigger = trigger;
        this.context = context;
    }
}
