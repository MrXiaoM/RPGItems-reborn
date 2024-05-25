package think.rpgitems.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    public final Player player;
    @NotNull
    public List<String> oldLore;
    @NotNull
    public List<String> newLore;
    @NotNull
    public final List<String> meta = new ArrayList<>();
    @NotNull
    public final RPGItem rpg;
    @NotNull
    public final ItemStack item;
    public LoreUpdateEvent(@NotNull RPGItem rpg, @Nullable Player player, @NotNull ItemStack item, @NotNull List<String> oldLore, @NotNull List<String> newLore) {
        this.rpg = rpg;
        this.player = player;
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
