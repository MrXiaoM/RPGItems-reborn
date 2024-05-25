package think.rpgitems.power.cond;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Meta;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;
import think.rpgitems.power.PropertyHolder;

import java.util.Map;

@Meta(marker = true)
public class SelfBurningCondition extends BaseCondition<Void> {
    @Property(order = 0, required = true)
    public String id;

    @Property
    public boolean isCritical = false;


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

    @Override
    public PowerResult<Void> check(Player player, ItemStack stack, Map<PropertyHolder, PowerResult<?>> context) {
        return player.getFireTicks() > 0 ? PowerResult.ok() : PowerResult.fail();
    }

    @Override
    public String getName() {
        return "selfburningcondition";
    }
}
