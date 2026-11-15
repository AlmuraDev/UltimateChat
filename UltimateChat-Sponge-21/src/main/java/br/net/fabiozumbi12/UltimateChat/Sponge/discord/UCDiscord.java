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

 Altered for SpongeAPI 21 and JDA 6 by AlmuraDev.
 */

package br.net.fabiozumbi12.UltimateChat.Sponge.discord;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Discord relay: channel messages both ways, server/player events to the log channel, private messages and commands
 * to their channels, and Discord commands to the console. The bot needs the Message Content intent enabled in the
 * Discord developer portal to read messages.
 */
public class UCDiscord extends ListenerAdapter {
    private static final Map<String, int[]> colorMap = new LinkedHashMap<>();

    static {
        colorMap.put("&0", new int[]{0, 0, 0});
        colorMap.put("&1", new int[]{0, 0, 170});
        colorMap.put("&2", new int[]{0, 170, 0});
        colorMap.put("&3", new int[]{0, 170, 170});
        colorMap.put("&4", new int[]{170, 0, 0});
        colorMap.put("&5", new int[]{170, 0, 170});
        colorMap.put("&6", new int[]{255, 170, 0});
        colorMap.put("&7", new int[]{170, 170, 170});
        colorMap.put("&8", new int[]{85, 85, 85});
        colorMap.put("&9", new int[]{85, 85, 255});
        colorMap.put("&a", new int[]{85, 255, 85});
        colorMap.put("&b", new int[]{85, 255, 255});
        colorMap.put("&c", new int[]{255, 85, 85});
        colorMap.put("&d", new int[]{255, 85, 255});
        colorMap.put("&e", new int[]{255, 255, 85});
        colorMap.put("&f", new int[]{255, 255, 255});
    }

    private final UChat uchat;
    private JDA jda;
    private ScheduledTask statusTask;
    private String lastStatus = "";

