/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoPerms;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

/**
 *	This plugin was built for the sake of having 100% custom plugins
 *	for japura.net. i actually highly recommend against using this
 *	for permissions, as there are currently no commands, and players
 * 	have to be set in a picky json format.
 */

public class MonoPerms extends JavaPlugin{
	
	private static Logger templateLogger = null;
	
	private static PermListener listener;
	private static JavaPlugin plugin;

	public void onEnable() {
		templateLogger = getLogger();
		plugin = this;
		
		//load configuration
		//TODO should use separate yaml file for permissions
		saveDefaultConfig();
		
		listener = new PermListener(this);
		setDonors();
		setAdmins();
		log("MonoPerms has been enabled");

	}
	
	public void onDisable() {
		
		saveConfig();
		listener.close();
		
		log("MonoPerms has been disabled");
		templateLogger = null;
	}
	
	public void loadData() {
		List<String> keys = this.getConfig().getStringList("groups.admin");
		
		for (String key : keys) {
			Player user = Bukkit.getPlayer(key);
			if (user == null) continue;
			if (user.getName().equals(key)) {
				//TODO why is this so wierd? why is all this in the listener too?
				//does this not work for offline players? why did i do it like this?
				//try this with Bukkit.getOfflinePlayer.
				addPerm(user,(String) "tribes.admin");
			}
		}
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
				reloadConfig();
				loadData();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				//TODO fix this up
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

	public static void setAdmins() {
		List<String> keys = plugin.getConfig().getStringList("groups.admin");
	
		for (String key : keys){
			Player user = Bukkit.getPlayer(key);
			if (user != null) {
				addPerm(user,"tribes.admin");
				addPerm(user,"nowither.admin");
				addPerm(user,"monoperms.admin");
				addPerm(user,"monolocks.admin");
				addPerm(user,"monobugs.admin");
				addPerm(user,"monochat.admin");
				addPerm(user,"monocities.admin");
			}
		}
	}

	public static void setDonors() {

		List<String> donors = plugin.getConfig().getStringList("groups.donors");

		for (String donorName : donors) {
			Player user = Bukkit.getPlayer(donorName);
			if (user != null) addPerm(user,"donor");
			Player donor = Bukkit.getPlayer(donorName);
			if (donor == null) continue; //that means they are offline
			if (donor.getName().equalsIgnoreCase("monofuel"))
				donor.setPlayerListName(ChatColor.YELLOW + donor.getName());
			//TODO 14 characters is silly and doesn't work with everyone. alternatives that don't crash the server?
			else if (donor.getName().length() < 14) //fix bug where chatcolor makes their name too long
			        donor.setPlayerListName(ChatColor.BLUE + donor.getName());




		}

	}
}
