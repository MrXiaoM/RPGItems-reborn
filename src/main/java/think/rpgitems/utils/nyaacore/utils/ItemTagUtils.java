package think.rpgitems.utils.nyaacore.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

import static de.tr7zw.changeme.nbtapi.NBTType.*;

public class ItemTagUtils {

    public static boolean checkItem(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() < 1;
    }

    public static Optional<String> getString(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagString)) return null;
            return it.getString(key);
        }));
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setString(key, value);
            return value;
        }));
    }

    public static Optional<List<String>> getStringList(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it -> {
            if (!it.hasTag(key, NBTTagList)) return null;
            return it.getStringList(key).toListCopy();
        }));
    }

    public static Optional<List<String>> setStringList(ItemStack item, String key, List<String> value) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            ReadWriteNBTList<String> list = it.getStringList(key);
            list.clear();
            list.addAll(value);
            return list.toListCopy();
        }));
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagInt)) return null;
            return it.getInteger(key);
        }));
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setInteger(key, value);
            return value;
        }));
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagDouble)) return null;
            return it.getDouble(key);
        }));
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setDouble(key, value);
            return value;
        }));
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagShort)) return null;
            return it.getShort(key);
        }));
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setShort(key, value);
            return value;
        }));
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagByte)) return null;
            return it.getByte(key);
        }));
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setByte(key, value);
            return value;
        }));
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagLong)) return null;
            return it.getLong(key);
        }));
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setLong(key, value);
            return value;
        }));
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagLongArray)) return null;
            return it.getLongArray(key);
        }));
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setLongArray(key, value);
            return value;
        }));
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagIntArray)) return null;
            return it.getIntArray(key);
        }));
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setIntArray(key, value);
            return value;
        }));
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagByteArray)) return null;
            return it.getByteArray(key);
        }));
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setByteArray(key, value);
            return value;
        }));
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagByte)) return null;
            return it.getBoolean(key);
        }));
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setBoolean(key, value);
            return value;
        }));
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.get(item, it ->{
            if (!it.hasTag(key, NBTTagFloat)) return null;
            return it.getFloat(key);
        }));
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return Optional.empty();
        return Optional.ofNullable(NBT.modify(item, it -> {
            it.setFloat(key, value);
            return value;
        }));
    }

    public static void remove(ItemStack item, String key) throws NoSuchFieldException, IllegalAccessException {
        if (checkItem(item)) return;
        NBT.modify(item, it -> {
            it.removeKey(key);
        });
    }
}
