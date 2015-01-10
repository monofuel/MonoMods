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
		
		createNewTribe(name,founder.getName());
	}

	public static void createNewTribe(String name, String founder) {
		Tribe group = new Tribe(name);
		group.setLeader(founder);

	}

	public static void createNewTribe(String name) {
		Tribe group = new Tribe(name);
		Tribes.addTribe(group);
		
	}
	
	public static void destroyTribe(Tribe group) {
		//TODO stub
	}
}
