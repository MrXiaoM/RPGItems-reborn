package think.rpgitems.utils;

import com.google.common.base.FinalizablePhantomReference;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.RPGItems;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@SuppressWarnings({"unused"})
public final class ItemPDC { // TODO: 通过 NBT-API 实现 ItemPDC，而非使用 Bukkit 内置实现

    public static final PersistentDataType<byte[], UUID> BA_UUID = new UUIDPersistentDataType();
    public static final PersistentDataType<Byte, Boolean> BYTE_BOOLEAN = new BooleanPersistentDataType();
    public static final PersistentDataType<byte[], OfflinePlayer> BA_OFFLINE_PLAYER = new OfflinePlayerPersistentDataType();
    public static final PersistentDataType<String, ItemStack> STRING_ITEMSTACK = new ItemStackPersistentDataType();

    private ItemPDC() {
        throw new IllegalStateException();
    }

    public static <T, Z> Z computeIfAbsent(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Supplier<? extends Z> mappingFunction) {
        return computeIfAbsent(container, key, type, (ignored) -> mappingFunction.get());
    }

    public static <T, Z> Z computeIfAbsent(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Function<NamespacedKey, ? extends Z> mappingFunction) {
        Z value = container.get(key, type);
        if (value == null) {
            value = mappingFunction.apply(key);
        }
        container.set(key, type, value);
        return value;
    }

    public static <T, Z> Z putIfAbsent(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Supplier<? extends Z> mappingFunction) {
        return computeIfAbsent(container, key, type, (ignored) -> mappingFunction.get());
    }

    public static <T, Z> Z putIfAbsent(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Function<NamespacedKey, ? extends Z> mappingFunction) {
        Z old = container.get(key, type);
        if (old == null) {
            container.set(key, type, mappingFunction.apply(key));
            return null;
        }
        return old;
    }

    public static <T, Z> Z putValueIfAbsent(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        return putIfAbsent(container, key, type, (ignored) -> value);
    }

