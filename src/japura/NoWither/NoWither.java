/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.NoWither;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.event.HandlerList;


public class NoWither extends JavaPlugin{

	
	private static Logger witherLogger = null;
	private boolean enabled = true;

	private static JavaPlugin witherPlugin = null;

	public void onEnable() {
		witherLogger = getLogger();
		witherPlugin = this;
		
		//new witherListener(this);
		getServer().getPluginManager().registerEvents(new WitherListener(),this);
		
		//set defaults if they do not exist
		saveDefaultConfig();
		enabled = getConfig().getBoolean("wither disabled");
		
		//check if we want wither disabled, and if so, disable
		if(enabled) {
			log("wither spawning disabled");
		} else {
			log("wither spawning enabled");
		}
	}
	
	public void onDisable() {

		HandlerList.unregisterAll(this);

		log("NoWither has been disabled");
		witherLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//verify this is the correct command, and check if 
		//it is being send via console or via player.
		//console always gets full access, but for the player we
		//will check the permission.
		//.hasPermission will only be tested if sender is indeed an instance of Player,
		//so this will not give an exception.
		if ("nowither".equalsIgnoreCase(cmd.getName()) &&
			(sender instanceof ConsoleCommandSender ||
			(sender instanceof Player && ((Player) sender).hasPermission("nowither.admin")))) {
			//safety first
			if (args.length < 1) return false;

			//valid cases will return true so that plugin help will not be displayed.
			//if none of these cases are met, then the 'return false' at the end
			//of this method would run. to be explicit, we'll default to return false.
			switch(args[0].toLowerCase()) {
				case "reload":
					this.getServer().getPluginManager().disablePlugin(this);
					this.getServer().getPluginManager().enablePlugin(this);
					return true;
				case "unload":
					this.getServer().getPluginManager().disablePlugin(this);
					return true;
				case "load":
					reloadConfig();
					return true;
				case "save":
					saveConfig();
					return true;
				case "enable":
					getConfig().set("wither disabled",false);
					sender.sendMessage("wither enabled");
					saveConfig();
					return true;
				case "disable":
					getConfig().set("wither disabled",true);
					sender.sendMessage("wither disabled");
					saveConfig();
					return true;
				case "help":
					String help = "NoWither can toggle the wither. all commands are in the style of /nowither <command>\n" +
						      "reload will disable and enable the plugin\n" +
						      "unload will disable the plugin\n" +
						      "load will reload the config\n" +
						      "save will save the current setting to the config\n" +
						      "enable will enable wither spawning\n" +
						      "disable will disable withe rspawning\n";
					sender.sendMessage(help);
					return true;
				default:
					return false;
			}
		//in the event that a player does not have permission...
		} else if (sender instanceof Player && !((Player) sender).hasPermission("nowither")) {
			sender.sendMessage("you do not have permission to use nowither");
			return true;

		}
		//failure case if all goes wrong, let's display plugin help.
		return false;
	}
	
	protected static boolean getWitherSetting() {
		return witherPlugin.getConfig().getBoolean("wither disabled");
	}

	//let other objects call our logger
	/**
	 * easy method any class in this plugin can use to log information.
	 * @param line	Line to be logged.
	 *
	 */
	protected static void log(String line) {
		witherLogger.info(line);
	}

	/**
	 * easy method any class in this plugin can use to log information.
	 * @param level severity of log
	 * @param line	Line to be logged.
	 *
	 */
	protected static void log(Level level, String line) {
		witherLogger.log(level,line);
	}

}
