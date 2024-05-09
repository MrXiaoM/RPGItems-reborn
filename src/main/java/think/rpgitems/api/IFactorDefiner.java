package think.rpgitems.api;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IFactorDefiner {
    /**
     * The priority to define factor. 50 default. The smaller value of priority, the earlier to execute the definer.
     */
    default int priority() {
        return 50;
    }

    /**
     * Define the factor id of entity. Return null means the definer has no result and then run the next definer.
     */
    @Nullable
    String define(LivingEntity entity);
}
