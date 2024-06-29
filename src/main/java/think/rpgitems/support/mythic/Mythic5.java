package think.rpgitems.support.mythic;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.utils.MythicUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Mythic5 implements IMythic {
    MythicBukkit mythic = MythicBukkit.inst();

    @Override
    @SuppressWarnings({"deprecation"})
    public String getVersion() {
        return mythic.getDescription().getVersion();
    }

    @Override
    public boolean castSkill(Player player, String spell) {
        List<Entity> eTargets = new ArrayList<>();
        List<Location> lTargets = new ArrayList<>();
        LivingEntity target = MythicUtil.getTargetedEntity(player);
        if (target != null) {
            eTargets.add(target);
        }
        return mythic.getAPIHelper().castSkill(player, spell, player, player.getLocation(), eTargets, lTargets, 1.0F);
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
                damage *= rpg.getMythicSkillDamageMultiple();
            }
            if (rpg.getMythicSkillCriticalRate() > 0) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (random.nextDouble(100) < rpg.getMythicSkillCriticalRate()) {
                    damage += rpg.getCriticalDamage();
                    damage *= rpg.getMythicSkillCriticalDamageMultiple();
                }
            }
        }
        return damage;
    }
}
