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

package br.net.fabiozumbi12.UltimateChat.Sponge;

import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCLogger.timingType;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a chat channel use by UltimateChat to control from where/to send/receive messages.
 *
 * @author FabioZumbi12
 */
public class UCChannel {
    private final List<String> ignoring = new ArrayList<>();
    private final List<String> mutes = new ArrayList<>();
    private final Properties properties = new Properties();
    private List<String> members = new ArrayList<>();

    public UCChannel(String name, String alias, String color) {
        addDefaults();
        properties.put("name", name);
        properties.put("alias", alias);
        properties.put("color", color);
    }

    public UCChannel(String name) {
        addDefaults();
        properties.put("name", name);
        properties.put("alias", name.substring(0, 1).toLowerCase());
    }

    public UCChannel(Map<String, Object> props) {
        addDefaults();
        properties.keySet().stream().filter(props::containsKey).filter(k -> props.get(k) != null)
                .forEach((nkey) -> properties.put(nkey, props.get(nkey)));
    }

    private static boolean bool(Object value) {
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }

    private void addDefaults() {
        properties.put("name", "");
        properties.put("alias", "");
        properties.put("char-alias", "");
        properties.put("color", "&b");
        properties.put("across-worlds", true);
        properties.put("distance", 0);
        properties.put("use-this-builder", false);
        properties.put("tag-builder", "world,ch-tags,prefix,nickname,suffix,message");
        properties.put("need-focus", false);
        properties.put("canLock", true);
        properties.put("receivers-message", false);
        properties.put("cost", 0.0);
        properties.put("bungee", false);
        properties.put("allow-hand", true);
        properties.put("jedis", false);
        properties.put("password", "");
        properties.put("channelAlias.enable", false);
        properties.put("channelAlias.sendAs", "player");
        properties.put("channelAlias.cmd", "");
        properties.put("available-worlds", new ArrayList<String>());
        properties.put("discord.channelID", "");
        properties.put("discord.mode", "none");
        properties.put("discord.allow-bot", true);
        properties.put("discord.hover", "&3Discord Channel: &a{dd-channel}\n&3Role Name: {dd-rolecolor}{dd-rolename}");
        properties.put("discord.allow-server-cmds", false);
        properties.put("discord.format-to-mc", "{ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: ");
        properties.put("discord.format-to-dd", ":regional_indicator_g: **{sender}**: {message}");
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String key, String value) {
        if (properties.get(key) instanceof List) {
            properties.put(key, new ArrayList<>(Arrays.asList(value.split(","))));
        } else if (properties.get(key) instanceof Boolean) {
            properties.put(key, Boolean.parseBoolean(value));
        } else if (properties.get(key) instanceof Integer) {
            try {
                properties.put(key, Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                UChat.get().getLogger().warning("Not a number for " + key + ": " + value);
            }
        } else {
            properties.put(key, value);
        }
    }

    public boolean allowHand() {
        return bool(properties.get("allow-hand"));
    }

    public String getCharAlias() {
        return this.properties.get("char-alias").toString();
    }

    public boolean AllowBot() {
        return bool(properties.get("discord.allow-bot"));
    }

    public String getPassword() {
        return String.valueOf(properties.get("password"));
    }

    public void setPassword(String pass) {
        properties.put("password", pass);
    }

    public boolean useJedis() {
        return bool(properties.get("jedis"));
    }

    public void setJedis(boolean use) {
        properties.put("jedis", use);
    }

    public boolean getDiscordAllowCmds() {
        return bool(properties.get("discord.allow-server-cmds"));
    }

    public boolean isTell() {
        return properties.get("name").toString().equals("tell");
    }

    public List<String> getDiscordChannelID() {
        String ids = properties.get("discord.channelID").toString().trim();
        return ids.isEmpty() ? Collections.emptyList() : Arrays.asList(ids.replace(" ", "").split(","));
    }

    public String getDiscordMode() {
        return properties.get("discord.mode").toString();
    }

    public boolean matchDiscordID(String id) {
        return getDiscordChannelID().contains(id);
    }

    public boolean isSendingDiscord() {
        return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("send"));
    }

