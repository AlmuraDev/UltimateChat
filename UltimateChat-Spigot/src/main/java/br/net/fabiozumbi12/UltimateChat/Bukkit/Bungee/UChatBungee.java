package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.Fanciful.FancyMessage;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class UChatBungee implements PluginMessageListener, Listener {
	
	@Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("uChat")){
			return;
		}
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
		String ch = "";
		String sender = "";
		String msg = "";
		String ws = "";
		try {
			ch = in.readUTF();
			sender = in.readUTF();
			msg = in.readUTF();
			ws = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}				
		UCChannel chan = UChat.get().getUCConfig().getChannel(ch);
		if (chan == null || !chan.isBungee()){
			return;
		}
        String toConsole = "";
		
        for (Player p:Bukkit.getOnlinePlayers()){
        	if (UCPerms.channelReadPerm(p, chan)){
        		FancyMessage fanci = new FancyMessage();
        		
        		String[] defaultBuilder = UChat.get().getUCConfig().getDefBuilder();
    			if (chan.useOwnBuilder()){
    				defaultBuilder = chan.getBuilder();
    			}
    			
    			for (String tag:defaultBuilder){
    				if (UChat.get().getUCConfig().getString("tags."+tag+".format") == null){
    					fanci.text(tag,tag).then("");
    					continue;
    				}        				
    				
    				String format = UChat.get().getUCConfig().getString("tags."+tag+".format");
    				String execute = UChat.get().getUCConfig().getString("tags."+tag+".click-cmd");
    				List<String> messages = UChat.get().getUCConfig().getStringList("tags."+tag+".hover-messages");
    						
    				
    				
    				String tooltip = "";
    				for (String tp:messages){
    					tooltip = tooltip+"\n"+tp;
    				}
    				if (tooltip.length() > 2){
    					tooltip = tooltip.substring(1);
    				}			
    					
    				//fix tag not found on bungee
    				format = format.replace("{world}", ws.split(",")[0]).replace("{server}", ws.split(",")[1]);
    				execute = execute.replace("{world}", ws.split(",")[0]).replace("{server}", ws.split(",")[1]);
    				tooltip = tooltip.replace("{world}", ws.split(",")[0]).replace("{server}", ws.split(",")[1]);
    				
    				if (execute != null && execute.length() > 0){
    					fanci.command(UCMessages.formatTags(tag, "/"+execute, sender, p.getName(), msg, chan));
    				}
    				
    				if (UChat.get().getUCConfig().getBool("mention.enable") && tag.equals("message") && !StringUtils.containsIgnoreCase(msg, sender)){
    					tooltip = UCMessages.formatTags(tag, tooltip, sender, p.getName(), msg, chan);	
    					format = UCMessages.formatTags(tag, format, sender, p.getName(), msg, chan);
    					if (UChat.get().getUCConfig().getString("mention.hover-message").length() > 0 && StringUtils.containsIgnoreCase(msg, p.getName())){
    						tooltip = UCMessages.formatTags(tag, UChat.get().getUCConfig().getString("mention.hover-message"), sender, p.getName(), msg, chan);
    						fanci.text(format,tag).formattedTooltip(new FancyMessage().text(tooltip, "")).then("");
    					} else if (tooltip.length() > 0){
    						fanci.text(format,tag).formattedTooltip(new FancyMessage().text(tooltip, "")).then("");
    					} else {
    						fanci.text(format,tag).then("");
    					}				
    				} else {
    					format = UCMessages.formatTags(tag, format, sender, p.getName(), msg, chan);
    					tooltip = UCMessages.formatTags(tag, tooltip, sender, p.getName(), msg, chan);
    					if (tooltip.length() > 0){	
    						fanci.text(format,tag).formattedTooltip(new FancyMessage().text(tooltip, "")).then("");
    					} else {
    						fanci.text(format,tag).then("");
    					}
    				}
    			}
    			fanci.send(p);
    			toConsole = fanci.toOldMessageFormat();        		
    		}
        }	
		UChat.get().getServ().getConsoleSender().sendMessage(toConsole);
	}	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onchatmessage(SendChannelMessageEvent e){
		if (e.isCancelled() || e.getChannel() == null || !e.getChannel().isBungee()){
			return;
		}
		e.setCancelled(true);
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(e.getChannel().getAlias());
        out.writeUTF(e.getSender().getName());
        out.writeUTF(e.getMessage());
        if (e.getSender() instanceof Player){
        	out.writeUTF(((Player)e.getSender()).getWorld().getName());
        } else {
        	out.writeUTF("Console");
        }        
	    
	    Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
	    p.sendPluginMessage(UChat.get(), "uChat", out.toByteArray());
	}
}
