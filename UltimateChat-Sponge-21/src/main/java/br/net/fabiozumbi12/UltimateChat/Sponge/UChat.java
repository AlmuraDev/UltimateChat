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

import br.net.fabiozumbi12.UltimateChat.Sponge.API.UChatReloadEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.MainCategory;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCLang;
import br.net.fabiozumbi12.UltimateChat.Sponge.discord.UCDiscord;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCLogger;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCPerms;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Plugin("ultimatechat")
public class UChat {

    private static UChat uchat;

    public final Map<String, String> tellPlayers = Collections.synchronizedMap(new HashMap<>());
    public final Map<String, String> respondTell = Collections.synchronizedMap(new HashMap<>());
    public final List<String> mutes = Collections.synchronizedList(new ArrayList<>());
    public final List<String> isSpy = Collections.synchronizedList(new ArrayList<>());
    public final Map<String, Integer> timeMute = Collections.synchronizedMap(new HashMap<>());
    public final List<String> msgTogglePlayers = Collections.synchronizedList(new ArrayList<>());
    public final Map<String, List<String>> ignoringPlayer = Collections.synchronizedMap(new HashMap<>());

    private final PluginContainer plugin;
    private final UCLogger logger;
    private final Path configDir;
    private UCConfig config;
    private UCPerms perms;
    private uChatAPI ucapi;
    private volatile UCDiscord UCJDA;
    private volatile boolean serverStarted;
    private UCLang lang;
    private HashMap<List<String>, UCChannel> channels;

    @Inject
    public UChat(PluginContainer plugin, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        this.plugin = plugin;
        this.logger = new UCLogger(logger);
        // The SpongeAPI 7 versions used config/UltimateChat; keep using it where it exists (file names are case sensitive on Linux)
        Path legacy = configDir.resolveSibling("UltimateChat");
        this.configDir = !Files.isDirectory(configDir) && Files.isDirectory(legacy) ? legacy : configDir;
    }

    public static UChat get() {
        return uchat;
    }

    public UCLogger getLogger() {
        return logger;
    }

    public File configDir() {
        return this.configDir.toFile();
    }

    public PluginContainer instance() {
        return this.plugin;
    }

    public String version() {
        return this.plugin.metadata().version().toString();
    }

    public UCConfig getConfig() {
        return this.config;
    }

    public UCPerms getPerms() {
        return this.perms;
    }

    public EconomyService getEco() {
        return Sponge.isServerAvailable() ? Sponge.server().serviceProvider().economyService().orElse(null) : null;
    }

    public uChatAPI getAPI() {
        return this.ucapi;
    }

    public UCDiscord getUCJDA() {
        return this.UCJDA;
    }

    public UCLang getLang() {
        return this.lang;
    }

    public HashMap<List<String>, UCChannel> getChannels() {
        return this.channels;
    }

    public void setChannels(HashMap<List<String>, UCChannel> channels) {
        this.channels = channels;
    }

    // ------------------------------------------------------------------ lifecycle

    @Listener
    public void onConstruct(ConstructPluginEvent event) {
        uchat = this;
        try {
            this.config = new UCConfig();
            this.lang = new UCLang();
        } catch (IOException e) {
            this.logger.severe("Could not load the UltimateChat configuration", e);
        }
        this.ucapi = new uChatAPI();
        Sponge.eventManager().registerListeners(this.plugin, new UCListener(), MethodHandles.lookup());
    }

