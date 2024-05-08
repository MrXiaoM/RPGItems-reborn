# RPGItems SweetRice Edition

The NyaaCat RPGItems-reloaded plugin continued from [NyaaCat/RPGItems-reloaded](https://github.com/NyaaCat/RPGItems-reloaded).

Take it easy. **NO ANY** dependency plugin is needed this time.

Recently, I targeted to make RPGItems more powerful and easy to use.  
Original plugin is not only hard to learn and understand how to use, but also there are very few choices of powers, conditions and triggers.  
That's the problem I want to solve.

Do not use Spigot, use Paper please.

## Fork

This is a personal fork. Only the branch `1.19` is supported. I add [LoreUpdateEvent](/src/main/java/think/rpgitems/event/LoreUpdateEvent.java) so it is friendly to *SoulBinding-kind* plugins. 
```grovvy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'top.mrxiaom:rpgitems:3.12.2:all'
}

```
I uploaded it to central. It is easy to depend it. :P

## LoreUpdateEvent Example

```java
class MyEventListener implements Listener {
    @EventHandler
    public void onLoreUpdate(LoreUpdateEvent e) {
        // think.rpgitems.utils.nyaacore.ItemTagUtils
        String owner = ItemTagUtils.getString(event.item, "bind_owner").orElse(null);
        if (owner != null) {
            e.newLore.add("");
            e.newLore.add(ColorHelper.parseColor("&aBound to &2" + owner));
        }
    }
}
```

## What's new 

* LoreUpdateEvent
* LoreUpdateEvent.Post (aim at edit NBT)
* ItemsLoadedEvent
* Gradient color support
* More friendly and developer comfortably help command
* More friendly Chinese translation with color
* Read-only mode
* BungeeCord notice read-only server to reload
* Register sub-command into `/rpgitem`

## Languages

Due to my works and study, I'm sorry that the `en_US` language file may no longer be supported.  
The old language file is available. But something new features are missing.  
If you want it, you can translate it from `zh_CN`.  
And also, PRs welcome.

## Accessible

We **will** publish the functional update and bug fixing. We **WON'T** publish the powers, triggers and conditions we made. They are packaged into an extension in our server.

## Resources

[Original Wiki](https://nyaacat.github.io/RPGItems-wiki/#/)
