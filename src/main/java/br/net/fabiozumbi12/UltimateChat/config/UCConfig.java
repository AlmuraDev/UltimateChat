package br.net.fabiozumbi12.UltimateChat.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UChat;

public class UCConfig{
	
	static UCYaml defConfigs = new UCYaml();
	static UCYaml configs = new UCYaml();
	static UCYaml Prots = new UCYaml();
	private HashMap<List<String>,UCChannel> channels = new HashMap<List<String>,UCChannel>();
	
	public UCConfig(UChat plugin, String mainPath) {
		            File main = new File(mainPath);
    	            File config = new File(mainPath+File.separator+"config.yml");
    	            File chfolder = new File(mainPath+File.separator+"channels");
    	            File defch = new File(mainPath+File.separator+"channels"+File.separator+"global.yml");
    	            File protections = new File(mainPath+File.separator+"protections.yml");
    	            
    	            if (!main.exists()) {
    	                main.mkdir();
    	                UChat.logger.info("Created folder: " +mainPath);
    	            }
    	            
    	            if (!chfolder.exists()) {
    	            	chfolder.mkdir();
    	                UChat.logger.info("Created folder: " +chfolder.getPath());
    	            }    	            

    	            if (!config.exists() && !defch.exists()) {
    	            	plugin.saveResource("channels/global.yml", false);//create config file    	 
    	            	plugin.saveResource("channels/local.yml", false);//create config file  
    	                UChat.logger.info("Created channels file...");
    	            } 
    	            
    	            if (!config.exists()) {
    	            	plugin.saveResource("config.yml", false);//create config file    	            	
    	                UChat.logger.info("Created config file: " + config.getPath());
    	            } 
    	            
    	            if (!protections.exists()) {
    	            	plugin.saveResource("protections.yml", false);//create protections file    	            	
    	                UChat.logger.info("Created protections file: " + protections.getPath());
    	            }
    	                	            
    	            //------------------------------ Add default Values ----------------------------//
    	            
    	            configs = updateFile(config, "config.yml"); 
    	            
                    //--------------------------------------------------------------------------//
                    
                    UChat.logger.info("Server version: " + plugin.serv.getBukkitVersion());
                    
                    // check if can enable json support
                    if (getBool("general.hover-events")){                    	
                    	try {
                    		Class.forName("com.google.gson.JsonParser");
                          	if (plugin.serv.getBukkitVersion().contains("1.7")){
                          		configs.set("general.hover-events", false);
                          		UChat.logger.warning("Your server version do not support Hover features, only 1.8.+");
                          	}                           	
                       	} catch(ClassNotFoundException e ) {
                       		configs.set("general.hover-events", false);
                       		UChat.logger.warning("Your server version do not support JSON events, disabling Hover and Clicking features.");
                       	}
                    }           
                    
                    //--------------------------------------- Load Aliases -----------------------------------//
                                        
                    File[] listOfFiles = chfolder.listFiles();
            		for (File file:listOfFiles){
            			if (file.getName().endsWith(".yml")){
            				YamlConfiguration channel = UCYaml.loadConfiguration(file);
							UCChannel ch = new UCChannel(channel.getString("name"), 
									channel.getString("alias"), 
									channel.getBoolean("across-worlds", true),
									channel.getInt("distance", 0),
									channel.getString("color", "&b"),
									channel.getString("tag-builder", getString("general.default-tag-builder")),
									channel.getBoolean("need-focus", false),
									channel.getBoolean("receivers-message", true),
									channel.getDouble("cost", 0.0),
									channel.getBoolean("bungee", false),
									channel.getBoolean("use-this-builder", false),
									channel.getBoolean("channelAlias.enable", false),
									channel.getString("channelAlias.sendAs", "player"),
									channel.getString("channelAlias.cmd", ""));
            				channels.put(Arrays.asList(file.getName().replace(".yml", ""), ch.getAlias()), ch);
            				channel.options().header(""
            						+ "###################################################\n"
            						+ "############## Channel Configuration ##############\n"
            						+ "###################################################\n"
            						+ "\n"
            						+ "This the channel configuration.\n"
            						+ "You can change and copy this file to create as many channels you want.\n"
            						+ "This is the default options:\n"
            						+ "\n"
            						+ "name: Global - The name of channel.\n"
            						+ "alias: g - The alias to use the channel\n"
            						+ "across-worlds: true - Send messages of this channel to all worlds?\n"
            						+ "distance: 0 - If across worlds is false, distance to receive this messages.\n"
            						+ "color: &b - The color of channel\n"
            						+ "tag-builder: ch-tags,world,clan-tag,marry-tag,group-prefix,nickname,group-suffix,message - Tags of this channel\n"
            						+ "need-focus: false - Player can use the alias or need to use '/ch g' to use this channel?\n"
            						+ "receivers-message: true - Send chat messages like if no player near to receive the message?\n"
            						+ "cost: 0.0 - Cost to player use this channel.\n"
            						+ "use-this-builder: false - Use this tag builder or use the 'config.yml' tag-builder?\n"
            						+ "channelAlias - Use this channel as a command alias.\n"
            						+ "  enable: true - Enable this execute a command alias?\n"
            						+ "  sendAs: player - Send the command alias as 'player' or 'console'?\n"
            						+ "  cmd: '' - Command to send on every message send by this channel.\n");
            				channel.set("name", ch.getName());
            				channel.set("alias", ch.getAlias());
            				channel.set("across-worlds", ch.crossWorlds());
            				channel.set("distance", ch.getDistance());
            				channel.set("color", ch.getColor());
            				channel.set("tag-builder", ch.getRawBuilder());
            				channel.set("need-focus", ch.neeFocus());
            				channel.set("receivers-message", ch.getReceiversMsg());
            				channel.set("cost", ch.getCost());
            				channel.set("bungee", ch.isBungee());
            				channel.set("channelAlias.enable", ch.isCmdAlias());
            				channel.set("channelAlias.sendAs", ch.getAliasSender());
            				channel.set("channelAlias.cmd", ch.getAliasCmd());
            				try {
								channel.save(file);
							} catch (IOException e) {
								e.printStackTrace();
							}
            			}
            		}
                    
                    //-------------------------------- Change config Header ----------------------------------//
                    
            		String lang = configs.getString("language");
            		if (lang.equalsIgnoreCase("EN-US")){
            			configs.options().header(""
            					+ "Uchat configuration file\n"
            					+ "Author: FabioZumbi12\n"
            					+ "We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
            					+ "------------------------------------------------------------------------\n"
            					+ "Tags is where you can customize what will show on chat, on hover or on click on tag.\n"
            					+ "To add a tag, you can copy an existent and change the name and the texts.\n"
            					+ "After add and customize your tag, put the tag name on 'general > default-tag-builder'.\n"
            					+ "------------------------------------------------------------------------\n"
            					+ "Available replacers:\n"
            					+ "uChat:\n"
            					+ " - {default-format-full}: Use this tag to see all plugin tags using the default bukkit format. "
            					+ "Normally used by 'myth' plugins and temporary tags."
            					+ "If you want to use only of this tags you can use the replacer bellow and get number of tag separated by spaces;\n"
            					+ " - {default-format-0}: use this tag to show only one of the tags described on '{default-format-full}'. "
            					+ "The number is the posiotion separated by spaces;\n"
            					+ " - {world}: Replaced by sender world;\n"
            					+ " - {message}: Message sent by player;\n"
            					+ " - {playername}: The name of player;\n"
            					+ " - {nickname}: The nickname of player. If not set, will show realname;\n"
            					+ " - {ch-name}: Channel name;\n"
            					+ " - {ch-alias}: Channel alias;\n"
            					+ " - {ch-color}: Channel color;\n"
            					+ "Vault:\n"
            					+ " - {group-prefix}: Get group prefix;\n"
            					+ " - {group-suffix}: Get group suffix;\n"
            					+ " - {balance}: Get the sender money;\n"
            					+ " - {prim-group}: Get the primary group tag;\n"
            					+ " - {player-groups}: Get all groups names the sender has;\n"
            					+ " - {player-groups-prefixes}: Get all group prefixes the sender has;\n"
            					+ " - {player-groups-suffixes}: Get all group suffixes the sender has;\n"
            					+ "Simpleclans:\n"
            					+ " - {clan-tag}: Clan tag without colors;\n"
            					+ " - {clan-ctag}: Clan with colors;\n"
            					+ " - {clan-name}: Clan name;\n"
            					+ " - {clan-kdr}: Player clan KDR;\n"
            					+ " - {clan-isleader}: The player is leader;\n"
            					+ " - {clan-rank}: Player rank on Clan;\n"
            					+ " - {clan-totalkdr}: Clan KDR (not player kdr);\n"
            					+ "Marry Plugins:\n"
            					+ " - {marry-partner}: Get the partner name;\n"       			
            					+ " - {marry-prefix}: Get the married prefix tag, normally the heart;\n"   
            					+ " - {marry-suffix}: Get the married suffix tag, or male tag for Marriage Reloaded;\n"
            					+ "BungeeCord:\n"
            					+ "For BungeeCord theres 2 new tags. The other tags still working except username, group and other plugin tags.\n"
            					+ "- {world}: World from sender;\n"
            					+ "- {server}: Server from sender, equals on server list on Bungee config.yml;\n"
            					+ "");
            		}
            		if (lang.equalsIgnoreCase("PT-BR")){
            			configs.options().header(""
            					+ "Arquivo de configuração do Uchat\n"
            					+ "Autor: FabioZumbi12\n"
            					+ "Recomando usar o Notepad++ para editar este arquivo!\n"
            					+ "------------------------------------------------------------------------\n"
            					+ "Tags é onde voce vai personalizar os textos pra aparecer no chat, ao passar o mouse ou clicar na tag.\n"
            					+ "Para adicionar uma tag, copie uma existente e troque o nome para um de sua escolha.\n"
            					+ "Depois de criar e personalizar a tag, adicione ela em 'general > default-tag-builder'.\n"
            					+ "------------------------------------------------------------------------\n"
            					+ "Replacers disponíveis:\n"
            					+ "uChat:\n"
            					+ " - {default-format-full}: Use esta tag para ver todas tags de plugins que estão usando o formato padrão do bukkit. "
            					+ "Normalmente usado por plugins de 'mito' e tags temporárias. "
            					+ "Caso queira apenas usar uma delas elas são separadas por espaços e abaixo vc pode usar apenas uma de cada.\n"
            					+ " - {default-format-0}: Use esta tag para usar apenas uma tag das descritas acima. O numero é a posição dela entre os espaços;\n"
            					+ " - {world}: O mundo de quem enviou a mensagem;\n"
            					+ " - {message}: Mensagem enviada;\n"
            					+ " - {playername}: O nome de quem enviou;\n"
            					+ " - {nickname}: O nick de quem enviou. Se o nick não foi definido irá mostrar o nome;\n"
            					+ " - {ch-name}: Nome do canal;\n"
            					+ " - {ch-alias}: Atalho do canal;\n"
            					+ " - {ch-color}: Cor do canal;\n"
            					+ "Vault:\n"
            					+ " - {group-prefix}: Prefixo do grupo do player;\n"
            					+ " - {group-suffix}: Sufixo do grupo do player;\n"
            					+ " - {balance}: Dinheiro do player;\n"
            					+ " - {prim-group}: Tag do grupo primário;\n"
            					+ " - {player-groups}: Lista todos grupos que o player faz parte;\n"
            					+ " - {player-groups-prefixes}: Lista todo prefixes dos grupos que o player esta;\n"
            					+ " - {player-groups-suffixes}: Lista todo suffixes dos grupos que o player esta;\n"
            					+ "Simpleclans:\n"
            					+ " - {clan-tag}: Tag do Clan sem cores;\n"
            					+ " - {clan-ctag}: Tag do Clan com cores;\n"
            					+ " - {clan-name}: Nome do Clan;\n"
            					+ " - {clan-kdr}: KDR do player do Clan;\n"
            					+ " - {clan-isleader}: O player é lider;\n"
            					+ " - {clan-rank}: Rank do player no Clan;\n"
            					+ " - {clan-totalkdr}: Clan KDR (não do player);\n"   
            					+ "Marry Plugins:\n"
            					+ " - {marry-partner}: Mostra o nome do(a) parceiro(a);\n"      			
            					+ " - {marry-prefix}: Pega a tag de prefixo, normalmente o coração;\n"   
            					+ " - {marry-suffix}: Pega a tag de sufixo, ou simbolo masculino do Marriage Reloaded;\n"  
            					+ "BungeeCord:\n"
            					+ "Para bungee cord tem 2 tags novas. Algumas das outras tags ainda funcionam, com excessão da username, de grupo e de outros plugins.\n"
            					+ "- {world}: Mundo de quem enviou;\n"
            					+ "- {server}: O Server de quem enviou, igual o especificado em config.yml do BungeeCord;\n"
            					+ "");
            		}
            		
            		/*------------------------------------------------------------------------------------*/
            		
            		//add new config on update
            		if (configs.getDouble("config-version") < 1.2){
            			configs.set("config-version", 1.2);
            			
            			configs.set("tags.custom-tag.format", "&7[&2MyTag&7]&r");
            			configs.set("tags.custom-tag.click-cmd", "");
            			configs.set("tags.custom-tag.hover-messages", new ArrayList<String>());
            			configs.set("tags.custom-tag.permission", "any-name-perm.custom-tag");            			
            		}
            		
                    /*------------------------------------------------------------------------------------*/
        			
        			//load protections file
        			Prots = updateFile(protections, "protections.yml");
                    
        			/*------------------------------------------------------------------------------------*/
        			
            		//----------------------------------------------------------------------------------------//
        			save();        			
    	            UChat.logger.info("All configurations loaded!");
	}
    
