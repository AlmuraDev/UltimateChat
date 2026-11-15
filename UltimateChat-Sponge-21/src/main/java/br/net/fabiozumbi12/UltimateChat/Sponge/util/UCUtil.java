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

package br.net.fabiozumbi12.UltimateChat.Sponge.util;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Text and sender helpers. A "sender" is a {@link ServerPlayer} or the console ({@link SystemSubject}); both are a
 * {@link Subject} and an {@link Audience}.
 */
public class UCUtil {

    /** Name used for the console, as SpongeAPI 21 gives the system subject no name of its own. */
    public static final String CONSOLE_NAME = "Console";

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public static Component toText(String str) {
        if (str == null || str.isEmpty()) {
            return Component.empty();
        }
        return LEGACY.deserialize(str.replace('§', '&'));
    }

    public static String toPlain(Component text) {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    public static String toColor(String str) {
        return str.replaceAll("(&([A-Fa-fK-Ok-oRr0-9]))", "§$2");
    }

    public static String stripColor(char symbol, String str) {
        return str.replaceAll("(" + symbol + "([A-Fa-fK-Ok-oRr0-9]))", "");
    }

    /** Opens the URL if the string is a full http(s) link, the same links the old java.net.URL check accepted. */
    public static ClickEvent urlClick(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            if (uri.getScheme() != null && uri.getHost() != null) {
                return ClickEvent.openUrl(uri.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String name(Object sender) {
        if (sender instanceof ServerPlayer) {
            return ((ServerPlayer) sender).name();
        }
        if (sender instanceof SystemSubject) {
            return CONSOLE_NAME;
        }
        if (sender instanceof Subject) {
            return ((Subject) sender).friendlyIdentifier().orElse(((Subject) sender).identifier());
        }
        return String.valueOf(sender);
    }

    public static void send(Object receiver, Component text) {
        if (receiver instanceof Audience) {
            ((Audience) receiver).sendMessage(text);
        }
    }

    public static boolean isConsole(Object sender) {
        return sender instanceof SystemSubject;
    }

    public static SystemSubject console() {
        return Sponge.systemSubject();
    }

    // ---------------- worlds ----------------
    // Old configs name worlds by their 1.12 folder name; SpongeAPI 21 names them by key. A world matches either its full key
    // ("minecraft:overworld") or its key value ("overworld").

    public static String worldName(ServerWorld world) {
        return world.key().value();
    }

    public static String worldKey(ServerWorld world) {
        return world.key().formatted();
    }

    public static boolean worldIn(ServerWorld world, List<String> names) {
        if (names == null) {
            return false;
        }
        ResourceKey key = world.key();
        for (String n : names) {
            if (n.equalsIgnoreCase(key.formatted()) || n.equalsIgnoreCase(key.value())) {
                return true;
            }
        }
        return false;
    }

    /** "minecraft:diamond_sword" -> "Diamond Sword" */
    public static String humanize(ResourceKey key) {
        StringBuilder sb = new StringBuilder();
        for (String part : key.value().split("[_/]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return sb.toString();
    }

    public static boolean sendBroadcast(Subject src, String[] args, boolean silent) {
        StringBuilder message = new StringBuilder();
        StringBuilder hover = new StringBuilder();
        StringBuilder cmdline = new StringBuilder();
        StringBuilder url = new StringBuilder();
        boolean isHover = false;
        boolean isCmd = false;
        boolean isUrl = false;
        for (String arg : args) {
            arg = arg.replace("\\n", "\n");
            if (arg.contains(UChat.get().getConfig().root().broadcast.on_hover)) {
                hover.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.on_hover, ""));
                isHover = true;
                isCmd = false;
                isUrl = false;
                continue;
            }
            if (arg.contains(UChat.get().getConfig().root().broadcast.on_click)) {
                cmdline.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.on_click, ""));
                isCmd = true;
                isHover = false;
                isUrl = false;
                continue;
            }
            if (arg.contains(UChat.get().getConfig().root().broadcast.url)) {
                url.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.url, ""));
                isCmd = false;
                isHover = false;
                isUrl = true;
                continue;
            }

            if (isCmd) {
                cmdline.append(" ").append(arg);
            } else if (isHover) {
                hover.append(" ").append(arg);
            } else if (isUrl) {
                url.append(" ").append(arg);
            } else {
                message.append(" ").append(arg);
            }
        }

        if (message.length() <= 1) {
            return false;
        }

        String finalMsg = UCMessages.formatTags("", message.substring(1), src, src, message.substring(1), new UCChannel("broadcast"));

        if (!silent) {
            console().sendMessage(toText("> Broadcast: &r" + finalMsg));
        }

        TextComponent.Builder fanci = Component.text();
        fanci.append(toText(finalMsg));

        if (hover.length() > 1) {
            fanci.hoverEvent(HoverEvent.showText(toText(hover.substring(1))));
            if (!silent) {
                console().sendMessage(toText("&8> OnHover: &r" + hover.substring(1)));
            }
        }

        if (cmdline.length() > 1 && !silent) {
            console().sendMessage(toText("&8> OnClick: &r" + cmdline.substring(1)));
        }

        if (url.length() > 1) {
            ClickEvent click = urlClick(url.substring(1));
            if (click != null) {
                fanci.clickEvent(click);
            }
            if (!silent) {
                console().sendMessage(toText("&8> Url: &r" + url.substring(1)));
            }
        }

        for (ServerPlayer p : Sponge.server().onlinePlayers()) {
            if (cmdline.length() > 1) {
                fanci.clickEvent(ClickEvent.runCommand("/" + cmdline.substring(1).replace("{clicked}", p.name())));
            }
            p.sendMessage(fanci.build());
        }
        return true;
    }
}
