package think.rpgitems.utils.nms.v1_20_R1;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.utils.nms.INBTTagTools;

import java.lang.reflect.Field;
import java.util.Optional;

public class NBTTagTools_v1_20_R1 implements INBTTagTools {

    @Override
    public Optional<String> getString(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.l(key));
    }

    @Override
    public Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Integer> getInt(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.h(key));
    }

    @Override
    public Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Double> getDouble(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.k(key));
    }

    @Override
    public Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Short> getShort(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.g(key));
    }

    @Override
    public Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Byte> getByte(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.f(key));
    }

    @Override
    public Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Long> getLong(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.i(key));
    }

    @Override
    public Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<long[]> getLongArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.o(key));
    }

    @Override
    public Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value); // this is anonymous
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<int[]> getIntArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.n(key));
    }

    @Override
    public Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<byte[]> getByteArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.m(key));
    }

    @Override
    public Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Boolean> getBoolean(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.q(key));
    }

    @Override
    public Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public Optional<Float> getFloat(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        NBTTagCompound tag = item1.get().v();
        if (tag == null) return Optional.empty();
        return !tag.e(key) ? Optional.empty() : Optional.of(tag.j(key));
    }

    @Override
    public Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.a(key, value);
        itemStack.c(tag);
        return Optional.of(value);
    }

    @Override
    public void remove(ItemStack item, String key) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.w();
        tag.r(key);
        itemStack.c(tag);
    }

    static Field handle;
    private static Optional<net.minecraft.world.item.ItemStack> getItem(ItemStack itemStack) throws NoSuchFieldException, IllegalAccessException {
        if (!(itemStack instanceof CraftItemStack)) {
            return Optional.empty();
        }
        try {
            if (handle == null) {
                handle = CraftItemStack.class.getDeclaredField("handle");
                handle.setAccessible(true);
            }
            return Optional.ofNullable((net.minecraft.world.item.ItemStack) handle.get(itemStack));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return Optional.empty();
    }
}
