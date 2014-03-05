package japura.MonoBugs;

import japura.MonoUtil.MonoConf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

public class MonoBugs extends JavaPlugin{
	
	private static Logger bugsLogger = null;
	
	private static MonoConf config = null;
	private static BugData data = null;
	//should not be needed anymore
	//configLoc is still used for BugData
	private static final String configLoc = "plugins/MonoBugs";
	enum status {open,fixed,closed,spam};
	
	HashMap<Integer,bug> bugs = new HashMap<Integer,bug>();
	int bugCount = 0;

	public JSONObject genDefaultConf() {

		JSONObject defaults = new JSONObject();

		//this is where i'd put config options. IF I HAD THEM

		return defaults;
	}

	public void onEnable() {
		bugsLogger = getLogger();
		
		//load configuration
		config = new MonoConf(this,genDefaultConf());
		data.init();
		data = new BugData(configLoc);
		loadData();
		log("MonoBugs has been enabled");
	}
	
	public void onDisable() {
		
		saveData();
		//write config back out to file
		//if there were no errors reading config in
		config.close();
		data.close();
		
		log("MonoBugs has been disabled");
		bugsLogger = null;
	}
	
	public void saveData() {
		for (int i = 0; i < bugCount; i++) {
			bug myBug = bugs.get(i);
			JSONObject item = new JSONObject();
			switch (myBug.getStatus()) {
			case fixed:
				item.put("status", "fixed");
				break;
			
			case closed:
				item.put("status", "closed");
				break;
			case spam:
				item.put("status", "spam");
				break;
			default:
				item.put("status", "open");
				break;
			}
			item.put("error",myBug.getError());
			item.put("player",myBug.getPlayer());
			item.put("reason",myBug.getReason());
			
			//json likes to cast my keys to a string, sooo...
			data.setConf(Integer.toString(i), item);
			
		}
		
		
	}
	
