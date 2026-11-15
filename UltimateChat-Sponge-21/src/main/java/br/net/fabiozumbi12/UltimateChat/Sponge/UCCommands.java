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

import br.net.fabiozumbi12.UltimateChat.Sponge.API.PlayerChangeChannelEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.MuteCountDown;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The commands, as raw commands (free text arguments, like the SpongeAPI 7 versions).
 *
 * <p>SpongeAPI 21 registers commands only while the server starts, so command aliases set in the config, and the
 * enabling of tell and broadcast, apply after a restart. Channels added later still work: their aliases are run by
 * {@link UCListener#onCommand}.</p>
 */
public class UCCommands {

    // ------------------------------------------------------------------ framework

    @FunctionalInterface
    interface Exec {
        void run(Subject src, String[] args) throws CommandException;
    }

    /** A raw command: permission check, executor and tab completion over space separated arguments. */
    static final class RawCmd implements Command.Raw {
        private final Predicate<Subject> canUse;
        private final Exec exec;
        private final BiFunction<Subject, String[], List<String>> completer;
        private final String description;
        private final String usage;

        RawCmd(String permission, String description, String usage, Exec exec, BiFunction<Subject, String[], List<String>> completer) {
            this(permission == null ? s -> true : s -> s instanceof SystemSubject || s.hasPermission(permission), description, usage, exec, completer);
        }

        RawCmd(Predicate<Subject> canUse, String description, String usage, Exec exec, BiFunction<Subject, String[], List<String>> completer) {
            this.canUse = canUse;
            this.exec = exec;
            this.completer = completer;
            this.description = description;
            this.usage = usage;
        }

        @Override
        public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
            Subject src = source(cause);
            if (!canUse.test(src)) {
                throw new CommandException(UChat.get().getLang().getText("chat.nopermission"));
            }
            exec.run(src, split(arguments.remaining(), false));
            return CommandResult.success();
        }

        @Override
        public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
            if (completer == null) {
                return Collections.emptyList();
            }
            String[] args = split(arguments.remaining(), true);
            String last = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
            List<String> options = completer.apply(source(cause), args);
            if (options == null) {
                return Collections.emptyList();
            }
            return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(last)).distinct()
                    .map(CommandCompletion::of).collect(Collectors.toList());
        }

        @Override
        public boolean canExecute(CommandCause cause) {
            return canUse.test(source(cause));
        }

        @Override
        public Optional<Component> shortDescription(CommandCause cause) {
            return Optional.of(Component.text(description));
        }

        @Override
        public Optional<Component> extendedDescription(CommandCause cause) {
            return Optional.empty();
        }

        @Override
        public Component usage(CommandCause cause) {
            return Component.text(usage);
        }
    }

    /** The player running the command, else the console (also for command blocks and other non-player sources). */
    static Subject source(CommandCause cause) {
        if (cause.root() instanceof ServerPlayer) {
            return (ServerPlayer) cause.root();
        }
        return UCUtil.console();
    }

    static String[] split(String input, boolean keepTrailingEmpty) {
        if (input == null || input.trim().isEmpty()) {
            return keepTrailingEmpty ? new String[]{""} : new String[0];
        }
        String trimmed = input.replaceAll("^\\s+", "");
        String[] parts = trimmed.split("\\s+");
        if (keepTrailingEmpty && trimmed.endsWith(" ")) {
            String[] withEmpty = Arrays.copyOf(parts, parts.length + 1);
            withEmpty[parts.length] = "";
            return withEmpty;
        }
        return parts;
    }

    static String join(String[] args, int from) {
        return from >= args.length ? "" : String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    static List<String> playerNames(Subject src) {
        return Sponge.server().onlinePlayers().stream()
                .filter(p -> !(src instanceof ServerPlayer) || ((ServerPlayer) src).canSee(p) || src.hasPermission("uchat.see-vanish"))
                .map(ServerPlayer::name).collect(Collectors.toList());
    }

    static List<String> channelNames(Subject src) {
        return UChat.get().getChannels().values().stream().filter(key -> UChat.get().getPerms().channelWritePerm(src, key))
                .sorted(Comparator.comparing(UCChannel::getName)).map(UCChannel::getName).collect(Collectors.toList());
    }

    static ServerPlayer player(String name) throws CommandException {
        return Sponge.server().player(name).orElseThrow(() -> new CommandException(UChat.get().getLang().getText("listener.invalidplayer")));
    }

    static UCChannel channel(String name) throws CommandException {
        UCChannel ch = UChat.get().getChannel(name);
        if (ch == null) {
            throw new CommandException(UChat.get().getLang().getText("channel.dontexist"));
        }
        return ch;
    }

    private static String first(List<String> aliases) {
        return aliases.get(0);
    }

    private static String[] rest(List<String> aliases) {
        return aliases.subList(1, aliases.size()).toArray(new String[0]);
    }

    private static void register(RegisterCommandEvent<Command.Raw> event, PluginContainer plugin, Command.Raw cmd, List<String> aliases) {
        List<String> clean = aliases.stream().map(String::trim).filter(a -> !a.isEmpty()).map(String::toLowerCase).distinct().collect(Collectors.toList());
        if (clean.isEmpty()) {
            return;
        }
        try {
            event.register(plugin, cmd, first(clean), rest(clean));
        } catch (Exception e) {
            UChat.get().getLogger().warning("Could not register the command /" + first(clean) + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ registration

    public static void register(RegisterCommandEvent<Command.Raw> event, PluginContainer plugin) {
        register(event, plugin, uchat(), Arrays.asList("ultimatechat", "uchat", "chat"));

        if (UChat.get().getConfig().root().tell.enable) {
            List<String> tell = new ArrayList<>(UChat.get().getConfig().getTellAliases());
            tell.remove("r");
            register(event, plugin, tellCmd(), tell);
            register(event, plugin, respondCmd(), Collections.singletonList("r"));
        }
        if (UChat.get().getConfig().root().broadcast.enable) {
            register(event, plugin, broadcastCmd(), UChat.get().getConfig().getBroadcastAliases());
        }
        register(event, plugin, umsgCmd(), UChat.get().getConfig().getMsgAliases());
        register(event, plugin, chCmd(), UChat.get().getConfig().getChCmd());

        for (UCChannel ch : UChat.get().getChannels().values()) {
            register(event, plugin, channelCmd(ch.getName()), Arrays.asList(ch.getAlias(), ch.getName()));
        }
    }

    // ------------------------------------------------------------------ chat helpers

    public static void sendMessageToPlayer(ServerPlayer src, UCChannel ch, String msg) {
        if (UChat.get().mutes.contains(src.name()) || ch.isMuted(src.name())) {
            if (UChat.get().timeMute.containsKey(src.name())) {
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.tempmuted").replace("{time}", String.valueOf(UChat.get().timeMute.get(src.name()))));
            } else {
                UChat.get().getLang().sendMessage(src, "channel.muted");
            }
            return;
        }

        if (!UChat.get().getPerms().channelWritePerm(src, ch)) {
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
            return;
        }

        if (!ch.availableInWorld(src.world())) {
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
            return;
        }

        if (!ch.getPassword().isEmpty() && !ch.isMember(src)) {
            if (msg.split(" ").length != 1 || !ch.getPassword().equals(msg.split(" ")[0])) {
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
                return;
            }
            if (!UChat.get().getPerms().hasPerm(src, "password")) {
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("chat.nopermission"));
                return;
            }

            if (Sponge.eventManager().post(new PlayerChangeChannelEvent(src, UChat.get().getPlayerChannel(src), ch))) {
                return;
            }

            ch.addMember(src);
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
            return;
        }

        UCListener.chatInChannel(src, ch, msg);
    }

    public static void addPlayerToChannel(UCChannel ch, ServerPlayer src) {
        if (!ch.canLock()) {
            UChat.get().getLang().sendMessage(src, "help.channels.send");
            return;
        }
        if (ch.isMember(src)) {
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
            return;
        }
        if (!ch.getPassword().isEmpty()) {
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
            return;
        }

        if (Sponge.eventManager().post(new PlayerChangeChannelEvent(src, UChat.get().getPlayerChannel(src), ch))) {
            return;
        }

        ch.addMember(src);
        UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
    }

    /** /<channel alias> [message] */
    static void runChannelAlias(Subject src, UCChannel ch, String message) {
        message = message == null ? "" : message.trim();
        if (src instanceof ServerPlayer) {
            if (!message.isEmpty()) {
                sendMessageToPlayer((ServerPlayer) src, ch, message);
            } else {
                addPlayerToChannel(ch, (ServerPlayer) src);
            }
        } else if (!message.isEmpty()) {
            UCMessages.sendFancyMessage(new String[0], Component.text(message), ch, src, null);
        } else {
            UCUtil.send(src, getHelpChannel(src));
        }
    }

    // ------------------------------------------------------------------ commands

    private static Command.Raw channelCmd(String channelName) {
        // Looks the channel up when run, so a reload or chconfig that changes the channel is used
        return new RawCmd(s -> {
            UCChannel ch = UChat.get().getChannel(channelName);
            return ch != null && UChat.get().getPerms().channelWritePerm(s, ch);
        }, "Command to use channel " + channelName + ".", "[message]", (src, args) -> {
            UCChannel ch = channel(channelName);
            runChannelAlias(src, ch, join(args, 0));
        }, (src, args) -> playerNames(src));
    }

    private static Command.Raw chCmd() {
        return new RawCmd((String) null, "Join in a channel if you have permission.", "<channel>", (src, args) -> {
            if (!(src instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer p = (ServerPlayer) src;
            if (args.length == 0 || UChat.get().getChannel(args[0]) == null) {
                p.sendMessage(getHelpChannel(src));
                return;
            }
            UCChannel ch = UChat.get().getChannel(args[0]);
            if (!UChat.get().getPerms().channelReadPerm(p, ch) && !UChat.get().getPerms().channelWritePerm(p, ch)) {
                throw new CommandException(UCUtil.toText(UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName())));
            }
            if (!ch.availableInWorld(p.world())) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
                return;
            }
            if (ch.isMember(p)) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
                return;
            }
            if (!ch.getPassword().isEmpty()) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
                return;
            }

            if (Sponge.eventManager().post(new PlayerChangeChannelEvent(p, UChat.get().getPlayerChannel(src), ch))) {
                return;
            }

            ch.addMember(p);
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
        }, (src, args) -> args.length <= 1 ? channelNames(src) : null);
    }

    private static Command.Raw tellCmd() {
        return new RawCmd("uchat.cmd.tell", "Lock your chat with a player or send private messages.", "[player] [message]", (src, args) -> {
            String srcName = UCUtil.name(src);
            if (args.length == 0) {
                if (UChat.get().tellPlayers.containsKey(srcName)) {
                    String tp = UChat.get().tellPlayers.remove(srcName);
                    UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", tp));
                    return;
                }
                sendTellHelp(src);
                return;
            }

            Optional<ServerPlayer> receiver = Sponge.server().player(args[0]);
            String msg = join(args, 1);

            if (src instanceof ServerPlayer) {
                ServerPlayer p = (ServerPlayer) src;
                if (!msg.isEmpty()) {
                    if (receiver.isPresent()) {
                        if (receiver.get().equals(p)) {
                            throw new CommandException(UChat.get().getLang().getText("cmd.tell.self"));
                        }
                        UCListener.sendTell(p, receiver.get(), msg);
                    } else if (args[0].equalsIgnoreCase("console")) {
                        UCListener.sendTell(p, UCUtil.console(), msg);
                    } else {
                        UChat.get().getLang().sendMessage(p, "listener.invalidplayer");
                    }
                    return;
                }
                //lock tell
                if (receiver.isPresent()) {
                    ServerPlayer rec = receiver.get();
                    if (!p.canSee(rec) && !src.hasPermission("uchat.see-vanish")) {
                        throw new CommandException(UChat.get().getLang().getText("listener.invalidplayer"));
                    }
                    if (rec.equals(p)) {
                        throw new CommandException(UChat.get().getLang().getText("cmd.tell.self"));
                    }
                    if (rec.name().equals(UChat.get().tellPlayers.get(p.name()))) {
                        UChat.get().tellPlayers.remove(p.name());
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", rec.name()));
                    } else {
                        UChat.get().tellPlayers.put(p.name(), rec.name());
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.locked").replace("{player}", rec.name()));
                    }
                    return;
                }
            } else if (receiver.isPresent() && !msg.isEmpty()) {
                //console to player
                UCListener.sendTell(src, receiver.get(), msg);
                return;
            }

            sendTellHelp(src);
        }, (src, args) -> {
            List<String> names = playerNames(src);
            if (args.length <= 1) {
                names.add("console");
            }
            return names;
        });
    }

    private static Command.Raw respondCmd() {
        return new RawCmd("uchat.cmd.tell", "Respond to the private messages of other players.", "<message>", (src, args) -> {
            if (!(src instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer p = (ServerPlayer) src;
            String to = UChat.get().respondTell.get(p.name());
            if (to == null) {
                throw new CommandException(UChat.get().getLang().getText("cmd.tell.nonetorespond"));
            }
            if (args.length == 0) {
                sendTellHelp(src);
                return;
            }
            Subject receiver = UCListener.tellTarget(to);
            if (receiver == null) {
                throw new CommandException(UChat.get().getLang().getText("cmd.tell.nonetorespond"));
            }
            UCListener.sendTell(p, receiver, join(args, 0));
        }, (src, args) -> playerNames(src));
    }

    private static Command.Raw umsgCmd() {
        return new RawCmd("uchat.cmd.umsg", "Send a message directly to a player.", "<player> <message>", (src, args) -> {
            if (args.length < 2) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.umsg"));
            }
            ServerPlayer receiver = player(args[0]);
            String msg = join(args, 1).replace("\\n", "\n");

            TextComponent.Builder txtBuilder = Component.text().append(UCUtil.toText(msg));
            for (String arg : msg.split(" ")) {
                ClickEvent click = UCUtil.urlClick(arg);
                if (click != null) {
                    txtBuilder.clickEvent(click);
                    txtBuilder.hoverEvent(HoverEvent.showText(UCUtil.toText(UChat.get().getConfig().root().general.URL_template.replace("{url}", arg).replace("\\n", "\n"))));
                }
            }
            Component built = txtBuilder.build();
            receiver.sendMessage(built);
            UCUtil.console().sendMessage(UCUtil.toText("&8> Private to &6" + receiver.name() + "&8: &r" + UCUtil.toPlain(built)));
        }, (src, args) -> args.length <= 1 ? playerNames(src) : null);
    }

    private static Command.Raw broadcastCmd() {
        return new RawCmd("uchat.cmd.broadcast", "Command to send broadcast to server.", "<message>", (src, args) -> {
            if (!UCUtil.sendBroadcast(src, args, false)) {
                sendHelp(src);
            }
        }, null);
    }

    // ------------------------------------------------------------------ /uchat

    private static final class Sub {
        final String permission;
        final Exec exec;
        final BiFunction<Subject, String[], List<String>> completer;

        Sub(String permission, Exec exec, BiFunction<Subject, String[], List<String>> completer) {
            this.permission = permission;
            this.exec = exec;
            this.completer = completer;
        }

        boolean allowed(Subject src) {
            return permission == null || src instanceof SystemSubject || src.hasPermission(permission);
        }
    }

    private static Map<String, Sub> subCommands() {
        Map<String, Sub> subs = new LinkedHashMap<>();
        BiFunction<Subject, String[], List<String>> players = (src, args) -> args.length == 1 ? playerNames(src) : null;
        BiFunction<Subject, String[], List<String>> channels = (src, args) -> args.length == 1 ? channelNames(src) : null;

        subs.put("?", new Sub(null, (src, args) -> sendHelp(src), null));

        subs.put("chconfig", new Sub("uchat.cmd.chconfig", (src, args) -> {
            //uchat chconfig <channel> <key> <value>
            if (args.length < 3) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.chconfig"));
            }
            UCChannel ch = channel(args[0]);
            String key = args[1];
            String value = join(args, 2);
            if (!ch.getProperties().containsKey(key)) {
                throw new CommandException(UChat.get().getLang().getText("cmd.chconfig.invalidkey"));
            }

            UChat.get().getConfig().delChannel(ch);
            ch.setProperty(key, value);
            try {
                UChat.get().getConfig().addChannel(ch);
            } catch (Exception e) {
                UChat.get().getLogger().severe("Could not save channel " + ch.getName(), e);
            }
            UChat.get().getLang().sendMessage(src, "cmd.chconfig.success");
        }, (src, args) -> {
            if (args.length == 1) return channelNames(src);
            if (args.length == 2) {
                return new UCChannel("keys", "k", "&b").getProperties().keySet().stream().map(Object::toString).sorted().collect(Collectors.toList());
            }
            return null;
        }));

        subs.put("delchannel", new Sub("uchat.cmd.delchannel", (src, args) -> {
            //uchat delchannel <channel>
            if (args.length < 1) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.delchannel"));
            }
            UCChannel ch = channel(args[0]);

            List<String> toAdd = new ArrayList<>(ch.getMembers());
            UChat.get().getConfig().delChannel(ch);
            UCChannel def = UChat.get().getDefChannel(src instanceof ServerPlayer ? ((ServerPlayer) src).world() : null);
            if (def != null) {
                toAdd.forEach(m -> {
                    def.addMember(m);
                    Sponge.server().player(m).ifPresent(pm -> Sponge.eventManager().post(new PlayerChangeChannelEvent(pm, null, def)));
                });
            }
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.delchannel.success").replace("{channel}", ch.getName()));
        }, channels));

        subs.put("newchannel", new Sub("uchat.cmd.newchannel", (src, args) -> {
            //uchat newchannel <channel> <alias> <color>
            if (args.length < 3) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.newchannel"));
            }
            String color = args[2];
            if (color.length() != 2 || !color.matches("(&([a-fk-or0-9]))$")) {
                throw new CommandException(UChat.get().getLang().getText("channel.invalidcolor"));
            }
            UCChannel newch = new UCChannel(args[0], args[1], color);
            try {
                UChat.get().getConfig().addChannel(newch);
            } catch (Exception e) {
                UChat.get().getLogger().severe("Could not save channel " + newch.getName(), e);
            }
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.newchannel.success").replace("{channel}", newch.getName()));
        }, null));

        subs.put("reload", new Sub("uchat.cmd.reload", (src, args) -> {
            try {
                UChat.get().reload();
                UChat.get().getLang().sendMessage(src, "plugin.reloaded");
            } catch (Exception e) {
                UChat.get().getLogger().severe("Could not reload UltimateChat", e);
            }
        }, null));

        subs.put("clear", new Sub("uchat.cmd.clear", (src, args) -> {
            if (src instanceof ServerPlayer) {
                ServerPlayer p = (ServerPlayer) src;
                for (int i = 0; i < 100; i++) {
                    p.sendMessage(Component.text(" "));
                }
                UChat.get().getLang().sendMessage(src, "cmd.clear.cleared");
            }
        }, null));

        subs.put("clear-all", new Sub("uchat.cmd.clear-all", (src, args) -> {
            for (ServerPlayer play : Sponge.server().onlinePlayers()) {
                for (int i = 0; i < 100; i++) {
                    play.sendMessage(Component.text(" "));
                }
            }
        }, null));

        subs.put("spy", new Sub("uchat.cmd.spy", (src, args) -> {
            if (src instanceof ServerPlayer) {
                ServerPlayer p = (ServerPlayer) src;
                if (!UChat.get().isSpy.contains(p.name())) {
                    UChat.get().isSpy.add(p.name());
                    UChat.get().getLang().sendMessage(src, "cmd.spy.enabled");
                } else {
                    UChat.get().isSpy.remove(p.name());
                    UChat.get().getLang().sendMessage(src, "cmd.spy.disabled");
                }
            }
        }, null));

        subs.put("ignore", new Sub(null, (src, args) -> {
            if (!(src instanceof ServerPlayer) || args.length < 2) {
                sendHelp(src);
                return;
            }
            ServerPlayer p = (ServerPlayer) src;
            if (args[0].equalsIgnoreCase("player")) {
                if (!p.hasPermission("uchat.cmd.ignore.player")) {
                    throw new CommandException(UChat.get().getLang().getText("chat.nopermission"));
                }
                ServerPlayer pi = player(args[1]);
                if (pi.equals(p)) {
                    throw new CommandException(UChat.get().getLang().getText("cmd.ignore.self"));
                }
                if (!UChat.get().getPerms().canIgnore(p, pi)) {
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
                    return;
                }
                if (UCMessages.isIgnoringPlayers(p.name(), pi.name())) {
                    UCMessages.unIgnorePlayer(p.name(), pi.name());
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.unignoring").replace("{player}", pi.name()));
                } else {
                    UCMessages.ignorePlayer(p.name(), pi.name());
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.ignoring").replace("{player}", pi.name()));
                }
            } else if (args[0].equalsIgnoreCase("channel")) {
                if (!p.hasPermission("uchat.cmd.ignore.channel")) {
                    throw new CommandException(UChat.get().getLang().getText("chat.nopermission"));
                }
                UCChannel ch = channel(args[1]);
                if (!UChat.get().getPerms().canIgnore(p, ch)) {
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
                    return;
                }
                if (ch.isIgnoring(p.name())) {
                    ch.unIgnoreThis(p.name());
                    UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.notignoring").replace("{channel}", ch.getName()));
                } else {
                    ch.ignoreThis(p.name());
                    UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.ignoring").replace("{channel}", ch.getName()));
                }
            } else {
                sendHelp(src);
            }
        }, (src, args) -> {
            if (args.length == 1) return Arrays.asList("player", "channel");
            if (args.length == 2) return args[0].equalsIgnoreCase("player") ? playerNames(src) : channelNames(src);
            return null;
        }));

        subs.put("mute", new Sub("uchat.cmd.mute", (src, args) -> {
            //uchat mute player [channel]
            if (args.length < 1) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.mute"));
            }
            ServerPlayer play = player(args[0]);
            if (args.length > 1) {
                UCChannel ch = channel(args[1]);
                if (ch.isMuted(play.name())) {
                    ch.unMuteThis(play.name());
                    UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.unmuted.this").replace("{player}", play.name()).replace("{channel}", ch.getName()));
                    UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.unmuted.this").replace("{channel}", ch.getName()));
                } else {
                    ch.muteThis(play.name());
                    UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.muted.this").replace("{player}", play.name()).replace("{channel}", ch.getName()));
                    UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.muted.this").replace("{channel}", ch.getName()));
                }
            } else if (UChat.get().mutes.contains(play.name())) {
                UChat.get().mutes.remove(play.name());
                UChat.get().unMuteInAllChannels(play.name());
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.unmuted.all").replace("{player}", play.name()));
                UChat.get().getLang().sendMessage(play, "channel.player.unmuted.all");
            } else {
                UChat.get().mutes.add(play.name());
                UChat.get().muteInAllChannels(play.name());
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.muted.all").replace("{player}", play.name()));
                UChat.get().getLang().sendMessage(play, "channel.player.muted.all");
            }
        }, (src, args) -> args.length == 1 ? playerNames(src) : args.length == 2 ? channelNames(src) : null));

        subs.put("tempmute", new Sub("uchat.cmd.tempmute", (src, args) -> {
            //uchat tempmute time player
            if (args.length < 2) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.tempmute"));
            }
            int time;
            try {
                time = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.tempmute"));
            }
            ServerPlayer play = player(args[1]);
            if (UChat.get().mutes.contains(play.name())) {
                UChat.get().getLang().sendMessage(src, "channel.already.muted");
            } else {
                UChat.get().mutes.add(play.name());
                UChat.get().muteInAllChannels(play.name());
                UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.tempmuted.all").replace("{player}", play.name()).replace("{time}", String.valueOf(time)));
                UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.tempmuted.all").replace("{time}", String.valueOf(time)));
                MuteCountDown.start(play.name(), time, "Mute Counter");
            }
        }, (src, args) -> args.length == 2 ? playerNames(src) : null));

        subs.put("msgtoggle", new Sub("uchat.cmd.msgtoggle", (src, args) -> {
            ServerPlayer p;
            if (args.length > 0) {
                if (!(src instanceof SystemSubject) && !src.hasPermission("uchat.msgtoggle.others")) {
                    throw new CommandException(UChat.get().getLang().getText("chat.nopermission"));
                }
                p = player(args[0]);
            } else if (src instanceof ServerPlayer) {
                p = (ServerPlayer) src;
            } else {
                throw new CommandException(Component.text("Only players can use this command or add a player name as last argument."));
            }
            if (UChat.get().msgTogglePlayers.contains(p.name())) {
                UChat.get().msgTogglePlayers.remove(p.name());
                UChat.get().getLang().sendMessage(p, "cmd.msgtoggle.enabled");
            } else {
                UChat.get().msgTogglePlayers.add(p.name());
                UChat.get().getLang().sendMessage(p, "cmd.msgtoggle.disabled");
            }
        }, players));

        subs.put("addfilter", new Sub("uchat.cmd.addfilter", (src, args) -> {
            //uchat addfilter word:replace
            if (args.length < 1 || !args[0].contains(":")) {
                throw new CommandException(UCUtil.toText(UChat.get().getLang().get("cmd.addfilter.invalid")));
            }
            String[] pair = args[0].split(":", 2);
            UChat.get().getConfig().protections().censor.replace_words.put(pair[0], pair[1]);
            UChat.get().getConfig().save();
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.addfilter.added").replace("{filter}", args[0]));
        }, null));

        subs.put("dellfilter", new Sub("uchat.cmd.addfilter", (src, args) -> {
            if (args.length < 1) {
                throw new CommandException(UChat.get().getLang().getText("help.cmd.delfilter"));
            }
            UChat.get().getConfig().protections().censor.replace_words.remove(args[0]);
            UChat.get().getConfig().save();
            UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.addfilter.removed").replace("{filter}", args[0]));
        }, (src, args) -> args.length == 1 ? new ArrayList<>(UChat.get().getConfig().protections().censor.replace_words.keySet()) : null));

        return subs;
    }

    private static Command.Raw uchat() {
        Map<String, Sub> subs = subCommands();
        return new RawCmd((String) null, "Main command for uchat.", "[" + String.join("|", subs.keySet()) + "]", (src, args) -> {
            if (args.length == 0) {
                UCUtil.send(src, UCUtil.toText("&b---------------- " + UChat.get().instance().metadata().name().orElse("UltimateChat") + " " + UChat.get().version() + " ---------------"));
                UCUtil.send(src, UCUtil.toText("&bDeveloped by &6FabioZumbi12&b, SpongeAPI 21 port by AlmuraDev."));
                UCUtil.send(src, UCUtil.toText("&bFor more information about the commands, type [&6/uchat ?&b]."));
                UCUtil.send(src, UCUtil.toText("&b---------------------------------------------------"));
                return;
            }
            Sub sub = subs.get(args[0].toLowerCase(Locale.ROOT));
            if (sub == null) {
                sendHelp(src);
                return;
            }
            if (!sub.allowed(src)) {
                throw new CommandException(UChat.get().getLang().getText("chat.nopermission"));
            }
            sub.exec.run(src, Arrays.copyOfRange(args, 1, args.length));
        }, (src, args) -> {
            if (args.length <= 1) {
                return subs.entrySet().stream().filter(e -> e.getValue().allowed(src)).map(Map.Entry::getKey).collect(Collectors.toList());
            }
            Sub sub = subs.get(args[0].toLowerCase(Locale.ROOT));
            if (sub == null || sub.completer == null || !sub.allowed(src)) {
                return null;
            }
            return sub.completer.apply(src, Arrays.copyOfRange(args, 1, args.length));
        });
    }

    // ------------------------------------------------------------------ help

    private static void sendHelp(Subject p) {
        TextComponent.Builder fancy = Component.text();

        fancy.append(UCUtil.toText("\n&7--------------- " + UChat.get().getLang().get("_UChat.prefix") + " Help &7---------------\n"));
        fancy.append(UChat.get().getLang().getText("help.channels.enter", "\n"));
        fancy.append(UChat.get().getLang().getText("help.channels.send", "\n"));
        fancy.append(UChat.get().getLang().getText("help.channels.list", "\n"));
        if (UChat.get().getPerms().cmdPerm(p, "tell")) {
            fancy.append(UChat.get().getLang().getText("help.tell.lock", "\n"));
            fancy.append(UChat.get().getLang().getText("help.tell.send", "\n"));
            fancy.append(UChat.get().getLang().getText("help.tell.respond", "\n"));
        }
        String[][] cmds = {
                {"addfilter", "help.cmd.addfilter"}, {"addfilter", "help.cmd.delfilter"}, {"broadcast", "help.cmd.broadcast"},
                {"umsg", "help.cmd.umsg"}, {"clear", "help.cmd.clear"}, {"clear-all", "help.cmd.clear-all"}, {"spy", "help.cmd.spy"},
                {"mute", "help.cmd.mute"}, {"tempmute", "help.cmd.tempmute"}, {"ignore.player", "help.cmd.ignore.player"},
                {"ignore.channel", "help.cmd.ignore.channel"}, {"chconfig", "help.cmd.chconfig"}, {"reload", "help.cmd.reload"},
                {"newchannel", "help.cmd.newchannel"}, {"delchannel", "help.cmd.delchannel"}
        };
        for (String[] cmd : cmds) {
            if (UChat.get().getPerms().cmdPerm(p, cmd[0])) {
                fancy.append(UChat.get().getLang().getText(cmd[1], "\n"));
            }
        }
        fancy.append(getHelpChannel(p));
        UCUtil.send(p, fancy.build());
    }

    private static Component getHelpChannel(Subject p) {
        TextComponent.Builder fancy = Component.text();
        fancy.append(UCUtil.toText("&7------------------------------------------\n"));
        fancy.append(UCUtil.toText(UChat.get().getLang().get("help.channels.available").replace("{channels}", "") + " "));

        boolean first = true;
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (!(p instanceof ServerPlayer) || UChat.get().getPerms().channelWritePerm(p, ch) && ch.availableInWorld(((ServerPlayer) p).world())) {
                Component name = UCUtil.toText((first ? " " : "&a, ") + ch.getColor() + ch.getName())
                        .hoverEvent(HoverEvent.showText(UCUtil.toText(ch.getColor() + "Alias: " + ch.getAlias())))
                        .clickEvent(ClickEvent.runCommand("/" + ch.getAlias()));
                first = false;
                fancy.append(name);
            }
        }
        fancy.append(UCUtil.toText("\n&7------------------------------------------ "));
        if (UChat.get().getPerms().hasPerm(p, "admin")) {
            fancy.append(UCUtil.toText("\n&8&o- UChat version: " + UChat.get().version() + " (SpongeAPI 21)"));
        }
        return fancy.build();
    }

    private static void sendTellHelp(Subject p) {
        UCUtil.send(p, UCUtil.toText("&7--------------- " + UChat.get().getLang().get("_UChat.prefix") + " Tell Help &7---------------"));
        UCUtil.send(p, UCUtil.toText(UChat.get().getLang().get("help.tell.unlock")));
        UCUtil.send(p, UCUtil.toText(UChat.get().getLang().get("help.tell.lock")));
        UCUtil.send(p, UCUtil.toText(UChat.get().getLang().get("help.tell.send")));
        UCUtil.send(p, UCUtil.toText(UChat.get().getLang().get("help.tell.respond")));
    }
}
