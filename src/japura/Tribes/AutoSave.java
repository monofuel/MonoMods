/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */

package japura.Tribes;

import japura.MonoUtil.MonoConf;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;

public class AutoSave extends BukkitRunnable{

	private final Tribes plugin;
	
	public AutoSave(Tribes plugin) {
		this.plugin = plugin;
		Tribes.log("Tribe AutoSave runner spawned");
		
	}
	
	public void run() {
		Player[] users = Bukkit.getOnlinePlayers();
		for (Player user : users)
			user.sendMessage("&6Tribes performing automatic save...");

		plugin.verifyTribes();
		plugin.saveData();
		plugin.verifyTribes();
	
		for (Player user : users)
			user.sendMessage("&6Tribes save complete.");

	}
}
