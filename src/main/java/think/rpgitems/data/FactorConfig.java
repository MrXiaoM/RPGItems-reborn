package think.rpgitems.data;

import com.udojava.evalex.Expression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.api.IFactorDefiner;
import think.rpgitems.utils.nyaacore.configuration.ISerializable;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

import static think.rpgitems.RPGItems.plugin;

public class FactorConfig implements ISerializable {
    List<IFactorDefiner> definerList = new ArrayList<>();
    Map<String, Factor> factors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void clearDefiner() {
        definerList.clear();
    }

    public void addDefiner(IFactorDefiner definer) {
        definerList.add(definer);
        definerList.sort(Comparator.comparingInt(IFactorDefiner::priority));
    }

    public void checkDefaultConfig(ConfigurationSection config) {
        if (factors.isEmpty() && (config.getKeys(false).isEmpty() || config.getConfigurationSection("factors") == null)) {
            config.set("factors.machine.name", "&bMachine");
            config.set("factors.machine.damage-to.creature", "damage * 1.2");
            config.set("factors.machine.damage-to.supernatural", "damage * 0.8");
            config.set("factors.creature.name", "&eCreature");
            config.set("factors.creature.damage-to.supernatural", "damage * 1.2");
            config.set("factors.creature.damage-to.machine", "damage * 0.8");
            config.set("factors.supernatural.name", "&dSuper Natural");
            config.set("factors.supernatural.damage-to.machine", "damage * 1.2");
            config.set("factors.supernatural.damage-to.creature", "damage * 0.8");
        }
    }

    /**
     * call when config load
     */
    @Override
    public void deserialize(ConfigurationSection config) {
        checkDefaultConfig(config);
        factors.clear();
        ConfigurationSection section = config.getConfigurationSection("factors");
        if (section != null) for (String factorId : section.getKeys(false)) {
            String name = section.getString(factorId + ".name", factorId);
            Map<String, String> damageTo = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            ConfigurationSection section1 = section.getConfigurationSection(factorId + ".damage-to");
            if (section1 != null) for (String otherFactorId : section1.getKeys(false)) {
                String exp = section1.getString(otherFactorId);
                damageTo.put(otherFactorId, exp);
            }
            factors.put(factorId, new Factor(factorId, name, damageTo));
        }
    }

    /**
     * call when config save
     */
    @Override
    public void serialize(ConfigurationSection config) {
        checkDefaultConfig(config);
        for (Factor factor : factors.values()) {
            config.set("factors." + factor.id + ".name", factor.name);
            for (Map.Entry<String, String> entry : factor.damageTo.entrySet()) {
                config.set("factors" + factor.id + ".damage-to." + entry.getKey(), entry.getValue());
            }
        }
    }

    public Factor getFactor(LivingEntity entity) {
        for (IFactorDefiner definer : definerList) {
            String id = definer.define(entity);
            Factor factor = id == null ? null : factors.get(id);
            if (factor != null) return factor;
        }
        return null;
    }

    public double getDamage(LivingEntity damager, LivingEntity entity, double damage) {
        Factor factorDamager = getFactor(damager);
        Factor factorEntity = getFactor(entity);
        if (factorEntity != null && factorDamager != null) {

            String expression = factorDamager.damageTo.get(factorEntity.id);
            if (expression != null) try {

                Expression exp = new Expression(expression)
                        .with("damage", BigDecimal.valueOf(damage));
                return exp.eval().doubleValue();

            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING,
                        "There are something wrong while processing factor damage. " +
                                "originalDamage=" + damage + ", " +
                                "expression=`" + expression + "`", t);
            }
        }
        return damage;
    }
}
