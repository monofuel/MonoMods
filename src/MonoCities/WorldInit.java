package japura.MonoCities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldInit implements Listener {

	CityPopulator pop;
	
	public WorldInit(JavaPlugin plugin,CityPopulator pop) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
		this.pop = pop;
	}
	
	@EventHandler
	public void init(WorldInitEvent e) {
		MonoCities.log("world " + e.getWorld().getName() + " initializing");
		if (e.getWorld().getName().equals("world")) {
			MonoCities.log("adding populator to world");
			Bukkit.getWorld("world").getPopulators().add(pop);
		}
	}
	
	public void stop() {
		pop = null;
	}
	
}
