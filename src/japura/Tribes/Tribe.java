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

public class Tribe {

	private DBObject myTribe;
	private String name = "invalid tribe";
	private String leader = "invalid leader";
	private boolean valid = false;
	BasicDBList invites;
	BasicDBList users;

	Block[] emeraldCache;
	Block[] diamondCache;


	public Tribe(String name) {
		if (name == null) {
			Tribes.log("attempted to get tribe null");
			return;
		}
		this.name = name.toLowerCase();
		BasicDBObject query = new BasicDBObject();
		query.put("name",this.name);

		myTribe = Tribes.getTribeTable().findOne(query);

		
		if (myTribe == null) {
			this.name = "invalid tribe";
			leader = "invalid tribe leader";
			valid = false;
		} else {
			Tribes.invalidateTribeNames();
			valid = true;
			users = (BasicDBList) myTribe.get("members");
			if (users == null) {
				users = new BasicDBList();
			} 
			invites = (BasicDBList) myTribe.get("invites");
			if (invites == null) {
				invites = new BasicDBList();
			}


			leader = (String) myTribe.get("leader");
			if (leader == null) {
				leader = "invalid leader";
			}
		}
	}
	
	public boolean isValid() {
		return valid;
	}

	public long getLastLogTime() {

		return (long) myTribe.get("lastLogTime");
	}

	public void setLastLogTime(long lastLogTime) {
		myTribe.put("lastLogTime",System.currentTimeMillis());
		Tribes.getTribeTable().save(myTribe);
	}
	
	public Block[] getEmeralds() {
		if (emeraldCache != null) return emeraldCache;
		Tribes.log("rebuilding emerald table for " + name);
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",name);
		DBCursor cursor = Tribes.getEmeraldTable().find(query);
		int size = cursor.count();
		Block[] blocks = new Block[size];
		double x;
		double y;
		double z;
		World world;
		Location loc;
		BasicDBObject current;
		for (int i = 0; i < size; i++) {
			if (!cursor.hasNext()) break;
			current = ((BasicDBObject) cursor.next());
			x = current.getLong("X");
			y = current.getLong("Y");
			z = current.getLong("Z");
			world = Bukkit.getWorld(current.getString("world"));
			loc = new Location(world,x,y,z);
			blocks[i] = loc.getBlock();
		}

		emeraldCache = blocks;
		return blocks;
	}

