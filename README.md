![Logo](https://media.forgecdn.net/attachments/123/815/red-protect-plus1.png)

[![Build Status](https://github.com/FabioZumbi12/RedProtect/actions/workflows/build-on-tag.yml/badge.svg)](https://github.com/FabioZumbi12/RedProtect/releases)

RedProtect is an area protection plugin focused on player-managed regions, giving users control over claiming and managing
their own protected areas without depending on staff to set them up.

## Links

- Discord: https://discord.gg/VHTwk53
- Wiki: https://github.com/FabioZumbi12/RedProtect/wiki
- Source: https://github.com/FabioZumbi12/RedProtect
- Releases (main plugin + add-ons): https://github.com/FabioZumbi12/RedProtect/releases/latest

## Available Versions

- Spigot: https://www.spigotmc.org/resources/redprotect.15841/
- Bukkit: http://dev.bukkit.org/bukkit-plugins/region-protect-plus/
- Sponge (legacy): https://ore.spongepowered.org/FabioZumbi12/RedProtect

## Add-ons

These add-ons extend RedProtect flags, functions and more. Downloads are available on the latest GitHub release.

- Killer Projectiles
  - How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/KillerProjectiles
  - Download: https://github.com/FabioZumbi12/RedProtect/releases/latest
- BuyRent Regions
  - How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/BuyRentRegion
  - Download: https://github.com/FabioZumbi12/RedProtect/releases/latest
- RedBackups
  - How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RedBackups
  - Download: https://github.com/FabioZumbi12/RedProtect/releases/latest
- RegionChat
  - How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RegionChat
  - Download: https://github.com/FabioZumbi12/RedProtect/releases/latest

## API Repository

RedProtect is hosted on Maven Central.

### Maven
```xml
<dependencies>
    <!-- Core is not needed but allow access to all region methods -->
    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Core</artifactId>
        <version>8.1.2-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <!-- We don't need any of the dependencies -->
                <groupId>*</groupId>
                <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Spigot</artifactId>
        <version>8.1.2-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <!-- We don't need any of the dependencies -->
                <groupId>*</groupId>
                <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Import Javadocs -->
    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Spigot</artifactId>
        <version>8.1.2-SNAPSHOT</version>
        <classifier>javadoc</classifier>
    </dependency> 
</dependencies>  
```

### Gradle
```
repositories {
    mavenCentral()
    maven { url = ' https://central.sonatype.com/repository/maven-snapshots/' } // Only for snapshots
}

dependencies {
    compileOnly ("io.github.fabiozumbi12.RedProtect:RedProtect-Core:8.1.2-SNAPSHOT"){ exclude(group = "*")} // Core is not needed but allow access to all region methods
    compileOnly ("io.github.fabiozumbi12.RedProtect:RedProtect-Spigot:8.1.2-SNAPSHOT"){ exclude(group = "*")}
}
```
