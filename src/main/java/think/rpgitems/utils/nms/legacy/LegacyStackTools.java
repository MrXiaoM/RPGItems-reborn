package think.rpgitems.utils.nms.legacy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import think.rpgitems.utils.nms.IStackTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LegacyStackTools implements IStackTools {
    @Override
    public byte[] itemToBinary(ItemStack itemStack) {
        return itemsToBase64(List.of(itemStack)).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ItemStack itemFromBinary(byte[] nbt, int offset, int len) {
        return itemFromBase64(new String(nbt, StandardCharsets.UTF_8));
    }

    @Override
    public String itemsToBase64(List<ItemStack> items) {
        return itemStackArrayToBase64(items);
    }

    @Override
    public List<ItemStack> itemsFromBase64(String base64) {
        return itemStackArrayFromBase64(base64);
    }

    @Override
    public String itemToJson(ItemStack itemStack) throws RuntimeException {
        return "{\"id\":\"minecraft:" + itemStack.getType().name().toLowerCase() + "\"," +
                "Count:" + itemStack.getAmount() + ", " +
                "\"base64\":\"" + itemToBase64(itemStack) + "\"}";
    }

    @Override
    public Object asNMSCopy(ItemStack itemStack) {
        return null;
    }

    public static String itemStackArrayToBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(dataOut);
            out.writeInt(items.size());

            for (ItemStack item : items) {
                out.writeObject(item);
            }

            out.close();
            return Base64.getEncoder().encodeToString(dataOut.toByteArray());
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static List<ItemStack> itemStackArrayFromBase64(String base64) {
        if (base64.trim().isEmpty()) return new ArrayList<>();
        try {
            ByteArrayInputStream dataIn = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream in = new BukkitObjectInputStream(dataIn);
            List<ItemStack> items = new ArrayList<>();

            int length = in.readInt();
            for (int i = 0; i < length; ++i) {
                items.set(i, (ItemStack) in.readObject());
            }

            in.close();
            return items;
        } catch (Throwable t) {
            return new ArrayList<>();
        }
    }
}
