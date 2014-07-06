/**
 *  *      author: Monofuel
 *   *      website: japura.net
 *    *      this file is distributed under the modified BSD license
 *     *      that should have been included with it.
 *      */


package japura.Tribes;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeleportData {

	Block spot;
	String name;
	Tribe owner;
	ArrayList<Tribe> allowed;

	public TeleportData(Block spot, String name,Tribe owner) {
		this.spot = spot;
		this.name = name;
		this.owner = owner;
		allowed = new ArrayList<Tribe>();
	}

	public Tribe getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public void rename(String name) {
		this.name = name;
	}

	public void addAllowed(Tribe group) {
		allowed.add(group);
	}
	public void rmAllowed(Tribe group) {
		allowed.remove(group);
	}
	public boolean isAllowed(Tribe group) {
		return allowed.contains(group);
	}

	public void TeleportPlayer(Player user) {
		//check if the block is obscured
		Location myloc = spot.getLocation();
		if (spot.getType() != Material.DIAMOND_BLOCK) {
			user.sendMessage("teleporter was destroyed");
			return;
		}
		for (int i = 0; i < 2; i++) {
			myloc = myloc.add(0,1,0);
			if (myloc.getBlock().getType() != Material.AIR) {
				user.sendMessage("teleporter is covered");
				return;
			}
		}
		//else we know it is not obscured
		myloc = spot.getLocation().add(0,1,0);
		user.teleport(myloc);
		user.sendMessage("You teleported to " + name);


	}
}

