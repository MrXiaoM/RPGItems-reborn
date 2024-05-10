package think.rpgitems.support;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.utils.MythicUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MythicSupport {
    private static final NamespacedKey type = new NamespacedKey("MythicMobs".toLowerCase(), "type");
    public static boolean isMythic(Entity entity) {
        return entity.getPersistentDataContainer().has(type, PersistentDataType.STRING);
    }

    public static boolean castSkill(Player player, String spell) {
        LivingEntity target = MythicUtil.getTargetedEntity(player);
        List<Entity> targets = new ArrayList<>();
        targets.add(target);
        return MythicBukkit.inst().getAPIHelper().castSkill(player, spell, player, player.getLocation(), targets, null, 1.0F);

    }
}
