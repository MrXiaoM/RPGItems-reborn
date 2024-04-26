package think.rpgitems.power.trigger;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.*;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings({"rawtypes"})
public abstract class Trigger<TEvent extends Event, TPower extends Pimpl, TResult, TReturn> extends BasePropertyHolder {

    private static final Map<String, Trigger> registry = new HashMap<>();
    private static boolean acceptingNew = true;
    private final Class<TEvent> eventClass;
    private final Class<TResult> resultClass;
    private final Class<TPower> powerClass;
    private final Class<TReturn> returnClass;
    private final String name;
    private final String base;
    @Property
    public int priority;

    @SuppressWarnings("unchecked")
    Trigger(Class<TEvent> eventClass, Class<TPower> powerClass, Class<TResult> resultClass, Class returnClass, String name) {
        this(name, eventClass, powerClass, resultClass, returnClass);
        register(this);
    }

    public Trigger(String name, Class<TEvent> eventClass, Class<TPower> powerClass, Class<TResult> resultClass, Class<TReturn> returnClass) {
        this.eventClass = eventClass;
        this.powerClass = powerClass;
        this.resultClass = resultClass;
        this.returnClass = returnClass;
        this.name = name;
        this.base = null;
    }

    @SuppressWarnings("unchecked")
    Trigger(String name, String base, Class<TEvent> eventClass, Class<TPower> powerClass, Class<TResult> resultClass, Class returnClass) {
        this.eventClass = eventClass;
        this.powerClass = powerClass;
        this.resultClass = resultClass;
        this.returnClass = returnClass;
        this.name = name;
        if (Trigger.get(base) == null) {
            throw new IllegalArgumentException();
        }
        this.base = base;
    }

    public static void fromInterface(Set<Trigger> addTo, Class<? extends Pimpl> power) {
        for (Trigger trigger : registry.values()) {
            if (trigger.powerClass.equals(power)) {
                addTo.add(trigger);
            }
        }
    }

    @Nullable
    public static Trigger get(String name) {
        return registry.get(name);
    }

    public static Set<Trigger> getValid(List<String> name, Set<String> ignored) {
        Set<Trigger> triggers = new HashSet<>();
        for (String s : name) {
            if (!Strings.isNullOrEmpty(s)) {
                Trigger trigger = Trigger.get(s);
                if (trigger == null) {
                    ignored.add(s);
                } else {
                    triggers.add(trigger);
                }
            }
        }
        return triggers;
    }

    public static Set<Trigger> getValid(List<String> name) {
        Set<Trigger> set = new HashSet<>();
        for (String n : name) {
            if (!Strings.isNullOrEmpty(n)) {
                Trigger trigger = get(n);
                if (Objects.nonNull(trigger)) {
                    set.add(trigger);
                }
            }
        }
        return set;
    }

    public static Trigger valueOf(String name) {
        Trigger trigger = registry.get(name);
        if (trigger == null) throw new IllegalArgumentException();
        return trigger;
    }

    public static Set<Trigger> valueOf(String[] name) {
        return valueOf(Lists.newArrayList(name));
    }

    public static Set<Trigger> valueOf(List<String> name) {
        Set<Trigger> set = new HashSet<>();
        for (String n : name) {
            if (!Strings.isNullOrEmpty(n)) {
                set.add(valueOf(n));
            }
        }
        return set;
    }

    public static void register(Trigger trigger) {
        String name = trigger.name();
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException("Cannot set already-set trigger: " + trigger.name);
        } else if (!isAcceptingRegistrations()) {
            throw new IllegalStateException("No longer accepting new triggers (can only be done when loading): " + trigger.name);
        }
        registry.put(name, trigger);
        PowerManager.registerMetas(trigger.getClass());
    }

    public static boolean isAcceptingRegistrations() {
        return acceptingNew;
    }

    public static void stopAcceptingRegistrations() {
        acceptingNew = false;
    }

    public static Set<String> keySet() {
        return registry.keySet();
    }

    public static Collection<Trigger> values() {
        return registry.values();
    }

    @Override
    public final String getPropertyHolderType() {
        return "trigger";
    }

    @Override
    public String getName() {
        return name();
    }

    public int getPriority() {
        return priority;
    }

    public TReturn def(Player player, ItemStack i, TEvent event) {
        return null;
    }

    public TReturn next(TReturn a, PowerResult<TResult> b) {
        return null;
    }

    public boolean check(Player player, ItemStack i, TEvent event) {
        return true;
    }

    public abstract PowerResult<TResult> run(TPower power, Player player, ItemStack i, TEvent event);

    public PowerResult<TResult> run(TPower power, Player player, ItemStack i, TEvent event, Object data) {
        return run(power, player, i, event);
    }

    public PowerResult<TResult> warpResult(PowerResult<Void> overrideResult, TPower power, Player player, ItemStack i, TEvent event) {
        return overrideResult.with(null);
    }

    public Class<TPower> getPowerClass() {
        return powerClass;
    }

    public Class<TResult> getResultClass() {
        return resultClass;
    }

    public Class<TEvent> getEventClass() {
        return eventClass;
    }

    public Class<TReturn> getReturnClass() {
        return returnClass;
    }

    public String name() {
        return name;
    }

    public String getBase() {
        return base;
    }

    @Override
    public String toString() {
        return name();
    }

    @SuppressWarnings("unchecked")
    public Trigger<TEvent, TPower, TResult, TReturn> copy(String name) {
        if (Trigger.get(name) != null) throw new IllegalArgumentException("name is used");
        try {
            return getClass()
                    .getConstructor(String.class)
                    .newInstance(name);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
