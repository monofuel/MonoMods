/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import org.bukkit.entity.Player;

public class TribePlayer {
	private String user;
	private Tribe group;
	
	public TribePlayer(Player user) {
		this.user = user.getName();
		
	}
	
	public TribePlayer(Player user, Tribe group) {
		this.user = user.getName();
		this.group = group;
		
	}
	
	public TribePlayer(String user) {
		this.user = user;
	}

	public void setTribe(Tribe group) {
		this.group = group;
	}
	
	public String getPlayer() {
		
		return user;
	}
	
	public Tribe getTribe() {
		return group;
	}

}
