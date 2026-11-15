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

package br.net.fabiozumbi12.UltimateChat.Sponge;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.PostFormatChatMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCLogger.timingType;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UCMessages {

    private static HashMap<String, String> registeredReplacers = new HashMap<>();
    private static String[] defFormat = new String[0];

    /**
     * Formats a chat message for each receiver of the channel (or of the private message when {@code channel} is null)
     * and sends it.
     *
     * @return The receivers, or null if the message was not sent.
     */
    public static Set<Subject> sendFancyMessage(String[] format, Component msg, UCChannel channel, Subject sender, Subject tellReceiver) {
        //Execute listener:
        HashMap<String, String> tags = new HashMap<>();
        for (String str : UChat.get().getConfig().root().general.custom_tags) {
            tags.put(str, str);
        }
        SendChannelMessageEvent event = new SendChannelMessageEvent(tags, format, sender, channel, msg, true);
        if (Sponge.eventManager().post(event)) {
            return null;
        }

        registeredReplacers = event.getResgisteredTags();
        defFormat = event.getDefFormat();

        String evmsg = UCUtil.toPlain(event.getMessage());

        evmsg = UCChatProtection.filterChatMessage(sender, evmsg, event.getChannel());
        if (evmsg == null) {
            return null;
        }

        LinkedHashMap<Subject, Component> msgPlayers = new LinkedHashMap<>();
        evmsg = composeColor(sender, evmsg);

        final String srcText = evmsg;

        UCChannel ch;
        if (event.getChannel() != null) {
            ch = event.getChannel();

            if (sender instanceof ServerPlayer && !ch.availableWorlds().isEmpty() && !ch.availableInWorld(((ServerPlayer) sender).world())) {
                UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
                return null;
            }

            if (!UChat.get().getPerms().channelWritePerm(sender, ch)) {
                UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
                return null;
            }

            EconomyService eco = UChat.get().getEco();
            if (!UChat.get().getPerms().hasPerm(sender, "bypass.cost") && eco != null && sender instanceof ServerPlayer && ch.getCost() > 0) {
                Optional<UniqueAccount> acc = eco.findOrCreateAccount(((ServerPlayer) sender).uniqueId());
                if (acc.isPresent()) {
                    if (acc.get().balance(eco.defaultCurrency()).doubleValue() < ch.getCost()) {
                        UCUtil.send(sender, UCUtil.toText(UChat.get().getLang().get("channel.cost").replace("{value}", "" + ch.getCost())));
                        return null;
                    } else {
                        acc.get().withdraw(eco.defaultCurrency(), BigDecimal.valueOf(ch.getCost()));
                    }
                }
            }

            int noWorldReceived = 0;
            int vanish = 0;
            List<Subject> receivers = new ArrayList<>();

            //put sender
            msgPlayers.put(sender, sendMessage(sender, sender, srcText, ch));

            if (ch.getDistance() > 0 && sender instanceof ServerPlayer) {
                ServerPlayer psender = (ServerPlayer) sender;
                for (ServerPlayer play : Sponge.server().onlinePlayers().stream().filter(p -> p.world().equals(psender.world())
                        && p.position().distance(psender.position()) <= ch.getDistance())
                        .collect(Collectors.toList())) {
                    if (UChat.get().getPerms().channelReadPerm(play, ch)) {
                        if (sender.equals(play)) {
                            continue;
                        }
                        if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(play.world())) {
                            continue;
                        }
                        if (ch.isIgnoring(play.name())) {
                            continue;
                        }
                        if (isIgnoringPlayers(play.name(), psender.name())) {
                            noWorldReceived++;
                            continue;
                        }
                        if (!psender.canSee(play) && !sender.hasPermission("uchat.see-vanish")) {
                            vanish++;
                        } else {
                            noWorldReceived++;
                        }
                        if (!ch.neeFocus() || ch.isMember(play)) {
                            msgPlayers.put(play, sendMessage(sender, play, srcText, ch));
                            receivers.add(play);
                        }
                    }
                }
            } else {
                for (ServerPlayer receiver : Sponge.server().onlinePlayers()) {
                    if (receiver.equals(sender) || !UChat.get().getPerms().channelReadPerm(receiver, ch) || (!ch.crossWorlds() && (sender instanceof ServerPlayer && !receiver.world().equals(((ServerPlayer) sender).world())))) {
                        continue;
                    }
                    if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(receiver.world())) {
                        continue;
                    }
                    if (ch.isIgnoring(receiver.name())) {
                        continue;
                    }
                    if (isIgnoringPlayers(receiver.name(), UCUtil.name(sender))) {
                        noWorldReceived++;
                        continue;
                    }
                    if (sender instanceof ServerPlayer && !((ServerPlayer) sender).canSee(receiver) && !sender.hasPermission("uchat.see-vanish")) {
                        vanish++;
                    } else {
                        noWorldReceived++;
                    }
                    if (!ch.neeFocus() || ch.isMember(receiver)) {
                        msgPlayers.put(receiver, sendMessage(sender, receiver, srcText, ch));
                        receivers.add(receiver);
                    }
                }
            }

            //chat spy
            for (ServerPlayer receiver : Sponge.server().onlinePlayers()) {
                if (!receiver.equals(sender) && !receivers.contains(receiver) && !receivers.contains(sender) &&
                        (UChat.get().isSpy.contains(receiver.name()) && UChat.get().getPerms().hasSpyPerm(receiver, ch.getName())
                                && !sender.hasPermission("uchat.chat-spy.bypass"))) {
                    String spyformat = UChat.get().getConfig().root().general.spy_format;
                    spyformat = spyformat.replace("{output}", UCUtil.stripColor('&', UCUtil.toPlain(sendMessage(sender, receiver, srcText, ch))));
                    receiver.sendMessage(UCUtil.toText(spyformat));
                }
            }

            if (ch.getDistance() == 0 && noWorldReceived <= 0) {
                if (ch.getReceiversMsg()) {
                    UChat.get().getLang().sendMessage(sender, "channel.noplayer.world");
                }
            }
            if ((receivers.size() - vanish) <= 0) {
                if (ch.getReceiversMsg()) {
                    UChat.get().getLang().sendMessage(sender, "channel.noplayer.near");
                }
            }

        } else {
            //send tell
            String receiverName = UCUtil.name(tellReceiver);
            String senderName = UCUtil.name(sender);

            if (UChat.get().msgTogglePlayers.contains(receiverName) && !sender.hasPermission("uchat.msgtoggle.exempt")) {
                UChat.get().getLang().sendMessage(sender, "cmd.msgtoggle.msgdisabled");
                return null;
            }

            ch = new UCChannel("tell");

            //send spy
            for (ServerPlayer receiver : Sponge.server().onlinePlayers()) {
                if (!receiver.equals(tellReceiver) && !receiver.equals(sender) && UChat.get().isSpy.contains(receiver.name())
                        && UChat.get().getPerms().hasSpyPerm(receiver, "private") && !sender.hasPermission("uchat.chat-spy.bypass")) {
                    String spyformat = UChat.get().getConfig().root().general.spy_format;
                    if (isIgnoringPlayers(receiverName, senderName)) {
                        spyformat = UChat.get().getLang().get("chat.ignored") + spyformat;
                    }
                    spyformat = spyformat.replace("{output}", UCUtil.stripColor('&', UCUtil.toPlain(sendMessage(sender, tellReceiver, srcText, ch))));
                    receiver.sendMessage(UCUtil.toText(spyformat));
                }
            }

            Component to = sendMessage(sender, tellReceiver, srcText, ch);
            msgPlayers.put(sender, to);
            if (!isIgnoringPlayers(receiverName, senderName)) {
                msgPlayers.put(tellReceiver, to);
            }
            if (isIgnoringPlayers(receiverName, senderName)) {
                to = Component.text().append(UCUtil.toText(UChat.get().getLang().get("chat.ignored")), to).build();
            }

            msgPlayers.put(UCUtil.console(), to);
        }

        if (!msgPlayers.containsKey(UCUtil.console())) {
            msgPlayers.put(UCUtil.console(), msgPlayers.values().iterator().next());
        }

        //fire post event
        PostFormatChatMessageEvent postEvent = new PostFormatChatMessageEvent(sender, msgPlayers, ch);
        if (Sponge.eventManager().post(postEvent)) {
            return null;
        }

        msgPlayers.forEach((send, text) -> {
            UChat.get().getLogger().timings(timingType.END, "UCMessages#send()|before send");
            UCUtil.send(send, text);
            UChat.get().getLogger().timings(timingType.END, "UCMessages#send()|after send");
        });

        //send to jda
        if (UChat.get().getUCJDA() != null) {
            if (ch.isTell()) {
                UChat.get().getUCJDA().sendTellToDiscord(UCUtil.toPlain(msgPlayers.get(sender)));
            } else {
                UChat.get().getUCJDA().sendToDiscord(sender, evmsg, ch);
            }
        }

        return msgPlayers.keySet();
    }

    private static String composeColor(Subject sender, String evmsg) {
        if (evmsg == null || evmsg.isEmpty()) return "";

        // Convert real color char to char code
        evmsg = evmsg.replaceAll("(§([A-Fa-fK-Ok-oRr0-9]))", "&$2");

        if (sender instanceof ServerPlayer) {
            Pattern mat0 = Pattern.compile("(?i)&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");
            Pattern mat1 = Pattern.compile("(?i)&([A-Fa-f0-9Rr])");
            Pattern mat2 = Pattern.compile("(?i)&([L-Ol-o])");
            Pattern mat3 = Pattern.compile("(?i)&([Kk])");

            if (!UChat.get().getPerms().hasPerm(sender, "chat.color.hex")) {
                evmsg = mat0.matcher(evmsg).replaceAll("");
            } else {
                // The legacy serializer reads hex colours as &#rrggbb
                evmsg = evmsg.replaceAll("(?i)(?<!&)#([A-Fa-f0-9]{6})", "&#$1");
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.color")) {
                while (mat1.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat1.pattern(), "");
                }
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.color.formats")) {
                while (mat2.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat2.pattern(), "");
                }
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.color.magic")) {
                while (mat3.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat3.pattern(), "");
                }
            }
            if (UChat.get().getPerms().hasPerm(sender, "chat.newline")) {
                evmsg = evmsg.replace("\\n", "\n");
            }
        }
        return evmsg;
    }

    static boolean isIgnoringPlayers(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        return list.contains(victim);
    }

    static void ignorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.add(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    static void unIgnorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.remove(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    private static String tooltip(List<String> messages) {
        StringBuilder tooltip = new StringBuilder();
        if (messages != null && !messages.isEmpty()) {
            for (String tp : messages) {
                tooltip.append("\n").append(tp.replace("\\n", "\n"));
            }
            if (tooltip.length() > 2) {
                tooltip = new StringBuilder(tooltip.substring(1));
            }
        }
        return tooltip.toString();
    }

    public static Component sendMessage(Subject sender, Object receiver, String msg, UCChannel ch) {
        TextComponent.Builder formatter = Component.text();

        if (!ch.getName().equals("tell")) {
            String[] defaultBuilder = UChat.get().getConfig().getDefBuilder();
            if (ch.useOwnBuilder()) {
                defaultBuilder = ch.getBuilder();
            }

            String handPlaceholder = UChat.get().getConfig().root().general.item_hand.placeholder;
            String lastColor = "";
            for (String tag : defaultBuilder) {
                if (UChat.get().getConfig().root().tags.get(tag) == null) {
                    continue;
                }

                String format = lastColor + UChat.get().getConfig().root().tags.get(tag).format;
                String perm = UChat.get().getConfig().root().tags.get(tag).permission;
                String execute = UChat.get().getConfig().root().tags.get(tag).click_cmd;
                String url = UChat.get().getConfig().root().tags.get(tag).click_url;
                String suggest = UChat.get().getConfig().root().tags.get(tag).suggest;
                List<String> messages = UChat.get().getConfig().root().tags.get(tag).hover_messages;
                List<String> showWorlds = UChat.get().getConfig().root().tags.get(tag).show_in_worlds;
                List<String> hideWorlds = UChat.get().getConfig().root().tags.get(tag).hide_in_worlds;

                //check perm
                if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
                    continue;
                }

                //check show or hide in world
                if (sender instanceof ServerPlayer) {
                    if (showWorlds != null && !showWorlds.isEmpty() && !UCUtil.worldIn(((ServerPlayer) sender).world(), showWorlds)) {
                        continue;
                    }
                    if (hideWorlds != null && UCUtil.worldIn(((ServerPlayer) sender).world(), hideWorlds)) {
                        continue;
                    }
                }

                TextComponent.Builder tagBuilder = Component.text();
                String tooltip = tooltip(messages);

                if (suggest != null && !suggest.isEmpty()) {
                    tagBuilder.clickEvent(ClickEvent.suggestCommand(formatTags(tag, "/" + suggest, sender, receiver, msg, ch)));
                }

                if (execute != null && !execute.isEmpty()) {
                    tagBuilder.clickEvent(ClickEvent.runCommand(formatTags(tag, "/" + execute, sender, receiver, msg, ch)));
                }

                if (url != null && !url.isEmpty()) {
                    ClickEvent click = UCUtil.urlClick(formatTags(tag, url, sender, receiver, msg, ch));
                    if (click != null) {
                        tagBuilder.clickEvent(click);
                    }
                }

                HoverEvent<?> urlHover = null;
                // Links in the message open on click. The old versions also put them on every other tag, replacing that
                // tag's own click; now only the message tag gets them.
                if (tag.equals("message") && UChat.get().getPerms().hasPerm(sender, "chat.click-urls")) {
                    for (String arg : msg.split(" ")) {
                        ClickEvent click = UCUtil.urlClick(UCUtil.stripColor('&', arg));
                        if (click != null) {
                            tagBuilder.clickEvent(click);
                            urlHover = HoverEvent.showText(UCUtil.toText(formatTags(tag, UChat.get().getConfig().root().general.URL_template.replace("{url}", arg), sender, receiver, msg, ch)));
                        }
                    }
                }

                format = formatTags(tag, format, sender, receiver, msg, ch);
                tooltip = formatTags("", tooltip, sender, receiver, msg, ch);
                lastColor = getLastColor(format);
                tagBuilder.append(UCUtil.toText(format));

                if (tag.equals("message") && (!msg.equals(mention(sender, receiver, msg)) || msg.contains(handPlaceholder))) {
                    if (ch.allowHand() && UChat.get().getConfig().root().general.item_hand.enable && msg.contains(handPlaceholder)
                            && sender instanceof ServerPlayer && UChat.get().getPerms().hasPerm(sender, "chat.hand")) {
                        ItemStack hand = ((ServerPlayer) sender).itemInHand(HandTypes.MAIN_HAND);
                        if (!hand.isEmpty()) tagBuilder.hoverEvent(hand.asHoverEvent());
                    } else if (!UChat.get().getConfig().root().mention.hover_message.isEmpty() && receiver instanceof Subject
                            && msg.toLowerCase().contains(UCUtil.name(receiver).toLowerCase())) {
                        tooltip = formatTags("", UChat.get().getConfig().root().mention.hover_message, sender, receiver, msg, ch);
                        tagBuilder.hoverEvent(HoverEvent.showText(UCUtil.toText(tooltip)));
                    } else if (!tooltip.isEmpty()) {
                        tagBuilder.hoverEvent(HoverEvent.showText(UCUtil.toText(tooltip)));
                    }
                } else if (!tooltip.isEmpty()) {
                    tagBuilder.hoverEvent(HoverEvent.showText(UCUtil.toText(tooltip)));
                } else if (urlHover != null) {
                    tagBuilder.hoverEvent(urlHover);
                }
                formatter.append(tagBuilder.build());
            }
        } else {
            //if tell
            String prefix = UChat.get().getConfig().root().tell.prefix;
            String format = UChat.get().getConfig().root().tell.format;
            List<String> messages = UChat.get().getConfig().root().tell.hover_messages;

            String tooltip = "";
            if (!messages.isEmpty() && messages.get(0).length() > 1) {
                tooltip = tooltip(messages);
            }

            prefix = formatTags("", prefix, sender, receiver, msg, ch);
            format = formatTags("tell", format, sender, receiver, msg, ch);
            tooltip = formatTags("", tooltip, sender, receiver, msg, ch);

            TextComponent.Builder prefixBuilder = Component.text().append(UCUtil.toText(prefix));
            if (!tooltip.isEmpty()) {
                prefixBuilder.hoverEvent(HoverEvent.showText(UCUtil.toText(tooltip)));
            }
            formatter.append(prefixBuilder.build());
            formatter.append(UCUtil.toText(format));
        }

        return formatter.build();
    }

    private static String getLastColor(String str) {
        if (str.length() > 2) {
            str = str.substring(str.length() - 2);
            if (str.matches("(&([A-Fa-fK-Ok-oRr0-9]))")) {
                return str;
            }
        }
        return "";
    }

    /** The 1.12 sound names in old configs ("block.note.pling") became "block.note_block.pling". */
    private static Optional<Sound> mentionSound() {
        String name = UChat.get().getConfig().root().mention.playsound;
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        name = name.replace("block.note.", "block.note_block.");
        try {
            return Optional.of(Sound.sound(Key.key(name), Sound.Source.MASTER, 1f, 1f));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String mention(Object sender, Object receiver, String msg) {
        if (UChat.get().getConfig().root().mention.enable) {
            for (ServerPlayer p : Sponge.server().onlinePlayers()) {
                if (!sender.equals(p) && Arrays.stream(msg.split(" ")).anyMatch(p.name()::equalsIgnoreCase)) {
                    if (receiver instanceof ServerPlayer && receiver.equals(p)) {

                        String mentionc = UChat.get().getConfig().root().mention.color_template.replace("{mentioned-player}", p.name());
                        mentionc = formatTags("", mentionc, sender, p, "", new UCChannel("mention"));

                        if (msg.contains(mentionc) || sender instanceof Subject && !UChat.get().getPerms().hasPerm((Subject) sender, "chat.mention")) {
                            msg = msg.replaceAll("(?i)\\b" + Pattern.quote(p.name()) + "\\b", Matcher.quoteReplacement(p.name()));
                            continue;
                        }

                        if (!msg.contains(mentionc)) {
                            mentionSound().ifPresent(p::playSound);
                        }

                        msg = msg.replace(mentionc, p.name());
                        msg = msg.replaceAll("(?i)\\b" + Pattern.quote(p.name()) + "\\b", Matcher.quoteReplacement(mentionc));
                    } else {
                        msg = msg.replaceAll("(?i)\\b" + Pattern.quote(p.name()) + "\\b", Matcher.quoteReplacement(p.name()));
                    }
                }
            }
        }
        return msg;
    }

    private static String enchantments(ItemStack item) {
        StringBuilder str = new StringBuilder();
        for (Enchantment ench : item.get(Keys.APPLIED_ENCHANTMENTS).orElse(Collections.emptyList())) {
            str.append(UCUtil.humanize(ench.type().key(RegistryTypes.ENCHANTMENT_TYPE))).append(" ").append(ench.level()).append(", ");
        }
        return str.length() >= 2 ? str.substring(0, str.length() - 2) : "";
    }

    public static String formatTags(String tag, String text, Object cmdSender, Object receiver, String msg, UCChannel ch) {
        if (text == null) {
            return "";
        }
        if (receiver instanceof Subject && tag.equals("message")) {
            text = text.replace("{message}", mention(cmdSender, receiver, msg));
        } else {
            text = text.replace("{message}", msg);
        }
        String handPlaceholder = UChat.get().getConfig().root().general.item_hand.placeholder;
        if (UChat.get().getConfig().root().general.item_hand.enable && cmdSender instanceof ServerPlayer && UChat.get().getPerms().hasPerm((ServerPlayer) cmdSender, "chat.hand") && text.contains(handPlaceholder)) {
            text = text.replace(handPlaceholder, formatTags("", UChat.get().getConfig().root().general.item_hand.format, cmdSender, receiver, msg, ch));
        }
        if (tag.equals("message") && !UChat.get().getConfig().root().general.enable_tags_on_messages) {
            return text;
        }
        text = text.replace("{ch-color}", ch.getColor())
                .replace("{ch-name}", ch.getName())
                .replace("{ch-alias}", ch.getAlias());

        if (cmdSender instanceof Subject) {
            text = text.replace("{playername}", UCUtil.name(cmdSender));
        } else {
            text = text.replace("{playername}", String.valueOf(cmdSender));
        }
        if (receiver instanceof Subject) {
            text = text.replace("{receivername}", UCUtil.name(receiver));
        } else if (receiver instanceof String) {
            text = text.replace("{receivername}", (String) receiver);
        }

        for (String repl : registeredReplacers.keySet()) {
            if (registeredReplacers.get(repl).equals(repl)) {
                text = text.replace(repl, "");
                continue;
            }
            text = text.replace(repl, registeredReplacers.get(repl));
        }

        if (defFormat.length > 0) {
            StringBuilder all = new StringBuilder();
            for (int i = 0; i < defFormat.length; i++) {
                if (i == 0) text = text.replace("{chat_header}", defFormat[i]);
                if (i == 1) text = text.replace("{chat_body}", defFormat[i]);
                if (i == 2) text = text.replace("{chat_footer}", defFormat[i]);

                all.append(defFormat[i]);
            }
            text = text.replace("{chat_all}", all.toString());
        }

        if (text.contains("{time-now}")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            text = text.replace("{time-now}", sdf.format(Calendar.getInstance().getTime()));
        }

        if (cmdSender instanceof ServerPlayer) {
            ServerPlayer sender = (ServerPlayer) cmdSender;

            if (text.contains("{nickname}")) {
                String nick = sender.get(Keys.DISPLAY_NAME).map(UCUtil::toPlain).orElse(sender.name());
                if (!nick.isEmpty() && !nick.equals(sender.name())) {
                    text = text.replace("{nick-symbol}", UChat.get().getConfig().root().general.nick_symbol);
                } else {
                    nick = sender.name();
                }
                text = text.replace("{nickname}", nick);
            }

            //replace item hand
            ItemStack item = sender.itemInHand(HandTypes.MAIN_HAND);
            if (UChat.get().getPerms().hasPerm(sender, "chat.hand")) {
                text = text.replace(handPlaceholder, UChat.get().getConfig().root().general.item_hand.format);
            }

            if (text.contains("{hand-") && !item.isEmpty()) {
                text = text.replace("{hand-durability}", item.get(Keys.ITEM_DURABILITY).map(String::valueOf).orElse(""));
                List<Component> lore = item.get(Keys.LORE).orElse(Collections.emptyList());
                if (!lore.isEmpty()) {
                    StringBuilder lorestr = new StringBuilder();
                    for (Component line : lore) {
                        lorestr.append("\n ").append(UCUtil.toPlain(line));
                    }
                    text = text.replace("{hand-lore}", lorestr.substring(1));
                }
                String enchants = enchantments(item);
                if (!enchants.isEmpty()) {
                    text = text.replace("{hand-enchants}", enchants);
                }
                text = text.replace("{hand-amount}", String.valueOf(item.quantity()));
                String itemType = UCUtil.humanize(item.type().key(RegistryTypes.ITEM_TYPE));
                Optional<Component> customName = item.get(Keys.CUSTOM_NAME);
                text = text.replace("{hand-name}", customName.map(UCUtil::toPlain).orElse(itemType));
                text = text.replace("{hand-type}", itemType);
            } else {
                text = text.replace("{hand-name}", UChat.get().getLang().get("chat.emptyslot"));
                text = text.replace("{hand-type}", "Air");
            }

            if (text.contains("{world}")) {
                Map<String, String> names = UChat.get().getConfig().root().general.world_names;
                String key = UCUtil.worldKey(sender.world());
                String world = UCUtil.worldName(sender.world());
                text = text.replace("{world}", names.getOrDefault(key, names.getOrDefault(world, world)));
            }

            EconomyService eco = UChat.get().getEco();
            if (text.contains("{balance}") && eco != null) {
                Optional<UniqueAccount> acc = eco.findOrCreateAccount(sender.uniqueId());
                text = text.replace("{balance}", acc.map(a -> "" + a.balance(eco.defaultCurrency()).intValue()).orElse("0"));
            }

            //parse permissions options
            if (text.contains("option_") || text.contains("player_option_")) {
                //all groups
                StringBuilder groupPrefs = new StringBuilder();
                StringBuilder groupSuffs = new StringBuilder();

                for (Subject group : UChat.get().getPerms().getPlayerGroups(sender).values()) {
                    if (UChat.get().getConfig().root().general.dont_show_groups.contains(group.identifier()))
                        continue;

                    group.option("prefix").ifPresent(groupPrefs::append);
                    group.option("suffix").ifPresent(groupSuffs::append);
                }
                text = text.replace("{option_all_prefixes}", groupPrefs.toString());
                text = text.replace("{option_all_suffixes}", groupSuffs.toString());

                //player options
                Pattern pp = Pattern.compile("\\{player_option_(.+?)\\}");
                Matcher pm = pp.matcher(text);

                while (pm.find()) {
                    Optional<String> opt = sender.option(pm.group(1));
                    if (opt.isPresent() && !text.contains(opt.get())) {
                        text = text.replace("{player_option_" + pm.group(1) + "}", opt.get());
                        pm = pp.matcher(text);
                    }
                }

                //group options
                Subject sub = UChat.get().getPerms().getGroupAndTag(sender);
                if (sub != null) {
                    Map<String, String> groupNames = UChat.get().getConfig().root().general.group_names;
                    text = text.replace("{option_group}", groupNames.getOrDefault(sub.identifier(), sub.identifier()));

                    Optional<String> displayName = sub.option("display_name");
                    text = text.replace("{option_display_name}", displayName.orElse(groupNames.getOrDefault(sub.identifier(), sub.identifier())));

                    Pattern gp = Pattern.compile("\\{option_(.+?)\\}");
                    Matcher gm = gp.matcher(text);

                    while (gm.find()) {
                        String gmp = gm.group(1);
                        Optional<String> opt = sub.option(gmp);
                        if (opt.isPresent() && !text.contains(opt.get())) {
                            text = text.replace("{option_" + gmp + "}", opt.get());
                            gm = gp.matcher(text);
                        }
                    }
                }
            }
        }

        text = text.replace("{option_suffix}", "&r: ");

        if (cmdSender instanceof Subject) {
            text = text.replace("{nickname}", UChat.get().getConfig().root().general.console_tag.replace("{console}", UCUtil.name(cmdSender)));
        } else {
            text = text.replace("{nickname}", UChat.get().getConfig().root().general.console_tag.replace("{console}", String.valueOf(cmdSender)));
        }

        //remove blank items
        text = text.replaceAll("\\{.*\\}", "");
        if (!tag.equals("message")) {
            for (String rpl : UChat.get().getConfig().root().general.remove_from_chat) {
                text = text.replace(rpl, "");
            }
        }
        if (text.equals(" ") || text.equals("  ")) {
            return "";
        }
        return text;
    }
}
