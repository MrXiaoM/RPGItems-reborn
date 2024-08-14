package think.rpgitems.item;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import think.rpgitems.utils.pdc.ItemPDC;
import think.rpgitems.utils.nyaacore.Message;
import think.rpgitems.utils.nyaacore.Pair;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.nio.file.ExtendedOpenOption;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import think.rpgitems.commands.AdminCommands;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.event.ItemsLoadedEvent;
import think.rpgitems.power.UnknownExtensionException;
import think.rpgitems.power.UnknownPowerException;
import think.rpgitems.support.WGSupport;
import think.rpgitems.utils.nyaacore.utils.ItemTagUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static think.rpgitems.item.RPGItem.*;
import static think.rpgitems.item.RPGStone.NBT_POWER_STONE_ITEM_UUID;
import static think.rpgitems.item.RPGStone.NBT_POWER_STONE_UID;
import static think.rpgitems.power.Utils.rethrow;

public class ItemManager {
    private static final long OFFSET_BASIS = 2166136261L;// 32位offset basis
    private static final long PRIME = 16777619; // 32位prime
    private static HashMap<Integer, RPGItem> itemById = new HashMap<>();
    private static HashMap<String, RPGItem> itemByName = new HashMap<>();
    private static HashMap<Integer, RPGStone> stoneById = new HashMap<>();
    private static HashMap<String, RPGStone> stoneByName = new HashMap<>();
    private static HashMap<Integer, ItemGroup> groupById = new HashMap<>();
    private static HashMap<String, ItemGroup> groupByName = new HashMap<>();
    private static HashMap<String, FileLock> itemFileLocks = new HashMap<>();
    private static HashMap<RPGItem, Pair<File, FileLock>> unlockedItem = new HashMap<>();
    private static RPGItems plugin;
    private static File itemsDir;
    private static File backupsDir;
    private static boolean extendedLock = true;
    private static final Cache<Long, ItemMeta> metaCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .initialCapacity(1024)
            .build();

    public static void updateItem(Player player, ItemStack item) {
        toRPGItemByMeta(item).ifPresent(rpgItem -> rpgItem.updateItem(player, item));
        toRPGStone(item).ifPresent(rpgStone -> rpgStone.updateItem(item));
    }

    public static boolean hasName(String name) {
        return itemByName.containsKey(name) || stoneByName.containsKey(name) || groupByName.containsKey(name);
    }

    public static boolean hasId(Integer id) {
        return itemById.containsKey(id) || stoneById.containsKey(id) || groupById.containsKey(id);
    }

    public static boolean isUnlocked(RPGItem item) {
        return unlockedItem.containsKey(item);
    }

    public static Collection<RPGItem> items() {
        return itemByName.values();
    }

    public static Collection<RPGStone> stones() {
        return stoneByName.values();
    }

    public static Pair<File, FileLock> getBackup(RPGItem item) {
        return unlockedItem.get(item);
    }

    public static void addBackup(RPGItem item, Pair<File, FileLock> of) {
        unlockedItem.put(item, of);
    }

    public static boolean hasBackup() {
        return unlockedItem.isEmpty();
    }

    public static Set<String> itemNames() {
        return itemByName.keySet();
    }

    public static Set<String> stoneNames() {
        return stoneByName.keySet();
    }

    public static Set<RPGItem> getUnlockedItem() {
        return unlockedItem.keySet();
    }

    public static File getItemsDir() {
        return itemsDir;
    }

    private static void setItemsDir(File itemsDir) {
        ItemManager.itemsDir = itemsDir;
    }

    public static File getBackupsDir() {
        return backupsDir;
    }

    private static void setBackupsDir(File backupsDir) {
        ItemManager.backupsDir = backupsDir;
    }

    public static void reload(RPGItems pl) {
        unload();
        load(pl);
    }

    public static void unload() {
        itemByName.values().forEach(RPGItem::deinit);
        stoneByName.values().forEach(RPGStone::deinit);
        itemById = new HashMap<>();
        itemByName = new HashMap<>();
        stoneById = new HashMap<>();
        stoneByName = new HashMap<>();
        groupById = new HashMap<>();
        groupByName = new HashMap<>();
        resetLock();
    }

