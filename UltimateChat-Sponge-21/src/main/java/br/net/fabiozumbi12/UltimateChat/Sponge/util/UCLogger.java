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

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.apache.logging.log4j.Logger;

/**
 * Logs to the plugin's Log4j logger. Colour codes in messages are stripped, as the server log has no colours.
 */
public class UCLogger {
    private final Logger logger;
    private long start = 0;

    public UCLogger(Logger logger) {
        this.logger = logger;
    }

    private static String clean(String s) {
        return UCUtil.stripColor('&', UCUtil.stripColor('§', s));
    }

    public void logClear(String s) {
        this.logger.info(clean(s));
    }

    public void sucess(String s) {
        this.logger.info(clean(s));
    }

    public void info(String s) {
        this.logger.info(clean(s));
    }

    public void warning(String s) {
        this.logger.warn(clean(s));
    }

    public void severe(String s) {
        this.logger.error(clean(s));
    }

    public void severe(String s, Throwable t) {
        this.logger.error(clean(s), t);
    }

    public void log(String s) {
        this.logger.info(clean(s));
    }

    public void debug(String s) {
        if (UChat.get().getConfig() != null && UChat.get().getConfig().root().debug.messages) {
            this.logger.info("[debug] " + clean(s));
        }
    }

    public void timings(timingType type, String message) {
        if (UChat.get().getConfig() != null && UChat.get().getConfig().root().debug.timings) {
            switch (type) {
                case START:
                    long diff = 0;
                    if (System.currentTimeMillis() - start > 5000) start = 0;
                    if (start != 0) {
                        diff = System.currentTimeMillis() - start;
                    }
                    start = System.currentTimeMillis();
                    this.logger.info("UC Timings - " + type + ": " + diff + "ms (" + clean(message) + ")");
                    break;
                case END:
                    this.logger.info("UC Timings - " + type + ": " + (System.currentTimeMillis() - start) + "ms (" + clean(message) + ")");
                    break;
                default:
                    break;
            }
        }
    }

    public enum timingType {
        START, END
    }
}
