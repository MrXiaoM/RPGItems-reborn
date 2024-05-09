package think.rpgitems.utils.nyaacore.utils;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Optional;

import static think.rpgitems.utils.nms.NMS.nbtTools;

public class ItemTagUtils {

    static Field handle;

    public static Optional<String> getString(ItemStack item, String key) {
        return nbtTools().getString(item, key);
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setString(item, key, value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        return nbtTools().getInt(item, key);
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setInt(item, key, value);
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        return nbtTools().getDouble(item, key);
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setDouble(item, key, value);
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        return nbtTools().getShort(item, key);
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setShort(item, key, value);
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        return nbtTools().getByte(item, key);
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setByte(item, key, value);
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        return nbtTools().getLong(item, key);
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setLong(item, key, value);
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        return nbtTools().getLongArray(item, key);
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setLongArray(item, key, value);
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        return nbtTools().getIntArray(item, key);
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setIntArray(item, key, value);
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        return nbtTools().getByteArray(item, key);
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setByteArray(item, key, value);
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        return nbtTools().getBoolean(item, key);
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setBoolean(item, key, value);
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        return nbtTools().getFloat(item, key);
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        return nbtTools().setFloat(item, key, value);
    }

    public static void remove(ItemStack item, String key) throws NoSuchFieldException, IllegalAccessException {
        nbtTools().remove(item, key);
    }
}
