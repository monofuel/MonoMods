/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import com.mongodb.*;

import org.bukkit.entity.Player;

public class TribeFactory {

	public static void createNewTribe(String name, Player founder) {
		
		createNewTribe(name,founder.getName());
	}

	public static void createNewTribe(String name, String founder) {

		if (name == null) {
			Tribes.log("attempted find a tribe named null");
			return;
		}
		name = name.toLowerCase();

		//check if it already exists
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBObject item = Tribes.getTribeTable().findOne(query);
		if (item == null) {
			//create the new tribe
			Tribes.log("adding new tribe " + name + " to database");
			BasicDBObject newTribe = new BasicDBObject();
			newTribe.put("name",name);
			newTribe.put("leader",founder);
			newTribe.put("getLastLogTime",System.currentTimeMillis());

			Tribes.getTribeTable().insert(newTribe);
		}

	}

	public static void createNewTribe(String name) {
		if (name == null) {
			Tribes.log("attempted find a tribe named null");
			return;
		}
		name = name.toLowerCase();
		//check if it already exists
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBObject item = Tribes.getTribeTable().findOne(query);
		if (item == null) {
			//create the new tribe
			BasicDBObject newTribe = new BasicDBObject();
			newTribe.put("name",name);
			newTribe.put("getLastLogTime",System.currentTimeMillis());

			Tribes.getTribeTable().insert(newTribe);
		}

	}
	
}
