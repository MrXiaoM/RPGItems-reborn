package think.rpgitems.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class LoreUpdateEvent extends Event {
    public static final HandlerList handlerList = new HandlerList();
    public List<String> oldLore;
    public List<String> newLore;

    public LoreUpdateEvent(List<String> oldLore, List<String> newLore) {
        this.oldLore = oldLore;
        this.newLore = newLore;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
