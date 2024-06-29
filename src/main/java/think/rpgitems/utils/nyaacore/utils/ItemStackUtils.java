package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.inventory.ItemStack;

import static think.rpgitems.utils.nms.NMS.stackTools;

public final class ItemStackUtils {
    /**
     * <a href="https://github.com/sainttx/Auctions/blob/12533c9af0b1dba700473bf728895abb9ff5b33b/Auctions/src/main/java/com/sainttx/auctions/SimpleMessageFactory.java#L197">...</a>
     * Convert an item to its JSON representation to be shown in chat.
     * NOTE: this method has no corresponding deserializer.
     */
    public static String itemToJson(ItemStack itemStack) throws RuntimeException {
        return stackTools().itemToJson(itemStack);
    }
}
