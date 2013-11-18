package japura.MonoChat;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoChat extends JavaPlugin{
	
	private static Logger templateLogger = null;
	
	private static MonoConf config = null;
	private static final String configLoc = "plugins/MonoChat";
	private static IRCListener irc;

	public void onEnable() {
		templateLogger = getLogger();
		
		//load configuration
		MonoConf.init();
		config = new MonoConf(configLoc);
		
		log("MonoChat has been enabled");
		
		irc = new IRCListener(this);
		
	}
	
	public void onDisable() {
		
		irc.close();
		//write config back out to file
		//if there were no errors reading config in
		config.close();
		
		log("MonoChat has been disabled");
		templateLogger = null;
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
				//GARBAGE EVERYWHERE
				config = new MonoConf(configLoc);
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				config.close();
				config = new MonoConf(configLoc);
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	
	public static MonoConf getConf() {
		return config;
	}
	
	//let other objects call our logger
	public static void log(String line) {
		templateLogger.info(line);
	}
}
