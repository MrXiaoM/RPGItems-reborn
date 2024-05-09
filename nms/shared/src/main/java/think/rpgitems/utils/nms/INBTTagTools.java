package think.rpgitems.utils.nms;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface INBTTagTools {
    Optional<String> getString(ItemStack item, String key);
    Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Integer> getInt(ItemStack item, String key);
    Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Double> getDouble(ItemStack item, String key);
    Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Short> getShort(ItemStack item, String key);
    Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Byte> getByte(ItemStack item, String key);
    Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Long> getLong(ItemStack item, String key);
    Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException;
    Optional<long[]> getLongArray(ItemStack item, String key);
    Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException;
    Optional<int[]> getIntArray(ItemStack item, String key);
    Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException;
    Optional<byte[]> getByteArray(ItemStack item, String key);
    Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Boolean> getBoolean(ItemStack item, String key);
    Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException;
    Optional<Float> getFloat(ItemStack item, String key);
    Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException;
    void remove(ItemStack item, String key) throws NoSuchFieldException, IllegalAccessException;
}
