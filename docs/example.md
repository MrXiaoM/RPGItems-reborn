# API Examples

There are usage examples that helps you learn how to use RPGItems quickly.

## Expansions

You can install expansions into RPGItems to add your `Powers`, `Conditions` and `Triggers`.

The expansion is the same as other normal plugin. Just write as usual with RPGItems dependency.

```java
Trigger trigger = TODO();

@Override
public void onLoad() {
    // Please register power, conditions and triggers at onLoad
    PowerManager.registerPowers(/*plugin: */this, /*package: */"top.mrxiaom.rpgitemsext.powers");
    PowerManager.registerConditions(/*plugin: */this, /*package: */"top.mrxiaom.rpgitemsext.cond");
    Trigger.register(trigger, /*override: */true); // override should be true, or you may receive exception while reloading.
}
```

**DO NOT** register command in `plugin.yml`. It is an expansion loaded by RPGItems not PlugManX. It may cause your plugin can't startup.

## Add Any SubCommands You Want
```java
import think.rpgitems.power.RPGCommandReceiver;
        import think.rpgitems.utils.nyaacore.cmdreceiver.Arguments;
        import think.rpgitems.utils.nyaacore.cmdreceiver.ISubCommandInfo;
        
// add subcommand /rpgitem example <message>
void registerSubCommands() {
    PluginCommand pluginCommand = RPGItems.plugin.getCommand("rpgitem");
    if (pluginCommand != null && pluginCommand.getExecutor() instanceof RPGCommandReceiver command) {
        command.registerCommand(new ISubCommandInfo() {
            @Override
            public String getName() { return "example"; }
            @Override
            public void callCommand(CommandSender sender, Arguments args) {
                if (args.length() == 1) {
                    String s = args.nextString();
                    sender.sendMessage("Hello! " + s);
                }
            }
            @Override
            public List<String> callTabComplete(CommandSender sender, Arguments args) { return null; }

            @Override
            public boolean hasPermission(CommandSender sender) { return true; }
        }, /*override: */false);
    }
}
```

## LoreUpdateEvent Example

```java
import think.rpgitems.utils.nyaacore.ItemTagUtils;

class MyEventListener implements Listener {
    @EventHandler
    public void onLoreUpdate(LoreUpdateEvent e) {
        // Other plugin save bound owner into nbt, we read it in the expansion plugin
        String owner = ItemTagUtils.getString(event.item, "bind_owner").orElse(null);
        if (owner != null) {
            // add to dynamic item lore
            e.newLore.add("");
            e.newLore.add(ColorHelper.parseColor("&aBound to &2" + owner));
        }
    }
}
```

## Factor Definer

You may want some other entity attack you or be damaged with a factor to make your server more functionally. There is the solution.

For example, if we want a `Mythic` mob with a factor, we could add this into `onEnable`.

```java
import think.rpgitems.api.Factors;

@Override
public void onEnable() {
    // Once user reload RPGItems, we should register it.
    Factors.registerFactorDefiner(new IFactorDefiner() {
        @Override
        public @Nullable String define(LivingEntity entity) {
            ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
            return mob == null ? null : active.getType().getConfig().getString("Options.SweetRiceFactor", null);
        }
        @Override // default priority is 50, less priority execute first.
        public int priority() { return 49; }
    });
}
```
```yaml
ExampleMob:
  Type: ZOMBIE
  Health: 50
  Options:
    SweetRiceFactor: DEAD
```
