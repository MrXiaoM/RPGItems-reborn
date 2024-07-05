package think.rpgitems.power.proxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.RPGItems;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PropertyInstance;
import think.rpgitems.power.propertymodifier.Modifier;
import think.rpgitems.power.propertymodifier.RgiParameter;
import think.rpgitems.power.trigger.Trigger;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Interceptor {

    public static class origPowerHolder {
        UUID playerId;
        ItemStack itemStack;
        Power orig;

        public origPowerHolder(UUID playerId, ItemStack itemStack, Power orig) {
            this.playerId = playerId;
            this.itemStack = itemStack;
            this.orig = orig;
        }

        public UUID playerId() {
            return playerId;
        }

        public ItemStack itemStack() {
            return itemStack;
        }

        public Power orig() {
            return orig;
        }
    }
    private static final Cache<String, Pair<origPowerHolder, Power>> POWER_CACHE = CacheBuilder.newBuilder().weakValues().build();
    private final Power orig;
    private final Player player;
    private final Map<Method, PropertyInstance> getters;
    private final ItemStack stack;
    private final MethodHandles.Lookup lookup;

    protected Interceptor(Power orig, Player player, ItemStack stack, MethodHandles.Lookup lookup) {
        this.lookup = lookup;
        this.orig = orig;
        this.player = player;
        this.getters = PowerManager.getProperties(orig.getClass())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getValue().getKey(), e -> e.getValue().getValue()));
        this.stack = stack;
    }

    public static Power create(Power orig, Player player, ItemStack stack, Trigger trigger) {
        Pair<origPowerHolder, Power> result = POWER_CACHE.getIfPresent(getCacheKey(player, stack, orig));
        if (result != null) {
            if (result.getKey().itemStack().equals(stack) && result.getKey().playerId().equals(player.getUniqueId()) && result.getKey().orig().equals(orig))
                return result.getValue();
        }
        Power proxyPower = makeProxy(orig, player, stack, trigger);
        POWER_CACHE.put(getCacheKey(player, stack, orig), Pair.of(new origPowerHolder(player.getUniqueId(), stack, orig), proxyPower));
        return proxyPower;

    }

    private static Power makeProxy(Power orig, Player player, ItemStack stack, Trigger trigger) {
        MethodHandles.Lookup lookup = orig.getLookup();
        if (lookup == null) lookup = MethodHandles.lookup();
        if (lookup.lookupClass() != orig.getClass()) {
            try {
                lookup = MethodHandles.privateLookupIn(orig.getClass(), lookup);
            } catch (IllegalAccessException e) {
                RPGItems.logger.severe("make proxy error: can not get lookup (is it outdated?): " + orig.getClass());
                e.printStackTrace();
                return orig;
            }
        }

        MethodHandle constructorMH;

        try {
            Class<? extends Power> proxyClass = makeProxyClass(orig, player, stack, trigger, lookup);
            constructorMH = lookup.findConstructor(proxyClass, MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            RPGItems.logger.severe("make proxy error: not instantiatable: " + orig.getClass());
            e.printStackTrace();
            return orig;
        }

        try {
            return (Power) constructorMH.invoke();
        } catch (Throwable e) {
            RPGItems.logger.severe("make proxy error: not instantiatable (invoke error): " + orig.getClass());
            e.printStackTrace();
            return orig;
        }
    }

    private static String getCacheKey(Player player, ItemStack itemStack, Power orig) {
        String playerHash = player.getUniqueId().toString();
        String itemHash = ItemTagUtils.getString(itemStack, RPGItem.NBT_ITEM_UUID).orElseGet(() -> String.valueOf(itemStack.hashCode()));
        String origHash = orig.getName() + ":" + orig.getPlaceholderId() + ":" + orig.getClass().getName();
        return playerHash + "-#-" + itemHash + "-#-" + origHash;

    }

    private static Class<? extends Power> makeProxyClass(Power orig, Player player, ItemStack stack, Trigger trigger, MethodHandles.Lookup lookup) throws NoSuchMethodException {
        Class<? extends Power> origClass = orig.getClass();


        return new ByteBuddy()
                .subclass(origClass)
                .implement(new Class[]{trigger.getPowerClass()})
                .implement(NotUser.class)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new Interceptor(orig, player, stack, lookup)))
                .make()
                .load(origClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(lookup))
                .getLoaded();
    }

    @RuntimeType
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object intercept(@AllArguments Object[] args, @Origin Method method) {
        try {
            if (getters.containsKey(method)) {
                PropertyInstance propertyInstance = getters.get(method);
                Class<?> type = propertyInstance.field().getType();
                List<Modifier> playerModifiers = RPGItem.getModifiers(player);
                List<Modifier> stackModifiers = RPGItem.getModifiers(player, stack);
                List<Modifier> modifiers = new ArrayList<>();
                modifiers.addAll(playerModifiers);
                modifiers.addAll(stackModifiers);
                modifiers.sort(Comparator.comparing(Modifier::priority));
                // Numeric modifiers
                if (type == int.class || type == Integer.class || type == float.class || type == Float.class || type == double.class || type == Double.class) {

                    List<Modifier<Double>> numberModifiers = new ArrayList<>();
                    for (Modifier m : modifiers) {
                        if (!(m.getModifierTargetType() == Double.class) && m.match(orig, propertyInstance)) continue;
                        numberModifiers.add((Modifier<Double>) m);
                    }
                    Number value = (Number) invokeMethod(method, orig, args);
                    double origValue = value.doubleValue();
                    for (Modifier<Double> numberModifier : numberModifiers) {
                        RgiParameter param = new RgiParameter<>(orig.getItem(), orig, stack, origValue);

                        origValue = numberModifier.apply(param);
                    }
                    if (int.class.equals(type) || Integer.class.equals(type)) {
                        return (int) Math.round(origValue);
                    } else if (float.class.equals(type) || Float.class.equals(type)) {
                        return (float) (origValue);
                    } else {
                        return origValue;
                    }
                }
            }

            return invokeMethod(method, orig, args);
        } catch (Throwable e) {
            RPGItems.logger.severe("invoke method error:" + method);
            e.printStackTrace();
        }
        return null;
    }

    private Object invokeMethod(Method method, Object obj, Object... args) throws Throwable {
        //method.trySetAccessible();
        MethodHandle MH;
        MH = lookup.unreflect(method);
        MH = MH.bindTo(obj);
        return MH.invokeWithArguments(args);
    }
}
