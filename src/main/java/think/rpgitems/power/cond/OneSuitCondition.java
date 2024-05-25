package think.rpgitems.power.cond;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Meta;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;
import think.rpgitems.power.PropertyHolder;

import java.util.Map;

import static think.rpgitems.power.PowerResult.fail;
import static think.rpgitems.power.PowerResult.ok;

@Meta(marker = true)
public class OneSuitCondition extends BaseCondition<Void> {
    @Property(order = 0, required = true)
    public String id;

    @Property
    public boolean isCritical = false;

    @Property
    public String helmet = "";

    @Property
    public String chestplate = "";

    @Property
    public String leggings = "";

    @Property
    public String boots = "";

    @Property
    public String mainHand = "";

    @Property
    public String offHand = "";

    @Override
    public String id() {
        return id;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }

    public String getMainHand() {
        return mainHand;
    }

    public String getOffHand() {
        return offHand;
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
        EntityEquipment equipment = player.getEquipment();
        return isMeetRPGItem(equipment.getHelmet(), getHelmet())
                && isMeetRPGItem(equipment.getChestplate(), getChestplate())
                && isMeetRPGItem(equipment.getLeggings(), getLeggings())
                && isMeetRPGItem(equipment.getBoots(), getBoots())
                && isMeetRPGItem(equipment.getItemInMainHand(), getMainHand())
                && isMeetRPGItem(equipment.getItemInOffHand(), getOffHand()) ? ok() : fail();
    }

    private static boolean isMeetRPGItem(ItemStack item, String id) {
        if (id == null || id.trim().isEmpty()) return true;
        RPGItem rpgItem = ItemManager.toRPGItem(item).orElse(null);
        return rpgItem != null && rpgItem.getName().equals(id);
    }

    @Override
    public String getName() {
        return "onesuit";
    }
}
