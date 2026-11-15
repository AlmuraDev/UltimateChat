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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Counts a temporary mute down once a second on the server thread, and unmutes when it runs out.
 */
public class MuteCountDown implements Consumer<ScheduledTask> {

    final String p;
    int time;

    public MuteCountDown(String p, int minutes) {
        this.p = p;
        this.time = minutes * 60;
    }

    public static void start(String player, int minutes, String name) {
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(UChat.get().instance())
                .interval(Duration.ofSeconds(1))
                .execute(new MuteCountDown(player, minutes))
                .build(), name);
    }

    @Override
    public void accept(ScheduledTask t) {
        if (UChat.get().timeMute.containsKey(p)) {
            time = UChat.get().timeMute.get(p) - 1;
        }
        if (UChat.get().mutes.contains(p)) {
            if (time > 0) {
                UChat.get().timeMute.put(p, time);
            } else {
                UChat.get().timeMute.remove(p);
                UChat.get().mutes.remove(p);
                UChat.get().unMuteInAllChannels(p);
                Sponge.server().player(p).ifPresent(player ->
                        UChat.get().getLang().sendMessage(player, UChat.get().getLang().get("channel.player.unmuted.all")));
                t.cancel();
            }
        } else {
            t.cancel();
        }
    }
}
