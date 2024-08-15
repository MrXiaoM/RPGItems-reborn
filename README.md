# RPGItems-reborn

[![Paper 1.8-1.21](https://img.shields.io/badge/Paper-1.8--1.21-blue)](https://img.shields.io/badge/Paper-1.8--1.21-blue)

The NyaaCat RPGItems-reloaded plugin continued from [NyaaCat/RPGItems-reloaded](https://github.com/NyaaCat/RPGItems-reloaded).

Active developing from [SweetRiceMC](https://www.pds.ink) developers team.

Take it easy. **NO ANY** hard-dependency plugin is needed this time. Install just one plugin is OK! And one plugin file supports multi Minecraft versions from `1.8` to `1.21`!

At least Java 11 is needed. Supports 1.14-1.21 currently.
PDC (PersistentDataContainer) since 1.14, so the plugin can't run on 1.8-1.13. We are trying to increase compatibility, target to support down to Minecraft 1.8 but Java 11. There is no way to downgrade java language version anymore.

Recently, I targeted to make RPGItems more powerful and easy to use.  
Original plugin is not only hard to learn and understand how to use, but also there are very few choices of powers, conditions and triggers.  
That's the problem I want to solve.

**CURRENT VERSION DO NOT COMPATIBLE WITH ANY OLD VERSION EXTENSIONS. BACK TO [HERE](https://github.com/MrXiaoM/RPGItems-reborn/tree/1b83b4d4b004aab6c7c33b837d0d42d615f7b2cd) IF YOU WANT TO USE OLD EXTENSIONS.**

We work well on Spigot, but we recommend you to use Paper.  
Spigot lost these features:
+ Trigger `ARMOR` and `ARMOR_UPDATE`

There are some links you may be finding.

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

| Minecraft Version | NMS Version                                    |
|-------------------|------------------------------------------------|
| `1.8.X`           | `v1_8_R1`, `v1_8_R2`, `v1_8_R3`                |
| `1.9.X`           | `v1_9_R1`, `v1_9_R2`                           |
| `1.10.X`-`1.12.X` | `v1_10_R1`, `v1_11_R1`, `v1_12_R1`             |
| `1.13.X`          | `v1_13_R1`, `v1_13_R2`                         |
| `1.14.X`-`1.15.X` | `v1_14_R1`, `v1_15_R1`                         |
| `1.16.1`          | `v1_16_R1`, `v1_16_R2`, `v1_16_R3`             |
| `1.17.X`          | `v1_17_R1`                                     |
| `1.18.X`          | `v1_18_R1`, `v1_18_R2`                         |
| `1.19.X`          | `v1_19_R1`, `v1_19_R2`, `v1_19_R3`             |
| `1.20.X`          | `v1_20_R1`, `v1_20_R2`, `v1_20_R3`, `v1_20_R4` |
| `1.21`            | `v1_21_R1`                                     |

We have tested it in `1.19.4 (v1_19_R3)` and `1.20.4 (v1_20_R3)`.

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
* (ProtocolLib needed) Send fake item to client in order to balance with vanilla weapons/armors.  
  (e.g. `NETHERITE_HELMET` actually in server but display fake `LEATHER_HELMET` in client)
* Run power would cost per-player magic value but not per-item durability.

## Languages

Due to my works and study, I'm sorry that the `en_US` language file may no longer be supported.  
The old language file is available. But something new features are missing.  
If you want it, you can translate it from `zh_CN`.  
And also, PRs welcome.

## Accessible

We **will** publish the functional update and bug fixing. We **WON'T** publish the powers, triggers and conditions we made. They are packaged into an extension in our server.

## Developer

Build the plugin with `Java 21` via `shadowJar` task.  
Don't worry, the built jar is target to `Java 11`.
```shell
./gradlew shadowJar
```
