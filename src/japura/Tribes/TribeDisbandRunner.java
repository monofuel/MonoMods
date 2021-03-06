/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */

package japura.Tribes;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import com.mongodb.*;

public class TribeDisbandRunner extends BukkitRunnable{

	private final JavaPlugin plugin;
	long timeDeltaInMillis = 0;

	public TribeDisbandRunner(JavaPlugin plugin) {
		this.plugin = plugin;
		//1000 milliseconds in a second
		//60 seconds in a minute
		//60 minutes in an hour
		//24 hours in a day
		timeDeltaInMillis = 1000 * 60 * 60 * 24 * (long) plugin.getConfig().getInt("Days before disband");
		Tribes.log("Tribe Disband spawned");
		Tribes.log("Disband set for " + plugin.getConfig().getInt("Days before disband") + " days, or " +
				timeDeltaInMillis + " seconds");

	}

	public void run() {
		DBCursor currentTribes = Tribes.getTribes();
		long currentTime = System.currentTimeMillis();
		for (DBObject item : currentTribes) {
			if ("safezone".equalsIgnoreCase((String) item.get("name"))) {
				continue;
			}
			if ((long) item.get("getLastLogTime") <= 0) {
				Tribes.log("Invalid last log time for " + item.get("name"));
				Tribes.log("time was: " + item.get("getLastLogTime"));
				continue;
			}
			if (currentTime - (long) item.get("getLastLogTime") > timeDeltaInMillis) {
				Tribes.log("Tribe " + (String) item.get("name") + " has exceeded the time since last login limit");
				Tribes.log("current time: " + currentTime + " last log time: " + (long) item.get("getLastLogTime"));
				Tribes.getTribe((String) item.get("name")).destroy();
			}

		}
	}

}
