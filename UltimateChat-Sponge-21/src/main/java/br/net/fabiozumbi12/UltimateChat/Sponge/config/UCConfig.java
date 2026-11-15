/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Altered for SpongeAPI 21 by AlmuraDev.
 */

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.ProtectionsCategory.Chatprotection;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

/**
 * Reads and writes config.conf, protections.conf and the channels folder. The files and keys are the same as in the
 * SpongeAPI 7 versions, so existing configs load unchanged.
 */
public class UCConfig {

    private static final String HEADER = ""
            + "Uchat configuration file\n"
            + "Author: FabioZumbi12\n"
            + "We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
            + "------------------------------------------------------------------------\n"
            + "\n"
            + "Tags is where you can customize what will show on chat, on hover or on click on tag.\n"
            + "To add a tag, you can copy an existent and change the name and the texts.\n"
            + "After add and customize your tag, put the tag name on 'general > default-tag-builder'.\n"
            + "------------------------------------------------------------------------\n"
            + "###### Do not rename the tags 'playername', 'nickname' and 'message' ########\n"
            + "############ or the plugin will not parse the tag correctly! ################\n"
            + "------------------------------------------------------------------------\n"
            + "\n"
            + "Available replacers:\n"
            + " - {world}: Replaced by sender world;\n"
            + " - {message}: Message sent by player;\n"
            + " - {playername}: The name of player;\n"
            + " - {nickname}: The nickname of player. If not set, will show realname;\n"
            + " - {nick-symbol}: The symbol to use before nickname;\n"
            + " - {ch-name}: Channel name;\n"
            + " - {ch-alias}: Channel alias;\n"
            + " - {ch-color}: Channel color;\n"
            + " - {balance}: Get the sender money;\n"
            + " - {hand-type}: Item type;\n"
            + " - {hand-name}: Item name;\n"
            + " - {hand-amount}: Item quantity;\n"
            + " - {hand-lore}: Item description (lore);\n"
            + " - {hand-durability}: Item durability;\n"
            + " - {hand-enchants}: Item enchantments;\n"
            + " - {time-now}: Prints the time now on server;\n"
            + "\n"
            + "Permissions Group Options:\n"
            + " - {option_group}: Get the group name;\n"
            + " - {option_prefix}: Get the prefix of group (if set);\n"
            + " - {option_all_prefixes}: Get and show all prefixes the player has;\n"
            + " - {option_suffix}: Get the suffix of group (if set);\n"
            + " - {option_all_suffixes}:  Get and show all suffixes the player has;\n"
            + " - {option_display_name}: Get the custom name of group (if set);\n"
            + " - {option_<key option>}: Get some custom key option from your group in permissions;\n"
            + "\n"
            + "Permissions Player Options:\n"
            + " - {player_option_prefix}: Get the prefix of player (if set);\n"
            + " - {player_option_suffix}: Get the suffix of player (if set);\n"
            + " - {player_option_<key option>}: Get some custom key option from your permissions like {player_option_viptime} or what you want;\n"
            + "\n"
            + "Vanilla Chat:\n"
            + " - {chat_header}: Get the header of chat;\n"
            + " - {chat_body}: Get the body of chat;\n"
            + " - {chat_footer}: Get the footer of chat;\n"
            + " - {chat_all}: Get all default formats;\n"
            + "\n"
            + "Worlds are named by their key (\"minecraft:overworld\") or its last part (\"overworld\").\n"
            + "Jedis, Bungee and MCClans are not supported on SpongeAPI 21; their settings are kept but ignored.\n";

    private static final String CHANNEL_COMMENT = ""
            + "###################################################\n"
            + "############## Channel Configuration ##############\n"
            + "###################################################\n"
            + "\n"
            + "This is the channel configuration.\n"
            + "You can change and copy this file to create as many channels you want.\n"
            + "This is the default options:\n"
            + "\n"
            + "name: Global - The name of channel.\n"
            + "alias: g - The alias to use the channel\n"
            + "across-worlds: true - Send messages of this channel to all worlds?\n"
            + "distance: 0 - If across worlds is false, distance to receive this messages.\n"
            + "color: &b - The color of channel\n"
            + "tag-builder: ch-tags,world,clan-tag,marry-tag,group-prefix,nickname,group-suffix,message - Tags of this channel\n"
            + "need-focus: false - Player can use the alias or need to use '/ch g' to use this channel?\n"
            + "canLock: true - Change if the player can use /<channel> to lock on channel.\n"
            + "receivers-message: true - Send chat messages like if no player near to receive the message?\n"
            + "cost: 0.0 - Cost to player use this channel.\n"
            + "use-this-builder: false - Use this tag builder or use the 'config.yml' tag-builder?\n"
            + "\n"
            + "channelAlias - Use this channel as a command alias.\n"
            + "  enable: true - Enable this execute a command alias?\n"
            + "  sendAs: player - Send the command alias as 'player' or 'console'?\n"
            + "  cmd: '' - Command to send on every message send by this channel.\n"
            + "available-worlds - Worlds and only this world where this chat can be used and messages sent/received.\n"
            + "discord:\n"
            + "  mode: NONE - The options are NONE, SEND, LISTEN, BOTH. If enabled and token code set and the channel ID matches with one discord channel, will react according the choosen mode.\n"
            + "  hover: &3Discord Channel: &a{dd-channel}\n"
            + "  format-to-mc: {ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: \n"
            + "  format-to-dd: :thought_balloon: **{sender}**: {message} \n"
            + "  allow-server-cmds: false - Use this channel to send commands from discord > minecraft.\n"
            + "  channelID: '' - The IDs of your Discord Channels. Enable debug on your discord to get the channel ID.\n"
            + "  Note: You can add more than one discord id, just separate by \",\" like: 13246579865498,3216587898754\n";

