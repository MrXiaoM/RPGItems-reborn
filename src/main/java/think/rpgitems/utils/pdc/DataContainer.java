package think.rpgitems.utils.pdc;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * PersistentDataContainer from Bukkit
 */
public interface DataContainer {

    <T, Z> void set(@NotNull NamespacedKey key, @NotNull DataType<T, Z> type, @NotNull Z value);

    <T, Z> boolean has(@NotNull NamespacedKey key, @NotNull DataType<T, Z> type);

    @Nullable
    <T, Z> Z get(@NotNull NamespacedKey key, @NotNull DataType<T, Z> type);

    @NotNull
    <T, Z> Z getOrDefault(@NotNull NamespacedKey key, @NotNull DataType<T, Z> type, @NotNull Z defaultValue);

    @NotNull
    Set<NamespacedKey> getKeys();

    void remove(@NotNull NamespacedKey key);

    boolean isEmpty();

    @NotNull
    DataAdapterContext getAdapterContext();
}
