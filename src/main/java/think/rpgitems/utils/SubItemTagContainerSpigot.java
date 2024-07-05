package think.rpgitems.utils;

import com.google.common.base.FinalizablePhantomReference;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import think.rpgitems.RPGItems;

import java.lang.ref.PhantomReference;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class SubItemTagContainerSpigot implements PersistentDataContainer, ISubItemTagContainer {
    private final PersistentDataContainer parent;
    private PersistentDataContainer self;
    private final NamespacedKey key;
    private PhantomReference<ISubItemTagContainer> reference;

    protected SubItemTagContainerSpigot(PersistentDataContainer parent, NamespacedKey key, PersistentDataContainer self) {
        this.parent = parent;
        this.self = self;
        this.key = key;
    }

    @Override
    public <T, Z> void set(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType, @NotNull Z z) {
        self.set(namespacedKey, persistentDataType, z);
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType) {
        return self.has(namespacedKey, persistentDataType);
    }

    @Override
    public <T, Z> Z get(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType) {
        return self.get(namespacedKey, persistentDataType);
    }

    @Override
    public <T, Z> @NotNull Z getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultValue) {
        return self.getOrDefault(key, type, defaultValue);
    }

    @Override
    public @NotNull Set<NamespacedKey> getKeys() {
        //todo check this
        return Collections.singleton(key);
    }

    @Override
    public void remove(@NotNull NamespacedKey namespacedKey) {
        self.remove(namespacedKey);
    }

    @Override
    public boolean isEmpty() {
        return self.isEmpty();
    }

    @Override
    public @NotNull PersistentDataAdapterContext getAdapterContext() {
        return self.getAdapterContext();
    }

    @Override
    public void commit() {
        parent.set(key, PersistentDataType.TAG_CONTAINER, self);
        if (parent instanceof ISubItemTagContainer) {
            ((ISubItemTagContainer) parent).commit();
        }
        dispose();
    }

    @Override
    public void dispose() {
        self = null;
        if (!ISubItemTagContainer.references.remove(reference)) {
            RPGItems.logger.log(Level.SEVERE, "Double handled SubItemTagContainer found: " + this + ": " + key + "@" + parent, new Exception());
        }
    }

    @Override
    public void tryDispose() {
        if (self != null) {
            dispose();
        }
    }

    @Override
    public void setReference(FinalizablePhantomReference<ISubItemTagContainer> reference) {
        this.reference = reference;
    }
}
