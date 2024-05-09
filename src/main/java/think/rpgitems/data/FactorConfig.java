package think.rpgitems.data;

import com.udojava.evalex.Expression;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import think.rpgitems.utils.nyaacore.configuration.ISerializable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

public class FactorConfig implements ISerializable {

    Map<String, Factor> factors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public void checkDefaultConfig(ConfigurationSection config) {
        if (config.getKeys(false).isEmpty() || config.getConfigurationSection("factors") == null) {
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
        throw new NotImplementedException("TODO");
    }

    public double getDamage(LivingEntity damager, LivingEntity entity, double damage) {
        Factor factorDamager = getFactor(damager);
        Factor factorEntity = getFactor(entity);
        if (factorEntity == null || factorDamager == null) return damage;

        String expression = factorDamager.damageTo.get(factorEntity.id);
        if (expression == null) return damage;
        Expression exp = new Expression(expression)
                .with("damage", BigDecimal.valueOf(damage));
        return exp.eval().doubleValue();
    }
}
