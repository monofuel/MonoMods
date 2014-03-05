/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoPerms;

import japura.MonoUtil.MonoConf;

import org.json.simple.JSONObject;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;

/**
 *	This plugin was built for the sake of having 100% custom plugins
 *	for japura.net. i actually highly recommend against using this
 *	for permissions, as there are currently no commands, and players
 * 	have to be set in a picky json format.
 */

public class MonoPerms extends JavaPlugin{
	
	private static Logger templateLogger = null;
	
	private static MonoConf config = null;
	private static PermData data = null;
	
	private static PermListener listener;
	private static JavaPlugin plugin;
	private static final String configLoc = "plugins/MonoPerms";

	public JSONObject genDefaultConf() {
		JSONObject defaults = new JSONObject();

		//this is where i'd put my config options. IF I HAD ANY
		
		return defaults;
	}

	public void onEnable() {
		templateLogger = getLogger();
		plugin = this;
		
		//load configuration
		config = new MonoConf(this,genDefaultConf());
		PermData.init();
		data = new PermData(configLoc);
		
		loadData();
		listener = new PermListener(this);
		log("MonoPerms has been enabled");
	}
	
	public void onDisable() {
		
		
		//write config back out to file
		//if there were no errors reading config in
		config.close();
		data.close();
		listener.close();
		
		log("MonoPerms has been disabled");
		templateLogger = null;
	}
	
	public void loadData() {
		Set<String> keys = MonoPerms.getData().getKeys();
		
		for (String key : keys) {
			Player user = Bukkit.getPlayer(key);
			if (user == null) continue;
			if (user.getName().equals(key)) {
				JSONArray permList = (JSONArray) MonoPerms.getData().getConf(key);
				for (int i = 0; i < permList.size(); i++) {
					addPerm(user,(String) permList.get(i));
				}
			}
		}
	}
	
	public static PermData getData() {
		return data;
	}
	
	public static void addPerm(Player user,String perm) {
		PermissionAttachment attachment = user.addAttachment(plugin);
		attachment.setPermission(perm, true);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("perm")) {
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				//GARBAGE EVERYWHERE
				config = new MonoConf(this,genDefaultConf());
				loadData();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				config.close();
				config = new MonoConf(this,genDefaultConf());
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	
	//let other objects call our logger
	public static void log(String line) {
		templateLogger.info(line);
	}
}
