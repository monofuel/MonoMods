/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

//TODO this class, and emeralds and tribes should probably extend DBObject
public class TeleportData {

	String name;
	Tribe owner;
	Block spot;
	private BasicDBObject myTeleport;
	private BasicDBList allowed;

	//for making a new teleport (or getting an existing once if you know the name)
	public TeleportData(Block spot, String name,Tribe owner) {
		this.name = name;
		this.owner = owner;
		this.spot = spot;

		BasicDBObject query = new BasicDBObject();
		query.put("tribe",owner);
		query.put("name",name);
		DBCursor cursor = Tribes.getDiamondTable().find(query);

		myTeleport = (BasicDBObject) cursor.next();
		if (myTeleport != null) {
			allowed = (BasicDBList) myTeleport.get("allowed");
			long x = myTeleport.getLong("X");
			long y = myTeleport.getLong("Y");
			long z = myTeleport.getLong("Z");
			World myWorld = Bukkit.getWorld((String)myTeleport.get("world"));
			Location loc1 = new Location(myWorld,x,y,z);
			if (!spot.equals(loc1.getBlock())) {
				Tribes.log("invalid diamond in DB");
			}
			return;
		}
		//else, create this teleport
		myTeleport = new BasicDBObject();
		myTeleport.put("tribe",owner);
		myTeleport.put("name",name);
		myTeleport.put("X",spot.getLocation().getBlockX());
		myTeleport.put("Y",spot.getLocation().getBlockY());
		myTeleport.put("Z",spot.getLocation().getBlockZ());
		myTeleport.put("world",spot.getLocation().getWorld().getName());
		allowed = new BasicDBList();
		allowed.add(owner.getName());
		myTeleport.put("allowed",allowed);

		Tribes.getDiamondTable().insert(myTeleport);
	}

	//for making a new teleport (or getting an existing once if you know the name)
	public TeleportData(Block spot) {
		//this.name = name;
		//this.owner = owner;
		this.spot = spot;

		long x = spot.getX();
		long y = spot.getY();
		long z = spot.getX();

		BasicDBObject query = new BasicDBObject();
		query.put("X",x);
		query.put("Y",y);
		query.put("Z",z);
		myTeleport = (BasicDBObject) Tribes.getDiamondTable().findOne(query);

		if (myTeleport != null) {
			allowed = (BasicDBList) myTeleport.get("allowed");
			this.owner = Tribes.getTribe(myTeleport.getString("owner"));
			this.name = myTeleport.getString("name");
			World myWorld = Bukkit.getWorld(myTeleport.getString("world"));
		} else {
			Tribes.log("invalid teleport");
		}

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

	public boolean equals(Tribe other) {
		return name.equals(other.getName());
	}

	public void rename(String name) {
		this.name = name;
		myTeleport.put("name",name);
		Tribes.getDiamondTable().save(myTeleport);
	}

	public void addAllowed(Tribe group) {
		//TODO should this also be in the constructor?
		if (!allowed.contains(group))
			allowed.add(group);
		myTeleport.put("allowed",allowed); //TODO: is this required?
		Tribes.getDiamondTable().save(myTeleport);
	}
	public void rmAllowed(Tribe group) {
		allowed.remove(group);
		myTeleport.put("allowed",allowed);
		Tribes.getDiamondTable().save(myTeleport);
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

