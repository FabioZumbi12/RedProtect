![Logo](https://media.forgecdn.net/attachments/123/815/red-protect-plus1.png)  
RedProtect World is an area protection plugin made for users. No longer do you need to have an admin worldguard a region
for you. The user can take care of themselves now.

Chat with the developer online on Discord: https://discord.gg/VHTwk53

## WIKI

See the WIKI for Help with commands, permissions and all features: https://github.com/FabioZumbi12/RedProtect/wiki

## Available Versions:

Spigot: https://www.spigotmc.org/resources/redprotect.15841/  
Bukkit: http://dev.bukkit.org/bukkit-plugins/region-protect-plus/  
Sponge(legacy): https://ore.spongepowered.org/FabioZumbi12/RedProtect

## RedProtect Add-ons:

These add-ons extend RedProtect flags, functions and more.

### Killer Projectiles:

> How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/KillerProjectiles  
> Download: https://github.com/FabioZumbi12/RedProtect/tree/master/add-ons

### BuyRent Regions:

> How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/BuyRentRegion  
> Download: https://github.com/FabioZumbi12/RedProtect/tree/master/add-ons

### RedBackups:

> How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RedBackups  
> Download: https://github.com/FabioZumbi12/RedProtect/tree/master/add-ons

### RegionChat:

> How to use: https://github.com/FabioZumbi12/RedProtect/tree/master/Addons/RegionChat  
> Download: https://github.com/FabioZumbi12/RedProtect/tree/master/add-ons

## Source:

The source is available on GitHub: https://github.com/FabioZumbi12/RedProtect

## Dev builds:

Available on
jenkins: [![Build Status](http://host.areaz12server.net.br:8081/buildStatus/icon?job=RedProtect)](http://host.areaz12server.net.br:8081/job/RedProtect/)

## API repository:

**Repository:**  
RedProtect is hosted on Maven Central  
### Maven
```xml
<dependencies>
    <!-- Core is not needed but allow access to all region methods -->
    <dependency>
        <groupId>io.github.fabiozumbi12.RedProtect</groupId>
        <artifactId>RedProtect-Core</artifactId>
        <version>8.1.1</version>
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
        <version>8.1.1</version>
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
        <version>8.1.1</version>
        <classifier>javadoc</classifier>
    </dependency> 
</dependencies>  
```

### Gradle:
```
repositories {
    mavenCentral()
    maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' } // Only for snapshots
}

dependencies {
    compileOnly ("io.github.fabiozumbi12.RedProtect:RedProtect-Core:8.1.1"){ exclude(group: "*") } // Core is not needed but allow access to all region methods
    compileOnly ("io.github.fabiozumbi12.RedProtect:RedProtect-Spigot:8.1.1"){ exclude(group: "*") }
}
```