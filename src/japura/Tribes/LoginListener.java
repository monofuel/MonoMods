/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginListener implements Listener {
	
	public LoginListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler
	public void PlayerLogin(PlayerJoinEvent user) {
		String motd = (String) Tribes.getConf().getConf("MOTD");
		user.getPlayer().sendMessage(motd);	

	}

	public void TimestampLogin(PlayerLoginEvent event) {
                TribePlayer user = Tribes.getPlayer(event.getPlayer().getName());
                if (user == null) return;
                Tribe group = user.getTribe();
                if (group == null) return;
                group.setLastLogTime(System.currentTimeMillis());
        }

}


