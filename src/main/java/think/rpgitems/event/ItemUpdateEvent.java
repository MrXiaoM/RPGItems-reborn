package think.rpgitems.event;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.item.RPGItem;

import java.util.List;

@Getter @Setter
public class ItemUpdateEvent extends Event {
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
    public final RPGItem rpg;
    public final ItemStack item;
    /**
     * if true, updateItem() method only update item material and lore. Modify other value is not available.
     */
    public final boolean isLoreAndMaterialOnly;
    private Material material;
    @Nullable
    private Integer customModelData;
    private List<ItemFlag> itemFlags;
    public ItemUpdateEvent(@NotNull RPGItem rpg, @Nullable Player player, @NotNull ItemStack item, boolean isLoreAndMaterialOnly, Material material, @Nullable Integer customModelData, List<ItemFlag> itemFlags) {
        this.rpg = rpg;
        this.player = player;
        this.item = item;
        this.isLoreAndMaterialOnly = isLoreAndMaterialOnly;
        this.material = material;
        this.customModelData = customModelData;
        this.itemFlags = itemFlags;
    }

    public void addItemFlags(ItemFlag... flags) {
        itemFlags.addAll(Lists.newArrayList(flags));
    }
}
