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

public class ItemUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Nullable
    public final Player player;
    @NotNull
    public final RPGItem rpg;
    @NotNull
    public final ItemStack item;
    /**
     * if true, updateItem() method only update item material and lore. Modify other value is not available.
     */
    public final boolean isLoreAndMaterialOnly;
    @Getter
    @Setter
    private Material material;
    @Setter
    @Getter
    @Nullable
    private Integer customModelData;
    @Getter
    @Setter
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
