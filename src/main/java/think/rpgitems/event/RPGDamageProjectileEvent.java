package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import think.rpgitems.item.RPGItem;

@Getter @Setter
public class RPGDamageProjectileEvent extends PlayerEvent implements Cancellable {
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
    private final Projectile projectile;
    private final Entity entity;
    private final double originalDamage;
    private double damage;

    public RPGDamageProjectileEvent(Player player, RPGItem rpg, ItemStack item, Projectile projectile, Entity entity, double originalDamage, double damage) {
        super(player);
        this.rpg = rpg;
        this.item = item;
        this.projectile = projectile;
        this.entity = entity;
        this.originalDamage = originalDamage;
        this.damage = damage;
    }
}
