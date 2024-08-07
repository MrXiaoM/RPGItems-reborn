package think.rpgitems.utils;

import com.google.common.base.FinalizablePhantomReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.collect.Sets;
import org.bukkit.persistence.PersistentDataContainer;

import java.lang.ref.Reference;
import java.util.Set;

public interface ISubItemTagContainer extends PersistentDataContainer {
    FinalizableReferenceQueue frq = new FinalizableReferenceQueue();
    Set<Reference<?>> references = Sets.newConcurrentHashSet();
    void commit();
    void dispose();
    void tryDispose();
    void setReference(FinalizablePhantomReference<ISubItemTagContainer> reference);
}
