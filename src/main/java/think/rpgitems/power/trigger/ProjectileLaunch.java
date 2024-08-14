package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.PowerProjectileLaunch;
import think.rpgitems.power.PowerResult;

class ProjectileLaunch extends Trigger<ProjectileLaunchEvent, PowerProjectileLaunch, Void, Void> {
    ProjectileLaunch() {
        super(ProjectileLaunchEvent.class, PowerProjectileLaunch.class, Void.class, Void.class, "PROJECTILE_LAUNCH");
    }

    public ProjectileLaunch(String name) {
        super(name, "PROJECTILE_LAUNCH", ProjectileLaunchEvent.class, PowerProjectileLaunch.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerProjectileLaunch power, Player player, ItemStack i, ProjectileLaunchEvent event) {
        return power.projectileLaunch(player, item, i, event);
    }
}
