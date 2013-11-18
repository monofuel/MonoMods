package japura.NoWither;


import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class WitherListener implements Listener{
	
	public WitherListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		LivingEntity mob = event.getEntity();
		
		if (mob instanceof Wither) {
			
			event.setCancelled(true);
			NoWither.log("Wither spawn averted");
		}
	}
	
	
}
