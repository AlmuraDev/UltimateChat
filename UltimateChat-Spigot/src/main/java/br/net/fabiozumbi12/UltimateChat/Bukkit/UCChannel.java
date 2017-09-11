package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;

/**Represents a chat channel use by UltimateChat to control from where/to send/receive messages.
 * 
 * @author FabioZumbi12
 *
 */
public class UCChannel {

	private String name;
	private String alias;
	private boolean worlds = true;
	private int dist = 0;
	private String color = "&a";
	private String builder = "";
	private boolean focus = false;
	private boolean receiversMsg = false;
	private List<String> ignoring = new ArrayList<String>();
	private List<String> mutes = new ArrayList<String>();
	private double cost = 0.0;
	private boolean bungee = false;
	private boolean ownBuilder = false;
	private boolean isAlias = false;
	private String aliasSender = "";
	private String aliasCmd = "";
	private List<String> availableWorlds = new ArrayList<String>();
	private boolean canLock = true;
	private String ddchannel = new String();
	private String ddmode = "NONE";
	private List<CommandSender> members = new ArrayList<CommandSender>();
	private String ddformat = "{ch-color}[{ch-alias}]&7[&3Discord&7]&b{sender}&r: {message}";
	private String ddhover = "&3Discord Channel: &a{dd-channel}";
	private boolean ddallowcmds = false;

