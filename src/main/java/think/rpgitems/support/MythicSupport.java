package think.rpgitems.support;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.utils.MythicUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.ArrayList;
import java.util.List;

public class MythicSupport implements Listener {
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

    @EventHandler
    public void onDamage(MythicDamageEvent e) {
        Entity entity = BukkitAdapter.adapt(e.getCaster().getEntity());
        if (entity instanceof LivingEntity living) {
            double damage = e.getDamage();
            for (RPGItem rpg : ItemManager.getEquipments(living).values()) {
                damage = processDamage(rpg, damage);
            }
            if (damage != e.getDamage()) {
                e.setDamage(damage);
            }
        }
    }
    
    private double processDamage(RPGItem rpg, double damage) {
        if (rpg != null) {
            if (rpg.getMythicSkillDamage() > 0) {
                damage += rpg.getMythicSkillDamage();
            }
            if (rpg.getMythicSkillDamageMultiple() > 0) {
                damage *= 1.0 + rpg.getMythicSkillDamageMultiple();
            }
        }
        return damage;
    }
}