    /**
     * Commands are only registered here, when the server starts (and on /reload of data packs).
     */
    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Raw> event) {
        if (this.config != null) {
            UCCommands.register(event, this.plugin);
        }
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        this.perms = new UCPerms(event.engine().serviceProvider().permissionService());
        this.config.addWorlds(event.engine().worldManager().worlds());
        this.serverStarted = true;
        startDiscord(true);

        this.logger.logClear("\n"
                + "  _    _ _ _   _                 _        _____ _           _  \n"
                + " | |  | | | | (_)               | |      / ____| |         | |  \n"
                + " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
                + " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
                + " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
                + "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
                + "                                                                \n"
                + "UltimateChat " + version() + " enabled!\n");
    }

    @Listener
    public void onServerStopping(StoppingEngineEvent<Server> event) {
        this.serverStarted = false;
        UCDiscord discord = this.UCJDA;
        this.UCJDA = null;
        if (discord != null) {
            discord.sendRawToDiscordNow(this.lang.get("discord.stop"));
            discord.shutdown();
        }
        this.logger.info("UltimateChat disabled!");
    }

    @Listener
    public void onRefresh(RefreshGameEvent event) {
        try {
            reload();
        } catch (IOException e) {
            this.logger.severe("Could not reload UltimateChat", e);
        }
    }

    public void reload() throws IOException {
        Map<String, List<String>> members = new HashMap<>();
        if (this.channels != null) {
            this.channels.values().forEach(ch -> members.put(ch.getName().toLowerCase(), ch.getMembers()));
        }
        this.channels = null;
        this.config = new UCConfig();
        this.lang = new UCLang();
        // Keep who is in which channel
        this.channels.values().forEach(ch -> {
            List<String> m = members.get(ch.getName().toLowerCase());
            if (m != null) ch.setMembers(m);
        });
        if (Sponge.isServerAvailable()) {
            this.config.addWorlds(Sponge.server().worldManager().worlds());
        }

        startDiscord(false);

        Sponge.eventManager().post(new UChatReloadEvent());
    }

    /**
     * (Re)connects the Discord relay off the server thread, as connecting blocks until Discord is ready.
     */
    private void startDiscord(boolean serverStart) {
        final MainCategory.DiscordCat discord = this.config.root().discord;
        Sponge.asyncScheduler().submit(Task.builder().plugin(this.plugin).execute(() -> {
            UCDiscord old = this.UCJDA;
            this.UCJDA = null;
            if (old != null) {
                old.shutdown();
            }
            if (!discord.use) {
                return;
            }
            UCDiscord jda = new UCDiscord(this);
            if (!jda.JDAAvailable()) {
                this.logger.warning("JDA is not available due errors before.");
                jda.shutdown();
                return;
            }
            this.UCJDA = jda;
            this.logger.info("JDA connected and ready to use!");
            if (serverStart && this.serverStarted) {
                jda.sendRawToDiscord(this.lang.get("discord.start"));
                jda.sendRawToDiscord(this.lang.get("discord.online"));
            }
        }).build(), "UltimateChat Discord connect");
    }

    // ------------------------------------------------------------------ commands

    public void runConsoleCommand(String command) {
        try {
            Sponge.server().commandManager().process(Sponge.systemSubject(), Sponge.systemSubject(), command);
        } catch (CommandException e) {
            this.logger.warning("Command '" + command + "' failed: " + e.getMessage());
        }
    }

    public void runCommand(ServerPlayer player, String command) {
        try {
            Sponge.server().commandManager().process(player, command);
        } catch (CommandException e) {
            player.sendMessage(e.componentMessage() != null ? e.componentMessage() : net.kyori.adventure.text.Component.text(String.valueOf(e.getMessage())));
        }
    }

    //----------- channels
    public UCChannel getChannel(String alias) {
        if (alias == null || this.channels == null) {
            return null;
        }
        for (Map.Entry<List<String>, UCChannel> entry : this.channels.entrySet()) {
            if (entry.getKey().contains(alias.toLowerCase())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void unMuteInAllChannels(String player) {
        for (UCChannel ch : this.channels.values()) {
            if (ch.isMuted(player)) {
                ch.unMuteThis(player);
            }
        }
    }

    public void muteInAllChannels(String player) {
        for (UCChannel ch : this.channels.values()) {
            if (!ch.isMuted(player)) {
                ch.muteThis(player);
            }
        }
    }

    public UCChannel getDefChannel(ServerWorld world) {
        UCChannel ch = getChannel(this.config.root().general.default_channels.default_channel);
        if (world != null) {
            MainCategory.WorldInfo info = this.config.worldInfo(world);
            if (info != null) {
                UCChannel wch = getChannel(info.channel);
                if (wch == null) {
                    this.logger.warning("Default channel not found with alias '" + info.channel + "'. Fix this setting to a valid channel alias.");
                } else {
                    ch = wch;
                }
            }
        }
        return ch;
    }

    public List<String> getChAliases() {
        List<String> aliases = new ArrayList<>();
        for (List<String> alias : this.channels.keySet()) {
            if (alias != null) {
                aliases.addAll(alias);
            }
        }
        return aliases;
    }

    public UCChannel getPlayerChannel(Subject p) {
        for (UCChannel ch : this.channels.values()) {
            if (ch.isMember(p)) {
                return ch;
            }
        }
        return p instanceof ServerPlayer ? getDefChannel(((ServerPlayer) p).world()) : getDefChannel(null);
    }
}
