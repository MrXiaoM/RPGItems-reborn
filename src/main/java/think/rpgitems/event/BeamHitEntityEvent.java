package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class BeamHitEntityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final Entity from;
    private final LivingEntity entity;
    private final Location loc;
    private final BoundingBox boundingBox;
    private final Vector velocity;
    private ItemStack itemStack;
    private double damage;
    private int depth;

    public BeamHitEntityEvent(Player player, Entity from, LivingEntity entity, ItemStack itemStack, double damage, Location loc, BoundingBox boundingBox, Vector vector, int depth){
        this.player = player;
        this.from = from;
        this.entity = entity;
        this.itemStack = itemStack;
        this.damage = damage;
        this.loc = loc;
        this.boundingBox = boundingBox;
        this.velocity = vector;
        this.depth = depth;
    }
}
