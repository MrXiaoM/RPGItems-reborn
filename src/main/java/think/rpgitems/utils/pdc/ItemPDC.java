package think.rpgitems.utils.pdc;

import com.google.common.base.FinalizablePhantomReference;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
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

public final class ItemPDC { // TODO: 通过 NBT-API 实现 ItemPDC，而非使用 Bukkit 内置实现

    public static final DataType<byte[], UUID> BA_UUID = new UUIDDataType();
    public static final DataType<Byte, Boolean> BYTE_BOOLEAN = new BooleanDataType();
    public static final DataType<byte[], OfflinePlayer> BA_OFFLINE_PLAYER = new OfflinePlayerDataType();
    public static final DataType<String, ItemStack> STRING_ITEMSTACK = new ItemStackDataType();

    private ItemPDC() {
        throw new IllegalStateException();
    }

    public static <T, Z> Z computeIfAbsent(DataContainer container, NamespacedKey key, DataType<T, Z> type, Supplier<? extends Z> mappingFunction) {
        return computeIfAbsent(container, key, type, (ignored) -> mappingFunction.get());
    }

    public static <T, Z> Z computeIfAbsent(DataContainer container, NamespacedKey key, DataType<T, Z> type, Function<NamespacedKey, ? extends Z> mappingFunction) {
        Z value = container.get(key, type);
        if (value == null) {
            value = mappingFunction.apply(key);
        }
        container.set(key, type, value);
        return value;
    }


    public static Boolean getBoolean(DataContainer container, NamespacedKey key) {
        return container.get(key, BYTE_BOOLEAN);
    }

    public static Optional<Boolean> optBoolean(DataContainer container, NamespacedKey key) {
        if (!container.has(key, BYTE_BOOLEAN)) return Optional.empty();
        return Optional.ofNullable(container.get(key, BYTE_BOOLEAN));
    }

    public static Integer getInt(DataContainer container, NamespacedKey key) {
        return container.get(key, DataType.INTEGER);
    }

    public static OptionalInt optInt(DataContainer container, NamespacedKey key) {
        Integer i = container.has(key, DataType.INTEGER)
                ? container.get(key, DataType.INTEGER)
                : null;
        return i != null ? OptionalInt.of(i) : OptionalInt.empty();
    }

    public static String getString(DataContainer container, NamespacedKey key) {
        return container.get(key, DataType.STRING);
    }

    public static String getString(DataContainer container, String key) {
        return container.get(PowerManager.parseKey(key), DataType.STRING);
    }

    public static Optional<UUID> optUUID(DataContainer container, NamespacedKey key) {
        return Optional.ofNullable(container.get(key, BA_UUID));
    }

    public static DataContainer getTag(DataContainer container, NamespacedKey key) {
        return container.get(key, DataType.TAG_CONTAINER);
    }

    public static OfflinePlayer getPlayer(DataContainer container, NamespacedKey key) {
        return container.get(key, BA_OFFLINE_PLAYER);
    }

    public static ItemStack getItemStack(DataContainer container, NamespacedKey key) {
        return container.get(key, STRING_ITEMSTACK);
    }

    public static ItemStack getItemStack(DataContainer container, String key) {
        return getItemStack(container, PowerManager.parseKey(key));
    }

    public static void set(DataContainer container, NamespacedKey key, boolean value) {
        container.set(key, BYTE_BOOLEAN, value);
    }

    public static void set(DataContainer container, NamespacedKey key, byte value) {
        container.set(key, DataType.BYTE, value);
    }

    public static void set(DataContainer container, NamespacedKey key, short value) {
        container.set(key, DataType.SHORT, value);
    }

    public static void set(DataContainer container, NamespacedKey key, int value) {
        container.set(key, DataType.INTEGER, value);
    }

    public static void set(DataContainer container, NamespacedKey key, long value) {
        container.set(key, DataType.LONG, value);
    }

    public static void set(DataContainer container, NamespacedKey key, float value) {
        container.set(key, DataType.FLOAT, value);
    }

    public static void set(DataContainer container, NamespacedKey key, double value) {
        container.set(key, DataType.DOUBLE, value);
    }

