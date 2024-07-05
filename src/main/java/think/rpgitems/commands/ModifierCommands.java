package think.rpgitems.commands;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import think.rpgitems.RPGItems;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Completion;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.RPGCommandReceiver;
import think.rpgitems.power.UnknownExtensionException;
import think.rpgitems.power.propertymodifier.Modifier;
import think.rpgitems.utils.ItemTagUtils;
import think.rpgitems.utils.nyaacore.Pair;
import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
import think.rpgitems.utils.nyaacore.cmdreceiver.BadCommandException;
import think.rpgitems.utils.nyaacore.cmdreceiver.SubCommand;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static think.rpgitems.commands.AdminCommands.*;
import static think.rpgitems.item.RPGItem.TAG_MODIFIER;
import static think.rpgitems.item.RPGItem.TAG_VERSION;
import static think.rpgitems.utils.ItemTagUtils.*;

@SuppressWarnings({"rawtypes"})
public class ModifierCommands extends RPGCommandReceiver {
    private final RPGItems plugin;

    public ModifierCommands(RPGItems plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "modifier";
    }

    private static Pair<NamespacedKey, Class<? extends Modifier>> getModifierClass(CommandSender sender, String modifierStr) {
        try {
            NamespacedKey key = PowerManager.parseKey(modifierStr);
            Class<? extends Modifier> cls = PowerManager.getModifier(key);
            if (cls == null) {
                msgs(sender, "message.modifier.unknown", modifierStr);
            }
            return Pair.of(key, cls);
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
            return null;
        }
    }

