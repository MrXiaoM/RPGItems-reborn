package think.rpgitems.power.propertymodifier;

import org.bukkit.persistence.PersistentDataContainer;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Power;
import think.rpgitems.power.PropertyHolder;
import think.rpgitems.power.PropertyInstance;

import java.util.function.Function;

public interface Modifier<T> extends Function<RgiParameter<T>, T>, PropertyHolder {
    void init(PersistentDataContainer section);

    void save(PersistentDataContainer section);

    boolean match(RPGItem rpg, Power orig, PropertyInstance propertyInstance);

    String id();

    int priority();

    RPGItem getItem();

    void setItem(RPGItem item);

    Class<T> getModifierTargetType();
}
