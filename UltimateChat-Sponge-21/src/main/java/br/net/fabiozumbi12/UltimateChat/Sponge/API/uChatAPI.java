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
import br.net.fabiozumbi12.UltimateChat.Sponge.UCChatProtection;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.TagsCategory;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class uChatAPI {

    public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages, String permission, List<String> shoinworlds, List<String> hideinworlds, String clickUrl) {
        TagsCategory tagsCat = new TagsCategory(format, clickCmd, hoverMessages, permission, shoinworlds, hideinworlds, clickUrl);
        UChat.get().getConfig().root().tags.put(tagName, tagsCat);
        return true;
    }

    public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages) {
        return registerNewTag(tagName, format, clickCmd, hoverMessages, null, null, null, null);
    }

    /**
     * Adds a channel. Its alias works as a command at once; it is tab-completed after the next server start.
     */
    public boolean registerNewChannel(UCChannel channel) throws IOException {
        UChat.get().getConfig().addChannel(channel);
        UChat.get().reload();
        return true;
    }

    public boolean registerNewChannel(Map<String, Object> properties) throws IOException {
        return registerNewChannel(new UCChannel(properties));
    }

    public UCChannel getChannel(String chName) {
        return UChat.get().getChannel(chName);
    }

    public UCChannel getPlayerChannel(ServerPlayer player) {
        return UChat.get().getPlayerChannel(player);
    }

    public Collection<UCChannel> getChannels() {
        return UChat.get().getChannels().values();
    }

    public String filterChatMessage(ServerPlayer receiver, String message, UCChannel channel) {
        return UCChatProtection.filterChatMessage(receiver, message, channel);
    }

    public String getTagFormat(String tagname, ServerPlayer sender, Optional<ServerPlayer> receiver) {
        if (UChat.get().getConfig().root().tags.containsKey(tagname)) {
            String format = UChat.get().getConfig().root().tags.get(tagname).format;
            return UCMessages.formatTags(tagname, format, sender, receiver.isPresent() ? receiver.get() : "", "", UChat.get().getPlayerChannel(sender));
        }
        return null;
    }
}
