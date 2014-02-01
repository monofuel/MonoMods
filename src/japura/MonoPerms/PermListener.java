package japura.MonoPerms;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;


public class PermListener implements Listener {

	JavaPlugin plugin;
	
	public PermListener(JavaPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler
	public void login(PlayerLoginEvent event) {
		Player user = event.getPlayer();
		
		Set<String> keys = MonoPerms.getData().getKeys();
		
		for (String key : keys) {
			if (user.getName().equals(key)) {
				JSONArray permList = (JSONArray) MonoPerms.getData().getConf(key);
				for (int i = 0; i < permList.size(); i++) {
					MonoPerms.addPerm(user, (String) permList.get(i));
				}
			}
		}
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
}
