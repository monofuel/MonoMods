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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Tribe {

	private DBObject myTribe;
	private String name;

	//should only be used for safezone or special tribes
	public Tribe(String name) {
		this.name = name;
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBCursor cursor = Tribes.getTribeTable().find(query);

		myTribe = cursor.next();
		if (cursor.hasNext()) {
			Tribes.log("FATAL: multiple tribes with the same name " + name);
		}
	}
	
	public long getLastLogTime() {

		if(!myTribe.containsField("lastLogTime")) {
			//only for importing old configs
			//TODO update field
			//System.currentTimeMillis();
			return 0;
		} else {
			//i'm sure return statements in an if violates some coding standard
			return (long) myTribe.get("lastLogTime");
		}
	}

	public void setLastLogTime(long lastLogTime) {
		//TODO stub
	}
	
	public Block[] getEmeralds() {
		//TODO fetch current list from db
		//return emeralds.toArray(new Block[emeralds.size()]);
	}

	public Block[] getDiamonds() {
		//TODO stub
		//return diamonds.toArray(new Block[diamonds.size()]);
	}
	public TeleportData getTeleData(Block em) {
		//TODO stub
		//return teleports.get(em);
		
	}

	public void addEmerald(Block em) {
		//TODO Log this
		if (em.getType() != Material.EMERALD_BLOCK) return;
		
		//emeralds.add(em);

		BasicDBObject emerald = new BasicDBObject();
		emerald.put("tribe",name);
		emerald.put("X",em.getLocation().getBlockX());
		emerald.put("Y",em.getLocation().getBlockY());
		emerald.put("Z",em.getLocation().getBlockZ());
		Tribes.getTribeTable().insert(emerald);
	}
	public void addDiamond(Block em,Player user) {
		if (em.getType() != Material.DIAMOND_BLOCK) return;
		diamonds.add(em);
		String name = "diamond_" + teleports.size();
		TeleportData data = new TeleportData(em,name,this);
		data.addAllowed(this); //TODO why is this needed?
		user.sendMessage("created new teleporter named " + name);
	}

	public void addTeleport(TeleportData teleData) {
		teleData.addAllowed(this);
		diamonds.add(teleData.getSpot());
		teleports.put(teleData.getSpot(),teleData);
	}

	public void checkEmerald(Block em) {
		if (!em.getChunk().isLoaded()) return;
		if (emeralds.contains(em)){
			if (em.getType() != Material.EMERALD_BLOCK) emeralds.remove(em);
		}
	}

	public void checkDiamond(Block em) {
		if (!em.getChunk().isLoaded()) return;
		if (diamonds.contains(em)) {
			if (em.getType() != Material.DIAMOND_BLOCK) diamonds.remove(em);
		}

	}
	
	public void delEmerald(Block em) {
		if (emeralds.contains(em)){
			emeralds.remove(em);
		}
	}
	public void delDiamond(Block em) {
		if (diamonds.contains(em)){
			diamonds.remove(em);
		}
	}
	
	public boolean checkLocOwnership(Location loc) {
		Block[] emeralds = getEmeralds();
		Location tmp;
		//claim size from one edge to the center
		long claimSize = (long) Tribes.getConf().getConf("ClaimSize");
		boolean YClaim = (boolean) Tribes.getConf().getConf("YAxisClaim");
		for (Block em : emeralds) {
			if (!loc.getWorld().equals(em.getWorld())) continue;
			tmp = em.getLocation().subtract(loc);
			
			if (Math.abs(tmp.getBlockX()) < claimSize &&
			    Math.abs(tmp.getBlockZ()) < claimSize) {
				if (YClaim) {
					if (Math.abs(tmp.getBlockY()) < claimSize) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setLeader(TribePlayer user) {
		leader = user;
		user.setTribe(this);
		addPlayer(user);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public TribePlayer getLeader() {
		return leader;
	}
	
	public void addPlayer(TribePlayer user) {
		//TODO:
		//for loop added because contains didn't work right
		for (TribePlayer item : users) {
			if (item.getPlayer().equalsIgnoreCase(user.getPlayer())) {
				user.setTribe(this);
				return;
			}
		}
		
		//if (users.contains(user)) {
		//	user.setTribe(this);
		//	return;
		//}
		users.add(user);
		user.setTribe(this);
		
		invites.remove(user.getPlayer());
	}
	
	public void delPlayer(TribePlayer user) {
		users.remove(user);
		user.setTribe(null);
		
	}
	
	public void unInvite(TribePlayer user) {
		invites.remove(user.getPlayer());
	}
	
	public void invite(Player user) {
		TribePlayer check = Tribes.getPlayer(user);
		if (check != null) {
			if (check.getTribe() == this)
				return;
		}
		
		invites.add(user);
	}
	
	public boolean isInvited(Player user) {
		return invites.contains(user);
	}
	
	public boolean hasPlayer(TribePlayer user) {
		return users.contains(user);
	}
	
	public String getName() {
		return name;
	}

	public TribePlayer[] getPlayers() {
		TribePlayer[] list = users.toArray(new TribePlayer[users.size()]);
		return list;
	}
	
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("Tribe: ");
		info.append(name);
		info.append("\n");
		if (leader != null) {
			info.append("Leader: ");
			info.append(leader.getPlayer());
			info.append("\n");
		}
		if (users.size() > 1) {
			info.append("Members: ");
			for (TribePlayer user : getPlayers()) {
				if (user.equals(leader)) continue;
				info.append(user.getPlayer());
				info.append(",");
			}
		}
		
		
		return info.toString();
	}
}