	public void loadData() {
		//TODO
		bugCount = 0;
		Set<String> keys = data.getKeys();
		//log("there are " + keys.size() + " bug reports");
		int unresolved = 0;
		for (String i : keys) {
			JSONObject item = (JSONObject) data.getConf(i);
			status stat;
			//if (item == null) log("item is null");
			//log(item.toString());
			switch ((String) item.get("status")) {
			case "fixed":
				stat = status.fixed;
				break;
			
			case "closed":
				stat = status.closed;
				break;
			case "spam":
				stat = status.spam;
				break;
			default:
				stat = status.open;
				unresolved++;
				break;
			}
			
			String error = (String) item.get("error");
			String player = (String) item.get("player");
			String reason = (String) item.get("reason");
			bug myBug = new bug(error,player,Integer.parseInt(i));
			myBug.updateReason(reason);
			myBug.updateStat(stat);
			bugs.put(Integer.parseInt(i), myBug);
			bugCount++;
		}
		log("there are " + unresolved + " unsolved bug reports");
		log("use /bug unresolved to see unsolved bugs");
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
				//GARBAGE EVERYWHERE
				config = new MonoConf(this,genDefaultConf());
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				config.close();
				config = new MonoConf(this,genDefaultConf());
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			} else if (args[0].equalsIgnoreCase("report")) {
				report(sender,args);
				return true;
			}else if (args[0].equalsIgnoreCase("list")) {
				list(sender,args);
				return true;
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
		bugs.put(bugCount,new bug(error,sender.getName(),bugCount));
		bugCount++;
		sender.sendMessage("success");
		return;
	}
	
	
	public void list(CommandSender sender, String[] args) {
		ArrayList<bug> myBugs = new ArrayList<bug>();
		for (int i = 0; i < bugCount; i++) {
			if(bugs.get(i).getPlayer().equals(sender.getName())) {
				myBugs.add(bugs.get(i));
			}
		}
		
		int pages = (myBugs.size() + 4)/5; //round up value in fancy integer math
		int myPage = 1;
		if (args.length >= 2) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				myPage = 1;
			}
		}
		if (pages == 0) {
			sender.sendMessage("you have no bugs atm");
			return;
		}
		if (myPage < 1 || myPage > pages) {
			sender.sendMessage("please give a valid page number");
			return;
		}
		
		sender.sendMessage("Page " + myPage + "/" + pages);
		String report;
		int bugsOnPage = 5;
		if (myPage == pages) bugsOnPage = myBugs.size() % 5;
		//TODO: examine this in further detail
		if (bugsOnPage == 0) bugsOnPage = 5;
		for (int i = bugsOnPage-1; i >= 0; i--) {
			report = myBugs.get((5*(myPage-1) + i)).toString();
			sender.sendMessage(report);
		}
		
		return;
	}
	
	public void unresolved(CommandSender sender, String[] args) {
		ArrayList<bug> myBugs = new ArrayList<bug>();
		for (int i = 0; i < bugCount; i++) {
			if(bugs.get(i).getStatus() == status.open) {
				myBugs.add(bugs.get(i));
			}
		}
		
		int pages = (myBugs.size() + 4)/5; //round up value in fancy integer math
		int myPage = 1;
		if (args.length >= 2) {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				myPage = 1;
			}
		}
		if (pages == 0) {
			sender.sendMessage("there are no open bugs atm");
			return;
		}
		if (myPage < 1 || myPage > pages) {
			sender.sendMessage("please give a valid page number");
			return;
		}
		
		sender.sendMessage("Page " + myPage + "/" + pages);
		String report;
		int bugsOnPage = 5;
		if (myPage == pages) bugsOnPage = myBugs.size() % 5;
		for (int i = bugsOnPage-1; i >= 0; i--) {
			report = myBugs.get((5*(myPage-1) + i)).toString();
			sender.sendMessage(report);
		}
		
		return;
	}
	
	
	public void fixed(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug fixed index reason");
		}
		update(status.fixed,sender,args);
		sender.sendMessage("success");
		return;
	}
	public void closed(CommandSender sender,String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug closed index reason");
		}
		update(status.closed,sender,args);
		sender.sendMessage("success");
		return;
	}
	public void spam(CommandSender sender,String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Syntax: /bug spam index reason");
		}
		update(status.spam,sender,args);
		sender.sendMessage("success");
		return;
	}
	
	public void update(status stat, CommandSender sender, String[] args) {
		int index = 0;
		try {
			index = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Invalid bug ID");
			return;
		}
		if (index > bugCount) {
			sender.sendMessage("Invalid bug ID");
			return;
		}
		
		bug myBug = bugs.get(index);
		myBug.updateStat(stat);
		if (args.length > 2) {
			StringBuilder result = new StringBuilder();
			for (int i = 3; i < args.length; i++) {
				result.append(" ");
				result.append(args[i]);
			}
			myBug.updateReason(result.toString());
		}
		
	}
	
	//let other objects call our logger
	public static void log(String line) {
		bugsLogger.info(line);
	}
	
	private class bug {
		String message;
		status stat = status.open;
		String reason = "";
		String player;
		int index;
		public bug(String error,String player, int index) {
			message = error;
			this.player = player;
			this.index = index;
		}
		
		public String getError() {
			return message;
		}

		public String getPlayer() {
			return player;
		}
		
		public String getReason() {
			return reason;
		}
		
		
		public status getStatus() {
			return stat;
		}
		public int getIndex() {
			return index;
		}
		
		public void updateStat(status stat){
			this.stat = stat;
		}
		
		public void updateReason(String reason) {
			this.reason = reason;
		}
		
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("[" + index + "]");
			result.append("(" + player + ")");
			result.append("Status - ");
			result.append(stat);
			result.append(": ");
			result.append(message);
			if (reason.equals("")) {
				return result.toString();
			}
			result.append("\n");
			result.append(reason);
			return result.toString();
		}
	}
}
