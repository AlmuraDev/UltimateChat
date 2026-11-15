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

package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;

/**
 * Fired when a player is about to enter a channel.
 */
public class PlayerChangeChannelEvent extends UCEvent implements Cancellable {
    private final ServerPlayer player;
    private final UCChannel channelTo;
    private final UCChannel channelFrom;
    private boolean isCancelled;

    public PlayerChangeChannelEvent(ServerPlayer p, UCChannel from, UCChannel to) {
        this.player = p;
        this.channelFrom = from;
        this.channelTo = to;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public UCChannel getChannelFrom() {
        return this.channelFrom;
    }

    public UCChannel getChannelTo() {
        return this.channelTo;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