	public Block[] getDiamonds() {

		if (diamondCache != null) return diamondCache;
		Tribes.log("rebuilding diamond table for " + name);
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",name);
		DBCursor cursor = Tribes.getDiamondTable().find(query);
		int size = cursor.count();
		Block[] blocks = new Block[size];
		double x;
		double y;
		double z;
		World world;
		Location loc;
		BasicDBObject current;
		for (int i = 0; i < size; i++) {
			current = ((BasicDBObject) cursor.next());
			x = current.getLong("X");
			y = current.getLong("Y");
			z = current.getLong("Z");
			world = Bukkit.getWorld(current.getString("world"));
			loc = new Location(world,x,y,z);
			blocks[i] = loc.getBlock();
		}

		diamondCache = blocks;
		return blocks;
	}
	public TeleportData getTeleData(Block em) {
		return new TeleportData(em);
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
		emerald.put("world",em.getLocation().getWorld().getName());
		Tribes.getPlugin().getEmeraldTable().insert(emerald);
		emeraldCache = null;
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
		diamondCache = null;
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

	public void delEmerald(Block em) {
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",name);
		query.put("world",em.getWorld().getName());
		query.put("X",em.getX());
		query.put("Y",em.getY());
		query.put("Z",em.getZ());

		Tribes.getEmeraldTable().remove(query);
		emeraldCache = null;
	}
	public void delDiamond(Block em) {
		BasicDBObject query = new BasicDBObject();
		query.put("tribe",name);
		query.put("world",em.getWorld().getName());
		query.put("X",em.getX());
		query.put("Y",em.getY());
		query.put("Z",em.getZ());

		Tribes.getDiamondTable().remove(query);
		diamondCache = null;
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
		Tribes.invalidatePlayer(user);
		if (!"invalid leader".equals(leader)) {
			String oldLeader = leader;
			this.leader = user;
			myTribe.put("leader",user);
			Tribes.getTribeTable().save(myTribe);
			addPlayer(oldLeader);
		} else {
			this.leader = user;
			myTribe.put("leader",user);
			Tribes.getTribeTable().save(myTribe);
			delPlayer(user);
		}
	}
	
	public void setName(String name) {
		Tribes.rmTribeCache(name);
		Tribes.invalidateTribeNames();
		this.name = name;
	}
	
	public String getLeader() {
		return leader;
	}
	
	public void addPlayer(String user) {
		for (String item : getAll()) {
			if (item.equalsIgnoreCase(user)) {
				//we already have this user, so quit
				return;
			}
		}
		
		users.add(user.toLowerCase());
		invites.remove(user.toLowerCase());
		myTribe.put("members",users);
		myTribe.put("invites",invites);

		Tribes.invalidatePlayer(user);
		Tribes.getTribeTable().save(myTribe);
		
	}
	
	public void delPlayer(String user) {
		users.remove(user.toLowerCase());
		users.remove(user);
		myTribe.put("members",users);
		Tribes.invalidatePlayer(user);
		Tribes.getTribeTable().save(myTribe);
		
	}
	
	public void unInvite(String user) {
		invites.remove(user.toLowerCase());
		invites.remove(user);
		myTribe.put("invites",invites);
		Tribes.getTribeTable().save(myTribe);
	}
	
	//returns if successful
	public boolean invite(String user) {
		Tribe check = Tribes.getPlayersTribe(user);
		if (check != null) {
			if(name.equalsIgnoreCase(check.getName()))
				return false;
		}
		if (isInvited(user)) return true;
		//TODO check if player exists?
		invites.add(user.toLowerCase());
		myTribe.put("invites",invites);
		Tribes.getTribeTable().save(myTribe);
		return true;
	}
	
	public boolean isInvited(String user) {
		return invites.contains(user.toLowerCase());
	}
	
	public boolean hasPlayer(String user) {
		if (users == null) return false;

		return users.contains(user.toLowerCase()) || user.equalsIgnoreCase(leader);
	}

	public void destroy() {
                BasicDBObject query = new BasicDBObject();
                query.put("name",name);
                DBObject item = Tribes.getTribeTable().findOne(query);

                //delete all emeralds and diamonds too
                BasicDBObject blockQuery = new BasicDBObject();
                blockQuery.put("tribe",name);

                Tribes.getEmeraldTable().remove(blockQuery);
                Tribes.getDiamondTable().remove(blockQuery);

                Tribes.getTribeTable().remove(item);
		Tribes.rmTribeCache(name);
		Tribes.log("tribe " + name + " destroyed");
		Tribes.invalidateTribeNames();
		this.valid = false;

		this.name = "invalid tribe";
		this.leader = "invalid leader";
	}
	
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (! (other instanceof Tribe)) return false;
		return name.equals(((Tribe) other).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String[] getMembers() {
		String[] list;
		if (myTribe == null) {
			Tribes.log("getMembers ran on null tribe");
			return new String[0];
		}
		BasicDBList players = (BasicDBList) myTribe.get("members");
		if (players == null) {
			list = new String[0];
		} else {
			list = players.toArray(new String[players.size()]);
		}

		return list;
	}

	public String[] getInvites() {
		
		String[] list;
		if (myTribe == null) {
			Tribes.log("getInvites ran on null tribe");
			return new String[0];
		}
		BasicDBList players = (BasicDBList) myTribe.get("invites");
		if (players == null) {
			list = new String[0];
		} else {
			list = players.toArray(new String[players.size()]);
		}

		return list;
	}
	
	public String[] getAll() {
		String[] list;
		if (myTribe == null) {
			Tribes.log("getMembers ran on null tribe");
			return new String[] {leader};
		}
		BasicDBList players = (BasicDBList) myTribe.get("members");
		if (players == null) {
			list = new String[] {leader};
		} else {
			list = players.toArray(new String[players.size()+1]);
			list[players.size()] = leader;
		}

		return list;
	}

	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("Tribe: ");
		info.append(name);
		info.append("\n");

		if ("invalid tribe".equals(name)) {
			return info.toString();
		}
		if (leader != null) {
			info.append("Leader: ");
			info.append(leader);
			info.append("\n");
		}

		String[] list = getMembers();
		if (list.length > 1) {
			info.append("Members: ");
			boolean first = true;
			for (String user : list) {
				if (!first) {
					info.append(",");
				} else {
					first = false;
				}
				info.append(user);
			}
		}
		
		list = getInvites();
		if (list.length > 1) {
			info.append("\nInvites: ");
			boolean first = true;
			for (String user : list) {
				if (!first) {
					info.append(",");
				} else {
					first = false;
				}
				info.append(user);
			}
		}
		
		
		return info.toString();
	}
}
