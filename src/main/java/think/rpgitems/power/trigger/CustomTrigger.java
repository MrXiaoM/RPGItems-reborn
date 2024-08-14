package think.rpgitems.power.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerPlain;
import think.rpgitems.power.PowerResult;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;

import java.util.List;

import static think.rpgitems.item.RPGStone.NBT_POWER_STONES;


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
        List<String> list = ItemTagUtils.getStringList(stack, NBT_POWER_STONES).orElse(null);
        if (list == null || list.isEmpty()) return false;
        for (String str : list) {
            if (!str.contains("/")) continue; // 没有设置触发器的不处理
            String[] split = str.split("/", 2);
            if (split[0].equals(id)) {
                return trigger.name().equals(split[1]);
            }
        }
        return false;
    }
}