	public List<String> getTagList(){
		List<String> tags = new ArrayList<String>();
		for (String key:configs.getKeys(true)){
			if (key.startsWith("tags.") && key.split("\\.").length == 2){
				tags.add(key.replace("tags.", ""));
			}			
		}
		for (String str:getStringList("general.custom-tags")){
			tags.add(str);
		}
		return tags;
	}
	
	public UCChannel getChannel(String alias){		
		for (List<String> aliases:channels.keySet()){
			if (aliases.contains(alias.toLowerCase())){				
				return channels.get(aliases);
			}
		}
		return null;
	}
	
	public Collection<UCChannel> getChannels(){
		return this.channels.values();
	}
	
	public void addChannel(UCChannel channel){
		channels.put(Arrays.asList(channel.getName(), channel.getAlias()), channel);
	}
	
	public void unMuteInAllChannels(String player){
		for (UCChannel ch:channels.values()){
			if (ch.isMuted(player)){				
				ch.unMuteThis(player);;
			}
		}
	}
	
	public void muteInAllChannels(String player){
		for (UCChannel ch:channels.values()){
			if (!ch.isMuted(player)){				
				ch.muteThis(player);;
			}
		}
	}
	
	public UCChannel getDefChannel(){
		UCChannel ch = getChannel(getString("general.default-channel"));
		if (ch == null){
			UChat.logger.severe("Defalt channel not found with alias '"+getString("general.default-channel")+"'. Fix this setting to a valid channel alias.");
		}
		return getChannel(getString("general.default-channel"));
	}
	
