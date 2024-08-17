package think.rpgitems.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class BeamHitBlockEvent extends Event {
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
    private final Block hitBlock;
    private final ItemStack itemStack;
    private Location location;
    private int depth;

    public BeamHitBlockEvent(Player player, Entity from, Block hitBlock, Location location, ItemStack itemStack, int depth){
        this.player = player;
        this.from = from;
        this.hitBlock = hitBlock;
        this.location = location;
        this.itemStack = itemStack;
        this.depth = depth;
    }
}
