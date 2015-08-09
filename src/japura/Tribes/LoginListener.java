/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import japura.MonoPerms.MonoPerms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginListener implements Listener {
	
	Tribes plugin;

	public LoginListener(Tribes plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler
	public void playerLogin(PlayerJoinEvent user) {
		String motd = plugin.getConfig().getString("MOTD");
		user.getPlayer().sendMessage(motd);	

	}
	
	@EventHandler
	public void login(PlayerLoginEvent event) {
		Player user = event.getPlayer();
		
		if (user.hasPermission("tribes.admin")) {
			user.setPlayerListName(ChatColor.YELLOW + user.getName());
		} else if (user.hasPermission("tribes.donor")) {
			user.setPlayerListName(ChatColor.BLUE + user.getName());
		}

		
	}
	
	//TODO
	//refactor this somewhere more appropriately
	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		Player user = e.getPlayer();
		
		if (user.hasPermission("tribes.admin")) {
			e.setFormat("[" + user.getWorld().getName()+ "]"
					+ "[" + Tribes.getPlayersTribe(user).getName()+ "]" 
					+ "<" + ChatColor.YELLOW +"%s" + ChatColor.WHITE + "> %s");
		} else if (user.hasPermission("tribes.donor")) {
			e.setFormat("[" + user.getWorld().getName()+ "]"
					+ "[" + Tribes.getPlayersTribe(user).getName()+ "]" 
					+ "<" + ChatColor.BLUE +"%s" + ChatColor.WHITE + "> %s");
		} else {
			//else if they are normal players
			e.setFormat("[" + user.getWorld().getName()+ "]"
					+ "[" + Tribes.getPlayersTribe(user).getName()+ "]"
					+ ChatColor.GRAY +"<%s> %s");
		}
	}

	public void timestampLogin(PlayerLoginEvent event) {
                String user = event.getPlayer().getName();
                Tribe group = Tribes.getPlayersTribe(user);
                if (group == null) return;
                group.setLastLogTime(System.currentTimeMillis());
        }

}


