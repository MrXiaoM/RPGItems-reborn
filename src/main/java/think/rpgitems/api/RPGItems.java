package think.rpgitems.api;

import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.Objects;

import static think.rpgitems.RPGItems.plugin;
import static think.rpgitems.item.ItemManager.parseItemInfo;

public class RPGItems {

    /**
     * If the itemstack is a RPGItem this will return the RPGItem version of the item. If the itemstack isn't a RPGItem this will return null.
     *
     * @param itemstack The item to converted
     * @return The RPGItem or null
     */
    public static RPGItem toRPGItem(ItemStack itemstack) {
        return ItemManager.toRPGItemByMeta(itemstack).orElse(null);
    }

    public static boolean isEquals(ItemStack a, ItemStack b) {
        return Objects.equals(parseItemInfo(a), parseItemInfo(b));
    }

    public static void openGui(IGui gui) {
        plugin.gui.openGui(gui);
    }
}
