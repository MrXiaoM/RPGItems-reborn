package think.rpgitems.data;

import com.udojava.evalex.Expression;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.util.logging.Level;

import static think.rpgitems.RPGItems.plugin;


public class FactorModifier {
    /**
     * Target factor
     */
    @Setter @Getter private String target;
    /**
     * Override damage expression when player attack the target factor entity
     */
    @Setter @Getter private String attack = "damage";
    /**
     * Override damage expression when player attacked by the target factor entity
     */
    @Setter @Getter private String defend = "damage";
    public FactorModifier(String target) {
        this.target = target;
    }
    public FactorModifier(String target, String attack, String defend) {
        this.target = target;
        this.attack = attack;
        this.defend = defend;
    }

    public double attack(String factor, double damage) {
        return matchFactor(factor) ? attack(damage) : damage;
    }

    public double defend(String factor, double damage) {
        return matchFactor(factor) ? defend(damage) : damage;
    }

    public boolean matchFactor(String factor) {
        return factor.equals(target);
    }

    public double attack(double damage) {
        try {
            Expression exp = new Expression(attack)
                    .with("damage", BigDecimal.valueOf(damage));
            return exp.eval().doubleValue();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                    "There are something wrong while processing factor modifier damage (attack). " +
                            "originalDamage=" + damage + ", " +
                            "expression=`" + attack + "`", t);
        }
        return damage;
    }

    public double defend(double damage) {
        try {
            Expression exp = new Expression(attack)
                    .with("damage", BigDecimal.valueOf(damage));
            return exp.eval().doubleValue();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                    "There are something wrong while processing factor modifier damage (defend). " +
                            "originalDamage=" + damage + ", " +
                            "expression=`" + defend + "`", t);
        }
        return damage;
    }

    public void save(ConfigurationSection section, String path) {
        String key = path + "." + target + ".";
        section.set(key + "attack", attack);
        section.set(key + "defend", defend);
    }

    public static FactorModifier load(ConfigurationSection section, String path, String target) {
        String key = path + "." + target + ".";
        String damage = section.getString(key + "attack", "damage");
        String armor = section.getString(key + "defend", "damage");
        return new FactorModifier(target, damage, armor);
    }
}
