package think.rpgitems.power.cond;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.*;

import java.util.Map;

import static think.rpgitems.power.PowerResult.fail;
import static think.rpgitems.power.PowerResult.ok;

@Meta(marker = true)
public class HealthCondition extends BaseCondition<Void> {
    @Property(order = 0, required = true)
    public String id;

    @Property
    public boolean isCritical = false;

    @Property(order = 1, required = true)
    @AcceptedValue({"<", "<=", ">", ">=", "=", "!=", "range"})
    public String type = "";

    @Property(required = true)
    public double value;

    @Property
    public double valueMax;

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isCritical() {
        return isCritical;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getValueMax() {
        return valueMax;
    }

    @Override
    public PowerResult<Void> check(Player player, ItemStack stack, Map<PropertyHolder, PowerResult<?>> context) {
        double health = player.getHealth();
        switch (getType()) {
            case "=":
                return health == getValue() ? ok() : fail();
            case "!=":
                return health != getValue() ? ok() : fail();
            case "<":
                return health < getValue() ? ok() : fail();
            case "<=":
                return health <= getValue() ? ok() : fail();
            case ">":
                return health > getValue() ? ok() : fail();
            case ">=":
                return health >= getValue() ? ok() : fail();
            case "range": {
                double value1 = getValue();
                double value2 = getValueMax() <= 0 ? getValue() : getValueMax();
                return health >= value1 && health <= value2 ? ok() : fail();
            }
            default:
                return fail();
        }
    }

    @Override
    public String getName() {
        return "healthcondition";
    }
}
