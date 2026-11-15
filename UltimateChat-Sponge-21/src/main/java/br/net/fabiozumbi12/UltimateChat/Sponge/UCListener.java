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

import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCLogger;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

public class UCListener {

    /**
     * Sends a private message, after checking that the receiver is online and visible to the sender.
     */
    static void sendTell(Subject sender, Subject receiver, String msg) {
        if (receiver == null || (receiver instanceof ServerPlayer && (!((ServerPlayer) receiver).isOnline()
                || (sender instanceof ServerPlayer && !((ServerPlayer) sender).canSee((ServerPlayer) receiver) && !sender.hasPermission("uchat.see-vanish"))))) {
            UChat.get().getLang().sendMessage(sender, "listener.invalidplayer");
            return;
        }
        UChat.get().respondTell.put(UCUtil.name(receiver), UCUtil.isConsole(sender) ? "CONSOLE" : UCUtil.name(sender));
        UCMessages.sendFancyMessage(new String[0], Component.text(msg), null, sender, receiver);
    }

    static Subject tellTarget(String name) {
        if (name.equals("CONSOLE")) {
            return UCUtil.console();
        }
        return Sponge.server().player(name).orElse(null);
    }

    /**
     * Sends a chat message from a player to a channel, as if typed with that channel selected.
     */
    static void chatInChannel(ServerPlayer p, UCChannel ch, String msg) {
        if (ch == null) {
            UChat.get().getLang().sendMessage(p, "channel.dontexist");
            return;
        }
        if (UChat.get().mutes.contains(p.name()) || ch.isMuted(p.name())) {
            if (UChat.get().timeMute.containsKey(p.name())) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.tempmuted").replace("{time}", String.valueOf(UChat.get().timeMute.get(p.name()))));
            } else {
                UChat.get().getLang().sendMessage(p, "channel.muted");
            }
            return;
        }

        if (ch.isCmdAlias()) {
            String start = ch.getAliasCmd();
            if (start.startsWith("/")) {
                start = start.substring(1);
            }
            if (ch.getAliasSender().equalsIgnoreCase("console")) {
                UChat.get().runConsoleCommand(start + " " + msg);
            } else {
                UChat.get().runCommand(p, start + " " + msg);
            }
            return;
        }

