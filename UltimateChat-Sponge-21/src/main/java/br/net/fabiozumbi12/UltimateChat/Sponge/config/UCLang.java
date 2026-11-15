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

import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;

public class UCLang {

    private static final String ASSETS = "/assets/ultimatechat/";
    // Last message key sent to each sender, to drop the same message sent twice within a second
    private static final Map<String, String> DelayedMessage = Collections.synchronizedMap(new HashMap<>());
    private static String pathLang;
    private final Properties loadedLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private final Properties baseLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };

    public UCLang() {
        String resLang = "lang" + UChat.get().getConfig().root().language + ".properties";
        pathLang = UChat.get().configDir() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            InputStream in = UCLang.class.getResourceAsStream(ASSETS + resLang);
            if (in == null) {
                in = UCLang.class.getResourceAsStream(ASSETS + "langEN-US.properties");
            }
            try (InputStream res = in) {
                Files.copy(res, lang.toPath());
            } catch (IOException | NullPointerException e) {
                UChat.get().getLogger().severe("Could not create the lang file " + pathLang, e);
            }
            UChat.get().getLogger().info("Created lang file: " + pathLang);
        }

        loadLang();
        loadbaseLang();
        updateLang();

        UChat.get().getLogger().info("Language file loaded - Using: " + UChat.get().getConfig().root().language);
    }

    private static int versionNumber(String version) {
        try {
            return Integer.parseInt(version.replaceAll("[^0-9.]", "").replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void loadbaseLang() {
        baseLang.clear();
        try (InputStream in = UCLang.class.getResourceAsStream(ASSETS + "langEN-US.properties")) {
            baseLang.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            UChat.get().getLogger().severe("Could not read the default lang file", e);
        }
    }

    private void loadLang() {
        loadedLang.clear();
        try (Reader reader = new InputStreamReader(new FileInputStream(pathLang), StandardCharsets.UTF_8)) {
            loadedLang.load(reader);
        } catch (Exception e) {
            UChat.get().getLogger().severe("Could not read the lang file " + pathLang, e);
        }

        String version = UChat.get().version();
        if (loadedLang.get("_lang.version") != null) {
            int langv = versionNumber((String) loadedLang.get("_lang.version"));
            if (langv < versionNumber(version) || langv == 0) {
                UChat.get().getLogger().info("Your lang file is outdated. Probably need strings updates!");
                UChat.get().getLogger().info("Lang file version: " + loadedLang.get("_lang.version"));
                loadedLang.put("_lang.version", version);
            }
        }
    }

    private void updateLang() {
        for (Entry<Object, Object> linha : baseLang.entrySet()) {
            if (!loadedLang.containsKey(linha.getKey())) {
                loadedLang.put(linha.getKey(), linha.getValue());
            }
        }
        if (!loadedLang.containsKey("_lang.version")) {
            loadedLang.put("_lang.version", UChat.get().version());
        }
        try (Writer out = new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8)) {
            loadedLang.store(out, null);
        } catch (IOException e) {
            UChat.get().getLogger().severe("Could not save the lang file " + pathLang, e);
        }
    }

    public String get(Subject player, String key) {
        return UCMessages.formatTags("", get(key), player, "", "", UChat.get().getPlayerChannel(player));
    }

    public String get(String key) {
        String FMsg;

        if (loadedLang.get(key) == null) {
            FMsg = "&c&oMissing language string for &4" + key;
        } else {
            FMsg = loadedLang.get(key).toString();
        }
        return FMsg.replace("\\n", "\n");
    }

    public Component getText(String key, String additional) {
        return UCUtil.toText(get(key) + additional);
    }

    public Component getText(String key) {
        return UCUtil.toText(get(key));
    }

    public void sendMessage(final Subject p, String key) {
        final String who = UCUtil.name(p);
        if (key.equals(DelayedMessage.get(who))) {
            return;
        }

        if (loadedLang.get(key) == null) {
            UCUtil.send(p, UCUtil.toText(get(p, "_UChat.prefix") + UCMessages.formatTags("", key, p, "", "", UChat.get().getPlayerChannel(p))));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            UCUtil.send(p, UCUtil.toText(get(p, "_UChat.prefix") + " " + get(p, key)));
        }

        DelayedMessage.put(who, key);
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(UChat.get().instance())
                .delay(Duration.ofSeconds(1))
                .execute(() -> DelayedMessage.remove(who, key))
                .build());
    }

}
