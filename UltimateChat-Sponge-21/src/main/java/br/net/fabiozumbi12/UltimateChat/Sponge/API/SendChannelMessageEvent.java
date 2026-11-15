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
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.service.permission.Subject;

import java.util.HashMap;

/**
 * Fired before a chat message is formatted. The channel is null for private messages.
 */
public class SendChannelMessageEvent extends UCEvent implements Cancellable {
    private final Subject sender;
    private final boolean cancelIncoming;
    private boolean cancelled;
    private Component msg;
    private UCChannel channel;
    private String[] defBuilder;
    private String[] defFormat;
    private HashMap<String, String> registeredTags;

    public SendChannelMessageEvent(HashMap<String, String> registeredReplacers, String[] defFormat, Subject sender, UCChannel channel, Component msg, boolean cancelIncoming) {
        this.sender = sender;
        this.msg = msg;
        this.channel = channel;
        this.defBuilder = UChat.get().getConfig().getDefBuilder();
        this.defFormat = defFormat;
        this.registeredTags = registeredReplacers;
        this.cancelIncoming = cancelIncoming;
    }

    public Subject getSender() {
        return this.sender;
    }

    public boolean removeTag(String tagname) {
        return this.registeredTags.remove(tagname) != null;
    }

    public boolean addTag(String tagname, String value) {
        return this.registeredTags.putIfAbsent(tagname, value) == null;
    }

    public void setTags(HashMap<String, String> tags) {
        this.registeredTags = tags;
    }

    public HashMap<String, String> getResgisteredTags() {
        return this.registeredTags;
    }

    public String[] getDefFormat() {
        return this.defFormat;
    }

    public void setDefFormat(String[] defFormat) {
        this.defFormat = defFormat;
    }

    public void setDefBuilder(String[] builder) {
        this.defBuilder = builder;
    }

    public String[] getDefaultBuilder() {
        return defBuilder;
    }

    public UCChannel getChannel() {
        return this.channel;
    }

    public void setChannel(String newCh) {
        this.channel = UChat.get().getChannel(newCh);
    }

    public Component getMessage() {
        return this.msg;
    }

    public void setMessage(Component newMsg) {
        this.msg = newMsg;
    }

    public void setMessage(String newMsg) {
        this.msg = Component.text(newMsg);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public boolean getCancelIncomingChat() {
        return cancelIncoming;
    }
}
