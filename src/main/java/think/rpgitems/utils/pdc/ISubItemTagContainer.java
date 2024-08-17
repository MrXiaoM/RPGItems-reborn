package think.rpgitems.utils.pdc;

import com.google.common.base.FinalizablePhantomReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.collect.Sets;

import java.lang.ref.Reference;
import java.util.Set;

public interface ISubItemTagContainer extends DataContainer {
    FinalizableReferenceQueue frq = new FinalizableReferenceQueue();
    Set<Reference<?>> references = Sets.newConcurrentHashSet();
    void commit();
    void dispose();
    void tryDispose();
    void setReference(FinalizablePhantomReference<ISubItemTagContainer> reference);
}
