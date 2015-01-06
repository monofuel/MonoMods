/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoPerms;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;


public class PermListener implements Listener {

	JavaPlugin plugin;
	
	public PermListener(JavaPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler
	public void login(PlayerLoginEvent event) {
		Player user = event.getPlayer();
		
		//TODO: should be refactored with setAdmins()
		List<String> keys = plugin.getConfig().getStringList("groups.admin");
		
		for (String key : keys) {
			if (user.getName().equals(key)) {
				//TODO add proper defining groups
				MonoPerms.addPerm(user,"tribes.admin");
				MonoPerms.addPerm(user,"nowither");
				MonoPerms.addPerm(user,"monoperms");
			}
		}

		//update player list color
		List<String> donors = plugin.getConfig().getStringList("groups.donors");
		for (String person : donors) {
			if (person.equalsIgnoreCase(user.getName())) {
				MonoPerms.addPerm(user,"donor");
				//BECAUSE MONOFUEL ROCKS
				//should probably be re-done for all admin users. (if there's ever more)
				if (user.getName().equalsIgnoreCase("monofuel"))
					user.setPlayerListName(ChatColor.YELLOW + user.getName());
				else user.setPlayerListName(ChatColor.BLUE + user.getName());


			}
		}

		
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		Player user = e.getPlayer();
		List<String> donors = plugin.getConfig().getStringList("groups.donors");
		for (String person : donors) {
			if (user.getName().equalsIgnoreCase("monofuel")) {
				e.setFormat("<" + ChatColor.YELLOW +"%s" + ChatColor.WHITE + "> %s");
				return;
			} else if (user.getName().equalsIgnoreCase(person)){
				e.setFormat("<" + ChatColor.BLUE +"%s" + ChatColor.WHITE + "> %s");
				return;
			}

		}

		//else if they are normal players
		e.setFormat(ChatColor.GRAY +"<%s> %s");
	}

	public void close() {

	}
}
