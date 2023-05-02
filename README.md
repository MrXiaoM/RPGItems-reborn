# RPGItems SweetRice Fork

The RPGItems2 plugin continued from [TheCreeperOfRedstone/RPG-Items-2](https://github.com/TheCreeperOfRedstone/RPG-Items-2)

LangUtils is needed and no longer need to install NyaaCore.

## Fork

This is a personal fork. Only the branch `1.16` is supported. I add [LoreUpdateEvent](/src/main/java/think/rpgitems/event/LoreUpdateEvent.java) so it is friendly to *SoulBinding-kind* plugins. 
```grovvy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'top.mrxiaom:rpgitems:3.10-c985dd:all'
}

```
I uploaded it to central. It is easy to depend it. :P

## LoreUpdateEvent Example

```java
class MyEventListener implements Listener {
    @EventHandler
    public void onLoreUpdate(LoreUpdateEvent e) {
        // cat.nyaa.nyaacore.utils.ItemTagUtils
        String owner = ItemTagUtils.getString(event.item, "bind_owner").orElse(null);
        if (owner != null) {
            e.newLore.add("");
            e.newLore.add(ColorHelper.parseColor("&Bound to &2" + owner));
        }
    }
}
```

## What's new 

* LoreUpdateEvent
* LoreUpdateEvent.Post (aim at edit NBT)
* Gradient color support
* More friendly and developer comfortably help command
* More friendly Chinese translation with color

## Accessible

We **will** publish the functional update and bug fixing. We **WON'T** publish the powers, triggers and conditions made by us. They are packaged into an extension in our server.

## Resources

[Original Wiki](https://nyaacat.github.io/RPGItems-wiki/#/)
