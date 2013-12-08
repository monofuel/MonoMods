package japura.Tribes;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Tribe {
	String name;
	TribePlayer leader;
	ArrayList<TribePlayer> users = new ArrayList<TribePlayer>();
	ArrayList<Player> invites = new ArrayList<Player>();
	ArrayList<Block> emeralds = new ArrayList<Block>();
	
	//should only be used for safezone or special tribes
	public Tribe(String name) {
		this.name = name;
		leader = new TribePlayer("");
	}
	
	public Tribe(String name,TribePlayer founder) {
		this.name = name;
		this.leader = founder;
		
		users.add(founder);
	}
	
	public Block[] getEmeralds() {
		return emeralds.toArray(new Block[emeralds.size()]);
	}
	
	public void addEmerald(Block em) {
		if (em.getType() != Material.EMERALD_BLOCK) return;
		
		emeralds.add(em);
	}
	public void checkEmerald(Block em) {
		if (!em.getChunk().isLoaded()) return;
		if (emeralds.contains(em)){
			if (em.getType() != Material.EMERALD_BLOCK) emeralds.remove(em);
		}
	}
	
	public void delEmerald(Block em) {
		if (emeralds.contains(em)){
			emeralds.remove(em);
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
		//for loop added becuse contains didn't work right
		for (TribePlayer item : users) {
			if (item.getPlayer().equalsIgnoreCase(user.getPlayer())) {
				user.setTribe(this);
				return;
			}
		}
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
