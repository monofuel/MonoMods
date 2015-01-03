/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoBugs;

//TODO: perhaps import individual things? or not, i'm not a namespace freak
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;

public class MonoBugs extends JavaPlugin{
	
	private static Logger bugsLogger = null;
	private static MongoClient mongo = null;
	private static DB db = null;
	private static DBCollection table = null;
	
	public void onEnable() {
		bugsLogger = getLogger();
		
		//TODO add hostname and port to config
		//no config, just monogoDB
		mongo = new MongoClient("localhost",27017);
		//TODO add this shit to config
		db = mongo.getDB("MonoMods");
		table = db.getCollection("MonoBugs");

		log("MonoBugs has been enabled");
	}
	
	public void onDisable() {
		
		//TODO close DB connection?

		log("MonoBugs has been disabled");
		bugsLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bug")) {
			if (args.length < 1) return false;
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				//TODO: STUB
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				//TODO: STUB
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				//TODO
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			} else if (args[0].equalsIgnoreCase("report")) {
				report(sender,args);
				return true;
			}else if (args[0].equalsIgnoreCase("list")) {
				return list(sender,args);
			}else if (args[0].equalsIgnoreCase("fixed")) {
				fixed(sender,args);
				return true;
			}else if (args[0].equalsIgnoreCase("closed")) {
				closed(sender,args);
				return true;
			}else if (args[0].equalsIgnoreCase("spam")) {
				spam(sender,args);
				return true;
			}else if (args[0].equalsIgnoreCase("unresolved")) {
				unresolved(sender,args);
				return true;
			}

		}
		
		return false;
	}
	
	//TODO Javadocs stuff
	public void report(CommandSender sender, String[] args) {
		if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
			sender.sendMessage("Report a bug on the server");
			sender.sendMessage("Syntax: /bug report desciption of the error");
			return;
		}
		String error = args[1];
		for (int i = 2; i < args.length; i++) {
			error += " " + args[i];
		}
		//bugs.put(bugCount,new bug(error,sender.getName(),bugCount));
		//TODO: should anything be sanitized first?
		BasicDBObject bugReport = new BasicDBObject();
		bugReport.put("bugID",table.count()+1);
		bugReport.put("user",sender.getName());
		bugReport.put("issue",error);
		bugReport.put("status","unresolved");
		bugReport.put("createdDate",new Date());
		table.insert(bugReport);

		sender.sendMessage("Bug reported successfully");
		return;
	}
	
	
	//TODO Javadocs stuff
	public boolean list(CommandSender sender, String[] args) {

		int myPage = 1;
		if (args.length == 2) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				return false;
			}
		} else if (args.length > 2) {
			return false;
		}
		//TODO update this
		//if (pages == 0) {
		//	sender.sendMessage("you have no bugs atm");
		//	return true;
		//}
		//if (myPage < 1 || myPage > pages) {
		//	sender.sendMessage("please give a valid page number");
		//	return true;
		//}
		
		//sender.sendMessage("Page " + myPage + "/" + pages);
		//String report;
		//int bugsOnPage = 5;
		//if (myPage == pages) bugsOnPage = myBugs.size() % 5;
		//if (bugsOnPage == 0) bugsOnPage = 5;
		//for (int i = bugsOnPage-1; i >= 0; i--) {
		//	report = myBugs.get((5*(myPage-1) + i)).toString();
		//	sender.sendMessage(report);
		//}
		
		
		//TODO make this last section easier to read
		String userReports = "Your user reports:\n";

		BasicDBObject query = new BasicDBObject();
		query.put("name",sender.getName());

		DBCursor cursor = table.find(query);

			//TODO should probably use a stringbuilder? depends if you believe in them
		while (cursor.hasNext()) { //TODO does this properly iterate over all elements?
			DBObject element = cursor.next();
			userReports += "ID: " + element.get("bugID") + " | " + element.get("status") + " | " + element.get("issue") + " | date: " + element.get("createdDate");
			if (element.containsField("reason"))
				userReports += " | reason: " + element.get("reason");
			userReports += "\n";
		}

		//TODO is chatpaginator.tostring correct?
		sender.sendMessage(ChatPaginator.paginate(userReports,myPage).toString());
		
		return true;
	}
	
	//TODO Javadocs stuff
	//TODO double check return values for everything in this class
	public boolean unresolved(CommandSender sender, String[] args) {
		int myPage = 1;
		if (args.length == 2) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				return false;
			}
		} else if (args.length > 2) {
			return false;
		}

		String userReports = "unresolved reports:\n";

		BasicDBObject query = new BasicDBObject();
		query.put("status","unresolved");

		DBCursor cursor = table.find(query);

		while (cursor.hasNext()) {
			DBObject element = cursor.next();
			//TODO should probably use a stringbuilder? depends if you believe in them
			userReports += "ID: " + element.get("bugID") + " | user: " + element.get("user") + " | date: " + element.get("createdDate") + "\n";
		}
		//TODO is chatpaginator.tostring correct?
		sender.sendMessage(ChatPaginator.paginate(userReports,myPage).toString());
	
		return true;
	}
	
	
	//TODO Javadocs stuff
	public void fixed(CommandSender sender, String[] args) {
		//TODO improve this shit
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug fixed index reason");
		}
		update("fixed",sender,args);
		//TODO make this message helpful
		sender.sendMessage("success");
		return;
	}
	//TODO Javadocs stuff
	public void closed(CommandSender sender,String[] args) {
		//TODO improve this shit
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug closed index reason");
		}
		update("closed",sender,args);
		//TODO make this helpful
		sender.sendMessage("success");
		return;
	}
	//TODO Javadocs stuff
	public void spam(CommandSender sender,String[] args) {
		//TODO improve this shit
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug spam index reason");
		}
		update("spam",sender,args);
		//TODO make this helpful
		sender.sendMessage("success");
		return;
	}
	
	//TODO Javadocs stuff
	public void update(String stat, CommandSender sender, String[] args) {
		//TODO improve this shit
		int index = 0;
		try {
			index = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Invalid bug ID");
			return;
		}
		if (index > table.count()) {
			sender.sendMessage("Invalid bug ID");
			return;
		}
		
		if (args.length > 2) {
			StringBuilder result = new StringBuilder();
			for (int i = 3; i < args.length; i++) {
				result.append(" ");
				result.append(args[i]);
			}
			result.toString();

			//TODO this is messy and confusing for updating. i don't like it.
			//followed official example for updating
			//myBug.updateReason(result.toString());
			BasicDBObject query = new BasicDBObject();
			query.put("bugID",index);

			BasicDBObject newBug = new BasicDBObject();
			newBug.put("status",stat);
			//TODO is ToString required?
			newBug.put("reason",result.toString());

			BasicDBObject updateBug = new BasicDBObject();
			updateBug.put("$set",newBug);

			table.update(query,updateBug);

		}
		
	}
	
	//TODO improve this shit
	//let other objects call our logger
	public static void log(String line) {
		bugsLogger.info(line);
	}
}
