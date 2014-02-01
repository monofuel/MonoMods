package japura.Tribes;

import org.bukkit.entity.Player;

public class TribePlayerFactory {
	
	public static TribePlayer createNewPlayer(Player user) {
		//check if player exists
		TribePlayer tPlayer = Tribes.getPlayer(user);
		if (tPlayer != null) {
			//user exists already!
			return tPlayer;
		}
		//otherwise create a new TribePlayer for them
		tPlayer = new TribePlayer(user);
		Tribes.addPlayer(tPlayer);
				
		return tPlayer;
	}
	
	
	public static TribePlayer createNewPlayer(Player user,Tribe group) {
		TribePlayer tPlayer = createNewPlayer(user);
		tPlayer.setTribe(group);
		return tPlayer;
	}


	public static TribePlayer createNewPlayer(String user) {
		TribePlayer tPlayer = Tribes.getPlayer(user);
		if (tPlayer != null) {
			//user exists already!
			return tPlayer;
		}
		//otherwise create a new TribePlayer for them
		tPlayer = new TribePlayer(user);
		Tribes.addPlayer(tPlayer);
				
		return tPlayer;
		
	}
	

}
