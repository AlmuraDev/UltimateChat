package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {
	
	public MainCategory(){
		defaultTags();
	}
		
	@Setting(value="_config-version")
	public Double config_version = 1.0;	
	@Setting()
	public final DebugCat debug = new DebugCat();
	
	//debug
	@ConfigSerializable
	public static class DebugCat{
		
		@Setting()
		public final boolean messages = false;
		@Setting()
		public final boolean timings = false;
	}
	
	@Setting(comment="Available languages: EN-US, PT-BR, FR, HU-HU, RU, ZH-CN")
	public final String language = "EN-US";
	
	// jedis
	@Setting(comment="Jedis configuration.\nUse Jedis to send messages between other servers running Jedis.\nConsider a replecement as Bungeecoord.")
	public final JedisCat jedis = new JedisCat();
	
	@ConfigSerializable
	public static class JedisCat{
		
		@Setting
		public final boolean enable = false;
		@Setting(value="server-id")
		public final String server_id = "&e-ChangeThis-&r ";
		@Setting
		public final String ip = "localhost";
		@Setting
		public final int port = 6379;
		@Setting
		public final String pass = "";
	}
	
	//discord
	@Setting(comment="Enable the two way chat into discord and minecraft.\nGenerate your bot token following this instructions: https://goo.gl/utfRRv")
	public final DiscordCat discord = new DiscordCat();
	@ConfigSerializable
	public static class DiscordCat{
		
		@Setting
		public final boolean use = false;
		@Setting(value="update-status")
		public final boolean update_status = true;
		@Setting
		public final String token = "";
		@Setting(value="log-channel-id", comment="Channel id to send server start/stop and player join/leave messages")
		public final String log_channel_id = "";
		@Setting(value="tell-channel-id", comment="Channel id to spy private messages")
		public final String tell_channel_id = "";
		@Setting(value="commands-channel-id", comment="Channel id to send commands issued by players")
		public final String commands_channel_id = "";
		@Setting(value="server-commands", comment="Put the id on 'commands-channel-id' option or/and enable server commands on channel configuration to use this.")
		public final ServerCmds server_commands = new ServerCmds();
		@ConfigSerializable
		public static class ServerCmds{
			
			@Setting(comment="This alias is not needed if using the channel set on 'commands-channel-id' option.")
			public final String alias = "!cmd";
			@Setting
			public final List<String> withelist = new ArrayList<>();
			@Setting
			public final List<String> blacklist = Arrays.asList("stop","whitelist");
		}						
	}
	
	//mention
	@Setting(comment="Use mentions on chat to change the player name color and play a sound on mention.")
	public final MentionCat mention = new MentionCat();
	@ConfigSerializable
	public static class MentionCat{
		
		@Setting
		public final boolean enable = true;
		@Setting(value="color-template")
		public final String color_template = "&e@{mentioned-player}&r";
		@Setting
		public final String playsound = "minecraft:block.note.pling";
		@Setting(value="hover-message")
		public final String hover_message = "&e{playername} mentioned you!";
	}
	
	//api
	@Setting(comment="API configurations.")
	public final ApiCat api = new ApiCat();
	@ConfigSerializable
	public static class ApiCat{
		
		@Setting(value="format-console-messages")
		public final boolean format_console_messages = false;
	}
	
	//general
	@Setting(comment="General settings.")
	public final GeneralCat general = new GeneralCat();
	
	@ConfigSerializable
	public static class GeneralCat{
		
		@Setting(value="URL-template", comment="Template to show when players send links or urls.")
		public final String URL_template = "&3Click to open &n{url}&r";
		@Setting(value="console-tag", comment="Tag to show when sent messages from console to channels.")
		public final String console_tag = "&6 {console}&3";
		@Setting(value="custom-tags")
		public final List<String> custom_tags = new ArrayList<>();
		@Setting(value="remove-from-chat", comment="Remove this from chat (like empty tags)")
		public final List<String> remove_from_chat = Arrays.asList("[]","&7[]","&7[&7]");
		@Setting(value="channel-cmd-aliases", comment="Command and aliases for /channel command.")
		public final String channel_cmd_aliases = "channel, ch";
		@Setting(value="umsg-cmd-aliases", comment="Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players).")
		public final String umsg_cmd_aliases = "umsg";
		@Setting(value="default-channel", comment="Set the default channel for new players or when players join on server.")
		public final String default_channel = "l";
		@Setting(value="spy-format", comment="Chat spy format.")
		public final String spy_format = "&c[Spy] {output}";
		@Setting(value="enable-tags-on-messages", comment="Enable to allow parse tags and placeholders on messages.")
		public final boolean enable_tags_on_messages = false;
		@Setting(value="nick-symbol")
		public final String nick_symbol = "&6~&f";
		@Setting(value="persist-channels")
		public final boolean persist_channels = true;
		@Setting(value="item-hand")
		public final ItemHandCat item_hand = new ItemHandCat();

		@ConfigSerializable
		public static class ItemHandCat{
			
			@Setting
			public final boolean enable = true;
			@Setting(comment="Text to show on chat on hover the tag.")
			public final String format = "&6[{hand-amount} {hand-type}]{group-suffix}";
			@Setting(comment="Placeholder to use on chat by players to show your item in hand.")
			public final String placeholder = "@hand";
		}
		
		@Setting(value="default-tag-builder", comment="This is the main tag builder.\n"
					+ "Change the order of this tags to change how tag is displayed on chat.\n"
					+ "This tags represent the names of tag in this configuration.")
		public final String default_tag_builder = "world,ch-tags,prefix,nickname,suffix,message";
	}
	
	//tell
	@Setting
	public final TellCat tell = new TellCat();
	
	@ConfigSerializable
	public static class TellCat{
		
		@Setting(comment="Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
		public final boolean enable = true;
		@Setting(value="cmd-aliases", comment="Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
		public final String cmd_aliases = "tell,t,w,m,msg,private,priv";
		@Setting(comment="Prefix of tell messages.")
		public final String prefix = "&6[&c{playername} &6-> &c{receivername}&6]: ";
		@Setting(comment="Suffix (or message) of tell.")
		public final String format = "{message}";
		@Setting(value="hover-messages", comment="Hover messages to show on tell messages.")
		public final List<String> hover_messages = new ArrayList<>();
	}
	
	@Setting
	public final BroadcastCat broadcast = new BroadcastCat();
	
	@ConfigSerializable
	public static class BroadcastCat{
		
		@Setting(comment="Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
		public final boolean enable = true;
		@Setting(value="on-hover", comment="Tag to use on broadcast message to set a hover message.")
		public final String on_hover = "hover:";
		@Setting(value="on-click", comment="Tag to use on broadcast message to set a click event.")
		public final String on_click = "click:";
		@Setting(comment="Tag to use on broadcast message to set a website url on click.")
		public final String url = "url:";
		@Setting(comment="Aliases to use for broadcast.")
		public final String aliases = "broadcast,broad,ubroad,announce,say,action,all,anunciar,todos";
	}
	
	@Setting(comment="Enable hook with other plugins here. Only enable if installed.")
	public final HooksCat hooks = new HooksCat();
	
	@ConfigSerializable
	public static class HooksCat{
		
		@Setting
		public final McclansCat MCClans = new McclansCat();
		
		@ConfigSerializable
		public static class McclansCat{
			
			@Setting(comment="Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
			public final boolean enable = false;
		}
	}
	
	@Setting(comment="This is where you will create as many tags you want.\n"
					+ "You can use the tag \"custom-tag\" as base to create your own tags.\n"
					+ "When finish, get the name of your tag and put on \"general.default-tag-build\" \n"
					+ "or on channel builder on \"channels\" folder.")	
	public final Map<String, TagsCategory> tags = new HashMap<>();
	
	private void defaultTags(){		
		tags.put("prefix", new TagsCategory("{option_prefix}", null, Collections.singletonList("&3Rank: &f{option_display_name}"), null, null, null, null));
		tags.put("nickname", new TagsCategory("{nick-symbol}{nickname}", null, Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"), null, null, null, null));
		tags.put("playername", new TagsCategory("{playername}", null, Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"), null, null, null, null));		
		tags.put("suffix", new TagsCategory("{option_suffix}", null, null, null, null, null, null));		
		tags.put("world", new TagsCategory("&7[{world}]&r", null, Collections.singletonList("&7Sent from world &8{world}"), null, null, null, null));
		tags.put("message", new TagsCategory("{message}", null, null, null, null, null, null));		
		tags.put("ch-tags", new TagsCategory("{ch-color}[{ch-alias}]&r", "ch {ch-alias}", Arrays.asList("&3Channel name: {ch-color}{ch-name}","&bClick to join this channel"), null, null, null, null));		
		tags.put("admin-chat", new TagsCategory("&b[&r{playername}&b]&r: &b", null, null, null, null, null, null));
		tags.put("custom-tag", new TagsCategory("&7[&2MyTag&7]", "say I created an awesome tag!", Collections.singletonList("You discovered me :P"), "any-name-perm.custom-tag", Collections.singletonList("world-show"), Collections.singletonList("world-hide"), "www.google.com"));
		tags.put("vanilla-chat", new TagsCategory("{chat_header}{chat_body}", null, null, null, null, null, null));	
		tags.put("jedis", new TagsCategory("{server-id}", null, Arrays.asList("&7Server: {jedis-id}","&cChange me on configuration!"), null, null, null, null));
	}		
}