    public boolean isListenDiscord() {
        return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("listen"));
    }

    public String getDiscordHover() {
        return properties.get("discord.hover").toString();
    }

    public String getDiscordtoMCFormat() {
        return properties.get("discord.format-to-mc").toString();
    }

    public String getMCtoDiscordFormat() {
        return properties.get("discord.format-to-dd").toString();
    }

    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(Subject member) {
        addMember(UCUtil.name(member));
    }

    public void addMember(String member) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            ch.removeMember(member);
        }
        this.members.add(member);
    }

    public void removeMember(Subject member) {
        removeMember(UCUtil.name(member));
    }

    public void removeMember(String member) {
        this.members.remove(member);
    }

    public void clearMembers() {
        this.members.clear();
    }

    public boolean isMember(Subject p) {
        return this.members.contains(UCUtil.name(p));
    }

    public boolean canLock() {
        return bool(properties.get("canLock"));
    }

    public boolean availableInWorld(ServerWorld w) {
        List<String> worlds = availableWorlds();
        return worlds.isEmpty() || UCUtil.worldIn(w, worlds);
    }

    @SuppressWarnings("unchecked")
    public List<String> availableWorlds() {
        Object worlds = properties.get("available-worlds");
        if (worlds instanceof List) {
            List<String> names = new ArrayList<>();
            for (Object o : (List<Object>) worlds) {
                names.add(String.valueOf(o));
            }
            return names;
        }
        String str = String.valueOf(worlds).trim();
        return str.isEmpty() ? Collections.emptyList() : Arrays.asList(str.split(","));
    }

    public String getAliasCmd() {
        return properties.get("channelAlias.cmd").toString();
    }

    public String getAliasSender() {
        return properties.get("channelAlias.sendAs").toString();
    }

    public boolean isCmdAlias() {
        return bool(properties.get("channelAlias.enable"));
    }

    public boolean useOwnBuilder() {
        return bool(properties.get("use-this-builder"));
    }

    public double getCost() {
        return Double.parseDouble(properties.get("cost").toString());
    }

    public void setCost(double cost) {
        properties.put("cost", cost);
    }

    public boolean getReceiversMsg() {
        // The SpongeAPI 7 versions read this setting with Boolean.getBoolean, which looks up a system property of that
        // name, so it was always false. Kept that way so players don't suddenly get "no one heard you" messages.
        return Boolean.getBoolean(properties.get("receivers-message").toString());
    }

    public void setReceiversMsg(boolean show) {
        properties.put("receivers-message", show);
    }

    public void muteThis(String player) {
        if (!this.mutes.contains(player)) {
            this.mutes.add(player);
        }
    }

    public void unMuteThis(String player) {
        this.mutes.remove(player);
    }

    public boolean isMuted(String player) {
        return this.mutes.contains(player);
    }

    public void ignoreThis(String player) {
        if (!this.ignoring.contains(player)) {
            this.ignoring.add(player);
        }
    }

    public void unIgnoreThis(String player) {
        this.ignoring.remove(player);
    }

    public boolean isIgnoring(String player) {
        return this.ignoring.contains(player);
    }

    public String[] getBuilder() {
        return properties.get("tag-builder").toString().replace(" ", "").split(",");
    }

    public String getRawBuilder() {
        return properties.get("tag-builder").toString();
    }

    public boolean crossWorlds() {
        return bool(properties.get("across-worlds"));
    }

    public int getDistance() {
        Object distance = properties.get("distance");
        return distance instanceof Number ? ((Number) distance).intValue() : Integer.parseInt(distance.toString());
    }

    public String getColor() {
        return properties.get("color").toString();
    }

    public String getName() {
        return properties.get("name").toString();
    }

    public String getAlias() {
        return properties.get("alias").toString();
    }

    public boolean neeFocus() {
        return bool(properties.get("need-focus"));
    }

    public boolean matchChannel(String aliasOrName) {
        return properties.get("alias").toString().equalsIgnoreCase(aliasOrName) || properties.get("name").toString().equalsIgnoreCase(aliasOrName);
    }

    public boolean isBungee() {
        return bool(properties.get("bungee"));
    }

    private void sendDirect(Subject sender, Component message) {
        for (ServerPlayer p : Sponge.server().onlinePlayers()) {
            UCChannel chp = UChat.get().getPlayerChannel(p);
            if (UChat.get().getPerms().channelReadPerm(p, this) && !this.isIgnoring(p.name()) && (!this.neeFocus() || this.equals(chp))) {
                UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
                p.sendMessage(message);
            }
        }
        UCUtil.send(sender, message);
    }

    /**
     * Send a message from a channel as player.
     *
     * @param src     {@code ServerPlayer}
     * @param message {@code Component} - Message to send.
     * @param direct  {@code boolean} - Send message direct to players on channel.
     */
    public void sendMessage(ServerPlayer src, Component message, boolean direct) {
        if (direct) {
            sendDirect(src, message);
        } else {
            UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Chat as player");
            UCListener.chatInChannel(src, this, UCUtil.toPlain(message));
        }
    }

    /**
     * Send a message from a channel as console.
     *
     * @param sender  {@code SystemSubject} - Console sender.
     * @param message {@code Component} - Message to send.
     * @param direct  {@code boolean} - Send message direct to players on channel.
     */
    public void sendMessage(SystemSubject sender, Component message, boolean direct) {
        if (direct) {
            sendDirect(sender, message);
        } else {
            UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Chat as console");
            UCMessages.sendFancyMessage(new String[0], message, this, sender, null);
        }
    }

    @Override
    public String toString() {
        JsonArray array = new JsonArray();
        for (Entry<Object, Object> prop : properties.entrySet()) {
            JsonObject json = new JsonObject();
            json.addProperty((String) prop.getKey(), prop.getValue().toString());
            array.add(json);
        }
        return array.toString();
    }
}
