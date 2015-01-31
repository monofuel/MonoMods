/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoChat;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoChat extends JavaPlugin{
	
	private static Logger chatLogger = null;
	
	private static IRCListener irc;

	public void onEnable() {
		chatLogger = getLogger();
		
		//set defaults if they do not exist
		saveDefaultConfig();
		
		log("MonoChat has been enabled");
		
		irc = new IRCListener(this);
		
	}
	
	public void onDisable() {
		
		irc.close();
		
		log("MonoChat has been disabled");
		chatLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("MonoChat")) {
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;

			} else if (args[0].equalsIgnoreCase("load")) {
				reloadConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				//TODO
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	//TODO investigate better way for logging. should it really be static?
        //let other objects call our logger
        /** 
         * easy method any class in this plugin can use to log information.
         * @param line  Line to be logged.
         *
         */
        protected static void log(String line) {
                chatLogger.info(line);
        }   

        /** 
         * easy method any class in this plugin can use to log information.
         * @param level severity of log
         * @param line  Line to be logged.
         *
         */
        protected static void log(Level level, String line) {
                chatLogger.log(level,line);
        }   
}
