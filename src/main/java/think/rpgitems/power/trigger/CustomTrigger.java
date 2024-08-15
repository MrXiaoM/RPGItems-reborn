package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.item.RPGStone;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;

import java.util.Map;

public class CustomTrigger extends Trigger<Event, PowerPlain, Void, Void> {
    public CustomTrigger() {
        super(Event.class, PowerPlain.class, Void.class, Void.class, "CUSTOM_TRIGGER");
    }

    CustomTrigger(String name) {
        super(name, "CUSTOM_TRIGGER", Event.class, PowerPlain.class, Void.class, Void.class);
    }

    @Override
    public PowerResult<Void> run(RPGItem item, PowerPlain power, Player player, ItemStack i, Event event) {
        return power.fire(player, item, i);
    }

    public boolean filter(Power power, Trigger<?, ?, ?, ?> trigger, ItemStack stack) {
        if (!power.getTriggers().contains(this)) return false;
        String id = power.getStoneFlag();
        if (id.isEmpty()) return false;
        // 从 stack 中获取玩家在技能石配置的触发器，并判定该触发器是否可用
        Map<RPGStone, String> map = ItemManager.toRPGStoneList(stack);
        for (Map.Entry<RPGStone, String> entry : map.entrySet()) {
            RPGStone stone = entry.getKey();
            String triggerName = entry.getValue();
            if (triggerName != null && stone.getName().equals(id)) {
                return trigger.name().equals(triggerName);
            }
        }
        return false;
    }
}
