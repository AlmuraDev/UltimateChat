# UltimateChat for SpongeAPI 21

UltimateChat 2.0 for SpongeNeo (Minecraft 26.3, SpongeAPI 21), ported from the SpongeAPI 5-7 plugin in
`UltimateChat-Sponge-56`. It is a separate Gradle build; the Maven modules are untouched.

Build with JDK 25:

```
gradlew build
```

The plugin jar is `build/libs/UltimateChat-<version>.jar`; put it in `mods/plugins`.

## What is in it

Channels, tags, private messages (`/tell` and its aliases, `/r`), chat spy, mute and temp mute, ignore, message toggle,
broadcast, `/umsg`, the chat protections (anti-spam, censor, anti-IP, caps, flood, enhancement) and all `/uchat`
subcommands, plus the Discord relay (JDA 6, shaded into the jar).

Not ported: Jedis, Bungee, MCClans, PlaceholderAPI, Nucleus nicknames (the display name is used instead) and the Discord
account sync. Their settings stay in the config and are ignored.

## Config

It reads the same files as before (`config.conf`, `protections.conf`, `channels/*.conf`, `lang*.properties`) from
`config/ultimatechat`, or `config/UltimateChat` if only that one exists.

- Worlds are named by key (`minecraft:overworld`) or its last part (`overworld`); entries for new worlds are added to
  `general.default-channels.worlds` on start.
- The 1.12 mention sound name `block.note.pling` is read as `block.note_block.pling`.

## Differences from the SpongeAPI 7 version

- SpongeAPI 21 registers commands only when the server starts. Changes to command aliases, or turning tell or
  broadcast on or off, need a restart. Channels made with `/uchat newchannel` work at once, but their aliases are
  tab-completed only after a restart.
- The Discord bot needs the **Message Content** intent turned on in the Discord developer portal, or messages from
  Discord arrive empty.
- Links in a chat message only get a click on the message itself, not on every tag in the line.
- Private-message spy follows `uchat.chat-spy.bypass` like channel spy: the old version showed every private message to
  players with that permission.
