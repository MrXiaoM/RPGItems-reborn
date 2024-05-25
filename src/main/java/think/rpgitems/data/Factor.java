package think.rpgitems.data;

import com.udojava.evalex.Expression;
import think.rpgitems.RPGItems;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class Factor {
    public final String id;
    public final String name;
    public final Map<String, String> damageTo;
    public final Map<String, Integer> damageToCompare;
    public Factor(String id, String name, Map<String, String> damageTo) {
        this.id = id;
        this.name = name;
        this.damageTo = damageTo;
        this.damageToCompare = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.updateCompare();
    }

    public void updateCompare() {
        damageToCompare.clear();
        BigDecimal compareNumber = BigDecimal.valueOf(10.0);
        for (Map.Entry<String, String> entry : damageTo.entrySet()) {
            try {
                BigDecimal damage = new Expression(entry.getValue()).with("damage", compareNumber).eval();
                damageToCompare.put(entry.getKey(), damage.compareTo(compareNumber));

            } catch (Throwable t) {
                RPGItems.logger.log(Level.WARNING, "Eval damage expression `" + entry.getValue() + "` failed! damage=" + compareNumber, t);
            }
        }
    }
}