    @Completion("")
    public List<String> addCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completeStr.add(player.getName());
                }
                break;
            }
            case 2: {
                for (NamespacedKey s : PowerManager.getModifiers().keySet()) {
                    Object obj = PowerManager.hasExtension() ? s : s.getKey();
                    completeStr.add(obj.toString());
                }
                break;
            }
            default: {
                arguments.next();
                String mod = arguments.next();
                NamespacedKey namespacedKey = PowerManager.parseKey(mod);
                Class<? extends Modifier> modifier = PowerManager.getModifier(namespacedKey);
                return resolveProperties(sender, null, modifier, namespacedKey, arguments.getRawArgs()[arguments.getRawArgs().length - 1], arguments, false);
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "add", tabCompleter = "addCompleter")
    public void add(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        String baseStr = args.top();
        if (baseStr == null || baseStr.equals("help") || args.remains() < 2) {
            msgs(sender, "manual.modifier.add.description");
            msgs(sender, "manual.modifier.add.usage");
            return;
        }
        Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> rootContainer = getRootContainer(sender, args, baseStr);
        PersistentDataContainer container = rootContainer.getValue();
        String modifierStr = args.nextString();

        Pair<NamespacedKey, Class<? extends Modifier>> keyClass = getModifierClass(sender, modifierStr);
        if (keyClass == null || keyClass.getValue() == null) return;
        Class<? extends Modifier> cls = keyClass.getValue();
        try {
            Modifier modifier = initPropertyHolder(sender, args, null, cls);
            SubItemTagContainer modifierContainer = ItemTagUtils.makeTag(container, TAG_MODIFIER);
            set(modifierContainer, TAG_VERSION, UUID.randomUUID());
            NamespacedKey seq = nextAvailable(modifierContainer);
            SubItemTagContainer modifierTag = ItemTagUtils.makeTag(modifierContainer, seq);
            modifier.save(modifierTag);
            modifierTag.commit();
            if (rootContainer.getKey() != null){
                saveItem(rootContainer.getKey());
            }
            RPGItem.invalidateModifierCache();
            msg(sender, "message.modifier.ok", modifierStr);
        } catch (Exception e) {
            if (e instanceof BadCommandException) {
                throw (BadCommandException) e;
            }
            plugin.getLogger().log(Level.WARNING, "Error adding modifier " + modifierStr + " to " + baseStr + " ", e);
            msgs(sender, "internal.error.command_exception");
        }
    }

    private void saveItem(Pair<ItemStack, ItemMeta> pair) {
        ItemStack key = pair.getKey();
        ItemMeta value = pair.getValue();
        key.setItemMeta(value);
    }

    private NamespacedKey nextAvailable(PersistentDataContainer modifierContainer) {
        int i = 0;
        for (NamespacedKey key = PowerManager.parseKey(String.valueOf(i)); modifierContainer.has(key, PersistentDataType.TAG_CONTAINER); key = PowerManager.parseKey(String.valueOf(i))) {
            ++i;
        }
        return PowerManager.parseKey(String.valueOf(i));
    }

    private Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> getRootContainer(CommandSender sender, Arguments arguments, String baseStr) {
        PersistentDataContainer container;
        ItemStack item = null;
        ItemMeta meta = null;
        Pair<ItemStack, ItemMeta> metaPair = null;
        if (baseStr.toLowerCase(Locale.ROOT).equals("hand") && sender instanceof Player) {
            arguments.next();
            item = ((Player) sender).getInventory().getItemInMainHand();
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();
        } else {
            Player player = arguments.nextPlayer();
            container = player.getPersistentDataContainer();
        }

        if (item != null){
            metaPair = new Pair<>(item, meta);
        }

        return new Pair<>(metaPair, container);
    }

    @Completion("")
    public List<String> propCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1: {
                completeStr.add("HAND");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completeStr.add(player.getName());
                }
                break;
            }
            case 2: {
                String baseStr = arguments.top();
                Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> rootContainer = getRootContainer(sender, arguments, baseStr);
                PersistentDataContainer container = rootContainer.getValue();
                SubItemTagContainer modifierContainer = ItemTagUtils.makeTag(container, TAG_MODIFIER);
                List<Modifier> modifiers = RPGItem.getModifiers(modifierContainer);
                for (Modifier modifier : modifiers) {
                    completeStr.add(modifier.id());
                }
                break;
            }
            default: {
                String baseStr = arguments.top();
                Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> rootContainer = getRootContainer(sender, arguments, baseStr);
                PersistentDataContainer container = rootContainer.getValue();
                Pair<Integer, Modifier> nextModifier = nextModifier(container, arguments);
                Modifier modifier = nextModifier.getValue();
                return resolveProperties(sender, null, modifier.getClass(), modifier.getNamespacedKey(), arguments.getRawArgs()[arguments.getRawArgs().length - 1], arguments, false);
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "prop", tabCompleter = "propCompleter")
    public void prop(CommandSender sender, Arguments args) throws IllegalAccessException {
        if (readOnly(sender)) return;
        String baseStr = args.top();
        Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> rootContainer = getRootContainer(sender, args, baseStr);
        PersistentDataContainer container = rootContainer.getValue();
        try {
            Pair<Integer, Modifier> modifierPair = nextModifier(container, args);
            Modifier modifier = modifierPair.getValue();
            if (args.top() == null) {
                showModifier(sender, modifier);
                return;
            }
            setPropertyHolder(sender, args, modifier.getClass(), modifier, false);
            SubItemTagContainer modifierContainer = ItemTagUtils.makeTag(container, TAG_MODIFIER);
            set(modifierContainer, TAG_VERSION, UUID.randomUUID());
            NamespacedKey namespacedKey = PowerManager.parseKey(String.valueOf(modifierPair.getKey()));
            modifierContainer.remove(namespacedKey);
            SubItemTagContainer m = ItemTagUtils.makeTag(modifierContainer, namespacedKey);
            modifier.save(m);
            m.commit();
            if (rootContainer.getKey() != null){
                saveItem(rootContainer.getKey());
            }
            RPGItem.invalidateModifierCache();
            msgs(sender, "message.marker.change");
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
        }
    }

    public void showModifier(CommandSender sender, Modifier modifier) {
        msgs(sender, "message.modifier.show", modifier.getLocalizedName(sender), modifier.getNamespacedKey().toString(), modifier.id());
        NamespacedKey modifierKey = modifier.getNamespacedKey();
        PowerManager.getProperties(modifierKey).forEach(
                (name, prop) -> showProp(sender, modifierKey, prop.getValue(), modifier)
        );
    }

    public Pair<Integer, Modifier> nextModifier(PersistentDataContainer container, Arguments args) {
        SubItemTagContainer modifierContainer = ItemTagUtils.makeTag(container, TAG_MODIFIER);
        List<Modifier> modifiers = RPGItem.getModifiers(modifierContainer);
        String next = args.nextString();
        OptionalInt index = IntStream.range(0, modifiers.size()).filter(i -> modifiers.get(i).id().equals((next))).findFirst();
        if (!index.isPresent()) {
            throw new BadCommandException("message.modifier.unknown", next);
        }
        return Pair.of(index.getAsInt(), modifiers.get(index.getAsInt()));
    }

    @SubCommand(value = "remove", tabCompleter = "propCompleter")
    public void remove(CommandSender sender, Arguments args) {
        if (readOnly(sender)) return;
        String baseStr = args.top();
        Pair<Pair<ItemStack, ItemMeta>, PersistentDataContainer> rootContainer = getRootContainer(sender, args, baseStr);
        PersistentDataContainer container = rootContainer.getValue();
        try {
            Pair<Integer, Modifier> modifierPair = nextModifier(container, args);
            SubItemTagContainer modifierContainer = makeTag(container, TAG_MODIFIER);
            set(modifierContainer, TAG_VERSION, UUID.randomUUID());
            NamespacedKey currentKey = PowerManager.parseKey(String.valueOf(modifierPair.getKey()));
            int i = 0;
            for (NamespacedKey key = PowerManager.parseKey(String.valueOf(i)); modifierContainer.has(key, PersistentDataType.TAG_CONTAINER); key = PowerManager.parseKey(String.valueOf(i))) {
                ++i;
            }
            --i;
            modifierContainer.remove(currentKey);
            NamespacedKey lastKey = PowerManager.parseKey(String.valueOf(i));
            PersistentDataContainer lastContainer = getTag(modifierContainer, lastKey);
            if (lastContainer != null){
                set(modifierContainer, currentKey, lastContainer);
            }
            modifierContainer.remove(lastKey);
            modifierContainer.commit();
            if (rootContainer.getKey() != null){
                saveItem(rootContainer.getKey());
            }
            RPGItem.invalidateModifierCache();
            msgs(sender, "message.modifier.remove");
        } catch (UnknownExtensionException e) {
            msgs(sender, "message.error.unknown.extension", e.getName());
        }
    }

    @SubCommand("list")
    public void list(CommandSender sender, Arguments args) {
        int perPage = RPGItems.plugin.cfg.powerPerPage;
        String nameSearch = args.argString("n", args.argString("name", ""));
        List<NamespacedKey> modifiers = new ArrayList<>();
        for (NamespacedKey i : PowerManager.getModifiers().keySet()) {
            if (!i.getKey().contains(nameSearch)) continue;
            modifiers.add(i);
        }
        if (modifiers.isEmpty()) {
            msgs(sender, "message.modifier.not_found", nameSearch);
            return;
        }
        modifiers.sort(Comparator.comparing(NamespacedKey::getKey));
        Stream<NamespacedKey> stream = modifiers.stream();
        Pair<Integer, Integer> maxPage = getPaging(modifiers.size(), perPage, args);
        int page = maxPage.getValue();
        int max = maxPage.getKey();
        stream = stream
                         .skip((long) (page - 1) * perPage)
                         .limit(perPage);
        msgs(sender, "message.modifier.page-header", page, max);

        stream.forEach(
                modifier -> {
                    msgs(sender, "message.modifier.key", modifier.toString());
                    msgs(sender, "message.modifier.description", PowerManager.getDescription(modifier, null));
                    PowerManager.getProperties(modifier).forEach(
                            (name, mp) -> showProp(sender, modifier, mp.getValue(), null)
                    );
                    msgs(sender, "message.line_separator");
                });
        msgs(sender, "message.modifier.page-footer", page, max);
    }
}
