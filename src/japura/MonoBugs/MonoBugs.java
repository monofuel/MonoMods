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
import java.util.logging.Level;
import java.util.Date;
import java.net.UnknownHostException;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

public class MonoBugs extends JavaPlugin{
	
	private static MongoClient mongo = null;
	private static DB db = null;
	private static DBCollection table = null;

	private static final String adminHelp = "/bug reload to reload the plugin\n" +
						"/bug unload to disable the plugin\n" +
						"/bug load to reload the config\n" +
						"/bug save to save the config" +
						"/bug fixed [id] [reason] to fix a bug\n" +
						"/bug closed [id] [reason] to close a report\n" +
						"/bug spam [id] to mark a report as spam\n";
	private static final String userHelp = "/bug report [issue] to report a bug\n" +
						"/bug list to list your bugs\n" +
						"/bug unresolved to list all unfixed bugs\n" +
						"/bug help to show this help";

	//number of arguments to skip at the start of each command
	private static final int CMD_ARGS = 2;
	
	public void onEnable() {
		saveDefaultConfig();		

		String mongoHost = getConfig().getString("mongo host");
		int port = getConfig().getInt("mongo port");
		String databaseName = getConfig().getString("mongo database");
		String tableName = getConfig().getString("mongo table");

		try {
			mongo = new MongoClient(mongoHost,port);
		} catch (UnknownHostException e) {
			getLogger().log(Level.SEVERE,"Error connecing to database, bailling out",e);
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		db = mongo.getDB(databaseName);
		table = db.getCollection(tableName);

		getLogger().info("MonoBugs has been enabled");
	}
	
	public void onDisable() {
		
		if (mongo != null) {
			mongo.close();
		}

		getLogger().info("MonoBugs has been disabled");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//verify that this is the correct command, and check if
		//it is being sent via the console or via player.
		//console always gets full access, but for the player we
		//will check the permission.
		//.hasPermission will only be tested if the sender is indeed an instance of Player,
		//so this will not give an exception.
		if ("bug".equalsIgnoreCase(cmd.getName()) &&
			(sender instanceof ConsoleCommandSender ||
			(sender instanceof Player && ((Player) sender).hasPermission("monobugs.admin")))) {
			//safety first
			if (args.length < 1) {
				return false;
			}
			//valid cases will return true so that the plugin help will not be displayed.
			//if none of these casese are met, then the 'return false' at the end
			//of this method would run.
			switch(args[0].toLowerCase()) {
				case "reload":
					this.getServer().getPluginManager().disablePlugin(this);
					this.getServer().getPluginManager().enablePlugin(this);
					return true;
				case "unload":
					this.getServer().getPluginManager().disablePlugin(this);
					return true;
				case "load":
					//TODO: STUB
					return true;
				case "save":
					//TODO: STUB
					return true;
				case "help":
					sender.sendMessage(userHelp);	
					sender.sendMessage(adminHelp);	
					return true;
			}
		//if they are a normal player without admin privileges
		//or if an admin issued a command that did not meet the above
		}
		
		if ("bug".equalsIgnoreCase(cmd.getName())) {
			if (args.length < 1) return false;
			//since we are calling sub-methods for commands,
			//these commands are responsible for displaying errors,
			//or for deciding if plugin help should be shown.
			//most likely they will give their own specific argument
			//error.
			switch (args[0].toLowerCase()) {
				case "report":
					report(sender,args);
					return true;
				case "list":
					return list(sender,args);
				case "fixed":
					fixed(sender,args);
					return true;
				case "closed":
					closed(sender,args);
					return true;
				case "spam":
					spam(sender,args);
					return true;
				case "unresolved":
					unresolved(sender,args);
					return true;
				case "help":
					sender.sendMessage(userHelp);
					return true;
			}
		}

		
		return false;
	}
	
	//TODO Javadocs stuff
	public void report(CommandSender sender, String[] args) {
		if (args.length < CMD_ARGS || args[1].equalsIgnoreCase("help")) {
			sender.sendMessage("Report a bug on the server");
			sender.sendMessage("Syntax: /bug report desciption of the error");
			return;
		}
		String error = args[1];
		for (int i = CMD_ARGS; i < args.length; i++) {
			error += " " + args[i];
		}
		//TODO: should anything be sanitized first?
		BasicDBObject bugReport = new BasicDBObject();
		bugReport.put("bugID",table.count()+1);
		bugReport.put("user",sender.getName());
		bugReport.put("issue",error);
		bugReport.put("status","unresolved");
		bugReport.put("createdDate",new Date());
		
		AsyncTask(() -> {
			table.insert(bugReport);
			SyncTask(() ->
				sender.sendMessage("Bug reported successfully")
			);
		});
		return;
	}
	
	
	//TODO Javadocs stuff
	public boolean list(CommandSender sender, String[] args) {

		int myPage = 1;
		if (args.length == CMD_ARGS) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				return false;
			}
		} else if (args.length > CMD_ARGS) {
			return false;
		}