    private final File defConfig = new File(UChat.get().configDir(), "config.conf");
    private final File defProt = new File(UChat.get().configDir(), "protections.conf");

    private CommentedConfigurationNode configRoot;
    private HoconConfigurationLoader cfgLoader;
    private MainCategory root = new MainCategory();
    private CommentedConfigurationNode protsRoot;
    private HoconConfigurationLoader protLoader;
    private ProtectionsCategory protections = new ProtectionsCategory();

    public UCConfig() throws IOException {
        UChat.get().getLogger().info("-> Config module");
        try {
            Files.createDirectories(UChat.get().configDir().toPath());

            /*--------------------- config.conf ---------------------------*/
            cfgLoader = HoconConfigurationLoader.builder()
                    .path(defConfig.toPath())
                    .defaultOptions(o -> o.shouldCopyDefaults(true).header(HEADER))
                    .build();
            configRoot = cfgLoader.load();
            MainCategory loaded = configRoot.get(MainCategory.class);
            if (loaded != null) {
                root = loaded;
            }

            //update old configs
            double update = 0;
            if (configRoot.node("_config-version").getDouble() < 1.2D) {
                root.config_version = 1.2D;
                configRoot.node("debug-messages").set(null);
                update = 1.2D;
            }
            if (update > 0) {
                UChat.get().getLogger().warning("Configuration updated to " + update);
            }

            /*--------------------- protections.conf ---------------------------*/
            protLoader = HoconConfigurationLoader.builder()
                    .path(defProt.toPath())
                    .defaultOptions(o -> o.shouldCopyDefaults(true))
                    .build();
            protsRoot = protLoader.load();
            ProtectionsCategory loadedProt = protsRoot.get(ProtectionsCategory.class);
            if (loadedProt != null) {
                protections = loadedProt;
            }
        } catch (ConfigurateException e) {
            UChat.get().getLogger().severe("Could not load the UltimateChat configuration", e);
        }

        /* Load Channels */
        loadChannels();

        //----------------------------------------------------------------------------------------//
        save();
        UChat.get().getLogger().info("All configurations loaded!");
    }

    /**
     * Adds a default-channel entry for each world that has none (worlds only exist once the server has started).
     */
    public void addWorlds(Collection<ServerWorld> worlds) {
        boolean changed = false;
        for (ServerWorld w : worlds) {
            changed |= addWorld(w);
        }
        if (changed) {
            save();
        }
    }

    public boolean addWorld(ServerWorld w) {
        Map<String, MainCategory.WorldInfo> map = root.general.default_channels.worlds;
        if (map.containsKey(UCUtil.worldKey(w)) || map.containsKey(UCUtil.worldName(w))) {
            return false;
        }
        map.put(UCUtil.worldKey(w), new MainCategory.WorldInfo(root.general.default_channels.default_channel, false));
        return true;
    }

    public MainCategory.WorldInfo worldInfo(ServerWorld w) {
        Map<String, MainCategory.WorldInfo> map = root.general.default_channels.worlds;
        MainCategory.WorldInfo info = map.get(UCUtil.worldKey(w));
        return info != null ? info : map.get(UCUtil.worldName(w));
    }

    public MainCategory root() {
        return this.root;
    }

    public Chatprotection protections() {
        return this.protections.chat_protection;
    }

    private static HoconConfigurationLoader channelLoader(File file) {
        return HoconConfigurationLoader.builder().path(file.toPath()).build();
    }

    private void loadChannels() throws IOException {
        File chfolder = new File(UChat.get().configDir(), "channels");

        if (!chfolder.exists()) {
            chfolder.mkdir();
            UChat.get().getLogger().info("Created folder: " + chfolder.getPath());
        }

        if (UChat.get().getChannels() == null) {
            UChat.get().setChannels(new HashMap<>());
        }

        File[] listOfFiles = chfolder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            //create default channels
            writeDefaultChannel(new File(chfolder, "global.conf"), "Global", "g", "&2", true, null, null);
            writeDefaultChannel(new File(chfolder, "local.conf"), "Local", "l", "&e", null, false, 40);
            writeDefaultChannel(new File(chfolder, "admin.conf"), "Admin", "ad", "&b", true, null, null);
            listOfFiles = chfolder.listFiles();
        }

