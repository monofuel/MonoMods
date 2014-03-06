/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoPerms;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

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
		
		//TODO:
		//should have 1 method in the main class that is called to do this
		//and the time this is done on plugin startup should call the smae method
		Set<String> keys = MonoPerms.getData().getKeys();
		
		for (String key : keys) {
			if (user.getName().equals(key)) {
				JSONArray permList = (JSONArray) MonoPerms.getData().getConf(key);
				for (int i = 0; i < permList.size(); i++) {
					MonoPerms.addPerm(user, (String) permList.get(i));
				}
			}
		}

		//update player list color
		String[] donors = MonoPerms.getDonors();
		for (String person : donors) {
			if (person.equalsIgnoreCase(user.getName())) {
				if (user.getName().equalsIgnoreCase("monofuel"))
					user.setPlayerListName(ChatColor.YELLOW + user.getName());
				else user.setPlayerListName(ChatColor.BLUE + user.getName());


			}
		}

		
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		Player user = e.getPlayer();
		String[] donors = MonoPerms.getDonors();
		for (String person : donors) {
			if (user.getName().equalsIgnoreCase("monofuel")) {
				e.setFormat("<" + ChatColor.YELLOW +"%s" + ChatColor.WHITE + "> %s");
				return;
			} else {
				e.setFormat("<" + ChatColor.BLUE +"%s" + ChatColor.WHITE + "> %s");
				return;
			}

		}

		//else if they are normal players
		e.setFormat(ChatColor.GRAY +"<%s> %s");
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
}
