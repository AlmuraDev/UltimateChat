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

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class TagsCategory {

    @Setting("format")
    public String format;
    @Setting("click-url")
    public String click_url;
    @Setting("suggest")
    public String suggest;
    @Setting("click-cmd")
    public String click_cmd;
    @Setting("hover-messages")
    public List<String> hover_messages;
    @Setting("permission")
    public String permission;
    @Setting("show-in-worlds")
    public List<String> show_in_worlds;
    @Setting("hide-in-worlds")
    public List<String> hide_in_worlds;

    public TagsCategory() {
    }

    public TagsCategory(String format, String click_cmd, List<String> hover_messages, String permission, List<String> show_in_worlds, List<String> hide_in_worlds, String click_url) {
        this.format = format;
        this.click_cmd = click_cmd;
        this.hover_messages = hover_messages;
        this.permission = permission;
        this.show_in_worlds = show_in_worlds;
        this.hide_in_worlds = hide_in_worlds;
        this.click_url = click_url;
    }
}