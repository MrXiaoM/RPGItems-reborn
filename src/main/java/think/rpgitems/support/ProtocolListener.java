package think.rpgitems.support;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.google.common.collect.Lists;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ProtocolListener extends PacketAdapter {
    private final ProtocolManager manager;
    RPGItems plugin;
    Set<String> materialToCheck = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    public ProtocolListener(RPGItems plugin) {
        super(plugin,
                ListenerPriority.LOW,
                Lists.newArrayList(
                        PacketType.Play.Server.ENTITY_EQUIPMENT,
                        PacketType.Play.Server.ENTITY_METADATA,
                        PacketType.Play.Server.WINDOW_ITEMS,
                        PacketType.Play.Server.SET_SLOT
                ),
                ListenerOptions.ASYNC,
                ListenerOptions.SKIP_PLUGIN_VERIFIER
        );
        this.plugin = plugin;
        if (RPGItems.isNetheriteAvailable()) {
            materialToCheck.add("NETHERITE_HELMET");
            materialToCheck.add("NETHERITE_CHESTPLATE");
            materialToCheck.add("NETHERITE_LEGGINGS");
            materialToCheck.add("NETHERITE_BOOTS");
        } else {
            materialToCheck.add("DIAMOND_HELMET");
            materialToCheck.add("DIAMOND_CHESTPLATE");
            materialToCheck.add("DIAMOND_LEGGINGS");
            materialToCheck.add("DIAMOND_BOOTS");
        }
        manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(this);
        plugin.disableHook.add(() -> manager.removePacketListener(this));
        plugin.getLogger().info("ProtocolLib hooked.");
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        PacketType type = packet.getType();
        if (type.equals(PacketType.Play.Server.ENTITY_EQUIPMENT)) {
            onSendingEntityEquipment(packet);
        } else if (type.equals(PacketType.Play.Server.ENTITY_METADATA)) {
            onSendingEntityData(packet);
        } else if (type.equals(PacketType.Play.Server.WINDOW_ITEMS)) {
            onSendingWindowItems(packet);
        } else if (type.equals(PacketType.Play.Server.SET_SLOT)) {
            onSendingSetSlot(packet);
        }
    }

    /**
     * <a href="https://wiki.vg/index.php?title=Protocol&oldid=18186#Set_Equipment">wiki</a>
     */
    private void onSendingEntityEquipment(PacketContainer packet) {
        StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> modifier = packet.getSlotStackPairLists();
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = modifier.read(0);
        for (int i = 0; i < list.size(); i++) {
            Pair<EnumWrappers.ItemSlot, ItemStack> pair = list.get(i);
            pair.setSecond(process(pair.getSecond()));
            list.set(i, pair);
        }
        modifier.write(0, list);
    }

    /**
     * <a href="https://wiki.vg/index.php?title=Protocol&oldid=18186#Set_Entity_Metadata">wiki</a>
     */
    private void onSendingEntityData(PacketContainer packet) {
        StructureModifier<List<WrappedDataValue>> modifier = packet.getDataValueCollectionModifier();
        List<WrappedDataValue> list = modifier.read(0);
        for (int i = 0; i < list.size(); i++) {
            WrappedDataValue data = list.get(i);
            if (data.getValue() instanceof ItemStack) {
                ItemStack item = (ItemStack) data.getValue();
                ItemStack process = process(item);
                if (!process.getType().equals(item.getType())) {
                    data.setValue(process);
                    list.set(i, data);
                }
            }
        }
        modifier.write(0, list);
    }

    /**
     * <a href="https://wiki.vg/index.php?title=Protocol&oldid=18186#Set_Container_Content">wiki</a>
     */
    private void onSendingWindowItems(PacketContainer packet) {
        List<ItemStack> items = packet.getItemListModifier().read(0);
        items.replaceAll(this::process);
        packet.getItemListModifier().write(0, items);
        ItemStack item = packet.getItemModifier().read(0);
        packet.getItemModifier().write(0, process(item));
    }

    /**
     * <a href="https://wiki.vg/index.php?title=Protocol&oldid=18186#Set_Container_Slot">wiki</a>
     */
    private void onSendingSetSlot(PacketContainer packet) {
        ItemStack item = packet.getItemModifier().read(0);
        packet.getItemModifier().write(0, process(item));
    }

    private ItemStack process(ItemStack item) {
        if (item == null) return null;
        RPGItem rpg = ItemManager.toRPGItem(item).orElse(null);
        if (rpg == null) return item;
        ItemStack copy = item.clone();
        if (rpg.getFakeItem().isAir()) {
            if (plugin.cfg.plAutoReplaceArmorMaterial && !materialToCheck.contains(item.getType().name())) return item;
            if (rpg.getItem().equals(item.getType())) return item;
            copy.setType(rpg.getItem());
        } else {
            if (rpg.getFakeItem().equals(item.getType())) return item;
            copy.setType(rpg.getFakeItem());
        }
        if (copy.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) copy.getItemMeta();
            meta.setColor(Color.fromRGB(rpg.getDataValue()));
            copy.setItemMeta(meta);
        }
        return copy;
    }
}
