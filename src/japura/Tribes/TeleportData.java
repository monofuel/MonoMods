/**
 *  *      author: Monofuel
 *   *      website: japura.net
 *    *      this file is distributed under the modified BSD license
 *     *      that should have been included with it.
 *      */


package japura.Tribes;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeleportData {

	String name;
	Tribe owner;
	private DBObject myTeleport;

	public TeleportData(Block spot, String name,Tribe owner) {
		this.name = name;
		this.owner = owner;

		BasicDBObject query = new BasicDBObject();
		query.put("tribe",owner);
		query.put("name",name);
		DBCursor cursor = Tribes.getDiamondTable().find(query);

		myTeleport = cursor.next();
		if (myTeleport != null) {
			Tribes.log("Teleport already exists for " + owner.getName() +
				   " with the name " + name);
			return;
		}

		myTeleport = new BasicDBObject();
		myTeleport.put("tribe",owner);
		myTeleport.put("name",name);
		myTeleport.put("X",em.getLocation().getBlockX());
		myTeleport.put("Y",em.getLocation().getBlockY());
		myTeleport.put("Z",em.getLocation().getBlockZ());
		BasicDBList allowed = new BasicDBList();
		allowed.add(owner.getName());
		myTeleport.put("allowed",allowed);

		Tribes.getDiamondTable.insert(myTeleport());
	}

	public Tribe getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public Block getSpot() {
		return spot;
	}

	public void rename(String name) {
		this.name = name;
	}

	public void addAllowed(Tribe group) {
		if (!allowed.contains(group))
			allowed.add(group);
	}
	public void rmAllowed(Tribe group) {
		allowed.remove(group);
	}
	public boolean isAllowed(Tribe group) {
		return allowed.contains(group);
	}

	public Tribe[] getAllowed() {
		return allowed.toArray(new Tribe[allowed.size()]);
	}

	public void teleportPlayer(Player user) {
		//check if the block is obscured
		Location myloc = spot.getLocation();
		if (spot.getType() != Material.DIAMOND_BLOCK && spot.getChunk().isLoaded()) {
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

