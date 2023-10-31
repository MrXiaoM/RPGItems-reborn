package think.rpgitems.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ItemsLoadedEvent extends Event {
    public static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public ItemsLoadedEvent() {

    }
}