	public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder, boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder, boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, String ddchannel, String ddmode, String ddformat, String ddhover, boolean ddallowcmds, boolean lock) {
		this.name = name;
		this.alias = alias;
		this.worlds = worlds;
		this.dist = dist;
		this.color = color;
		this.builder = builder;
		this.focus = focus;
		this.receiversMsg = receiversMsg;
		this.cost = cost;
		this.bungee = isbungee;
		this.ownBuilder  = ownBuilder;
		this.isAlias = isAlias;
		this.aliasCmd  = aliasCmd;
		this.aliasSender = aliasSender;
		this.availableWorlds = availableWorlds;
		this.canLock = lock;
		this.ddchannel = ddchannel;
		this.ddmode = ddmode;
		this.ddformat = ddformat;
		this.ddhover = ddhover;
		this.ddallowcmds = ddallowcmds;
	}
	
	public UCChannel(String name, String alias, String color) {
		this.name = name;
		this.alias = alias;
		this.color = color;
	}
	
	public UCChannel(String name) {
		this.name = name;
		this.alias = name.substring(0, 1).toLowerCase();
	}

	public boolean getDiscordAllowCmds(){
		return this.ddallowcmds;
	}
	
	public boolean isTell(){
		return this.name.equals("tell");		
	}
	
	public String getDiscordChannelID(){
		return this.ddchannel;
	}
	
	public String getDiscordMode(){
		return this.ddmode;
	}
	
	public boolean matchDiscordID(String id){
		return this.ddchannel.equals(id);
	}
	
	public boolean isSendingDiscord(){
		return !ddchannel.isEmpty() && (ddmode.equalsIgnoreCase("both") || ddmode.equalsIgnoreCase("send"));
	}
	
	public boolean isListenDiscord(){
		return !ddchannel.isEmpty() && (ddmode.equalsIgnoreCase("both") || ddmode.equalsIgnoreCase("listen"));
	}
	
	public String getDiscordHover(){
		return this.ddhover;
	}
	
	public String getDiscordFormat(){
		return this.ddformat;
	}
	
	public List<CommandSender> getMembers(){
		return this.members;
	}
	
	public void clearMembers(){
		this.members.clear();
	}
	
	public boolean addMember(CommandSender p){
		for (UCChannel ch:UChat.get().getUCConfig().getChannels()){
			ch.removeMember(p);
		}
		return this.members.add(p);
	}
	
	public boolean removeMember(CommandSender p){
		return this.members.remove(p);
	}
	
	public boolean isMember(CommandSender p){
		return this.members.contains(p);
	}
		
	public boolean canLock(){
		return this.canLock;
	}
	
	public boolean availableInWorld(World w){
		return this.availableWorlds.contains(w.getName());
	}
	
	public List<String> availableWorlds(){
		return this.availableWorlds;
	}
	
	public String getAliasCmd(){
		return this.aliasCmd;
	}
	
	public String getAliasSender(){		
		return this.aliasSender;
	}
	
	public boolean isCmdAlias(){
		return this.isAlias;
	}
	
	public boolean useOwnBuilder(){
		return this.ownBuilder;
	}
	
	public double getCost(){
		return this.cost;
	}
	
	public void setCost(double cost){
		this.cost = cost;
	}
	
	public void setReceiversMsg(boolean show){
		this.receiversMsg = show;
	}
	
	public boolean getReceiversMsg(){
		return this.receiversMsg;
	}
	
	public void muteThis(String player){
		if (!this.mutes.contains(player)){
			this.mutes.add(player);
		}		
	}
	
	public void unMuteThis(String player){
		if (this.mutes.contains(player)){
			this.mutes.remove(player);
		}		
	}
	
	public boolean isMuted(String player){
		return this.mutes.contains(player);
	}
	
	public void ignoreThis(String player){
		if (!this.ignoring.contains(player)){
			this.ignoring.add(player);
		}		
	}
	
	public void unIgnoreThis(String player){
		if (this.ignoring.contains(player)){
			this.ignoring.remove(player);
		}		
	}
	
	public boolean isIgnoring(String player){
		return this.ignoring.contains(player);
	}
	
	public String[] getBuilder(){
		return this.builder.split(",");
	}
	
	public String getRawBuilder(){
		return this.builder;
	}
	
	public boolean crossWorlds(){
		return this.worlds;
	}
	
	public int getDistance(){
		return this.dist;
	}
	
	public String getColor(){
		return this.color;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getAlias(){
		return this.alias;
	}

	public boolean neeFocus() {
		return this.focus;
	}
	
	public boolean matchChannel(String aliasOrName){
		return this.alias.equalsIgnoreCase(aliasOrName) || this.name.equalsIgnoreCase(aliasOrName);
	}

	public boolean isBungee() {		
		return this.bungee ;
	}
	
	/** Send a message from a channel as player.
	 * @param src {@code Player}
	 * @param message {@code String} - Message to send.
	 */
	@Deprecated
	public void sendMessage(Player sender, String message){
		sendMessage(sender, new UltimateFancy(message), false);
	}
	
	/** Send a message from a channel as player.
	 * @param src {@code Player}
	 * @param message {@code FancyMessage} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(Player sender, UltimateFancy message, boolean direct){
		if (direct){
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getUCConfig().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					message.send(p);					
				}
			}			
			message.send(sender);	
		} else {
			Set<Player> pls = new HashSet<Player>();
			pls.addAll(Bukkit.getOnlinePlayers());
			UChat.get().tempChannels.put(sender.getName(), this.alias);
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, message.toOldFormat(), pls);
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){

				@Override
				public void run() {
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
					UChat.get().getServ().getPluginManager().callEvent(event); 
				}			
			});
		}		
	}
	
	/** Send a message from a channel as console.
	 * @param sender {@code ConsoleCommandSender} - Console sender.
	 * @param message {@code FancyMessage} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(ConsoleCommandSender sender, UltimateFancy message, boolean direct){	
		if (direct){
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getUCConfig().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					message.send(p);					
				}
			}
			message.send(sender);	
		} else {			
			UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire MessageChannelEvent");
			UCMessages.sendFancyMessage(new String[0], message.toOldFormat(), this, sender, null);
		}
	}
	
	/** Send a message from a channel as console.
	 * @param sender {@code ConsoleCommandSender} - Console sender.
	 * @param message {@code FancyMessage} - Message to send.
	 */
	@Deprecated
	public void sendMessage(ConsoleCommandSender sender, String message){	
		if (UChat.get().getUCConfig().getBool("api.format-console-messages")){
			UCMessages.sendFancyMessage(new String[0], message, this, sender, null);
		} else {
			UltimateFancy fmsg = new UltimateFancy(message);
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getUCConfig().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){	
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
					fmsg.send(p);					
				}
			}
			fmsg.send(sender);
		}
	}
}
