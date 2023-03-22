package think.rpgitems.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class LoreUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    public List<String> oldLore;
    public List<String> newLore;
    public final List<String> meta = new ArrayList<>();
    public LoreUpdateEvent(List<String> oldLore, List<String> newLore) {
        this.oldLore = oldLore;
        this.newLore = newLore;
    }
}
