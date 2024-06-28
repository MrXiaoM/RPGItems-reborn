# RPGItems-reborn

[![Paper 1.17.1-1.20.4](https://img.shields.io/badge/Paper-1.17.1--1.20.4-blue)](https://img.shields.io/badge/Paper-1.17.1--1.20.4-blue)

The NyaaCat RPGItems-reloaded plugin continued from [NyaaCat/RPGItems-reloaded](https://github.com/NyaaCat/RPGItems-reloaded).

Active developing from [SweetRiceMC](https://www.pds.ink) developers team.

Take it easy. **NO ANY** hard-dependency plugin is needed this time. Install just one plugin is OK! And one plugin file supports multi Minecraft versions from `1.17` to `1.20.4`!

Recently, I targeted to make RPGItems more powerful and easy to use.  
Original plugin is not only hard to learn and understand how to use, but also there are very few choices of powers, conditions and triggers.  
That's the problem I want to solve.

Do not use Spigot, use Paper please.

+ [Wiki (WIP)](https://rpgitems.mcio.dev)
+ [Original Wiki](https://nyaacat.github.io/RPGItems-wiki/#/)

## Developer Support

```grovvy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'top.mrxiaom:rpgitems:3.12.2:all'
}

```
I uploaded it to central. It is easy to depend it. :P

New version is currently testing. The version will be `4.0.0` when we release it.

## Support Versions

| Minecraft Version | NMS Version |
|-------------------|-------------|
| `1.17`, `1.17.1`  | `v1_17_R1`  |
| `1.18`, `1.18.1`  | `v1_18_R1`  |
| `1.18.2`          | `v1_18_R2`  |
| `1.19.4`          | `v1_19_R3`  |
| `1.20`, `1.20.1`  | `v1_20_R1`  |
| `1.20.2`          | `v1_20_R2`  |
| `1.20.4`          | `v1_20_R3`  |
| `1.20.6` (WIP)    | `v1_20_R4`  |


## What's new 

* LoreUpdateEvent
* LoreUpdateEvent.Post (aim at edit NBT)
* ItemsLoadedEvent
* Gradient color support
* More friendly and developer comfortably help command
* More friendly Chinese translation with color
* Read-only mode
* Redirect `items` data folder
* BungeeCord notice read-only server to reload
* Register sub-command into `/rpgitem`
* High playability Factor system
* More interesting preset `powers`, `conditions` and `triggers`
* Link with more plugins such as MythicMobs, ItemsAdder and so on!
* Multi Minecraft version in just ONE jar! Not specific `1.XX.X-3.12.X` anymore.
* Hot load support (experimental function with `PlugManX`)
* Allow using a hoe item to farm a land (Add `noPlace` into comment to disallow).
* (ProtocolLib needed) Send fake item to client in order to balance with vanilla weapons/armors. (e.g. `NETHERITE_HELMET` actually in server but display fake `LEATHER_HELMET` in client)
* Run power would cost per-player magic value but not per-item durability.

## Languages

Due to my works and study, I'm sorry that the `en_US` language file may no longer be supported.  
The old language file is available. But something new features are missing.  
If you want it, you can translate it from `zh_CN`.  
And also, PRs welcome.

## Accessible

We **will** publish the functional update and bug fixing. We **WON'T** publish the powers, triggers and conditions we made. They are packaged into an extension in our server.
