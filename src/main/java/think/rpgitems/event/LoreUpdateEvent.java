package think.rpgitems.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

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
    public final RPGItem rpg;
    public final ItemStack item;
    public LoreUpdateEvent(RPGItem rpg, ItemStack item, List<String> oldLore, List<String> newLore) {
        this.rpg = rpg;
        this.item = item;
        this.oldLore = oldLore;
        this.newLore = newLore;
    }
    public static class Post extends Event {
        private static final HandlerList handlers = new HandlerList();
        public HandlerList getHandlers() {
            return handlers;
        }
        public static HandlerList getHandlerList() {
            return handlers;
        }
        public final LoreUpdateEvent old;
        public final RPGItem rpg;
        public final ItemStack item;
        public Post(LoreUpdateEvent old, RPGItem rpg, ItemStack item) {
            this.old = old;
            this.rpg = rpg;
            this.item = item;
        }
    }
}
