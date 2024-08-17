package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.item.RPGItem;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class LoreUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Nullable
    public final Player player;
    public List<String> oldLore;
    public List<String> newLore;
    public final List<String> meta = new ArrayList<>();
    public final RPGItem rpg;
    public final ItemStack item;
    public LoreUpdateEvent(@NotNull RPGItem rpg, @Nullable Player player, @NotNull ItemStack item, @NotNull List<String> oldLore, @NotNull List<String> newLore) {
        this.rpg = rpg;
        this.player = player;
        this.item = item;
        this.oldLore = oldLore;
        this.newLore = newLore;
    }
    @Getter
    public static class Post extends Event {
        private static final HandlerList handlers = new HandlerList();
        @Override @NotNull
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
