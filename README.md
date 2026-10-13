<p align="center">
  <img src="https://media.forgecdn.net/attachments/123/815/red-protect-plus1.png" alt="RedProtect Logo" width="450" />
</p>

<p align="center">
  <a href="https://github.com/FabioZumbi12/RedProtect/releases"><img src="https://img.shields.io/github/v/release/FabioZumbi12/RedProtect?include_prereleases&label=release&color=red" alt="Latest Release" /></a>
  <a href="https://github.com/FabioZumbi12/RedProtect/actions/workflows/build-on-tag.yml"><img src="https://github.com/FabioZumbi12/RedProtect/actions/workflows/build-on-tag.yml/badge.svg" alt="Build Status" /></a>
  <a href="https://central.sonatype.com/artifact/io.github.fabiozumbi12.RedProtect/RedProtect-Spigot"><img src="https://img.shields.io/maven-central/v/io.github.fabiozumbi12.RedProtect/RedProtect-Spigot" alt="Maven Central" /></a>
  <a href="https://discord.gg/VHTwk53"><img src="https://img.shields.io/badge/Discord-Join%20Chat-5865F2?logo=discord&logoColor=white" alt="Discord" /></a>
  <a href="https://github.com/FabioZumbi12/RedProtect/blob/master/LICENSE"><img src="https://img.shields.io/github/license/FabioZumbi12/RedProtect" alt="License" /></a>
</p>

---

**RedProtect** is a high-performance, feature-rich area protection and anti-griefing plugin designed for player autonomy. It eliminates the need for staff members to manually claim regions with WorldGuard—players can create, customize, and manage their own claims effortlessly with fences or commands.

---

## 🌟 Key Features

* **Player-Centric Claiming:** Fast and intuitive region creation using fences and redstone, visual tools, or commands.
* **Hybrid & Modded Server Support:** Unmatched protection for Forge, NeoForge, and Fabric mod blocks, machines, pipes, inventories, and modded entities (Arclight, Mohist, KCauldron, Thermos).
* **Interactive Flag GUIs:**
  * **Mob Spawn GUI:** Toggle individual animal and monster spawns with auto-translated mob names via [TranslationAPI](https://www.spigotmc.org/resources/translationapi.70692/).
  * **Container Selector (Chest Flag):** Choose exactly which containers can be opened (Chests, Barrels, Shulker Boxes, Ender Chests, Furnaces, and mod blocks).
* **Push & Pull Flag:** Prevent players and hostile visitors from dragging livestock or players out of safe zones with fishing rods or knockback.
* **Border Visualization:** In-game particle borders and live client rendering with [WorldEditCUI (WECUI)](https://minecraft.curseforge.com/projects/worldeditcui-forge-edition-2).
* **Live Dynmap Integration:** Automatically renders 2D boundaries and 3D cuboids without external bridge plugins.
* **Economy Plots:** Buy and rent regions via signs with Vault economy integration.
* **Offline-Mode Security:** Anti-impersonation system preventing players from joining with casing variations of existing usernames.
* **Extensive Hook Support:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/), EssentialsX, Vault, SimpleClans, mcMMO, PvPManager, BossBarAPI, and more.

---

## 🔗 Quick Links

* 🌐 **SpigotMC Resource:** [spigotmc.org/resources/redprotect.15841](https://www.spigotmc.org/resources/redprotect.15841/)
* 📦 **GitHub Releases (Plugin & Add-ons):** [Releases](https://github.com/FabioZumbi12/RedProtect/releases/latest)
* 📖 **Documentation & Wiki:** [RedProtect Wiki](https://github.com/FabioZumbi12/RedProtect/wiki)
* 💬 **Discord Support:** [discord.gg/VHTwk53](https://discord.gg/VHTwk53)
* 🐞 **Issue Tracker:** [GitHub Issues](https://github.com/FabioZumbi12/RedProtect/issues)
* 🏗️ **CI Automated Builds:** [Jenkins Server](http://host.areaz12server.net.br:8081/job/RedProtect/)

---

## 🧩 Official Add-ons

RedProtect provides official modular add-ons to expand its functionality without bloating the core:

| Add-on | Description | Documentation | Download |
| :--- | :--- | :---: | :---: |
| **BuyRentRegion** | Buy and rent regions using signs hooked into Vault economy | [Docs](https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/BuyRentRegion) | [Download](https://github.com/FabioZumbi12/RedProtect/releases/latest) |
| **KillerProjectiles** | Configurable projectile damage flags for PvP arenas | [Docs](https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/KillerProjectiles) | [Download](https://github.com/FabioZumbi12/RedProtect/releases/latest) |
| **RedBackups** | Automatic backups of `.mca` world chunk files for claimed regions | [Docs](https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RedBackups) | [Download](https://github.com/FabioZumbi12/RedProtect/releases/latest) |
| **RegionChat** | Dedicated local chat channels exclusive to region members | [Docs](https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RegionChat) | [Download](https://github.com/FabioZumbi12/RedProtect/releases/latest) |

---

## 💻 Developer API

RedProtect is published to **Maven Central** and Sonatype Snapshots.

### Maven

```xml
<repositories>
    <!-- Snapshots repository (for development snapshots) -->
    <repository>
        <id>sonatype-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Core</artifactId>
        <version>8.2.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Spigot</artifactId>
        <version>8.2.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://central.sonatype.com/repository/maven-snapshots/' }
}

dependencies {
    compileOnly 'io.github.fabiozumbi12.RedProtect:RedProtect-Core:8.2.0-SNAPSHOT'
    compileOnly 'io.github.fabiozumbi12.RedProtect:RedProtect-Spigot:8.2.0-SNAPSHOT'
}
```

---

## 📜 License & Credits

* Originally created by **ikillforeyou**.
* Actively maintained and expanded by **FabioZumbi12**.
* Licensed under the [GNU General Public License v3.0 (GPL-3.0)](https://github.com/FabioZumbi12/RedProtect/blob/master/LICENSE).
