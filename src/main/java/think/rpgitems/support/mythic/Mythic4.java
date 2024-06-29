package think.rpgitems.support.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.util.MythicUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Mythic4 implements IMythic {
    MythicMobs mythic = MythicMobs.inst();

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
    public void onDamage(EntityDamageByEntityEvent e) {
        ActiveMob mob = mythic.getMobManager().getMythicMobInstance(e.getDamager());
        if (mob == null) return;
        Player caster = mob.getOwner().map(Bukkit::getPlayer).orElse(null);
        if (caster != null) {
            double damage = e.getDamage();
            for (RPGItem rpg : ItemManager.getEquipments(caster).values()) {
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
