package think.rpgitems.power;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import think.rpgitems.RPGItems;
import think.rpgitems.power.trigger.Trigger;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base interface for all powers
 */
@SuppressWarnings({"rawtypes"})
public interface Power extends PropertyHolder, PlaceholderHolder, TagHolder {
    String CGLIB_CLASS_SEPARATOR = "$$";
    String BYTE_BUDDY_CLASS_SEPARATOR = "$ByteBuddy";

    static Set<Trigger> getTriggers(Class<? extends Pimpl> cls) {
        Set<Class<? extends Pimpl>> dynamic = getDynamicInterfaces(cls);
        Set<Trigger> triggers = new HashSet<>();
        for (Class<? extends Pimpl> i : dynamic) {
            Trigger.fromInterface(triggers, i);
        }
        return triggers;
    }

    /**
     * @param cls Class of Power
     * @return All static implemented interfaces
     */
    @SuppressWarnings("unchecked")
    static Set<Class<? extends Pimpl>> getStaticInterfaces(Class<? extends Pimpl> cls) {
        Set<Class<? extends Pimpl>> set = new HashSet<>();
        for (TypeToken<?> type : TypeToken.of(cls).getTypes().interfaces()) {
            Class<?> rawType = type.getRawType();
            if (Pimpl.class.isAssignableFrom(rawType) && !Objects.equals(rawType, Pimpl.class)) {
                set.add((Class<? extends Pimpl>) rawType);
            }
        }
        return set;
    }

    /**
     * @param cls Class of Power
     * @return All static and dynamic implemented interfaces
     */
    static Set<Class<? extends Pimpl>> getDynamicInterfaces(Class<? extends Pimpl> cls) {
        Set<Class<? extends Pimpl>> set = new HashSet<>();
        for (Class<? extends Pimpl> staticInterface : getStaticInterfaces(cls)) {
            set.add(staticInterface);
            set.addAll(PowerManager.adapters.row(staticInterface).keySet());
        }
        return set;
    }

    static Set<Trigger> getDefaultTriggers(Class<? extends Power> cls) {
        cls = getUserClass(cls);
        Meta annotation = Objects.requireNonNull(cls.getAnnotation(Meta.class));
        if (annotation.defaultTrigger().length > 0) {
            return Trigger.valueOf(annotation.defaultTrigger());
        }
        if (annotation.marker()) {
            return Collections.emptySet();
        }

        return getTriggers(annotation.implClass());
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T> getUserClass(Class<T> clazz) {
        if (clazz.getName().contains(BYTE_BUDDY_CLASS_SEPARATOR)) {
            Class<T> superclass = (Class<T>) clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    /**
     * Display name of this power
     *
     * @return Display name
     */
    @Nullable
    String displayName();

    /**
     * Display name or default name of this power
     *
     * @return Display name or default name
     */
    default String getLocalizedDisplayName() {
        return Strings.isNullOrEmpty(displayName()) ? getLocalizedName(RPGItems.plugin.cfg.language) : displayName();
    }

    /**
     * Display text of this power
     *
     * @return Display text
     */
    String displayText();

    /**
     * Localized name of this power
     *
     * @param locale Locale tag
     * @return Localized name
     */
    default String localizedDisplayText(String locale) {
        return displayText();
    }

    /**
     * Localized name of this power
     *
     * @param locale Locale tag
     * @return Localized name
     */
    default String localizedDisplayText(Locale locale) {
        return localizedDisplayText(locale.toLanguageTag());
    }

    Set<Trigger> getTriggers();

    Set<String> getSelectors();

    Set<String> getConditions();

    String requiredContext();

    default void deinit() {
    }

    default MethodHandles.Lookup getLookup() {
        return MethodHandles.lookup();
    }
}
