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

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {

    @Setting("_config-version")
    public Double config_version = 1.0;
    @Setting("debug")
    public DebugCat debug = new DebugCat();
    @Comment("Available languages: EN-US, PT-BR, FR-FR, FR-ES, HU-HU, RU, SP-ES, ZH-CN and KO-KR")
    @Setting("language")
    public String language = "EN-US";
    // jedis
    @Comment("Jedis configuration.\nUse Jedis to send messages between other servers running Jedis.\nConsider a replecement as Bungeecoord.")
    @Setting("jedis")
    public JedisCat jedis = new JedisCat();
    //discord
    @Comment("Enable the two way chat into discord and minecraft.\nGenerate your bot token following this instructions: https://goo.gl/utfRRv")
    @Setting("discord")
    public DiscordCat discord = new DiscordCat();
    //mention
    @Comment("Use mentions on chat to change the player name color and play a sound on mention.")
    @Setting("mention")
    public MentionCat mention = new MentionCat();
    //api
    @Comment("API configurations.")
    @Setting("api")
    public ApiCat api = new ApiCat();
    //general
    @Comment("General settings.")
    @Setting("general")
    public GeneralCat general = new GeneralCat();
    //tell
    @Setting("tell")
    public TellCat tell = new TellCat();
    @Setting("broadcast")
    public BroadcastCat broadcast = new BroadcastCat();
    @Comment("Enable hook with other plugins here. Only enable if installed.")
    @Setting("hooks")
    public HooksCat hooks = new HooksCat();
    @Comment("This is where you will create as many tags you want.\n"
            + "You can use the tag \"custom-tag\" as base to create your own tags.\n"
            + "When finish, get the name of your tag and put on \"general.default-tag-build\" \n"
            + "or on channel builder on \"channels\" folder.")
    @Setting("tags")
    public Map<String, TagsCategory> tags = defaultTags();
    //BungeeCoord
    @Setting("bungee")
    public BungeeCat bungee = new BungeeCat();

    public MainCategory() {
    }

    private Map<String, TagsCategory> defaultTags() {
        Map<String, TagsCategory> myMap = new HashMap<>();
        myMap.put("prefix", new TagsCategory("{option_prefix}", null, Collections.singletonList("&3Rank: &f{option_display_name}"), null, null, null, null));
        myMap.put("nickname", new TagsCategory("{nick-symbol}{nickname}", null, Arrays.asList("&3Player: &f{playername}", "&3Money: &7{balance}"), null, null, null, null));
        myMap.put("playername", new TagsCategory("{playername}", null, Arrays.asList("&3Player: &f{playername}", "&3Money: &7{balance}"), null, null, null, null));
        myMap.put("suffix", new TagsCategory("{option_suffix}", null, null, null, null, null, null));
        myMap.put("world", new TagsCategory("&7[{world}&7]&r", null, Collections.singletonList("&7Sent from world &8{world}"), null, null, null, null));
        myMap.put("message", new TagsCategory("{message}", null, null, null, null, null, null));
        myMap.put("ch-tags", new TagsCategory("{ch-color}[{ch-alias}]&r", "ch {ch-alias}", Arrays.asList("&3Channel name: {ch-color}{ch-name}", "&bClick to join this channel"), null, null, null, null));
        myMap.put("admin-chat", new TagsCategory("&b[&r{playername}&b]&r: &b", null, null, null, null, null, null));
        myMap.put("custom-tag", new TagsCategory("&7[&2MyTag&7]", "say I created an awesome tag!", Collections.singletonList("You discovered me :P"), "any-name-perm.custom-tag", Collections.singletonList("world-show"), Collections.singletonList("world-hide"), "www.google.com"));
        myMap.put("vanilla-chat", new TagsCategory("{chat_header}{chat_body}", null, null, null, null, null, null));
        myMap.put("jedis", new TagsCategory("{server-id}", null, Arrays.asList("&7Server: {jedis-id}", "&cChange me on configuration!"), null, null, null, null));
        return myMap;
    }

    @ConfigSerializable
    public static class BungeeCat {

        @Setting("server_id")
        public String server_id = "Sponge";
        @Setting("enable")
        public boolean enable = false;
    }

    //debug
    @ConfigSerializable
    public static class DebugCat {

        @Setting("messages")
        public boolean messages = false;
        @Setting("timings")
        public boolean timings = false;
    }

    @ConfigSerializable
    public static class JedisCat {

        @Setting("enable")
        public boolean enable = false;
        @Setting("server-id")
        public String server_id = "&e-ChangeThis-&r ";
        @Setting("ip")
        public String ip = "localhost";
        @Setting("port")
        public int port = 6379;
        @Setting("pass")
        public String pass = "";
    }

    @ConfigSerializable
    public static class DiscordCat {

        @Setting("use")
        public boolean use = false;
        @Setting("update-status")
        public boolean update_status = true;
        @Comment("The default status of bot. Available status: DEFAULT, LISTENING, WATCHING and STREAMING")
    @Setting("game-type")
        public String game_type = "DEFAULT";
        @Comment("If game-type = STREAMING, set the twitch url.")
    @Setting("twitch")
        public String twitch = "";
        @Setting("token")
        public String token = "";
        @Comment("Set your vanish plugin permissions here to do not announce player join/leave players with this permission.")
    @Setting("vanish-perm")
        public String vanish_perm = "nucleus.vanish.onlogin";
        @Comment("Channel id to send server start/stop and player join/leave messages")
    @Setting("log-channel-id")
        public String log_channel_id = "";
        @Comment("Channel id to spy private messages")
    @Setting("tell-channel-id")
        public String tell_channel_id = "";
        @Comment("Channel id to send commands issued by players")
    @Setting("commands-channel-id")
        public String commands_channel_id = "";

        @Comment("Pixelmon announces")
    @Setting("pixelmon")
        public Pixelmon pixelmon = new Pixelmon();
        @Comment("Put the id on 'commands-channel-id' option or/and enable server commands on channel configuration to use this.")
    @Setting("server-commands")
        public ServerCmds server_commands = new ServerCmds();

        @ConfigSerializable
        public static class Pixelmon {

            @Comment("Text to show on Legendary Spawn")
    @Setting("legendary-text")
            public String legendary_text = ":loudspeaker: A Legendary **%s** has spawned on biome **%s** in world **%s**";
            @Comment("Announce to a discord channel when a Legendary Spawns")
    @Setting("legendary-channel-id")
            public String legendary_channel_id = "";
        }

        @ConfigSerializable
        public static class ServerCmds {

            @Comment("This alias is not needed if using the channel set on 'commands-channel-id' option.")
    @Setting("alias")
            public String alias = "!cmd";
            @Setting("withelist")
            public List<String> withelist = new ArrayList<>();
            @Setting("blacklist")
            public List<String> blacklist = Arrays.asList("stop", "whitelist");
        }
    }

    @ConfigSerializable
    public static class MentionCat {

        @Setting("enable")
        public boolean enable = true;
        @Setting("color-template")
        public String color_template = "&e@{mentioned-player}&r";
        @Setting("playsound")
        public String playsound = "minecraft:block.note.pling";
        @Setting("hover-message")
        public String hover_message = "&e{playername} mentioned you!";
    }

    @ConfigSerializable
    public static class ApiCat {

        @Setting("format-console-messages")
        public boolean format_console_messages = false;
        @Comment("Change this if using SpongeVanilla or some issues to UChat detect the API versions.\n" +
                "- Available: 5, 6, 7, 8")
    @Setting("sponge-api")
        public int sponge_api = 7;
    }

    @ConfigSerializable
    public static class GeneralCat {

        @Comment("The default channel for fakeplayers from mods and other plugins.")
    @Setting("fakeplayer-channel")
        public String fakeplayer_channel = "l";
        @Comment("Template to show when players send links or urls.")
    @Setting("URL-template")
        public String URL_template = "&3Click to open &n{url}&r";
        @Comment("Deny muted players to use this commands.")
    @Setting("muted-deny-cmds")
        public List<String> muted_deny_cmds = Collections.singletonList("mail");
        @Comment("Tag to show when sent messages from console to channels.")
    @Setting("console-tag")
        public String console_tag = "&6 {console}&3";
        @Setting("custom-tags")
        public List<String> custom_tags = new ArrayList<>();
        @Comment("If using the placeholder \"{option_all_prefixes/suffixes}\", exclude this groups from tags.")
    @Setting("dont-show-groups")
        public List<String> dont_show_groups = Collections.singletonList("default");
        @Comment("Remove this from chat (like empty tags)")
    @Setting("remove-from-chat")
        public List<String> remove_from_chat = Arrays.asList("[]", "&7[]", "&7[&7]");
        @Comment("Command and aliases for /channel command.")
    @Setting("channel-cmd-aliases")
        public String channel_cmd_aliases = "channel, ch";
        @Comment("Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players).")
    @Setting("umsg-cmd-aliases")
        public String umsg_cmd_aliases = "umsg";
        @Comment("Chat spy format.")
    @Setting("spy-format")
        public String spy_format = "&c[Spy] {output}";
        @Comment("Enable spy on join?")
    @Setting("spy-enabled-onjoin")
        public boolean spy_enabled_onjoin = true;
        @Comment("Enable to allow parse tags and placeholders on messages.")
    @Setting("enable-tags-on-messages")
        public boolean enable_tags_on_messages = false;
        @Setting("nick-symbol")
        public String nick_symbol = "&6~&f";
        @Setting("persist-channels")
        public boolean persist_channels = true;
        @Setting("item-hand")
        public ItemHandCat item_hand = new ItemHandCat();
        @Comment("Example alias for rename world name to other name. Support color codes.")
    @Setting("world-names")
        public Map<String, String> world_names = createMapWorlds();
        @Comment("Example alias for rename group name to other name. Support color codes.")
    @Setting("group-names")
        public Map<String, String> group_names = createMapGroup();
        @Setting("default-channels")
        public DefaultChannels default_channels = new DefaultChannels();
        @Comment("This will make a check if the player channel is available on destination world and put on the world channel if is not available.")
    @Setting("check-channel-change-world")
        public boolean check_channel_change_world = false;
        @Comment("This is the main tag builder.\n"
                + "Change the order of this tags to change how tag is displayed on chat.\n"
                + "This tags represent the names of tag in this configuration.")
    @Setting("default-tag-builder")
        public String default_tag_builder = "world,ch-tags,prefix,nickname,suffix,message";

        private HashMap<String, String> createMapWorlds() {
            HashMap<String, String> myMap = new HashMap<>();
            myMap.put("my-end", "&5The-End&r");
            myMap.put("my-nether", "&4Hell&r");
            return myMap;
        }

        private Map<String, String> createMapGroup() {
            Map<String, String> myMap = new HashMap<>();
            myMap.put("my-admin", "&4Admin&r");
            myMap.put("my-moderator", "&2MOD&r");
            return myMap;
        }

        @ConfigSerializable
        public static class DefaultChannels {

            @Comment("Default channel for new added worlds")
    @Setting("default-channel")
            public String default_channel = "l";

            @Setting("worlds")
            public Map<String, WorldInfo> worlds = new HashMap<>();
        }

        @ConfigSerializable
        public static class ItemHandCat {

            @Setting("enable")
            public boolean enable = true;
            @Comment("Text to show on chat on hover the tag.")
    @Setting("format")
            public String format = "&6[{hand-amount} {hand-type}]{group-suffix}";
            @Comment("Placeholder to use on chat by players to show your item in hand.\n" +
                    "Placeholders: {hand-amount}, {hand-type}, {hand-name}")
    @Setting("placeholder")
            public String placeholder = "@hand";
        }
    }

    @ConfigSerializable
    public static class TellCat {

        @Comment("Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
    @Setting("enable")
        public boolean enable = true;
        @Comment("Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
    @Setting("cmd-aliases")
        public String cmd_aliases = "tell,t,w,m,msg,private,priv";
        @Comment("Prefix of tell messages.")
    @Setting("prefix")
        public String prefix = "&6[&c{playername} &6-> &c{receivername}&6]: ";
        @Comment("Suffix (or message) of tell.")
    @Setting("format")
        public String format = "{message}";
        @Comment("Hover messages to show on tell messages.")
    @Setting("hover-messages")
        public List<String> hover_messages = new ArrayList<>();
    }

    @ConfigSerializable
    public static class BroadcastCat {

        @Comment("Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
    @Setting("enable")
        public boolean enable = true;
        @Comment("Tag to use on broadcast message to set a hover message.")
    @Setting("on-hover")
        public String on_hover = "hover:";
        @Comment("Tag to use on broadcast message to set a click event.")
    @Setting("on-click")
        public String on_click = "click:";
        @Comment("Tag to use on broadcast message to set a website url on click.")
    @Setting("url")
        public String url = "url:";
        @Comment("Aliases to use for broadcast.")
    @Setting("aliases")
        public String aliases = "broadcast,broad,ubroad,announce,say,action,all,anunciar,todos";
    }

    @ConfigSerializable
    public static class HooksCat {

        @Setting("MCClans")
        public McclansCat MCClans = new McclansCat();

        @ConfigSerializable
        public static class McclansCat {

            @Comment("Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
    @Setting("enable")
            public boolean enable = false;
        }
    }

    @ConfigSerializable
    public static class WorldInfo {
        @Setting("channel")
        public String channel = "l";
        @Setting("force")
        public boolean force = false;

        public WorldInfo() {
        }

        public WorldInfo(String ch, boolean force) {
            this.channel = ch;
            this.force = force;
        }
    }
}
