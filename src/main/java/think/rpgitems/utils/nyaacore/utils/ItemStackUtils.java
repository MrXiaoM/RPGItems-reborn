package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

import static think.rpgitems.utils.nms.NMS.stackTools;

public final class ItemStackUtils {
    /**
     * Get the binary representation of ItemStack
     * for fast ItemStack serialization
     *
     * @param itemStack the item to be serialized
     * @return binary NBT representation of the item stack
     */
    public static byte[] itemToBinary(ItemStack itemStack) throws IOException {
        return stackTools().itemToBinary(itemStack);
    }

    /**
     * Get the ItemStack from its binary representation
     * for fast ItemStack deserialization
     *
     * @param nbt binary item nbt data
     * @return constructed item
     */
    public static ItemStack itemFromBinary(byte[] nbt) throws ReflectiveOperationException, IOException {
        return stackTools().itemFromBinary(nbt);
    }

    public static ItemStack itemFromBinary(byte[] nbt, int offset, int len) throws IOException {
        return stackTools().itemFromBinary(nbt, offset, len);
    }

    /* *
     * Structure of binary NBT list:
     * - First byte (n): number of items (currently limit to 0<=n<=127 i.e. MSB=0)
     * - Next 4*n bytes (s1~sn): size of binary nbt for each item
     * - Next sum(s1~sn) bytes: actual data nbt
     */

    /**
     * Convert a list of items into compressed base64 string
     */
    public static String itemsToBase64(List<ItemStack> items) {
        return stackTools().itemsToBase64(items);
    }

    /**
     * Convert base64 string back to a list of items
     */
    public static List<ItemStack> itemsFromBase64(String base64) {
        return stackTools().itemsFromBase64(base64);
    }

    public static String itemToBase64(ItemStack item) {
        return stackTools().itemToBase64(item);
    }

    public static ItemStack itemFromBase64(String base64) {
        return stackTools().itemFromBase64(base64);
    }

    /**
     * <a href="https://github.com/sainttx/Auctions/blob/12533c9af0b1dba700473bf728895abb9ff5b33b/Auctions/src/main/java/com/sainttx/auctions/SimpleMessageFactory.java#L197">...</a>
     * Convert an item to its JSON representation to be shown in chat.
     * NOTE: this method has no corresponding deserializer.
     */
    public static String itemToJson(ItemStack itemStack) throws RuntimeException {
        return stackTools().itemToJson(itemStack);
    }

    @Deprecated
    public static Object asNMSCopy(ItemStack itemStack) {
        return stackTools().asNMSCopy(itemStack);
    }
}
