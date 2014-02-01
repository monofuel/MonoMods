package japura;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MonoConf {
	
	private static JSONObject defaults;
	
	private JSONObject config = null;
	private boolean configError = false;
	
	public static void init() {
		defaults = new JSONObject();
		defaults.put("world", "world");
		defaults.put("threads",4);
		defaults.put("zombie cap",2000);
		defaults.put("zed per player",50);
		defaults.put("zed distance",100);
		defaults.put("zed y distance",50);
		defaults.put("zed spawn tick offset",40);
		defaults.put("zed spawn tick offset",52);
		defaults.put("max light to spawn",7);
		defaults.put("wither disabled", "true");
	}

	public MonoConf(String dir) {
		this(dir,"conf.json");
	}
	
	public MonoConf(String dir, String configName) {
		File confFile = new File(dir + "/" +  configName);
		File confDir = new File(dir);
		if (!confFile.exists()) {
			if (!confDir.isDirectory()) {
				confDir.mkdir();
			}
			popNewConf();
			MonoMobs.log("creating new config: " + dir + "/" + configName);
		} else {
			JSONParser parser = new JSONParser();
			try {
				config = (JSONObject) parser.parse(new FileReader(confFile));
				checkNewOptions();
				configError=false;
				MonoMobs.log("config successfully loaded");
			} catch (IOException | ParseException e) {
				
				/*
				 * if there is an error reading from config,
				 * generate a new default config for this session.
				 * we set configError to true, so when we
				 * disable the plugin the config is NOT
				 * written out to file. 
				 */
				MonoMobs.log("Error reading config file");
				configError = true;
				popNewConf();
				e.printStackTrace();
			}
		}
	}

	
	public void close() {
		if (!configError && config != null) {
			try {
				File confFile = new File("plugins/MonoMobs/conf.json");
				PrintWriter confWriter = new PrintWriter(confFile);
				
				//separate each value by \n to make it user-friendly
				String confString = config.toJSONString();
				String[] confArray = confString.split(",");
				for (int i = 0; i < confArray.length; i++) {
					if (i != confArray.length-1) {
					confWriter.println(confArray[i] + ",");
					} else {
						confWriter.println(confArray[i]);
					}
				}
					
				confWriter.close();
				MonoMobs.log("config successfully saved");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				MonoMobs.log("Error writing to file");
				MonoMobs.log("Either delete file or fix error");
			}
		}
	}
	
	private volatile boolean lock = false;
	
	public Object getConf(Object key) {
		Object item;
		while (true) {
			if (lock == false) {
				lock = true;
				item = config.get(key);
				lock = false;
				return item;
			}
		}
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
			MonoMobs.log("defaults have been loaded for new config options");
	}
}
