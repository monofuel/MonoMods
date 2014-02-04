package japura.MonoUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MonoConf {
	
	//NO STATIC VARIABLES
	//this is a re-usable config class for all
	//japura plugins
	private JSONObject defaults = null; //default plugin options
	
	private JavaPlugin master = null; //the caller
	private Logger logger = null; //the logger associated with the plugin

	private JSONObject config = null; //the actual config loaded from the file

	private String confDir = ""; //the directory for our plugin
	private String confName = ""; //the name of our config. config.json is the default
	private boolean configError = false; //remember if the config had an error in loading, so as not to clobber a broken config.

	
	/**
	 * Wrapper for the MonoConf constructor.
	 * assumes the same name as the plugin for the folder
	 * also assumes the default config name
	 */
	public MonoConf(JavaPlugin plugin, JSONObject defaults) {
		this(plugin.getDescription().getName(),plugin,defaults);

	}

	/**
	 * Wrapper for the MonoConf constructor.
	 * assumes the config name of conf.json
	 */
	public MonoConf(String dir,JavaPlugin plugin, JSONObject defaults) {
		this(dir, "conf.json", plugin, defaults);
	}
	
	
	/**
	 * Loads or creates the configuration for this plugin.
	 * TODO: review error checking at the json level
	 * @param confDir The directory for this plugin
	 * @param configName The name of the configuration
	 * @param plugin the plugin that is creating this config
	 * @param defaults the default key:value settings for this plugin config
	 */

	public MonoConf(String confDir, String configName, JavaPlugin plugin, JSONObject defaults) {

		//remember the plugin that owns this config, and get its logger
		this.master = plugin;
		this.logger = master.getLogger();

		//TODO modularize this better
		this.confDir = confDir;
		File confFile = new File(confDir + "/" +  configName);
		File folder = new File(confDir);
		if (!confFile.exists()) {
			if (!folder.isDirectory()) {
				folder.mkdir();
			}
			popNewConf();
			logger.log(Level.INFO,"creating new config: " + confDir + "/" + configName);
		} else {
			JSONParser parser = new JSONParser();
			try {
				config = (JSONObject) parser.parse(new FileReader(confFile));
				checkNewOptions();
				configError=false;
				logger.log(Level.INFO,"config successfully loaded");
			} catch (IOException | ParseException e) {
				
				/*
				 * if there is an error reading from config,
				 * generate a new default config for this session.
				 * we set configError to true, so when we
				 * disable the plugin the config is NOT
				 * written out to file. 
				 */
				logger.log(Level.WARNING,"error reading config file");
				configError = true;
				popNewConf();
				e.printStackTrace();
			}
		}


		//assign the default configuration options
		this.defaults = defaults;
	}

	
	public void close() {
		if (!configError && config != null) {
			try {
				File confFile = new File(confDir + "/" + confName);
				PrintWriter confWriter = new PrintWriter(confFile);
				
				//separate each value by \n to make it user-friendly
				String confString = config.toJSONString();

				//big ugly .split shamelessly stolen from stack overflow
				//it splits by commas not included in quotes
				String[] confArray = confString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				for (int i = 0; i < confArray.length; i++) {
					if (i != confArray.length-1) {
					confWriter.println(confArray[i] + ",");
					} else {
						confWriter.println(confArray[i]);
					}
				}
					
				confWriter.close();
				logger.log(Level.INFO,"config successfully saved");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.log(Level.WARNING, "Error writing to file");
				logger.log(Level.WARNING, "Either delete file or fix error");
			}
		}
	}
	
	public Object getConf(Object key) {

		if (key == null) {
			//garbage in and garbage out
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			String methodName = stackTraceElements[stackTraceElements.length-1].getMethodName();
			int lineNo = stackTraceElements[stackTraceElements.length-1].getLineNumber();
			logger.log(Level.INFO,"method " + methodName + " called getConf at line " + lineNo + " and gave null");
			return null;
		}

		Object item;
		item = config.get(key);

		//if we're returning null, let's do some logging.
		if (item == null) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			logger.log(Level.INFO,"key: " + key);
			String methodName = stackTraceElements[stackTraceElements.length-1].getMethodName();
			int lineNo = stackTraceElements[stackTraceElements.length-1].getLineNumber();
			logger.log(Level.INFO,"method " + methodName + " called getConf at line " + lineNo + " and we're returning null");
		}
		return item;

	
	}
	
	/*
	 * Initializes a new json config
	 * and populates it with default
	 * values
	 */
	private void popNewConf() {
		config = defaults;
	}
	
	/*
	 * Check an existing config for new values
	 */
	private void checkNewOptions() {
		Set<Object> keys = defaults.keySet();
		
		boolean change = false;
		
		for (Object key : keys) {
			if (!config.containsKey(key)) {
				config.put(key, defaults.get(key));
				change = true;
			}
		}
		if (change)
			logger.log(Level.INFO, "defaults have been loaded for new config options");
	}
}
