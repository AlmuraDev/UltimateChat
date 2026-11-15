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
public class ProtectionsCategory {

    @Setting("chat-protection")
    public Chatprotection chat_protection = new Chatprotection();

    public ProtectionsCategory() {
    }

    @ConfigSerializable
    public static class Chatprotection {

        //chat_enhancement
        @Setting("chat-enhancement")
        public ChatEnhancementCat chat_enhancement = new ChatEnhancementCat();
        //anti-flood
        @Setting("anti-flood")
        public AntiFloodCat anti_flood = new AntiFloodCat();
        //caps-filter
        @Setting("caps-filter")
        public CapsFilterCat caps_filter = new CapsFilterCat();
        //anti-spam
        @Setting("antispam")
        public AntiSpamCat antispam = new AntiSpamCat();
        //censor
        @Setting("censor")
        public CensorCat censor = new CensorCat();
        //anti-ip
        @Setting("anti-ip")
        public AntiIpCat anti_ip = new AntiIpCat();

        @ConfigSerializable
        public static class ChatEnhancementCat {

            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting("end-with-dot")
            public boolean end_with_dot = true;
            @Setting("minimum-length")
            public int minimum_length = 3;
        }

        @ConfigSerializable
        public static class AntiFloodCat {

            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = Collections.singletonList("Local");
            @Setting("whitelist-flood-characs")
            public List<String> whitelist_flood_characs = Arrays.asList("k", "w");
        }

        @ConfigSerializable
        public static class CapsFilterCat {

            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting("minimum-length")
            public int minimum_length = 3;
        }

        @ConfigSerializable
        public static class AntiSpamCat {

            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = Collections.singletonList("Local");
            @Comment("In seconds")
    @Setting("time-between-messages")
            public int time_between_messages = 1;
            @Setting("count-of-same-message")
            public int count_of_same_message = 5;
            @Comment("In seconds.")
    @Setting("time-between-same-messages")
            public int time_between_same_messages = 10;
            @Setting("cooldown-msg")
            public String cooldown_msg = "&6Slow down your messages!";
            @Setting("wait-message")
            public String wait_message = "&cWait to send the same message again!";
            @Setting("cmd-action")
            public String cmd_action = "kick {player} Relax, slow down your messages frequency ;)";
        }

        @ConfigSerializable
        public static class CensorCat {
            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Comment("Disable to use the replace-words list.")
    @Setting("replace-by-symbol")
            public boolean replace_by_symbol = true;
            @Setting("by-symbol")
            public String by_symbol = "*";
            @Comment("Use uchat pre actions to replace partial words?")
    @Setting("replace-partial-word")
            public boolean replace_partial_word = false;
            @Comment("Use uchat pre-actions regex or your custom regex on replace-words?")
    @Setting("use-pre-actions")
            public boolean use_pre_actions = true;
            @Comment("List of words to replace (case insensitive). Accept regex. When using regex, add an extra \"\\\" for each \"\\\" or will break your configuration'.")
    @Setting("replace-words")
            public Map<String, String> replace_words = createMap();
            //action
            @Setting("action")
            public ActionCat action = new ActionCat();

            private HashMap<String, String> createMap() {
                HashMap<String, String> myMap = new HashMap<>();
                myMap.put("fuck", "*flower*");
                myMap.put("ass", "*finger*");
                return myMap;
            }

            @ConfigSerializable
            public static class ActionCat {

                @Setting("cmd")
                public String cmd = "";
                @Setting("only-on-channels")
                public List<String> only_on_channels = Collections.singletonList("global");
                @Setting("on-partial-words")
                public boolean on_partial_words = false;
            }
        }

        @ConfigSerializable
        public static class AntiIpCat {

            @Setting("enable")
            public boolean enable = true;
            @Setting("disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting("custom-ip-regex")
            public String custom_ip_regex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
            @Setting("custom-url-regex")
            public String custom_url_regex = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
            @Setting("check-for-words")
            public List<String> check_for_words = Collections.singletonList("www.google.com");
            @Setting("whitelist-words")
            public List<String> whitelist_words = Arrays.asList("www.myserver.com", "prntscr.com", "gyazo.com", "www.youtube.com");
            @Comment("The options are: \"cancel\" or \"replace\"")
    @Setting("cancel-or-replace")
            public String cancel_or_replace = "cancel";
            @Setting("cancel-msg")
            public String cancel_msg = "&cYou cant send websites or ips on chat";
            @Setting("replace-by-word")
            public String replace_by_word = "-removed-";

            @Setting("punish")
            public PunishCat punish = new PunishCat();

            @ConfigSerializable
            public static class PunishCat {

                @Setting("enable")
                public boolean enable = true;
                @Setting("max-attempts")
                public int max_attempts = 3;
                @Comment("The options are: \"mute\" or \"cmd\"")
    @Setting("mute-or-cmd")
                public String mute_or_cmd = "mute";
                @Comment("In minutes.")
    @Setting("mute-duration")
                public int mute_duration = 1;
                @Setting("mute-msg")
                public String mute_msg = "&cYou have been muted for send IPs or URLs on chat!";
                @Setting("unmute-msg")
                public String unmute_msg = "&aYou can chat again!";
                @Setting("cmd-punish")
                public String cmd_punish = "tempban {player} 10m &cYou have been warned about send links or IPs on chat!";
            }
        }
    }


}
