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
		//check if it already exists
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBObject item = Tribes.getTribeTable().findOne(query);
		if (item == null) {
			//create the new tribe
			BasicDBObject newTribe = new BasicDBObject();
			newTribe.put("name",name);
			newTribe.put("leader",name);

			Tribes.getTribeTable().insert(newTribe);
		}

	}

	public static void createNewTribe(String name) {
		
		if (name == null) {
			Tribes.log("attempted find a tribe named null");
			return;
		}
		//check if it already exists
		BasicDBObject query = new BasicDBObject();
		query.put("name",name);

		DBObject item = Tribes.getTribeTable().findOne(query);
		if (item == null) {
			//create the new tribe
			BasicDBObject newTribe = new BasicDBObject();
			newTribe.put("name",name);

			Tribes.getTribeTable().insert(newTribe);
		}

	}
	
	//TODO this should probably be moved
	//to be a method in the tribe class, rather than here in the factory.
	//this is from back when tribes was a little more complicated.
	public static void destroyTribe(Tribe group) {
		BasicDBObject query = new BasicDBObject();
		query.put("name",group.getName());
		DBObject item = Tribes.getTribeTable().findOne(query);

		//delete all emeralds and diamonds too
		BasicDBObject blockQuery = new BasicDBObject();
		blockQuery.put("tribe",group.getName());

		Tribes.getEmeraldTable().remove(blockQuery);
		Tribes.getDiamondTable().remove(blockQuery);

		Tribes.getTribeTable().remove(item);
	}
}