    public static void set(DataContainer container, NamespacedKey key, String value) {
        container.set(key, DataType.STRING, value);
    }

    public static void set(DataContainer container, String key, String value) {
        container.set(PowerManager.parseKey(key), DataType.STRING, value);
    }

    public static void set(DataContainer container, NamespacedKey key, byte[] value) {
        container.set(key, DataType.BYTE_ARRAY, value);
    }

    public static void set(DataContainer container, NamespacedKey key, int[] value) {
        container.set(key, DataType.INTEGER_ARRAY, value);
    }

    public static void set(DataContainer container, NamespacedKey key, long[] value) {
        container.set(key, DataType.LONG_ARRAY, value);
    }

    public static void set(DataContainer container, NamespacedKey key, DataContainer value) {
        container.set(key, DataType.TAG_CONTAINER, value);
    }

    public static void set(DataContainer container, NamespacedKey key, UUID value) {
        container.set(key, BA_UUID, value);
    }

    public static void set(DataContainer container, NamespacedKey key, OfflinePlayer value) {
        container.set(key, BA_OFFLINE_PLAYER, value);
    }

    public static ISubItemTagContainer makeTag(DataContainer container, NamespacedKey key) {
        DataContainer self = computeIfAbsent(container, key, DataType.TAG_CONTAINER, (k) -> container.getAdapterContext().newDataContainer());

        ISubItemTagContainer subItemTagContainer = new NBTTagContainer(container, key, self);
        WeakReference<DataContainer> weakParent = new WeakReference<>(container);
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

    public static ISubItemTagContainer makeTag(ItemStack item, NamespacedKey key) {
        DataContainer container = getTag(item);
        return makeTag(container, key);
    }

    public static ISubItemTagContainer makeTag(Entity entity, NamespacedKey key) {
        DataContainer container = getTag(entity);
        return makeTag(container, key);
    }

    public static DataContainer getTag(ItemStack item) {
        throw new NotImplementedException("TODO");
    }

    public static DataContainer getTag(Entity entity) {
        throw new NotImplementedException("TODO");
    }

    public static class UUIDDataType implements DataType<byte[], UUID> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull DataAdapterContext context) {
            return Utils.decodeUUID(complex);
        }

        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull DataAdapterContext context) {
            return Utils.encodeUUID(primitive);
        }
    }

    public static class BooleanDataType implements DataType<Byte, Boolean> {
        @Override
        public @NotNull Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public @NotNull Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public @NotNull Byte toPrimitive(@Nullable Boolean complex, @NotNull DataAdapterContext context) {
            return (byte) (complex == null ? 0b10101010 : complex ? 0b00000001 : 0b00000000);
        }

        @Override
        public @Nullable Boolean fromPrimitive(Byte primitive, @NotNull DataAdapterContext context) {
            switch (primitive) {
                case (byte) 0b10101010: return null;
                case (byte) 0b00000001: return true;
                case (byte) 0b00000000: return false;
                default: throw new IllegalArgumentException();
            }
        }
    }

    public static class OfflinePlayerDataType implements DataType<byte[], OfflinePlayer> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<OfflinePlayer> getComplexType() {
            return OfflinePlayer.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(OfflinePlayer complex, @NotNull DataAdapterContext context) {
            return Utils.decodeUUID(complex.getUniqueId());
        }

        @Override
        public @NotNull OfflinePlayer fromPrimitive(byte @NotNull [] primitive, @NotNull DataAdapterContext context) {
            return Bukkit.getOfflinePlayer(Utils.encodeUUID(primitive));
        }
    }

    public static class ItemStackDataType implements DataType<String, ItemStack> {
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
        public @NotNull String toPrimitive(@NotNull ItemStack complex, @NotNull DataAdapterContext context) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkit = new BukkitObjectOutputStream(out)) {
                bukkit.writeObject(complex);
                return BaseEncoding.base64().encode(compress(out.toByteArray()));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public @NotNull ItemStack fromPrimitive(@NotNull String primitive, @NotNull DataAdapterContext context) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(decompress(BaseEncoding.base64().decode(primitive)));
                 BukkitObjectInputStream bukkit = new BukkitObjectInputStream(in)) {
                return (ItemStack) bukkit.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