	public String[] getDefBuilder(){
		return getString("general.default-tag-builder").replace(" ", "").split(",");
	}
	
	public List<String> getChAliases(){
		List<String> aliases = new ArrayList<String>();
		aliases.addAll(Arrays.asList(configs.getString("general.channel-cmd-aliases").replace(" ", "").split(",")));
		for (List<String> alias:channels.keySet()){
			aliases.addAll(alias);
		}
		return aliases;
	}
	
	public List<String> getBroadcastAliases() {
		return Arrays.asList(configs.getString("broadcast.aliases").replace(" ", "").split(","));
	}
	
	public List<String> getTellAliases() {
		return Arrays.asList(configs.getString("tell.cmd-aliases").replace(" ", "").split(","));
	}
	
    public Boolean getBool(String key){		
		return configs.getBoolean(key, false);
	}
    
    public void setConfig(String key, Object value){
    	configs.set(key, value);
    }
    
    public String getString(String key){		
		return configs.getString(key);
	}
    
    public Integer getInt(String key){		
		return configs.getInt(key);
	}
    
    public List<String> getStringList(String key){		
		return configs.getStringList(key);
	}
    
    public Material getMaterial(String key){
    	return Material.getMaterial(configs.getString(key));
    }
    
    public void save(){
    	try {
    		configs.save();
			Prots.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static UCYaml inputLoader(InputStream inp) {
		UCYaml file = new UCYaml();
		try {
			file.load(new InputStreamReader(inp, StandardCharsets.UTF_8));
			inp.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return file;
	}	
    
    //protection methods

	public int getProtInt(String key){
		return Prots.getInt(key);
	}
	
	public boolean getProtBool(String key){
		return Prots.getBoolean(key);
	}
	
	public List<String> getProtStringList(String key){
		return Prots.getStringList(key);
	}
	
    public boolean containsProtKey(String key){		
		return Prots.contains(key);
	}
    
	public String getProtString(String key){
		return Prots.getString(key);
	}
	
	public String getProtMsg(String key){
		return ChatColor.translateAlternateColorCodes('&',Prots.getString(key));
	}
	
	public String getColor(String key){
		return ChatColor.translateAlternateColorCodes('&',configs.getString(key));
	}
	
	private static UCYaml updateFile(File saved, String filename){
		UCYaml finalyml = new UCYaml();    			
        try {
        	finalyml.load(saved);
		} catch (Exception e) {
			e.printStackTrace();
		} 
                			
        UCYaml tempProts = inputLoader(UChat.plugin.getResource(filename));  
        for (String key:tempProts.getKeys(true)){    
        	Object obj = tempProts.get(key);
        	if (finalyml.get(key) != null){
        		obj = finalyml.get(key);
        	}
        	finalyml.set(key, obj);    	            	   	            	
        }  
        return finalyml;
	}
	
}
   
