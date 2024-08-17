package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import think.rpgitems.item.RPGItem;

@Getter
public class RPGDamageMeleeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Setter
    private boolean cancelled = false;
    private final RPGItem rpg;
    private final ItemStack item;
    private final Entity entity;
    private final double originalDamage;
    @Setter
    private double damage;

    public RPGDamageMeleeEvent(@NotNull Player player, RPGItem rpg, ItemStack item, Entity entity, double originalDamage, double damage) {
        super(player);
        this.rpg = rpg;
        this.item = item;
        this.entity = entity;
        this.originalDamage = originalDamage;
        this.damage = damage;
    }
}
