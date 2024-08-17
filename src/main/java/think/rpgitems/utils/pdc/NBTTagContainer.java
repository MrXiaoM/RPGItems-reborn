package think.rpgitems.utils.pdc;

import com.google.common.base.FinalizablePhantomReference;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.PhantomReference;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NBTTagContainer implements ISubItemTagContainer {
    private final DataContainer parent;
    private DataContainer self;
    private final NamespacedKey key;
    private PhantomReference<ISubItemTagContainer> reference;
    public NBTTagContainer(DataContainer parent, NamespacedKey key, DataContainer self) {
        this.parent = parent;
        this.self = self;
        this.key = key;
    }

    @Override
    public <T, Z> void set(@NotNull NamespacedKey namespacedKey, @NotNull DataType<T, Z> DataType, @NotNull Z z) {
        self.set(namespacedKey, DataType, z);
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey namespacedKey, @NotNull DataType<T, Z> DataType) {
        return self.has(namespacedKey, DataType);
    }

    @Override
    public <T, Z> Z get(@NotNull NamespacedKey namespacedKey, @NotNull DataType<T, Z> DataType) {
        return self.get(namespacedKey, DataType);
    }

    @Override
    public <T, Z> @NotNull Z getOrDefault(@NotNull NamespacedKey key, @NotNull DataType<T, Z> type, @NotNull Z defaultValue) {
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
    public @NotNull DataAdapterContext getAdapterContext() {
        return self.getAdapterContext();
    }

    @Override
    public void commit() {
        parent.set(key, DataType.TAG_CONTAINER, self);
        if (parent instanceof ISubItemTagContainer) {
            ((ISubItemTagContainer) parent).commit();
        }
        dispose();
    }

    @Override
    public void dispose() {
        self = null;
        if (!references.remove(reference)) {
            Logger.getLogger("RPGItems").log(Level.SEVERE,
                    "Double handled SubItemTagContainer found: " + this + ": " + key + "@" + parent,
                    new Exception()
            );
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
