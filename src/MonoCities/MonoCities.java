package japura.MonoCities;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoCities extends JavaPlugin{
	
	private static Logger citiesLogger = null;
	
	private static MonoConf config = null;
	private static CityPopulator pop = null;
	private static final String configLoc = "plugins/MonoCities";
	private static WorldInit listener = null;

	public void onEnable() {
		citiesLogger = getLogger();
		
		//load configuration
		MonoConf.init();
		config = new MonoConf(configLoc);
		
		pop = new CityPopulator();
		
		
		World world = Bukkit.getWorld("World");
		if (world == null) {
			listener = new WorldInit(this,pop);
		} else {
			Bukkit.getWorld("World").getPopulators().add(pop);
		}
		
		log("MonoCities has been enabled");
	}
	
	public void onDisable() {
		
		//remove populator
		Bukkit.getWorld("World").getPopulators().remove(pop);
		
		//write config back out to file
		//if there were no errors reading config in
		config.close();
		if (listener != null)
			listener.stop();
		pop.close();
		
		log("MonoCities has been disabled");
		citiesLogger = null;
	}
	
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("plugintemplate")) {
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
	
	//let other objects call our logger
	public static void log(String line) {
		citiesLogger.info(line);
	}
}
