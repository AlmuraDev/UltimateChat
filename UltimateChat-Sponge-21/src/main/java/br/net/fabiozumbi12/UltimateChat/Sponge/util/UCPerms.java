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
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Permission checks (the API 5-8 variants merged into one class for SpongeAPI 21).
 */
public class UCPerms {

    private static final String[] DEFAULT_PERMS = {
            "uchat.channel.local.read", "uchat.channel.local.write", "uchat.channel.global.read", "uchat.channel.global.write",
            "uchat.cmd.tell", "uchat.chat.click-urls", "uchat.cant-ignore.local", "uchat.chat.hand", "uchat.cmd.ignore.channel",
            "uchat.cmd.ignore.player", "uchat.cmd.clear", "uchat.cmd.msgtoggle", "uchat.password"
    };

    private final PermissionService permissionService;

    public UCPerms(PermissionService permissionService) {
        this.permissionService = permissionService;
        for (String perm : DEFAULT_PERMS) {
            this.permissionService.defaults().transientSubjectData().setPermission(Set.of(), perm, Tristate.TRUE);
        }
    }

    private static boolean isConsole(Subject p) {
        return p instanceof SystemSubject;
    }

    public boolean hasSpyPerm(Subject receiver, String ch) {
        return hasPerm(receiver, "chat-spy." + ch) || hasPerm(receiver, "chat-spy.all");
    }

    public boolean cmdPerm(Subject p, String cmd) {
        return hasPerm(p, "cmd." + cmd);
    }

    public boolean channelReadPerm(Subject p, UCChannel ch) {
        UCChannel defCh = UChat.get().getDefChannel(p instanceof ServerPlayer ? ((ServerPlayer) p).world() : null);
        return ch.equals(defCh) || hasPerm(p, "channel." + ch.getName().toLowerCase() + ".read");
    }

    public boolean channelWritePerm(Subject p, UCChannel ch) {
        UCChannel defCh = UChat.get().getDefChannel(p instanceof ServerPlayer ? ((ServerPlayer) p).world() : null);
        return ch.equals(defCh) || hasPerm(p, "channel." + ch.getName().toLowerCase() + ".write");
    }

    public boolean canIgnore(Subject sender, Object toignore) {
        if (toignore instanceof ServerPlayer) {
            return !sender.hasPermission("uchat.cant-ignore." + ((ServerPlayer) toignore).name());
        }
        // Channels can always be ignored: the old versions never checked uchat.cant-ignore.<channel> either
        return true;
    }

    public boolean hasPerm(Subject p, String perm) {
        return isConsole(p) || p.hasPermission("uchat." + perm) || p.hasPermission("uchat.admin");
    }

    public Subject getGroupAndTag(Subject player) {
        HashMap<Integer, Subject> subs = getPlayerGroups(player);
        return subs.isEmpty() ? null : subs.get(Collections.max(subs.keySet()));
    }

    public HashMap<Integer, Subject> getPlayerGroups(Subject player) {
        HashMap<Integer, Subject> subs = new HashMap<>();
        String groups = this.permissionService.groupSubjects().identifier();
        for (SubjectReference sub : player.parents()) {
            if (sub.collectionIdentifier().equals(groups) && sub.subjectIdentifier() != null) {
                try {
                    Subject subj = sub.resolve().join();
                    subs.put(subj.parents().size(), subj);
                } catch (Exception e) {
                    UChat.get().getLogger().debug("Could not resolve group " + sub.subjectIdentifier() + ": " + e.getMessage());
                }
            }
        }
        return subs;
    }
}