    public static Boolean getBoolean(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, BYTE_BOOLEAN);
    }

    public static Optional<Boolean> optBoolean(PersistentDataContainer container, NamespacedKey key) {
        if (!container.has(key, BYTE_BOOLEAN)) return Optional.empty();
        return Optional.ofNullable(container.get(key, BYTE_BOOLEAN));
    }

    public static Byte getByte(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.BYTE);
    }

    public static Short getShort(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.SHORT);
    }

    public static Integer getInt(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.INTEGER);
    }

    public static OptionalInt optInt(PersistentDataContainer container, NamespacedKey key) {
        Integer i = container.has(key, PersistentDataType.INTEGER)
                ? container.get(key, PersistentDataType.INTEGER)
                : null;
        return i != null ? OptionalInt.of(i) : OptionalInt.empty();
    }

    public static Long getLong(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.LONG);
    }

    public static Float getFloat(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.FLOAT);
    }

    public static Double getDouble(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.DOUBLE);
    }

    public static String getString(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.STRING);
    }

    public static String getString(PersistentDataContainer container, String key) {
        return container.get(PowerManager.parseKey(key), PersistentDataType.STRING);
    }

    public static byte[] getByteArray(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.BYTE_ARRAY);
    }

    public static int[] getIntArray(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.INTEGER_ARRAY);
    }

    public static long[] getLongArray(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.LONG_ARRAY);
    }

    public static UUID getUUID(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, BA_UUID);
    }

    public static Optional<UUID> optUUID(PersistentDataContainer container, NamespacedKey key) {
        return Optional.ofNullable(container.get(key, BA_UUID));
    }

    public static PersistentDataContainer getTag(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, PersistentDataType.TAG_CONTAINER);
    }

    public static OfflinePlayer getPlayer(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, BA_OFFLINE_PLAYER);
    }

    public static ItemStack getItemStack(PersistentDataContainer container, NamespacedKey key) {
        return container.get(key, STRING_ITEMSTACK);
    }

    public static ItemStack getItemStack(PersistentDataContainer container, String key) {
        return getItemStack(container, PowerManager.parseKey(key));
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, boolean value) {
        container.set(key, BYTE_BOOLEAN, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, byte value) {
        container.set(key, PersistentDataType.BYTE, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, short value) {
        container.set(key, PersistentDataType.SHORT, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, int value) {
        container.set(key, PersistentDataType.INTEGER, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, long value) {
        container.set(key, PersistentDataType.LONG, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, float value) {
        container.set(key, PersistentDataType.FLOAT, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, double value) {
        container.set(key, PersistentDataType.DOUBLE, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, String value) {
        container.set(key, PersistentDataType.STRING, value);
    }

    public static void set(PersistentDataContainer container, String key, String value) {
        container.set(PowerManager.parseKey(key), PersistentDataType.STRING, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, byte[] value) {
        container.set(key, PersistentDataType.BYTE_ARRAY, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, int[] value) {
        container.set(key, PersistentDataType.INTEGER_ARRAY, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, long[] value) {
        container.set(key, PersistentDataType.LONG_ARRAY, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, PersistentDataContainer value) {
        container.set(key, PersistentDataType.TAG_CONTAINER, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, UUID value) {
        container.set(key, BA_UUID, value);
    }

    public static void set(PersistentDataContainer container, NamespacedKey key, OfflinePlayer value) {
        container.set(key, BA_OFFLINE_PLAYER, value);
    }

    public static ISubItemTagContainer makeTag(PersistentDataContainer container, NamespacedKey key) {
        PersistentDataContainer self = computeIfAbsent(container, key, PersistentDataType.TAG_CONTAINER, (k) -> container.getAdapterContext().newPersistentDataContainer());

        ISubItemTagContainer subItemTagContainer = RPGItems.isPaper()
                ? new SubItemTagContainerPaper(container, key, self)
                : new SubItemTagContainerSpigot(container, key, self);
        WeakReference<PersistentDataContainer> weakParent = new WeakReference<>(container);
        FinalizablePhantomReference<ISubItemTagContainer> reference = new FinalizablePhantomReference<>(subItemTagContainer, ISubItemTagContainer.frq) {
            public void finalizeReferent() {
                if (ISubItemTagContainer.references.remove(this)) {
                    RPGItems.logger.severe("Unhandled SubItemTagContainer found: " + key + "@" + weakParent.get());
                }
            }
        };
        subItemTagContainer.setReference(reference);
        ISubItemTagContainer.references.add(reference);
        return subItemTagContainer;
    }

    public static ISubItemTagContainer makeTag(ItemMeta itemMeta, NamespacedKey key) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return makeTag(container, key);
    }

    public static class UUIDPersistentDataType implements PersistentDataType<byte[], UUID> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
            return Utils.decodeUUID(complex);
        }

        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            return Utils.encodeUUID(primitive);
        }
    }

    public static class BooleanPersistentDataType implements PersistentDataType<Byte, Boolean> {
        @Override
        public @NotNull Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public @NotNull Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public @NotNull Byte toPrimitive(@Nullable Boolean complex, @NotNull PersistentDataAdapterContext context) {
            return (byte) (complex == null ? 0b10101010 : complex ? 0b00000001 : 0b00000000);
        }

        @Override
        public @Nullable Boolean fromPrimitive(Byte primitive, @NotNull PersistentDataAdapterContext context) {
            switch (primitive) {
                case (byte) 0b10101010: return null;
                case (byte) 0b00000001: return true;
                case (byte) 0b00000000: return false;
                default: throw new IllegalArgumentException();
            }
        }
    }

    public static class OfflinePlayerPersistentDataType implements PersistentDataType<byte[], OfflinePlayer> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<OfflinePlayer> getComplexType() {
            return OfflinePlayer.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(OfflinePlayer complex, @NotNull PersistentDataAdapterContext context) {
            return Utils.decodeUUID(complex.getUniqueId());
        }

        @Override
        public @NotNull OfflinePlayer fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            return Bukkit.getOfflinePlayer(Utils.encodeUUID(primitive));
        }
    }

    public static class ItemStackPersistentDataType implements PersistentDataType<String, ItemStack> {
        private static final ThreadLocal<Inflater> NYAA_INFLATER = ThreadLocal.withInitial(Inflater::new);
        private static final ThreadLocal<Deflater> NYAA_DEFLATER = ThreadLocal.withInitial(Deflater::new);

        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<ItemStack> getComplexType() {
            return ItemStack.class;
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
        public @NotNull String toPrimitive(@NotNull ItemStack complex, @NotNull PersistentDataAdapterContext context) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkit = new BukkitObjectOutputStream(out)) {
                bukkit.writeObject(complex);
                return BaseEncoding.base64().encode(compress(out.toByteArray()));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public @NotNull ItemStack fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(decompress(BaseEncoding.base64().decode(primitive)));
                 BukkitObjectInputStream bukkit = new BukkitObjectInputStream(in)) {
                return (ItemStack) bukkit.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
