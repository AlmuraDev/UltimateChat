![logo](https://media-elerium.cursecdn.com/attachments/123/595/uchat.png)  
Ultimate and advanced Chat for Spigot, Sponge and BungeeCoord

## AlmuraDev 4.0 branch: SpongeAPI 21
This branch adds **UltimateChat 2.0.0** for SpongeNeo (Minecraft 26.3, SpongeAPI 21), used by Almura 4.0. It lives in
[`UltimateChat-Sponge-21`](UltimateChat-Sponge-21/README.md), a separate Gradle build. The Maven modules below are
unchanged and still build the SpongeAPI 5-7, Spigot, Bungee and Velocity versions.

```
cd UltimateChat-Sponge-21
gradlew build
```

It needs JDK 25. The plugin jar is `UltimateChat-Sponge-21/build/libs/UltimateChat-2.0.0.jar`; put it in `mods/plugins`.

- **Included:** channels, tags, private messages, chat spy, mute and temp mute, ignore, message toggle, broadcast,
  `/umsg`, the chat protections, all `/uchat` subcommands, and the Discord relay (JDA 6, bundled in the jar).
- **Not ported:** Jedis, Bungee, MCClans, PlaceholderAPI, Nucleus nicknames and the Discord account sync. Their
  settings are kept in the config and ignored.
- **Config:** the existing `config.conf`, `protections.conf`, `channels/*.conf` and lang files load unchanged, from
  `config/ultimatechat` (or `config/UltimateChat` if only that exists).

See [`UltimateChat-Sponge-21/README.md`](UltimateChat-Sponge-21/README.md) for the differences from the SpongeAPI 7
version, such as command aliases needing a restart and the Discord bot needing the Message Content intent.

The original project is [FabioZumbi12/UltimateChat](https://github.com/FabioZumbi12/UltimateChat); the sections below
are its README.

## Available versions:
Spigot: [https://www.spigotmc.org/resources/uchat.23767/](https://www.spigotmc.org/resources/uchat.23767/)  
Bukkit: [https://dev.bukkit.org/projects/uchat](https://dev.bukkit.org/projects/uchat)  
Sponge: [https://ore.spongepowered.org/FabioZumbi12/UltimateChat](https://ore.spongepowered.org/FabioZumbi12/UltimateChat)  

## Source:
The source is available on GitHub: [https://github.com/FabioZumbi12/UltimateChat](https://github.com/FabioZumbi12/UltimateChat)  

## Dev Builds:
Dev builds on Jenkins: [![Build Status](http://host.areaz12server.net.br:8081/buildStatus/icon?job=UltimateChat)](http://host.areaz12server.net.br:8081/job/UltimateChat/)

## UltimateChat WIKI:
Check WIKI for:  
* Commands
* Permissions
* Channels
* BungeeCoord
* Redis Server Messaging
* Discord Configuration
* API Usage
* ...and more

## APIs:

**Repository:**  
```xml
<repositories> 
    <repository> 
        <id>ultimatechat-repo</id> 
        <url>https://raw.githubusercontent.com/FabioZumbi12/UltimateChat/mvn-repo/</url> 
    </repository> 
</repositories>
```


**UltimateChat API:**  
```xml
<dependencies>
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-[version]</artifactId>
        <version>LATEST</version>
        <scope>provided</scope>
    </dependency> 
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-[version]</artifactId>
        <version>LATEST</version>
        <classifier>javadoc</classifier>
    </dependency>
</dependencies>
```
Check ultimatechat version here: https://github.com/FabioZumbi12/UltimateChat/tree/mvn-repo/br/net/fabiozumbi12/UltimateChat  


**UltimateFancy API:**  
You need to shade UltimateFancy into your plugin or compile the jar.  
```xml
<dependencies>
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateFancy</artifactId>
        <version>LATEST</version>
        <scope>compile</scope>
    </dependency> 
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateFancy</artifactId>
        <version>LATEST</version>
        <classifier>javadoc</classifier>
    </dependency>
</dependencies>
```

## Wiki:
UltimateChat WIKI: [Click Here!](https://github.com/FabioZumbi12/UltimateChat/wiki)