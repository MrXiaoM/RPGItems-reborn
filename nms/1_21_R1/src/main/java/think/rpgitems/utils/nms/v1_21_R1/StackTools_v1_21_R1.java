package think.rpgitems.utils.nms.v1_21_R1;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import net.minecraft.nbt.NBTReadLimiter;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.utils.nms.IStackTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class StackTools_v1_21_R1 implements IStackTools {

    private static final String NYAACORE_ITEMSTACK_DATAVERSION_KEY = "nyaacore_itemstack_dataversion";
    private static final int NYAACORE_ITEMSTACK_DEFAULT_DATAVERSION = 1139;
    private static final ThreadLocal<Inflater> NYAA_INFLATER = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> NYAA_DEFLATER = ThreadLocal.withInitial(Deflater::new);
    private static final int currentDataVersion;
    private static final Cache<String, List<ItemStack>> itemDeserializerCache = CacheBuilder.newBuilder()
            .weigher((String k, List<ItemStack> v) -> k.getBytes().length)
            .maximumWeight(256L * 1024 * 1024).build(); // Hard Coded 256M
    private static NBTReadLimiter unlimitedNbtAccounter = null;

    static {
        //noinspection deprecation
        currentDataVersion = Bukkit.getUnsafe().getDataVersion();
    }

    @Override
    public byte[] itemToBinary(ItemStack itemStack) throws IOException {
        throw new IllegalStateException("TODO: Not yet implemented");
    }

    @Override
    public ItemStack itemFromBinary(byte[] nbt, int offset, int len) throws IOException {
        throw new IllegalStateException("TODO: Not yet implemented");
    }

    private static byte[] compress(byte[] data) {
        byte[] ret;
        Deflater deflater = NYAA_DEFLATER.get();
        deflater.reset();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new DeflaterInputStream(bis, deflater), bos);
            ret = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    private static byte[] decompress(byte[] data) {
        byte[] ret;
        Inflater inflater = NYAA_INFLATER.get();
        inflater.reset();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new InflaterInputStream(bis, inflater), bos);
            ret = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    @Override
    public String itemsToBase64(List<ItemStack> items) {
        if (items.isEmpty()) return "";
        if (items.size() > 127) {
            throw new IllegalArgumentException("Too many items");
        }

        List<byte[]> nbts = new ArrayList<>();
        for (ItemStack item : items) {
            try {
                nbts.add(itemToBinary(item));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        byte[] uncompressed_binary;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeByte(items.size());
            for (byte[] nbt : nbts) dos.writeInt(nbt.length);
            for (byte[] nbt : nbts) dos.write(nbt);
            uncompressed_binary = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return BaseEncoding.base64().encode(compress(uncompressed_binary));
    }

    @Override
    public List<ItemStack> itemsFromBase64(String base64) {
        List<ItemStack> stack = itemDeserializerCache.getIfPresent(base64);
        if (stack != null) return stack.stream().map(ItemStack::clone).collect(Collectors.toList());
        if (base64.isEmpty()) return new ArrayList<>();

        byte[] uncompressedBinary = decompress(BaseEncoding.base64().decode(base64));
        List<ItemStack> ret = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(uncompressedBinary);
             DataInputStream dis = new DataInputStream(bis)) {
            int n = dis.readByte();
            int[] nbtLength = new int[n];
            for (int i = 0; i < n; i++) nbtLength[i] = dis.readInt();
            for (int i = 0; i < n; i++) {
                byte[] tmp = new byte[nbtLength[i]];
                dis.readFully(tmp);
                ret.add(itemFromBinary(tmp));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        itemDeserializerCache.put(base64, ret.stream().map(ItemStack::clone).collect(Collectors.toList()));
        return ret;
    }

    @Override
    public String itemToJson(ItemStack itemStack) throws RuntimeException {
        throw new IllegalStateException("TODO: Not yet implemented");
    }

    @Override
    public Object asNMSCopy(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }
}
