package think.rpgitems.data;

import com.udojava.evalex.Expression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import think.rpgitems.api.IFactorDefiner;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.configuration.ISerializable;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

import static think.rpgitems.RPGItems.plugin;

public class FactorConfig implements ISerializable {
    List<IFactorDefiner> definerList = new ArrayList<>();
    Map<String, Factor> factors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    List<Pair<Set<String>, String>> conflictOverrides = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.put(factor.id, factor);
    }

    public void addConflictOverride(Collection<String> conflictFactors, String override) {
        Set<String> factors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        factors.addAll(conflictFactors);
        conflictOverrides.add(Pair.of(factors, override));
    }

    public void clearDefiner() {
        definerList.clear();
    }

    public void addDefiner(IFactorDefiner definer) {
        definerList.add(definer);
        definerList.sort(Comparator.comparingInt(IFactorDefiner::priority));
    }

    /**
     * call when config load
     */
    @Override
    public void deserialize(ConfigurationSection config) {
        factors.clear();
        conflictOverrides.clear();
        ConfigurationSection section = config.getConfigurationSection("factors");
        if (section != null) for (String factorId : section.getKeys(false)) {
            String name = section.getString(factorId + ".name", factorId);
            Map<String, String> damageTo = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            ConfigurationSection section1 = section.getConfigurationSection(factorId + ".damage_to");
            if (section1 != null) for (String otherFactorId : section1.getKeys(false)) {
                String exp = section1.getString(otherFactorId);
                damageTo.put(otherFactorId, exp);
            }
            addFactor(new Factor(factorId, name, damageTo));
        }
        section = config.getConfigurationSection("conflict_override");
        if (section != null) for (String conflictFactors : section.getKeys(false)) {
            List<String> list = Arrays.asList(conflictFactors.split(","));
            String factorId = section.getString(conflictFactors);
            addConflictOverride(list, factorId);
        }
    }

    /**
     * call when config save
     */
    @Override
    public void serialize(ConfigurationSection config) {
        for (Factor factor : factors.values()) {
            config.set("factors." + factor.id + ".name", factor.name);
            for (Map.Entry<String, String> entry : factor.damageTo.entrySet()) {
                config.set("factors." + factor.id + ".damage_to." + entry.getKey(), entry.getValue());
            }
        }
        for (Pair<Set<String>, String> pair : conflictOverrides) {
            config.set("conflict_override." + String.join(",", pair.getKey()), pair.getValue());
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

    public List<String> getFactorList() {
        return new ArrayList<>(factors.keySet());
    }

    public List<Factor> getFactors() {
        return new ArrayList<>(factors.values());
    }

    /**
     * Check factors that have been defined. If there is a conflict, return a new factor.
     * @param factors factors to check
     * @return new factor, null if there is no conflict.
     */
    @Nullable
    public String getConflictOverride(Collection<String> factors) {
        for (Pair<Set<String>, String> pair : conflictOverrides) {
            if (factors.containsAll(pair.getKey())) {
                return pair.getValue();
            }
        }
        return null;
    }

    public double getDamage(LivingEntity damager, LivingEntity entity, double damage) {
        double finDamage = damage;
        Factor factorDamager = getFactor(damager);
        Factor factorEntity = getFactor(entity);
        if (factorEntity != null && factorDamager != null) {

            String expression = factorDamager.damageTo.get(factorEntity.id);
            if (expression != null) try {

                Expression exp = new Expression(expression)
                        .with("damage", BigDecimal.valueOf(damage));
                finDamage = exp.eval().doubleValue();

            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING,
                        "There are something wrong while processing factor damage. " +
                                "originalDamage=" + damage + ", " +
                                "expression=`" + expression + "`", t);
            }
        }

        Collection<RPGItem> items;
        if (factorEntity != null) {
            // attack damage override
            items = ItemManager.getEquipments(damager).values();
            for (RPGItem rpg : items) {
                FactorModifier modifier = rpg.getFactorModifiers().get(factorEntity.id);
                if (modifier != null) {
                    finDamage = modifier.attack(factorEntity.id, finDamage);
                }
            }
        }
        if (factorDamager != null) {
            // defend damage override
            items = ItemManager.getEquipments(entity).values();
            for (RPGItem rpg : items) {
                FactorModifier modifier = rpg.getFactorModifiers().get(factorDamager.id);
                if (modifier != null) {
                    finDamage = modifier.defend(factorDamager.id, finDamage);
                }
            }
        }

        return finDamage;
    }
}
