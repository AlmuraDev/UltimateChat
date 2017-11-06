package br.net.fabiozumbi12.UltimateChat.Bukkit.config;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class UCConfig extends FileConfiguration {
	
	static YamlConfiguration defConfigs = new YamlConfiguration();
	static YamlConfiguration configs = new YamlConfiguration();
	static YamlConfiguration Prots = new YamlConfiguration();
		
	public UCConfig(UChat plugin) throws IOException, InvalidConfigurationException {
		            File main = UChat.get().getDataFolder();
    	            File config = new File(UChat.get().getDataFolder(),"config.yml");    	            
    	            File protections = new File(UChat.get().getDataFolder(),"protections.yml");
    	            
    	            if (!main.exists()) {
    	                main.mkdir();
    	                plugin.getUCLogger().info("Created folder: " +UChat.get().getDataFolder());
    	            }
    	            
    	            if (!config.exists()) {
    	            	UCUtil.saveResource("/assets/ultimatechat/config.yml", new File(UChat.get().getDataFolder(),"config.yml"));
    	            	plugin.getUCLogger().info("Created config file: " + config.getPath());
    	            } 
    	            
    	            if (!protections.exists()) {
    	            	UCUtil.saveResource("/assets/ultimatechat/protections.yml", new File(UChat.get().getDataFolder(),"protections.yml"));
    	            	plugin.getUCLogger().info("Created protections file: " + protections.getPath());
    	            }
    	                	            
    	            //------------------------------ Add default Values ----------------------------//
    	            
    	            configs = updateFile(config, "assets/ultimatechat/config.yml"); 
    	            
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
            					+ "\n"   
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
            					+ " - {hand-type}: Item type;\n"
            					+ " - {hand-name}: Item name;\n"
            					+ " - {hand-amount}: Item quantity;\n"
            					+ " - {hand-lore}: Item description (lore);\n"
            					+ " - {hand-durability}: Item durability;\n"
            					+ " - {hand-enchants}: Item enchantments;\n"
            					+ "\n"   
            					+ "Vault:\n"
            					+ " - {group-prefix}: Get group prefix;\n"
            					+ " - {group-suffix}: Get group suffix;\n"
            					+ " - {balance}: Get the sender money;\n"
            					+ " - {prim-group}: Get the primary group tag;\n"
            					+ " - {player-groups}: Get all groups names the sender has;\n"
            					+ " - {player-groups-prefixes}: Get all group prefixes the sender has;\n"
            					+ " - {player-groups-suffixes}: Get all group suffixes the sender has;\n"
            					+ "\n"   
            					+ "Simpleclans:\n"
            					+ " - {clan-tag}: Clan tag without colors;\n"
            					+ " - {clan-fulltag}: Clan tag with brackets, colors and separator;\n"
            					+ " - {clan-ctag}: Clan with colors;\n"
            					+ " - {clan-name}: Clan name;\n"
            					+ " - {clan-kdr}: Player clan KDR;\n"
            					+ " - {clan-isleader}: The player is leader;\n"
            					+ " - {clan-rank}: Player rank on Clan;\n"
            					+ " - {clan-totalkdr}: Clan KDR (not player kdr);\n"
            					+ "\n"   
            					+ "Marry Plugins:\n"
            					+ " - {marry-partner}: Get the partner name;\n"       			
            					+ " - {marry-prefix}: Get the married prefix tag, normally the heart;\n"   
            					+ " - {marry-suffix}: Get the married suffix tag, or male tag for Marriage Reloaded;\n"
            					+ "\n"   
            					+ "BungeeCord:\n"
            					+ "- {bungee-id}: Server ID from sender;\n"
            					+ "\n"   
            					+ "Jedis (redis):\n"
            					+ "- {jedis-id}: This server id;\n"
            					+ "\n"   
            					+ "Factions:\n"
            					+ "Gets the info of faction to show on chat.\n"
            					+ "- {fac-id}: Faction ID;\n"
            					+ "- {fac-name}: Faction Name;\n"
            					+ "- {fac-motd}: Faction MOTD;\n"
            					+ "- {fac-description}: Faction Description;\n"
            					+ "- {fac-relation-name}: Faction name in relation of reader of tag;\n"
            					+ "- {fac-relation-color}: Faction color in relation of reader of tag;\n"
            					+ "\n");
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
            					+ "\n"   
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
            					+ " - {hand-type}: Tipo do item;\n"
            					+ " - {hand-name}: Nome do item;\n"
            					+ " - {hand-amount}: Quantidade do item;\n"
            					+ " - {hand-lore}: Descrição do item(lore);\n"
            					+ " - {hand-durability}: Durabilidade do item;\n"
            					+ " - {hand-enchants}: Encantamentos do item;\n"
            					+ "\n"   
            					+ "Vault:\n"
            					+ " - {group-prefix}: Prefixo do grupo do player;\n"
            					+ " - {group-suffix}: Sufixo do grupo do player;\n"
            					+ " - {balance}: Dinheiro do player;\n"
            					+ " - {prim-group}: Tag do grupo primário;\n"
            					+ " - {player-groups}: Lista todos grupos que o player faz parte;\n"
            					+ " - {player-groups-prefixes}: Lista todo prefixes dos grupos que o player esta;\n"
            					+ " - {player-groups-suffixes}: Lista todo suffixes dos grupos que o player esta;\n"
            					+ "\n"   
            					+ "Simpleclans:\n"
            					+ " - {clan-tag}: Tag do Clan sem cores;\n"
            					+ " - {clan-fulltag}: Tag do clan com os colchetes, cores e separador;\n"
            					+ " - {clan-ctag}: Tag do Clan com cores;\n"
            					+ " - {clan-name}: Nome do Clan;\n"
            					+ " - {clan-kdr}: KDR do player do Clan;\n"
            					+ " - {clan-isleader}: O player é lider;\n"
            					+ " - {clan-rank}: Rank do player no Clan;\n"
            					+ " - {clan-totalkdr}: Clan KDR (não do player);\n"
            					+ "\n"   
            					+ "Marry Plugins:\n"
            					+ " - {marry-partner}: Mostra o nome do(a) parceiro(a);\n"      			
            					+ " - {marry-prefix}: Pega a tag de prefixo, normalmente o coração;\n"   
            					+ " - {marry-suffix}: Pega a tag de sufixo, ou simbolo masculino do Marriage Reloaded;\n"
            					+ "\n"   
            					+ "BungeeCord:\n"
            					+ "- {bungee-id}: O ID do Server de quem enviou;\n"
            					+ "\n"   
            					+ "Jedis (redis):\n"
            					+ "- {jedis-id}: O ID deste server;\n"
            					+ "\n"   
            					+ "Factions:\n"
            					+ "Pega as informações das Factions pra mostrar no chat.\n"
            					+ "- {fac-id}: Faction ID;\n"
            					+ "- {fac-name}: Nome da Faction;\n"
            					+ "- {fac-motd}: MOTD da faction;\n"
            					+ "- {fac-description}: Descrição da Faction;\n"
            					+ "- {fac-relation-name}: Nome da Faction em relação á quem ta lendo a tag;\n"
            					+ "- {fac-relation-color}: Cor da Faction em relação á quem ta lendo a tag;\n"
            					+ "\n");
            		}
            		
            		/*------------------------------------------------------------------------------------*/
            		            		
            		//add new config on update
            		if (configs.getDouble("config-version") < 1.2){
            			configs.set("config-version", 1.2);
            			
            			configs.set("tags.custom-tag.format", "&7[&2MyTag&7]&r");
            			configs.set("tags.custom-tag.click-cmd", "");
            			configs.set("tags.custom-tag.suggest-cmd", "");
            			configs.set("tags.custom-tag.hover-messages", new ArrayList<String>());
            			configs.set("tags.custom-tag.permission", "any-name-perm.custom-tag");     
            			configs.set("tags.custom-tag.show-in-worlds", new ArrayList<String>());
            			configs.set("tags.custom-tag.hide-in-worlds", new ArrayList<String>());
            		}
            		if (configs.getDouble("config-version") < 1.3){
            			configs.set("config-version", 1.3);
            			
            			configs.set("tags.factions.format", "&7[{fac-relation-color}{fac-relation-name}&7]&r");
            			configs.set("tags.factions.click-cmd", "");
            			configs.set("tags.factions.hover-messages", Arrays.asList(
            					"&7Faction name: {fac-relation-color}{fac-name}", 
            					"&7Motd: &a{fac-motd}", 
            					"&7Description: {fac-description}"));   
            			configs.set("tags.factions.permission", ""); 
            			configs.set("tags.factions.show-in-worlds", new ArrayList<String>());
            			configs.set("tags.factions.hide-in-worlds", new ArrayList<String>());
            		}
            		if (configs.getDouble("config-version") < 1.4){
            			configs.set("config-version", 1.4);
            			
            			configs.set("tags.custom-tag.click-url", "");
            		}
            		if (configs.getDouble("config-version") < 1.5){
            			configs.set("config-version", 1.5);
            			
            			configs.set("tags.jedis.format", "{jedis-id}");
            			configs.set("tags.jedis.hover-messages", Arrays.asList("&7Server: {jedis-id}","&cChange me on configuration!"));
            		}
            		
                    /*------------------------------------------------------------------------------------*/
        			
        			//load protections file
        			Prots = updateFile(protections, "assets/ultimatechat/protections.yml");

            		/* Load Channels */
            		loadChannels();
            		
        			/*------------------------------------------------------------------------------------*/
        			
            		//----------------------------------------------------------------------------------------//
        			save();        			
    	            plugin.getUCLogger().info("All configurations loaded!");
	}
    
	/* Channels */
	private void loadChannels() throws IOException{
		File chfolder = new File(UChat.get().getDataFolder(),"channels");
		
		if (!chfolder.exists()) {
        	chfolder.mkdir();
        	UChat.get().getUCLogger().info("Created folder: " +chfolder.getPath());
        } 
		
		if (UChat.get().getChannels() == null){
			UChat.get().setChannels(new HashMap<List<String>,UCChannel>());
		}
		
        File[] listOfFiles = chfolder.listFiles();
        
        YamlConfiguration channel = new YamlConfiguration();
        
        if (listOfFiles.length == 0){
        	//create default channels
        	File g = new File(chfolder, "global.yml"); 
        	channel = YamlConfiguration.loadConfiguration(g);
        	channel.set("name", "Global");
        	channel.set("alias", "g");
        	channel.set("color", "&2");
        	channel.set("jedis", true);
        	channel.save(g);
        	
        	File l = new File(chfolder, "local.yml");
        	channel = YamlConfiguration.loadConfiguration(l);
        	channel.set("name", "Local");
        	channel.set("alias", "l");
        	channel.set("color", "&e");
        	channel.set("across-worlds", false);
        	channel.set("distance", 40);
        	channel.save(l);
        	
        	File ad = new File(chfolder, "admin.yml");
        	channel = YamlConfiguration.loadConfiguration(ad);
        	channel.set("name", "Admin");
        	channel.set("alias", "ad");
        	channel.set("color", "&b");
        	channel.set("jedis", true);
        	channel.save(ad);
        	
        	listOfFiles = chfolder.listFiles();
        }
        
		for (File file:listOfFiles){
			if (file.getName().endsWith(".yml")){
				channel = YamlConfiguration.loadConfiguration(file);            				
				UCChannel ch = new UCChannel(channel.getValues(true));
				
				try {
					addChannel(ch);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void delChannel(UCChannel ch) {
		for (Entry<List<String>, UCChannel> ch0:UChat.get().getChannels().entrySet()){
			if (ch0.getValue().equals(ch)){
				UChat.get().getChannels().remove(ch0.getKey());
				break;
			}
		}
		File defch = new File(UChat.get().getDataFolder(),"channels"+File.separator+ch.getName().toLowerCase()+".yml");	
		if (defch.exists()){
			defch.delete();
		}
	}
	
	public void addChannel(UCChannel ch) throws IOException{	
		File defch = new File(UChat.get().getDataFolder(),"channels"+File.separator+ch.getName().toLowerCase()+".yml");		
		YamlConfiguration chFile = YamlConfiguration.loadConfiguration(defch);
		chFile.options().header(""
				+ "###################################################\n"
				+ "############## Channel Configuration ##############\n"
				+ "###################################################\n"
				+ "\n"
				+ "This is the channel configuration.\n"
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
				+ "canLock: true - Change if the player can use /<channel> to lock on channel."
				+ "receivers-message: true - Send chat messages like if 'no players near to receive the message'?\n"
				+ "cost: 0.0 - Cost to player use this channel.\n"
				+ "use-this-builder: false - Use this tag builder or use the 'config.yml' tag-builder?\n"
				+ "channelAlias - Use this channel as a command alias.\n"
				+ "  enable: true - Enable this execute a command alias?\n"
				+ "  sendAs: player - Send the command alias as 'player' or 'console'?\n"
				+ "  cmd: '' - Command to send on every message send by this channel.\n"
				+ "available-worlds - Worlds and only this world where this chat can be used and messages sent/received.\n"
				+ "discord:\n"
				+ "  mode: NONE - The options are NONE, SEND, LISTEN, BOTH. If enabled and token code set and the channel ID matches with one discord channel, will react according the choosen mode.\n"
				+ "  hover: &3Discord Channel: &a{dd-channel}\\n&3Role Name: {dd-rolecolor}{dd-rolename}\n"
				+ "  format-to-mc: {ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: \n"
				+ "  format-to-dd: :thought_balloon: **{sender}**: {message} \n"
				+ "  allow-server-cmds: false - Use this channel to send commands from discord > minecraft.\n"
				+ "  channelID: '' - The ID of your Discord Channel. Enable debug on your discord to get the channel ID.\n");
		
		ch.getProperties().entrySet().forEach((map)->{
			chFile.set((String) map.getKey(), map.getValue());			
		});
		chFile.save(defch);		
		
		if (UChat.get().getChannel(ch.getName()) != null){
			ch.setMembers(UChat.get().getChannel(ch.getName()).getMembers());
			UChat.get().getChannels().remove(Arrays.asList(ch.getName().toLowerCase(), ch.getAlias().toLowerCase()));
		}
		UChat.get().getChannels().put(Arrays.asList(ch.getName().toLowerCase(), ch.getAlias().toLowerCase()), ch);
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
		
	public String[] getDefBuilder(){
		return getString("general.default-tag-builder").replace(" ", "").split(",");
	}
		
	public List<String> getBroadcastAliases() {
		return Arrays.asList(configs.getString("broadcast.aliases").replace(" ", "").split(","));
	}
	
	public List<String> getTellAliases() {
		return Arrays.asList(configs.getString("tell.cmd-aliases").replace(" ", "").split(","));
	}
	
	public List<String> getMsgAliases() {
		return Arrays.asList(configs.getString("general.umsg-cmd-aliases").replace(" ", "").split(","));
	}
	
    public boolean getBoolean(String key){		
		return configs.getBoolean(key, false);
	}
    
    public void setConfig(String key, Object value){
    	configs.set(key, value);
    }
    
    public String getString(String key){		
		return configs.getString(key);
	}
    
    public int getInt(String key){		
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
    		configs.save(new File(UChat.get().getDataFolder(),"config.yml"));
			Prots.save(new File(UChat.get().getDataFolder(),"protections.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //protection methods
    public ConfigurationSection getProtReplecements() {
		return Prots.getConfigurationSection("chat-protection.censor.replace-words");
	}
    
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
	
	public String getColorStr(String key){
		return ChatColor.translateAlternateColorCodes('&',configs.getString(key));
	}
	
	private static YamlConfiguration updateFile(File saved, String resource){
		YamlConfiguration finalyml = new YamlConfiguration();   
		YamlConfiguration tempProts = new YamlConfiguration();
        try {
        	finalyml.load(saved);
        		      
        	tempProts.load(new InputStreamReader(UChat.get().getResource(resource), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
        for (String key:tempProts.getKeys(true)){    
        	Object obj = tempProts.get(key);
        	if (finalyml.get(key) != null){
        		obj = finalyml.get(key);
        	}
        	finalyml.set(key, obj);    	            	   	            	
        }  
        return finalyml;
	}

	public String getURLTemplate() {
		return ChatColor.translateAlternateColorCodes('&', configs.getString("general.URL-template"));
	}

	@Override
	public String saveToString() {
		return null;
	}

	@Override
	public void loadFromString(String contents)
			throws InvalidConfigurationException {
	}

	@Override
	protected String buildHeader() {
		return null;
	}
}
   
