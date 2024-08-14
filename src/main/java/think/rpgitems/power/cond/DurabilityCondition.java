package think.rpgitems.power.cond;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Meta;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;
import think.rpgitems.power.PropertyHolder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Meta(marker = true)
public class DurabilityCondition extends BaseCondition<Void> {

    @Property(order = 0, required = true)
    public String id;

    @Property
    public boolean isStatic = false;

    @Property
    public boolean isCritical = false;

    @Property
    public int durabilityMin = Integer.MIN_VALUE;

    @Property
    public int durabilityMax = Integer.MAX_VALUE;

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isCritical() {
        return isCritical;
    }

    @Override
    public PowerResult<Void> check(Player player, ItemStack stack, Map<PropertyHolder, PowerResult<?>> context) {
        RPGItem item = ItemManager.toRPGItem(stack).orElse(null);
        if (item == null) return PowerResult.fail();
        int durability = item.getItemStackDurability(stack).orElseThrow(() -> new IllegalStateException("Durability condition is not allowed on item without durability"));
        if ((durabilityMax > 0 && durability > durabilityMax) || durability < durabilityMin) return PowerResult.fail();
        return PowerResult.ok();
    }

    @Override
    public Set<String> getConditions() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return "durabilitycondition";
    }

    @Override
    public String displayText() {
        return null;
    }
}
