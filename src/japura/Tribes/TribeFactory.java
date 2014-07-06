/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import org.bukkit.entity.Player;

public class TribeFactory {

	public static void createNewTribe(String name, Player founder) {
		
		TribePlayer user = Tribes.getPlayer(founder);
		if (user == null) {
			user = TribePlayerFactory.createNewPlayer(founder);
		}
		if (user == null) {
			Tribes.log("error creating new user " + founder);
			return;
		}
		
		createNewTribe(name,user);
	}
	
	public static void createNewTribe(String name, TribePlayer founder) {
		Tribe group = new Tribe(name,founder);
		founder.setTribe(group);
		
		Tribes.addTribe(group);
		
		
	}
	
	public static void destroyTribe(Tribe group) {
		Tribes.destroyTribe(group);
		for (TribePlayer user : group.getPlayers()) {
			Tribes.delPlayer(user);
		}
	}

	public static void createNewTribe(String name, String founder) {
		TribePlayer user = Tribes.getPlayer(founder);
		if (user == null) {
			user = TribePlayerFactory.createNewPlayer(founder);
		}
		if (user == null) {
			Tribes.log("error creating new user " + founder);
			return;
		}
		
		createNewTribe(name,user);
		
	}

	public static void createNewTribe(String name) {
		Tribe group = new Tribe(name);
		Tribes.addTribe(group);
		
	}
	
}