        Arrays.sort(listOfFiles);
        for (File file : listOfFiles) {
            if (file.getName().endsWith(".conf")) {
                CommentedConfigurationNode channel = channelLoader(file).load();

                Map<String, Object> chProps = new HashMap<>();
                channel.childrenMap().forEach((key, value) -> {
                    if (value.isMap()) {
                        String rkey = key.toString();
                        for (Entry<Object, CommentedConfigurationNode> vl : value.childrenMap().entrySet()) {
                            chProps.put(rkey + "." + vl.getKey(), vl.getValue().raw());
                        }
                    } else {
                        chProps.put(key.toString(), value.raw());
                    }
                });

                UCChannel ch = new UCChannel(chProps);
                addChannel(ch);
            }
        }
    }

    private static void writeDefaultChannel(File file, String name, String alias, String color, Boolean jedis, Boolean acrossWorlds,
                                            Integer distance) throws ConfigurateException {
        HoconConfigurationLoader loader = channelLoader(file);
        CommentedConfigurationNode channel = loader.load();
        channel.node("name").set(name);
        channel.node("alias").set(alias);
        channel.node("color").set(color);
        if (jedis != null) channel.node("jedis").set(jedis);
        if (acrossWorlds != null) channel.node("across-worlds").set(acrossWorlds);
        if (distance != null) channel.node("distance").set(distance);
        loader.save(channel);
    }

    public void delChannel(UCChannel ch) {
        for (Entry<List<String>, UCChannel> ch0 : UChat.get().getChannels().entrySet()) {
            if (ch0.getValue().equals(ch)) {
                UChat.get().getChannels().remove(ch0.getKey());
                break;
            }
        }
        File defch = new File(UChat.get().configDir(), "channels" + File.separator + ch.getName().toLowerCase() + ".conf");
        if (defch.exists()) {
            defch.delete();
        }
    }

    public void addChannel(UCChannel ch) throws IOException {
        File defch = new File(UChat.get().configDir(), "channels" + File.separator + ch.getName().toLowerCase() + ".conf");

        HoconConfigurationLoader channelManager = channelLoader(defch);
        CommentedConfigurationNode chFile = channelManager.load();

        chFile.node("across-worlds").comment(CHANNEL_COMMENT);

        for (Entry<Object, Object> prop : ch.getProperties().entrySet()) {
            chFile.node((Object[]) prop.getKey().toString().split("\\.")).raw(prop.getValue());
        }
        channelManager.save(chFile);

        UCChannel existing = UChat.get().getChannel(ch.getName());
        if (existing != null) {
            ch.setMembers(existing.getMembers());
            UChat.get().getChannels().values().remove(existing);
        }
        UChat.get().getChannels().put(Arrays.asList(ch.getName().toLowerCase(), ch.getAlias().toLowerCase()), ch);
    }

    public String[] getDefBuilder() {
        return root.general.default_tag_builder.replace(" ", "").split(",");
    }

    public List<String> getChCmd() {
        return Arrays.asList(root.general.channel_cmd_aliases.replace(" ", "").split(","));
    }

    public List<String> getBroadcastAliases() {
        return Arrays.asList(root.broadcast.aliases.replace(" ", "").split(","));
    }

    public List<String> getTellAliases() {
        return Arrays.asList((root.tell.cmd_aliases + ",r").replace(" ", "").split(","));
    }

    public List<String> getMsgAliases() {
        return Arrays.asList(root.general.umsg_cmd_aliases.replace(" ", "").split(","));
    }

    public void save() {
        try {
            if (configRoot != null) {
                // Configurate 4 reads a missing list as an empty one; write those back as missing, as before
                for (TagsCategory tag : root.tags.values()) {
                    if (tag.hover_messages != null && tag.hover_messages.isEmpty()) tag.hover_messages = null;
                    if (tag.show_in_worlds != null && tag.show_in_worlds.isEmpty()) tag.show_in_worlds = null;
                    if (tag.hide_in_worlds != null && tag.hide_in_worlds.isEmpty()) tag.hide_in_worlds = null;
                }
                configRoot.set(MainCategory.class, root);
                cfgLoader.save(configRoot);
            }
            if (protsRoot != null) {
                protsRoot.set(ProtectionsCategory.class, protections);
                protLoader.save(protsRoot);
            }
        } catch (ConfigurateException e) {
            UChat.get().getLogger().severe("Could not save the UltimateChat configuration", e);
        }
    }
}
