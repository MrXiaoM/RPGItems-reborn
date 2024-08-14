package think.rpgitems.item;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import think.rpgitems.RPGItems;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class ItemGroup {
    static RPGItems plugin;

    private final String name;
    private final int uid;
    private final String regex;
    private Set<Integer> itemUids;
    @Setter
    private Set<RPGItem> items;
    @Setter
    private File file;

    @Setter
    private String author = plugin.cfg.defaultAuthor;
    @Setter
    private String note = plugin.cfg.defaultNote;
    @Setter
    private String license = plugin.cfg.defaultLicense;

    public ItemGroup(String name, int uid, String regex, CommandSender author) {
        this.name = name;
        this.uid = uid;
        this.regex = regex;
        this.setAuthor(author instanceof Player ? ((Player) author).getUniqueId().toString() : plugin.cfg.defaultAuthor);
        if (!Strings.isNullOrEmpty(regex)) {
            Pattern p = Pattern.compile(regex);
            setItems(ItemManager.itemNames().stream().filter(p.asPredicate()).map(ItemManager::getItemByName).collect(Collectors.toSet()));
            setItemUids(items.stream().map(RPGItem::getUid).collect(Collectors.toSet()));
        } else {
            setItems(new HashSet<>());
            setItemUids(new HashSet<>());
        }
    }

    public ItemGroup(ConfigurationSection s, File f) {
        setFile(f);
        name = s.getString("name");
        uid = s.getInt("uid");
        regex = s.getString("regex");

        restore(s);
    }

    public ItemGroup(ConfigurationSection s, String name, int uid, String regex) {
        if (uid >= 0) throw new IllegalArgumentException();
        this.name = name;
        this.uid = uid;
        this.regex = regex;
        restore(s);
    }

    public void refresh() {
        setItems(getItemUids().stream().map(ItemManager::getItemById).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    private void restore(ConfigurationSection s) {
        setAuthor(s.getString("author", ""));
        setNote(s.getString("note", ""));
        setLicense(s.getString("license", ""));

        setItemUids(new HashSet<>(s.getIntegerList("item_uids")));
        setItems(getItemUids().stream().map(ItemManager::getItemById).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    public void addItem(RPGItem item) {
        int uid = item.getUid();
        try {
            itemUids.add(uid);
            items.add(item);
        } catch (Throwable e) {
            items.remove(item);
            itemUids.remove(uid);
            throw e;
        }
    }

    public void removeItem(RPGItem item) {
        itemUids.remove(item.getUid());
        items.remove(item);
    }

    public void save(ConfigurationSection s) {
        s.set("author", getAuthor());
        s.set("note", getNote());
        s.set("license", getLicense());

        s.set("name", getName());
        s.set("uid", getUid());
        s.set("regex", getRegex());

        s.set("item_uids", new ArrayList<>(getItemUids()));
    }

    public void give(Player sender, int count, boolean wear) {
        refresh();
        for (RPGItem item : items) {
            item.give(sender, count, wear);
        }
    }

    public void setItemUids(Set<Integer> itemUids) {
        this.itemUids = itemUids;
        setItems(getItemUids().stream().map(ItemManager::getItemById).filter(Objects::nonNull).collect(Collectors.toSet()));
    }
}
