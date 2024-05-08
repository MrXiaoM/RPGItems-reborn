package cat.nyaa.nyaacore.utils;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Optional;

public class ItemTagUtils {

    static Field handle;

    public static Optional<String> getString(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.l(key));
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.h(key));
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.k(key));
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.g(key));
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.f(key));
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.i(key));
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.o(key));
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value); // this is anonymous
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.n(key));
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.m(key));
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.q(key));
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().u();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.j(key));
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    public static void remove(ItemStack item, String key) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.v();
        tag.r(key);
        itemStack.c(tag);
    }

    private static Field field = null;
    private static Optional<net.minecraft.world.item.ItemStack> getItem(ItemStack itemStack) throws NoSuchFieldException, IllegalAccessException {
        if (!(itemStack instanceof CraftItemStack)) {
            return Optional.empty();
        }
        try {
            if (field == null) {
                field = CraftItemStack.class.getDeclaredField("handle");
                field.setAccessible(true);
            }
            return Optional.ofNullable((net.minecraft.world.item.ItemStack) field.get(itemStack));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return Optional.empty();
    }
}