        String display = p.get(org.spongepowered.api.data.Keys.DISPLAY_NAME).map(UCUtil::toPlain).orElse(p.name());
        UCMessages.sendFancyMessage(new String[]{"<" + display + "> ", msg, ""}, Component.text(msg), ch, p, null);
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent e) {
        if (UChat.get().getConfig().addWorld(e.world())) {
            UChat.get().getConfig().save();
        }
    }

    @Listener(order = Order.LATE)
    public void onChat(PlayerChatEvent.Submit e) {
        Optional<ServerPlayer> optPlayer = e.player();
        if (!optPlayer.isPresent()) {
            return;
        }
        ServerPlayer p = optPlayer.get();
        // UltimateChat formats and sends every chat message itself
        e.setCancelled(true);

        String rawMsg = UCUtil.toPlain(e.originalMessage());
        UChat.get().getLogger().debug("PlayerChatEvent.Submit: " + rawMsg);

        //check channel char
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.getCharAlias().isEmpty()) continue;

            if (rawMsg.equalsIgnoreCase(ch.getCharAlias())) {
                UCCommands.addPlayerToChannel(ch, p);
                return;
            }
            if (rawMsg.startsWith(ch.getCharAlias())) {
                UCCommands.sendMessageToPlayer(p, ch, rawMsg.substring(ch.getCharAlias().length()));
                return;
            }
        }

        UChat.get().getLogger().timings(UCLogger.timingType.START, "UCListener#onChat()|Listening PlayerChatEvent");

        if (UChat.get().tellPlayers.containsKey(p.name())) {
            sendTell(p, tellTarget(UChat.get().tellPlayers.get(p.name())), rawMsg);
            return;
        }

        chatInChannel(p, UChat.get().getPlayerChannel(p), rawMsg);
    }

    /**
     * Blocks commands for muted players, logs commands to Discord, and runs the aliases of channels added after the server
     * started (only the channels present at start are registered as commands).
     */
    @Listener(order = Order.POST)
    public void onCommand(ExecuteCommandEvent.Pre e) {
        Object root = e.commandCause().root();
        String cmd = e.command();

        if (root instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer) root;
            if (UChat.get().mutes.contains(p.name()) && UChat.get().getConfig().root().general.muted_deny_cmds.contains(cmd)) {
                UChat.get().getLang().sendMessage(p, "player.muted.denycmd");
                e.setResult(CommandResult.success());
                e.setCancelled(true);
                return;
            }
        }

        if (UChat.get().getUCJDA() != null) {
            String name = root instanceof ServerPlayer ? ((ServerPlayer) root).name() : UCUtil.CONSOLE_NAME;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            UChat.get().getUCJDA().sendCommandsToDiscord(UChat.get().getLang().get("discord.command")
                    .replace("{player}", name)
                    .replace("{cmd}", "/" + cmd + " " + e.arguments())
                    .replace("{time-now}", sdf.format(Calendar.getInstance().getTime())));
        }

        if (Sponge.server().commandManager().commandMapping(cmd).isEmpty() && UChat.get().getChannel(cmd) != null) {
            Subject src = root instanceof ServerPlayer ? (ServerPlayer) root : UCUtil.console();
            UCCommands.runChannelAlias(src, UChat.get().getChannel(cmd), e.arguments());
            e.setResult(CommandResult.success());
        }
    }

    @Listener
    public void onDeath(DestructEntityEvent.Death e) {
        if (!(e.entity() instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer) e.entity();
        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getConfig().root().discord.vanish_perm)) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.death").replace("{player}", p.name()));
        }
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join e) {
        ServerPlayer p = e.player();
        if (!UChat.get().getConfig().root().general.persist_channels) {
            UCChannel def = UChat.get().getDefChannel(p.world());
            if (def != null) {
                def.addMember(p);
            }
        }
        if (p.hasPermission("uchat.auto-msgtoggle")) {
            UChat.get().msgTogglePlayers.remove(p.name());
        }

        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getConfig().root().discord.vanish_perm)) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.join").replace("{player}", p.name()));
        }
        if (UChat.get().getConfig().root().general.spy_enabled_onjoin && p.hasPermission("uchat.cmd.spy") && !UChat.get().isSpy.contains(p.name())) {
            UChat.get().isSpy.add(p.name());
            UChat.get().getLang().sendMessage(p, "cmd.spy.enabled");
        }
    }

    @Listener
    public void onQuit(ServerSideConnectionEvent.Leave e) {
        ServerPlayer p = e.player();
        String name = p.name();
        if (!UChat.get().getConfig().root().general.persist_channels) {
            UChat.get().getPlayerChannel(p).removeMember(p);
        }

        UChat.get().tellPlayers.remove(name);
        UChat.get().tellPlayers.entrySet().removeIf(k -> k.getValue().equals(name));
        UChat.get().respondTell.remove(name);
        UChat.get().respondTell.entrySet().removeIf(k -> k.getValue().equals(name));

        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getConfig().root().discord.vanish_perm)) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.leave").replace("{player}", name));
        }
    }

    @Listener
    public void onChangeWorld(ChangeEntityWorldEvent.Post e) {
        if (!(e.entity() instanceof ServerPlayer) || !UChat.get().getConfig().root().general.check_channel_change_world) return;
        ServerPlayer p = (ServerPlayer) e.entity();

        ServerWorld tw = e.destinationWorld();
        UCChannel pch = UChat.get().getPlayerChannel(p);

        String toCh = "";
        if (!pch.availableInWorld(tw)) {
            UCChannel def = UChat.get().getDefChannel(tw);
            if (def != null && def.availableInWorld(tw)) {
                def.addMember(p);
                toCh = def.getName();
            } else {
                for (UCChannel ch : UChat.get().getChannels().values()) {
                    if (ch.availableInWorld(tw)) {
                        ch.addMember(p);
                        toCh = ch.getName();
                        break;
                    }
                }
            }
        }
        if (!toCh.isEmpty()) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", toCh));
        } else if (!pch.availableInWorld(tw)) {
            pch.removeMember(p);
            UChat.get().getLang().sendMessage(p, "channel.world.none");
        }
    }
}
