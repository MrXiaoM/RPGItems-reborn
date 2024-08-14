package think.rpgitems.item;

import org.bukkit.NamespacedKey;
import think.rpgitems.power.Condition;
import think.rpgitems.power.Power;
import think.rpgitems.power.PropertyHolder;

import java.util.List;

public interface RPGBaseHolder {
    int getUid();
    String getName();
    List<Power> getPowers();
    List<Condition<?>> getConditions();
    <T extends Power> List<T> getPower(NamespacedKey key, Class<T> power);
    <T extends Condition<?>> List<T> getCondition(NamespacedKey key, Class<T> condition);
    Condition<?> getCondition(String id);
    void addPower(NamespacedKey key, Power power);
    void removePower(Power power);
    void addCondition(NamespacedKey key, Condition<?> condition);
    void removeCondition(Condition<?> condition);
    void deinit();
    NamespacedKey getPropertyHolderKey(PropertyHolder power);
    NamespacedKey removePropertyHolderKey(PropertyHolder power);
    void save();
}
