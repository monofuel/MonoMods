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
	private String leader;
	ArrayList<String> invites = new ArrayList<String>();



	//should only be used for safezone or special tribes
	public Tribe(String name) {
		this.name = name.toLowerCase();
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBCursor cursor = Tribes.getTribeTable().find(query);

		myTribe = cursor.next();

		leader = (String) myTribe.get("leader");
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
		return null;
	}

	public Block[] getDiamonds() {
		//TODO stub
		//return diamonds.toArray(new Block[diamonds.size()]);
		return null;
	}
	public TeleportData getTeleData(Block em) {
		//TODO stub
		//return teleports.get(em);
		return null;
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
		Tribes.getPlugin().getTribeTable().insert(emerald);
	}
	public void addDiamond(Block em,Player user) {
		if (em.getType() != Material.DIAMOND_BLOCK) return;
		//diamonds.add(em);
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",name);
		DBCursor cursor = Tribes.getPlugin().getDiamondTable().find(query);

		String name = "diamond_" + cursor.count();
		TeleportData data = new TeleportData(em,name,this);
		data.addAllowed(this); //TODO why is this needed?
		user.sendMessage("created new teleporter named " + name);
	}

	//TODO what is this used for
	/*
	public void addTeleport(TeleportData teleData) {
		teleData.addAllowed(this);
		teleports.put(teleData.getSpot(),teleData);
	}*/


	//TODO actually why do we have this. this is silly. come up with a better way to verify emeralds.
	/*
	public void checkEmerald(Block em) {
		if (!em.getChunk().isLoaded()) return;
		if (emeralds.contains(em)){
			if (em.getType() != Material.EMERALD_BLOCK) emeralds.remove(em);
		}
	}*/

	//TODO actually why do we have this. this is silly. come up with a better way to verify diamonds.
	/*
	public void checkDiamond(Block em) {
		if (!em.getChunk().isLoaded()) return;
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",owner);
		DBCursor blocks = Tribes.getEmeraldTable().find(query);
		for (DBObject block : blocks) {
			if (em.getType() != Material.DIAMOND_BLOCK) diamonds.remove(em);
		}

	}*/
	
	public void delEmerald(Block em) {
		//TODO stub
		/*if (emeralds.contains(em)){
			emeralds.remove(em);
		}*/
	}
	public void delDiamond(Block em) {
		//TODO stub
		/*if (diamonds.contains(em)){
			diamonds.remove(em);
		}*/
	}
	
	public boolean checkLocOwnership(Location loc) {
		Block[] emeralds = getEmeralds();
		Location tmp;
		//claim size from one edge to the center
		long claimSize = Tribes.getPlugin().getConfig().getLong("ClaimSize");
		boolean YClaim = (boolean) Tribes.getPlugin().getConfig().getBoolean("YAxisClaim");
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
	
	public void setLeader(String user) {
		//TODO stub
		/*leader = user;
		user.setTribe(this);
		addPlayer(user);*/
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLeader() {
		return leader;
	}
	
	public void addPlayer(String user) {
		//TODO: STUB
		//for loop added because contains didn't work right
		/*for (TribePlayer item : users) {
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
		*/
	}
	
	public void delPlayer(String user) {
		//TODO stub
		/*
		users.remove(user);
		user.setTribe(null);
		*/
		
	}
	
	public void unInvite(String user) {
		invites.remove(user);
	}
	
	//returns if successful
	public boolean invite(String user) {
		Tribe check = Tribes.getPlayersTribe(user);
		if (check != null) {
			if(name.equals(check.getName()))
				return false;
		}
		//TODO check if player exists?
		invites.add(user);
		return true;
	}
	
	public boolean isInvited(String user) {
		//TODO: maybe this should be equals ignore case?
		return invites.contains(user);
	}
	
	public boolean hasPlayer(String user) {
		//TODO stub
		/*
		return users.contains(user);
		*/
		return false;
	}
	
	public String getName() {
		return name;
	}

	public String[] getPlayers() {
		//TODO stub
		//TribePlayer[] list = users.toArray(new TribePlayer[users.size()]);
		String[] list = null;
		return list;
	}
	
	public String toString() {
		//TODO stub
		StringBuilder info = new StringBuilder();
		info.append("Tribe: ");
		info.append(name);
		info.append("\n");
		if (leader != null) {
			info.append("Leader: ");
			info.append(leader);
			info.append("\n");
		}
		/*
		if (users.size() > 1) {
			info.append("Members: ");
			for (TribePlayer user : getPlayers()) {
				if (user.equals(leader)) continue;
				info.append(user.getPlayer());
				info.append(",");
			}
		}*/
		
		
		return info.toString();
	}
}
