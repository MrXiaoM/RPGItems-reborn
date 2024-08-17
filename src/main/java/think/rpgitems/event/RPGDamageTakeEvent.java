package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.item.RPGItem;

@Getter
public class RPGDamageTakeEvent extends Event implements Cancellable {
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
    private final Player entity;
    @Nullable
    private final Entity damager;
    private final double originalDamage;
    private final boolean projectile;
    @Setter
    private double armour;

    public RPGDamageTakeEvent(@NotNull Player entity, RPGItem rpg, ItemStack item, @Nullable Entity damager, double originalDamage, boolean projectile, double armour) {
        this.entity = entity;
        this.rpg = rpg;
        this.item = item;
        this.damager = damager;
        this.originalDamage = originalDamage;
        this.projectile = projectile;
        this.armour = armour;
    }
}