    /**
     * Connects to Discord and waits until ready. Blocks, so call it off the server thread.
     */
    public UCDiscord(UChat plugin) {
        this.uchat = plugin;
        try {
            this.jda = JDABuilder.createDefault(this.uchat.getConfig().root().discord.token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();
            this.jda.awaitReady();
        } catch (InvalidTokenException | IllegalArgumentException e) {
            uchat.getLogger().severe("The Discord TOKEN is wrong or empty! Check your config and your token: " + e.getMessage());
            shutdownQuietly();
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            shutdownQuietly();
            return;
        } catch (Exception e) {
            uchat.getLogger().severe("Could not connect to Discord", e);
            shutdownQuietly();
            return;
        }

        if (this.uchat.getConfig().root().discord.update_status) {
            updateGame("");
            this.statusTask = Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.uchat.instance())
                    .delay(Duration.ofSeconds(5))
                    .interval(Duration.ofSeconds(5))
                    .execute(() -> updateGame(""))
                    .build(), "UltimateChat Discord status");
        }
    }

    private void shutdownQuietly() {
        if (this.jda != null) {
            this.jda.shutdownNow();
            this.jda = null;
        }
    }

    public void shutdown() {
        this.uchat.getLogger().info("Shutdown JDA...");
        if (this.statusTask != null) {
            this.statusTask.cancel();
        }
        if (this.jda != null) {
            this.jda.getRegisteredListeners().forEach(l -> this.jda.removeEventListener(l));
            this.jda.shutdown();
            try {
                if (!this.jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    this.jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.jda.shutdownNow();
            }
        }
        this.uchat.getLogger().info("JDA disabled!");
    }

    /*   ------ color util --------   */
    private static String fromRGB(int r, int g, int b) {
        TreeMap<Integer, String> closest = new TreeMap<>();
        colorMap.forEach((color, set) -> closest.putIfAbsent(Math.abs(r - set[0]) + Math.abs(g - set[1]) + Math.abs(b - set[2]), color));
        return closest.firstEntry().getValue();
    }

    public boolean JDAAvailable() {
        return this.jda != null && this.jda.getStatus() == JDA.Status.CONNECTED;
    }

    private static boolean allowed(String message) {
        if (!UChat.get().getConfig().root().discord.server_commands.withelist.isEmpty()
                && UChat.get().getConfig().root().discord.server_commands.withelist.stream().noneMatch(message::startsWith)) {
            return false;
        }
        return UChat.get().getConfig().root().discord.server_commands.blacklist.stream().noneMatch(message::startsWith);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId())) return;
        // Private messages were only used by the Discord account sync, which is not part of the SpongeAPI 21 version
        if (!e.isFromGuild() || e.getMember() == null || e.isWebhookMessage()) return;
        // Everything below touches the game, so it runs on the server thread
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(this.uchat.instance())
                .execute(() -> handleGuildMessage(e))
                .build());
    }

    private void handleGuildMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getContentRaw();
        int used = 0;

        for (UCChannel ch : this.uchat.getChannels().values()) {
            if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())) {
                if (e.getAuthor().isBot() && !ch.AllowBot()) {
                    continue;
                }
                //check if is cmd
                String alias = this.uchat.getConfig().root().discord.server_commands.alias;
                if (message.startsWith(alias) && ch.getDiscordAllowCmds()) {
                    String cmd = message.replace(alias + " ", "");
                    if (!allowed(cmd)) continue;
                    this.uchat.runConsoleCommand(cmd);
                    used++;
                } else {
                    TextComponent.Builder text = Component.text();

                    //format prefixes tags
                    String formated = formatTags(ch.getDiscordtoMCFormat(), ch, e, "", "");

                    //add dd channel name to hover
                    String hovered = formatTags(ch.getDiscordHover(), ch, e, "", "");
                    text.append(UCUtil.toText(formated));
                    if (!hovered.isEmpty()) {
                        text.hoverEvent(HoverEvent.showText(UCUtil.toText(hovered)));
                    }

                    //format message
                    if (!e.getMessage().getAttachments().isEmpty()) {
                        Message.Attachment att = e.getMessage().getAttachments().get(0);
                        var click = UCUtil.urlClick(att.getUrl());
                        if (click != null) text.clickEvent(click);
                        text.hoverEvent(HoverEvent.showText(Component.text(att.getFileName())));
                        text.append(Component.text(message.isEmpty() ? "- Attachment -" : message));
                    } else {
                        text.append(Component.text(cleanMentions(e, message.replace("\\n", "\n"))));
                    }
                    ch.sendMessage(UCUtil.console(), text.build(), true);
                    used++;
                }
            }
        }

        //check if is from log command chanel
        if (used == 0 && e.getChannel().getId().equals(this.uchat.getConfig().root().discord.commands_channel_id) && allowed(message)) {
            this.uchat.runConsoleCommand(message);
        }
    }

    private static String cleanMentions(MessageReceivedEvent e, String message) {
        //emoji
        Matcher me = Pattern.compile("<a?:(.+?):\\d+>").matcher(message);
        message = me.replaceAll(m -> Matcher.quoteReplacement(":" + m.group(1) + ":"));

        //user and role name
        List<Role> roles = e.getMessage().getMentions().getRoles();
        List<Member> members = e.getMessage().getMentions().getMembers();
        for (Role role : roles) {
            message = message.replace("<@&" + role.getId() + ">", "@" + role.getName());
        }
        for (Member member : members) {
            message = message.replace("<@" + member.getId() + ">", "@" + member.getEffectiveName())
                    .replace("<@!" + member.getId() + ">", "@" + member.getEffectiveName());
        }
        message = message.replaceAll("<@[!&]?\\d+>", "@?");

        //channel
        for (GuildChannel chg : e.getMessage().getMentions().getChannels()) {
            message = message.replace("<#" + chg.getId() + ">", "#" + chg.getName());
        }
        return message.replaceAll("<#\\d+>", "#?");
    }

    public void updateGame(String text) {
        if (this.jda == null) return;
        String game = uchat.getLang().get("discord.game").replace("{online}",
                String.valueOf(Sponge.server().onlinePlayers().stream().filter(p -> !p.hasPermission("uchat.discord.hide")).count()));
        if (game.equals(this.lastStatus)) return;
        this.lastStatus = game;
        String type = uchat.getConfig().root().discord.game_type.toUpperCase(Locale.ROOT);
        Activity activity;
        switch (type) {
            case "LISTENING":
                activity = Activity.listening(game);
                break;
            case "WATCHING":
                activity = Activity.watching(game);
                break;
            case "STREAMING":
                activity = Activity.streaming(game, uchat.getConfig().root().discord.twitch);
                break;
            default:
                activity = Activity.playing(game);
        }
        this.jda.getPresence().setActivity(activity);
    }

    public void sendTellToDiscord(String text) {
        if (!uchat.getConfig().root().discord.tell_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.tell_channel_id, text);
        }
    }

    public void sendCommandsToDiscord(String text) {
        if (!uchat.getConfig().root().discord.commands_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.commands_channel_id, text);
        }
    }

    public void sendRawToDiscord(String text) {
        if (!uchat.getConfig().root().discord.log_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.log_channel_id, text);
        }
    }

    /** Sends to the log channel and waits up to 5 seconds, for the stop message which must go out before JDA shuts down. */
    public void sendRawToDiscordNow(String text) {
        String id = uchat.getConfig().root().discord.log_channel_id;
        TextChannel ch = id.isEmpty() || this.jda == null ? null : this.jda.getTextChannelById(id);
        if (ch == null) return;
        try {
            ch.sendMessage(UCUtil.stripColor('&', UCUtil.stripColor('§', text))).submit().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            uchat.getLogger().warning("JDA: Could not send the message to the log channel: " + e.getMessage());
        }
    }

    public void sendToDiscord(Subject sender, String text, UCChannel ch) {
        if (ch.isSendingDiscord()) {
            if (!uchat.getPerms().hasPerm(sender, "discord.mention")) {
                text = text.replace("@everyone", "everyone")
                        .replace("@here", "here");
            }
            text = formatTags(ch.getMCtoDiscordFormat(), ch, null, UCUtil.name(sender), text);
            for (String ddid : ch.getDiscordChannelID()) {
                sendToChannel(ddid, text);
            }
        }
    }

    private void sendToChannel(String id, String text) {
        if (this.jda == null) return;
        text = text.replaceAll("([&§]([a-fk-or0-9]))", "");
        if (text.isEmpty()) return;
        TextChannel ch;
        try {
            ch = jda.getTextChannelById(id);
        } catch (NumberFormatException e) {
            ch = null;
        }
        if (ch == null) {
            uchat.getLogger().warning("JDA: The channel ID " + id + " is incorrect or not available.");
            return;
        }
        final TextChannel target = ch;
        try {
            target.sendMessage(text.length() > 2000 ? text.substring(0, 2000) : text).queue(null,
                    err -> uchat.getLogger().warning("JDA: Could not send a message to channel " + target.getName() + ": " + err.getMessage()));
        } catch (InsufficientPermissionException e) {
            uchat.getLogger().severe("JDA: No permission to send messages to channel " + target.getName() + ".");
        }
    }

    private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message) {
        format = format.replace("{ch-color}", ch.getColor())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-name}", ch.getName());
        if (e != null && e.getMember() != null) {
            Member member = e.getMember();
            sender = UCUtil.stripColor('§', UCUtil.toColor(member.getEffectiveName()));
            format = format.replace("{sender}", sender)
                    .replace("{dd-channel}", e.getChannel().getName())
                    .replace("{message}", e.getMessage().getContentRaw());
            if (!member.getRoles().isEmpty()) {
                Role role = member.getRoles().get(0);
                if (!role.getColors().isDefault()) {
                    int raw = role.getColors().getPrimaryRaw();
                    format = format.replace("{dd-rolecolor}", fromRGB((raw >> 16) & 0xFF, (raw >> 8) & 0xFF, raw & 0xFF));
                }
                format = format.replace("{dd-rolename}", role.getName());
            }
            if (member.getNickname() != null) {
                format = format.replace("{nickname}", UCUtil.stripColor('§', UCUtil.toColor(member.getNickname())));
            } else {
                format = format.replace("{nickname}", sender);
            }
        }
        //if not filtered
        format = format
                .replace("{sender}", sender)
                .replace("{message}", message);
        format = format.replaceAll("\\{.*?\\}", "");
        return format;
    }
}