    public static void refreshItem() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory()) {
                updateItem(player, item);
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                updateItem(player, item);
            }
        }
    }

    public static void load(RPGItems pl) {
        plugin = pl;
        RPGItem.plugin = pl;
        ItemGroup.plugin = pl;
        RPGStone.plugin = pl;

        try {
            File testFile = new File(plugin.getDataFolder(), "lock_test" + System.currentTimeMillis() + ".tmp");
            if (!testFile.createNewFile()) {
                throw new IllegalStateException("Not writable data folder!");
            }
            try (FileChannel channel = FileChannel.open(testFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ, ExtendedOpenOption.NOSHARE_WRITE, ExtendedOpenOption.NOSHARE_DELETE)) {
                FileLock fileLock = channel.tryLock(0L, Long.MAX_VALUE, true);
                fileLock.release();
            } catch (Exception e) {
                plugin.getLogger().log(Level.FINER, "Disabling extended lock", e);
                extendedLock = false;
            }
            Files.delete(testFile.toPath());
        } catch (IOException e) {
            extendedLock = false;
            plugin.getLogger().log(Level.WARNING, "Not writable data folder!", e);
        }
        setItemsDir(mkdir());
        setBackupsDir(mkbkdir());
        load(getItemsDir(), plugin.cfg.itemShowLoaded ? Bukkit.getConsoleSender() : null);
        groupById.values().forEach(ItemGroup::refresh);
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new ItemsLoadedEvent()));
    }

    public static boolean load(File file, CommandSender sender) {
        String locale = RPGItems.plugin.cfg.language;
        if (sender instanceof Player) {
            locale = ((Player) sender).getLocale();
        }
        I18n i18n = I18n.getInstance(locale);
        try {
            if (!file.exists()) {
                plugin.getLogger().severe("Trying to load " + file + " that does not exist.");
                throw new IllegalStateException("Trying to load " + file + " that does not exist.");
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles(f -> !f.getName().startsWith(".") && ((f.isFile() && f.getName().endsWith("yml")) || f.isDirectory()));
                if (Objects.requireNonNull(subFiles).length == 0) {
                    if (sender != null) {
                        new Message(I18n.formatDefault("message.item.empty_dir", file.getPath())).send(sender);
                    } else {
                        new Message(I18n.formatDefault("message.item.empty_dir", file.getPath())).send(Bukkit.getConsoleSender());
                    }
                    return false;
                }
                for (File subFile : subFiles) {
                    load(subFile, sender);
                }
                return false;
            }
            RPGItem item = load(file);
            if (item != null && sender != null) {
                new Message("")
                        .append(I18n.formatDefault("message.item.load", item.getName()), Collections.singletonMap("{item}", item.getComponent(sender)))
                        .send(sender);
            }
            return true;
        } catch (Exception e) {
            if (e instanceof UnknownExtensionException || e instanceof UnknownPowerException) {
                plugin.getLogger().log(Level.WARNING, "Missing dependency when loading " + file + ". ", e);
            } else {
                plugin.getLogger().log(Level.SEVERE, "Error loading " + file + ".", e);
            }

            Message message = new Message(I18n.formatDefault("message.error.loading", file.getPath(), e.getLocalizedMessage()));
            if (sender == null) {
                Bukkit.getOperators().forEach(t -> message.send(t.getPlayer()));
                message.send(Bukkit.getConsoleSender());
            } else {
                message.send(sender);
            }

            if (sender == null || sender instanceof ConsoleCommandSender) {
                Bukkit.getOperators().forEach(message::sendTo);
            }
        }
        return false;
    }

    private static RPGItem load(File file) throws Exception {
        String canonicalPath = file.getCanonicalPath();
        if (itemFileLocks.containsKey(canonicalPath) && itemFileLocks.get(canonicalPath).isValid()) {
            plugin.getLogger().severe("Trying to load " + file + " that already loaded.");
            throw new IllegalStateException("Trying to load " + file + " that already loaded.");
        }
        Path path = file.toPath().toRealPath();
        Path base = getItemsDir().toPath().toRealPath();
        if (!path.startsWith(base)) {
            plugin.getLogger().info("Copying " + file + " to " + getItemsDir() + ".");
            File newFile = createFile(getItemsDir(), file.getName(), "", false);
            plugin.getLogger().info("As " + newFile + ".");
            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file = newFile;
        }
        YamlConfiguration itemStorage = new YamlConfiguration();
        itemStorage.load(file);
        if (file.getName().endsWith("-group.yml")) {
            ItemGroup group = new ItemGroup(itemStorage, file);
            addGroup(group);
            return null;
        }
        if (file.getName().endsWith("-stone.yml")) {
            RPGStone stone = new RPGStone(itemStorage, file);
            addStone(stone);
            return null;
        }
        if (itemStorage.getInt("uid", 0) >= 0) {
            itemStorage.set("uid", nextUid());
            itemStorage.save(file);
            plugin.getLogger().info(file.getName() + " have no `uid`, generated new one and saved.");
        }
        RPGItem item = new RPGItem(itemStorage, file);
        addItem(item);
        lock(file);
        return item;
    }

    public static void addItem(RPGItem item) {
        try {
            if (groupById.containsKey(item.getUid()) || stoneById.containsKey(item.getUid()) || itemById.putIfAbsent(item.getUid(), item) != null) {
                throw new IllegalArgumentException("Duplicated item uid:" + item.getUid());
            }
            if (groupByName.containsKey(item.getName()) || stoneByName.containsKey(item.getName()) || itemByName.putIfAbsent(item.getName(), item) != null) {
                throw new IllegalArgumentException("Duplicated item name:" + item.getName());
            }
        } catch (Exception e) {
            itemById.remove(item.getUid(), item);
            itemByName.remove(item.getName(), item);
            throw e;
        }
    }

    public static void addStone(RPGStone stone) {
        try {
            if (groupById.containsKey(stone.getUid()) || itemById.containsKey(stone.getUid()) || stoneById.putIfAbsent(stone.getUid(), stone) != null) {
                throw new IllegalArgumentException("Duplicated stone uid:" + stone.getUid());
            }
            if (groupByName.containsKey(stone.getName()) || itemByName.containsKey(stone.getName()) || stoneByName.putIfAbsent(stone.getName(), stone) != null) {
                throw new IllegalArgumentException("Duplicated stone name:" + stone.getName());
            }
        } catch (Exception e) {
            stoneById.remove(stone.getUid(), stone);
            stoneByName.remove(stone.getName(), stone);
            throw e;
        }
    }

    public static void addGroup(ItemGroup group) {
        try {
            if (itemById.containsKey(group.getUid()) || stoneById.containsKey(group.getUid()) || groupById.putIfAbsent(group.getUid(), group) != null) {
                throw new IllegalArgumentException("Duplicated group uid:" + group.getUid());
            }
            if (itemByName.containsKey(group.getName()) || stoneByName.containsKey(group.getName()) || groupByName.putIfAbsent(group.getName(), group) != null) {
                throw new IllegalArgumentException("Duplicated group name:" + group.getName());
            }
        } catch (Exception e) {
            groupById.remove(group.getUid(), group);
            groupByName.remove(group.getName(), group);
            throw e;
        }
    }

    public static void save() {
        if (plugin.cfg.readonly) return;
        for (RPGItem item : itemByName.values()) {
            save(item);
        }
        for (RPGStone stone : stoneByName.values()) {
            save(stone);
        }
    }

    private static File mkdir() {
        String itemsDirRedirect = plugin.cfg.itemsDirRedirect;
        File items;
        if (itemsDirRedirect == null)
            items = new File(plugin.getDataFolder(), "items");
        else {
            if (itemsDirRedirect.startsWith("."))
                items = new File(plugin.getDataFolder(), itemsDirRedirect.substring(1));
            else items = new File(itemsDirRedirect);
        }
        if (!items.exists() || !items.isDirectory()) {
            if (!items.mkdir()) {
                throw new IllegalStateException();
            }
        }
        return items;
    }

    private static File mkbkdir() {
        File backup = new File(plugin.getDataFolder(), "backup");
        if (!backup.exists() || !backup.isDirectory()) {
            if (!backup.mkdir()) {
                throw new IllegalStateException();
            }
        }
        return backup;
    }

    public static void save(RPGItem item) {
        if (plugin.cfg.readonly) return;
        String itemName = item.getName();
        File itemFile = item.getFile() == null ? createFile(getItemsDir(), item.getName(), "-item", true) : item.getFile();
        boolean exist = itemFile.exists();
        String cfgStr = "";
        File backup = null;
        item.setPluginVersion(RPGItems.getVersion());
        item.setPluginSerial(RPGItems.getSerial());
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            item.save(configuration);
            cfgStr = configuration.saveToString();
            if (exist) {
                backup = unlockAndBackup(item, false);
            }
            configuration.save(itemFile);

            try {
                String canonicalPath = itemFile.getCanonicalPath();
                YamlConfiguration test = new YamlConfiguration();
                test.load(canonicalPath);
                RPGItem testItem = new RPGItem(test, null);
                testItem.deinit();
                if (backup != null && backup.exists()) {
                    backup.deleteOnExit();
                }
                item.setFile(itemFile);
                lock(itemFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error verifying integrity for " + itemName + ".", e);
                throw new AdminCommands.CommandException("message.error.verifying", e, itemName, e.getLocalizedMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving " + itemName + ".", e);
            plugin.getLogger().severe("Dumping current item");
            plugin.getLogger().severe("===============");
            plugin.getLogger().severe(cfgStr);
            plugin.getLogger().severe("===============");
            if (exist && backup != null && backup.exists()) {
                try {
                    plugin.getLogger().severe("Recovering backup: " + backup);
                    Files.copy(backup.toPath(), itemFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    lock(itemFile);
                } catch (Exception exRec) {
                    plugin.getLogger().log(Level.SEVERE, "Error recovering backup: " + backup, exRec);
                    throw new AdminCommands.CommandException("message.error.recovering", exRec, itemName, backup.getPath(), exRec.getLocalizedMessage());
                }
            }
            rethrow(e);
        }
    }

    public static void save(RPGStone stone) {
        if (plugin.cfg.readonly) return;
        String itemName = stone.getName();
        File itemFile = stone.getFile() == null ? createFile(getItemsDir(), stone.getName(), "-stone", true) : stone.getFile();
        boolean exist = itemFile.exists();
        String cfgStr = "";
        File backup = null;
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            stone.save(configuration);
            cfgStr = configuration.saveToString();
            if (exist) {
                backup = unlockAndBackup(stone, false);
            }
            configuration.save(itemFile);

            try {
                String canonicalPath = itemFile.getCanonicalPath();
                YamlConfiguration test = new YamlConfiguration();
                test.load(canonicalPath);
                RPGItem testItem = new RPGItem(test, null);
                testItem.deinit();
                if (backup != null && backup.exists()) {
                    backup.deleteOnExit();
                }
                stone.setFile(itemFile);
                lock(itemFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error verifying integrity for " + itemName + ".", e);
                throw new AdminCommands.CommandException("message.error.verifying", e, itemName, e.getLocalizedMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving " + itemName + ".", e);
            plugin.getLogger().severe("Dumping current stone");
            plugin.getLogger().severe("===============");
            plugin.getLogger().severe(cfgStr);
            plugin.getLogger().severe("===============");
            if (exist && backup != null && backup.exists()) {
                try {
                    plugin.getLogger().severe("Recovering backup: " + backup);
                    Files.copy(backup.toPath(), itemFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    lock(itemFile);
                } catch (Exception exRec) {
                    plugin.getLogger().log(Level.SEVERE, "Error recovering backup: " + backup, exRec);
                    throw new AdminCommands.CommandException("message.error.recovering", exRec, itemName, backup.getPath(), exRec.getLocalizedMessage());
                }
            }
            rethrow(e);
        }
    }

    public static void save(ItemGroup itemGroup) {
        if (plugin.cfg.readonly) return;
        String itemName = itemGroup.getName();
        File itemFile = itemGroup.getFile() == null ? createFile(getItemsDir(), itemGroup.getName(), "-group", true) : itemGroup.getFile();
        String cfgStr = "";
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            itemGroup.save(configuration);
            cfgStr = configuration.saveToString();
            configuration.save(itemFile);

            try {
                String canonicalPath = itemFile.getCanonicalPath();
                YamlConfiguration test = new YamlConfiguration();
                test.load(canonicalPath);
                new ItemGroup(test, null);
                itemGroup.setFile(itemFile);
                lock(itemFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error verifying integrity for " + itemName + ".", e);
                throw new AdminCommands.CommandException("message.error.verifying", e, itemName, e.getLocalizedMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving " + itemName + ".", e);
            plugin.getLogger().severe("Dumping current itemGroup");
            plugin.getLogger().severe("===============");
            plugin.getLogger().severe(cfgStr);
            plugin.getLogger().severe("===============");
            rethrow(e);
        }
    }

    public static void lock(File file) throws IOException {
        if (!plugin.cfg.itemFsLock) return;
        FileLock oldLock = itemFileLocks.get(file.getCanonicalPath());
        if (oldLock != null) {
            if (oldLock.isValid()) {
                plugin.getLogger().severe("Trying to lock a already locked file " + file + ".");
                throw new IllegalStateException();
            }
            oldLock.channel().close();
            itemFileLocks.remove(file.getCanonicalPath());
        }

        FileLock lock = lockFile(file);
        if (lock == null) {
            plugin.getLogger().severe("Error locking " + file + ".");
            throw new IllegalStateException();
        }
        itemFileLocks.put(file.getCanonicalPath(), lock);
    }

    public static FileLock lockFile(File file) throws IOException {
        if (extendedLock) {
            return FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ, ExtendedOpenOption.NOSHARE_WRITE, ExtendedOpenOption.NOSHARE_DELETE).tryLock(0L, Long.MAX_VALUE, true);
        } else {
            return new RandomAccessFile(file, "rw").getChannel().tryLock();
        }
    }

    private static void unlock(File itemFile, boolean remove) throws IOException {
        if (!plugin.cfg.itemFsLock) return;
        FileLock fileLock = remove ? itemFileLocks.remove(itemFile.getCanonicalPath()) : itemFileLocks.get(itemFile.getCanonicalPath());
        if (fileLock != null) {
            if (fileLock.isValid()) {
                fileLock.release();
            }
            fileLock.channel().close();
        } else {
            plugin.getLogger().warning("Lock for " + itemFile + " does not exist? If you are reloading a item, that's OK.");
        }
    }

    public static void removeLock(RPGItem item) throws IOException {
        ItemManager.itemFileLocks.remove(item.getFile().getCanonicalPath());
        ItemManager.unlockedItem.remove(item);
    }

    public static Pair<File, FileLock> removeBackup(RPGItem item) {
        return ItemManager.unlockedItem.remove(item);
    }

    private static File createFile(File items, String itemName, String postfix, boolean tran) {
        String filename = tran ? getItemFilename(itemName, postfix) + ".yml" : itemName;
        File file = new File(items, filename);
        while (file.exists()) {
            file = new File(items, tran ? getItemFilename(itemName + ThreadLocalRandom.current().nextInt(), postfix) + ".yml" : itemName + ThreadLocalRandom.current().nextInt());
        }
        return file;
    }

    private static void resetLock() {
        for (FileLock fileLock : itemFileLocks.values()) {
            try {
                fileLock.release();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error releasing " + fileLock + ".", e);
            } finally {
                try {
                    fileLock.channel().close();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Error closing channel " + fileLock.channel() + ".", e);
                }
            }
        }
        itemFileLocks = new HashMap<>();
        for (Pair<File, FileLock> lockPair : unlockedItem.values()) {
            try {
                lockPair.getValue().release();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error releasing " + lockPair.getValue() + " for " + lockPair.getKey() + ".", e);
            } finally {
                try {
                    lockPair.getValue().channel().close();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Error closing channel " + lockPair.getValue().channel() + " for " + lockPair.getKey() + ".", e);
                }
            }
        }
        unlockedItem = new HashMap<>();
    }

    public static File unlockAndBackup(RPGItem item, boolean remove) throws IOException {
        return unlockAndBackup(item.getFile(), item.getName(), remove);
    }
    public static File unlockAndBackup(RPGStone stone, boolean remove) throws IOException {
        return unlockAndBackup(stone.getFile(), stone.getName(), remove);
    }
    public static File unlockAndBackup(File file, String name, boolean remove) throws IOException {
        File backup = new File(getBackupsDir(), file.getName().replaceAll("\\.yml$", "") + "." + System.currentTimeMillis() + ".bak");
        unlock(file, remove);
        try {
            if (!backup.createNewFile()) throw new IllegalStateException();
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot create backup for" + name + ".", e);
        }
        return backup;
    }

    /**
     * Get equipments in slots of helmet, chestplate, leggings, boots, off hand, main hand.
     */
    public static Map<EquipmentSlot, RPGItem> getEquipments(LivingEntity entity) {
        Map<EquipmentSlot, RPGItem> items = new HashMap<>();
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            toRPGItem(equipment.getHelmet()).ifPresent(it -> items.put(EquipmentSlot.HEAD, it));
            toRPGItem(equipment.getChestplate()).ifPresent(it -> items.put(EquipmentSlot.CHEST, it));
            toRPGItem(equipment.getLeggings()).ifPresent(it -> items.put(EquipmentSlot.LEGS, it));
            toRPGItem(equipment.getBoots()).ifPresent(it -> items.put(EquipmentSlot.FEET, it));
            toRPGItem(equipment.getItemInMainHand()).ifPresent(it -> items.put(EquipmentSlot.HAND, it));
            toRPGItem(equipment.getItemInOffHand()).ifPresent(it -> items.put(EquipmentSlot.OFF_HAND, it));
        }
        return items;
    }

    public static Optional<RPGItem> toRPGItem(ItemStack item) {
        return toRPGItem(item, true);
    }

    public static Optional<RPGItem> toRPGItem(ItemStack item, boolean ignoreModel) {
        if (item == null || item.getType() == Material.AIR)
            return Optional.empty();
        if (!item.hasItemMeta())
            return Optional.empty();

        Optional<Integer> uid = ItemTagUtils.getInt(item, NBT_UID);
        Optional<Integer> itemUuid = ItemTagUtils.getInt(item, NBT_ITEM_UUID);
        Optional<Boolean> isModel = ItemTagUtils.getBoolean(item, NBT_IS_MODEL);

        if (uid.isEmpty()) {
            return Optional.empty();
        }
        if (ignoreModel && isModel.orElse(false)) {
            return Optional.empty();
        }
        return ItemManager.getItem(uid.get());
    }

    public static Optional<RPGItem> toRPGItemByMeta(ItemStack item) {
        return toRPGItemByMeta(item, true);
    }

    public static Optional<RPGItem> toRPGItemByMeta(ItemStack item, boolean ignoreModel) {
        if (item == null || item.getType() == Material.AIR)
            return Optional.empty();
        if (!item.hasItemMeta())
            return Optional.empty();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();

        PersistentDataContainer tagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        if (tagContainer.has(TAG_META, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer metaTag = ItemPDC.getTag(tagContainer, TAG_META);
            Integer uid = ItemPDC.getInt(metaTag, TAG_ITEM_UID);
            if (uid == null)return Optional.empty();
            Optional<Boolean> optIsModel = ItemPDC.optBoolean(metaTag, TAG_IS_MODEL);
            if (ignoreModel && optIsModel.orElse(false)) {
                return Optional.empty();
            }
            return ItemManager.getItem(uid);
        }
        return Optional.empty();
    }

    public static Optional<RPGStone> toRPGStone(ItemStack item) {
        return toRPGStone(item, true);
    }

    public static Optional<RPGStone> toRPGStone(ItemStack item, boolean ignoreModel) {
        if (item == null || item.getType() == Material.AIR)
            return Optional.empty();
        if (!item.hasItemMeta())
            return Optional.empty();

        Optional<Integer> uid = ItemTagUtils.getInt(item, NBT_POWER_STONE_UID);
        Optional<Integer> itemUuid = ItemTagUtils.getInt(item, NBT_POWER_STONE_ITEM_UUID);
        Optional<Boolean> isModel = ItemTagUtils.getBoolean(item, NBT_IS_MODEL);

        if (uid.isEmpty()) {
            return Optional.empty();
        }
        if (ignoreModel && isModel.orElse(false)) {
            return Optional.empty();
        }
        return ItemManager.getStone(uid.get());
    }

    public static long hash(byte[] src) {
        long hash = OFFSET_BASIS;
        for (byte b : src) {
            hash ^= b;
            hash *= PRIME;
        }
        return hash;
    }

    public static ItemInfo parseItemInfo(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        if (!item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tagContainer = Objects.requireNonNull(meta).getPersistentDataContainer();
        if (tagContainer.has(TAG_META, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer itemMeta = ItemPDC.getTag(tagContainer, TAG_META);
            int uid = ItemPDC.getInt(itemMeta, TAG_ITEM_UID);
            Optional<RPGItem> opt = ItemManager.getItem(uid);
            if (opt.isEmpty()) return null;
            RPGItem rpgItem = opt.get();
            ItemInfo itemInfo = new ItemInfo(rpgItem);
            if (rpgItem.getMaxDurability() > 0) {
                OptionalInt optDur = ItemPDC.optInt(itemMeta, TAG_DURABILITY);
                itemInfo.durability = optDur.orElseGet(rpgItem::getDefaultDurability);
            }

            itemInfo.stackOwner = ItemPDC.optUUID(itemMeta, TAG_OWNER).orElse(null);
            itemInfo.stackId = ItemPDC.optUUID(itemMeta, TAG_STACK_ID).orElse(null);
            return itemInfo;
        }
        return null;
    }

    public static RPGItem newItem(String name, CommandSender sender) {
        if (hasName(name)) return null;
        int free = nextUid();
        RPGItem item = new RPGItem(name, free, sender);
        addItem(item);
        return item;
    }

    public static RPGStone newStone(String name, CommandSender sender) {
        if (hasName(name)) return null;
        int free = nextUid();
        RPGStone stone = new RPGStone(name, free, sender);
        addStone(stone);
        return stone;
    }

    public static ItemGroup newGroup(String name, CommandSender sender) {
        return newGroup(name, null, sender);
    }

    public static ItemGroup newGroup(String name, String regex, CommandSender sender) {
        if (hasName(name)) return null;
        int free = nextUid();
        ItemGroup group = new ItemGroup(name, free, regex, sender);
        addGroup(group);
        return group;
    }

    static int nextUid() {
        int free;
        do {
            free = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, 0);
        } while (hasId(free));
        return free;
    }

    public static RPGItem cloneItem(RPGItem item, String name) {
        if (plugin.cfg.readonly)
            return null;
        if (hasName(name)) return null;
        int free = nextUid();
        ConfigurationSection section = new MemoryConfiguration();
        item.save(section);
        RPGItem newItem;
        try {
            newItem = new RPGItem(section, name, free);
            newItem.setTemplate(false);
            newItem.setTemplateOf(item.getName());
        } catch (UnknownPowerException | UnknownExtensionException e) {
            throw new IllegalStateException(e);
        }
        addItem(newItem);
        return newItem;
    }

    public static Optional<RPGItem> getItem(int id) {
        return Optional.ofNullable(itemById.get(id));
    }

    public static Optional<RPGItem> getItem(String name) {
        return Optional.ofNullable(itemByName.get(name));
    }

    static RPGItem getItemById(int id) {
        return itemById.get(id);
    }

    static RPGItem getItemByName(String name) {
        return itemByName.get(name);
    }

    public static Optional<RPGStone> getStone(int id) {
        return Optional.ofNullable(stoneById.get(id));
    }

    public static Optional<RPGStone> getStone(String name) {
        return Optional.ofNullable(stoneByName.get(name));
    }

    static RPGStone getStoneById(int id) {
        return stoneById.get(id);
    }

    static RPGStone getStoneByName(String name) {
        return stoneByName.get(name);
    }

    public static Optional<ItemGroup> getGroup(int uid) {
        return Optional.ofNullable(groupById.get(uid));
    }

    public static Optional<ItemGroup> getGroup(String name) {
        return Optional.ofNullable(groupByName.get(name));
    }

    public static Set<RPGItem> getItems(int id) {
        return itemOrGroup(itemById.get(id), groupById.get(id));
    }

    public static Set<RPGItem> getItems(String name) {
        return itemOrGroup(itemByName.get(name), groupByName.get(name));
    }

    private static Set<RPGItem> itemOrGroup(RPGItem rpgItem, ItemGroup group) {
        if (rpgItem != null) return Collections.singleton(rpgItem);
        if (group != null) {
            return Collections.unmodifiableSet(group.getItems());
        }
        return Collections.emptySet();
    }

    public static void remove(RPGItem item, boolean delete) {
        item.deinit();
        itemByName.remove(item.getName());
        itemById.remove(item.getUid());
        if (delete) {
            if (plugin.cfg.readonly) return;
            try {
                File backup = unlockAndBackup(item, true);
                Files.delete(item.getFile().toPath());
                backup.deleteOnExit();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error deleting file " + item.getFile() + ".", e);
            }
        }
    }

    public static void remove(RPGStone stone, boolean delete) {
        stone.deinit();
        stoneByName.remove(stone.getName());
        stoneById.remove(stone.getUid());
        if (delete) {
            if (plugin.cfg.readonly) return;
            try {
                File backup = unlockAndBackup(stone, true);
                Files.delete(stone.getFile().toPath());
                backup.deleteOnExit();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error deleting file " + stone.getFile() + ".", e);
            }
        }
    }

    public static void remove(ItemGroup group, boolean delete) {
        groupByName.remove(group.getName());
        groupById.remove(group.getUid());
        if (delete) {
            if (plugin.cfg.readonly) return;
            try {
                File itemFile = group.getFile();
                File backup = new File(getBackupsDir(), itemFile.getName().replaceAll("\\.yml$", "") + "." + System.currentTimeMillis() + ".bak");
                try {
                    if (!backup.createNewFile()) throw new IllegalStateException();
                    Files.copy(itemFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Cannot create backup for" + group.getName() + ".", e);
                }
                Files.delete(group.getFile().toPath());
                backup.deleteOnExit();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error deleting file " + group.getFile() + ".", e);
            }
        }
    }

    public static String getItemFilename(String itemName, String postfix) {
        // ensure Windows servers won't be blown up by CONs or NULs
        // and escape some character that don't fit into a file name
        return
                itemName
                        .replace("_", "__")
                        .replace("/", "_f")
                        .replace("\\", "_b")
                        .replace("*", "_a")
                        .replace("\"", "_o")
                        .replace("'", "_i")
                        .replace("?", "_q")
                        .replace("<", "_l")
                        .replace(">", "_g")
                        .replace("|", "_p")
                        .replace(":", "_c")
                        .replace(".", "_d")
                        + postfix;
    }

    public static Event.Result canUse(Player p, RPGItem rItem) {
        return canUse(p, rItem, true);
    }

    public static Event.Result canUse(Player p, RPGItem rItem, boolean showWarn) {
        if (WGSupport.canUse(p, rItem, null, showWarn) == Event.Result.DENY)
            return Event.Result.DENY;
        return (rItem == null || rItem.checkPermission(p, showWarn) == Event.Result.ALLOW) ? Event.Result.ALLOW : Event.Result.DENY;
    }
}