		final int MY_PAGE = myPage; //appease the java 8 lambda gods
		
		final String USERNAME = sender.getName();
		
		AsyncTask(() -> {
			//query all of our user's bug reports
			BasicDBObject query = new BasicDBObject();
			query.put("user",USERNAME);
			DBCursor cursor = table.find(query);

			//if there are none to show..
			if (cursor.count() == 0) {
				SyncTask(
					() -> sender.sendMessage("there are no reports to show")
				);
				return;
			}
			//otherwise, list them all together separated by newlines.
			String userReports = "";

			while (cursor.hasNext()) {
				DBObject element = cursor.next();
				userReports += "ID: " + element.get("bugID") + " | " + element.get("status") + " | " + element.get("issue") + " | date: " + element.get("createdDate");
				if (element.containsField("reason")) {
					userReports += " | reason: " + element.get("reason");
				}
				userReports += "\n";
			}

			final String USER_REPORTS = userReports;
			SyncTask(() -> {
				//divy the report into pages and get the desired page
				ChatPage page = ChatPaginator.paginate(USER_REPORTS,MY_PAGE);

				//send each line of our page to the user
				sender.sendMessage("Page " + page.getPageNumber() + " of " + page.getTotalPages() + " for reports:");	
				for (String line : page.getLines()) {
					sender.sendMessage(line);
				}
			});
		});
		
		return true;
	}
	
	//TODO Javadocs stuff
	//TODO double check return values for everything in this class
	public boolean unresolved(CommandSender sender, String[] args) {
		int myPage = 1;
		if (args.length == CMD_ARGS) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				return false;
			}
		} else if (args.length > CMD_ARGS) {
			return false;
		}
		final int MY_PAGE = myPage;
		
		AsyncTask(() -> {
			String userReports = "unresolved reports:\n";

			BasicDBObject query = new BasicDBObject();
			query.put("status","unresolved");

			DBCursor cursor = table.find(query);

			while (cursor.hasNext()) {
				DBObject element = cursor.next();
				//TODO should probably use a stringbuilder? depends if you believe in them
				userReports += "ID: " + element.get("bugID") + " | user: " + element.get("user") + " | issue: " +  element.get("issue") + " | date: " + element.get("createdDate") + "\n";
			}
		
			final String USER_REPORTS = userReports;
			SyncTask(() -> {
				ChatPage page = ChatPaginator.paginate(USER_REPORTS,MY_PAGE);

				for (String line : page.getLines()) {
					sender.sendMessage(line);
				}
			});
		});
		

		return true;
	}
	
	
	//TODO Javadocs stuff
	public void fixed(CommandSender sender, String[] args) {
		//TODO improve this shit
		if (args.length < CMD_ARGS) {
			sender.sendMessage("Syntax: /bug fixed index reason");
		}
		update("fixed",sender,args);
		return;
	}
	//TODO Javadocs stuff
	public void closed(CommandSender sender,String[] args) {
		//TODO improve this shit
		if (args.length < CMD_ARGS) {
			sender.sendMessage("Syntax: /bug closed index reason");
		}
		update("closed",sender,args);
		return;
	}
	//TODO Javadocs stuff
	public void spam(CommandSender sender,String[] args) {
		//TODO improve this shit
		if (args.length < CMD_ARGS) {
			sender.sendMessage("Syntax: /bug spam index reason");
		}
		update("spam",sender,args);
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
		
		if (args.length >= CMD_ARGS) {
			StringBuilder result = new StringBuilder();
			for (int i = CMD_ARGS; i < args.length; i++) {
				result.append(" ");
				result.append(args[i]);
			}
			result.toString();

			//TODO this is messy and confusing for updating. i don't like it.
			//followed official example for updating
			//myBug.updateReason(result.toString());
			BasicDBObject query = new BasicDBObject();
			query.put("bugID",index);

			DBObject myBug = table.findOne(query);
			myBug.put("status",stat);
			//TODO is ToString required?
			if (result.length() > 0) {
				myBug.put("reason",result.toString());
			}
			AsyncTask(() -> {
				table.save(myBug);
				
				SyncTask(() ->
					sender.sendMessage("success")
				);
			});


		}
		
	}
	
	/**
	 *  Asynchronous runnable task helper
	 */
	private void AsyncTask(Runnable run) {
		getServer().getScheduler().runTaskAsynchronously(this, run);
	}
	
	/**
	 *  Runnable task helper
	 */
	private void SyncTask(Runnable run) {
		getServer().getScheduler().runTask(this, run);
	}
	
}
