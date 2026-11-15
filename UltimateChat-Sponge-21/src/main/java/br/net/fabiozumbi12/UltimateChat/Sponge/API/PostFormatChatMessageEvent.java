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
import net.kyori.adventure.text.Component;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.service.permission.Subject;

import java.util.Map;

/**
 * Fired after a chat message was formatted for each receiver and before it is sent. The receivers are players and the
 * console ({@link org.spongepowered.api.SystemSubject}).
 */
public class PostFormatChatMessageEvent extends UCEvent implements Cancellable {
    private final Subject sender;
    private final UCChannel channel;
    private final Map<Subject, Component> receivers;
    private boolean cancelled;

    public PostFormatChatMessageEvent(Subject sender, Map<Subject, Component> receivers, UCChannel channel) {
        this.sender = sender;
        this.channel = channel;
        this.receivers = receivers;
    }

    public UCChannel getChannel() {
        return this.channel;
    }

    public Subject getSender() {
        return this.sender;
    }

    public Map<Subject, Component> getMessages() {
        return this.receivers;
    }

    public Component getReceiverMessage(Subject receiver) {
        return this.receivers.getOrDefault(receiver, null);
    }

    public void setReceiverMessage(Subject receiver, Component message) {
        this.receivers.put(receiver, message);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
