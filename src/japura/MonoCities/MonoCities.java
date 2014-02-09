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

	//used to disable the plugin before it fully loads if it 
	//cannot find any schematic files.
	private boolean disabled = true;

	public void onEnable() {
		citiesLogger = getLogger();
		
		//load configuration
		MonoConf.init();
		config = new MonoConf(configLoc);

		disabled = false;
		pop = new CityPopulator(this);
		//if no schematics are found, disabled will be set true.
		//this means we should probably not continue enabling.
		if (disabled) return;
		
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
		//do an extra check to make sure we aren't being disabled
		//before the world is initialized
		if (Bukkit.getWorld("World") != null)
			Bukkit.getWorld("World").getPopulators().remove(pop);
		
		//write config back out to file
		//if there were no errors reading config in
		if (config != null)
			config.close();
		if (listener != null)
			listener.stop();
		
		//If no schematics are found, pop will not have been initialized.
		if (pop != null)
			pop.close();
		
		log("MonoCities has been disabled");
		citiesLogger = null;
		disabled = true;
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
