package think.rpgitems.utils.pdc;

import org.jetbrains.annotations.NotNull;

/**
 * PersistentDataAdapterContext from Bukkit
 */
public interface DataAdapterContext {

    /**
     * Creates a new and empty meta container instance.
     *
     * @return the fresh container instance
     */
    @NotNull
    DataContainer newDataContainer();
}
