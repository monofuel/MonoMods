/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoChat;

import java.util.Collection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class IRCListener implements Listener {

	private String nick;
	private String host;
	private String channel;
	private int port;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private IRCRunner runner;
	private ChannelQuery query;

	//TODO does this have to be final? i'm just copying from MonoMobs.
	private final JavaPlugin chatPlugin;

	public IRCListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
		chatPlugin = plugin;

		nick = chatPlugin.getConfig().getString("username");
		port = chatPlugin.getConfig().getInt("port");
		host = chatPlugin.getConfig().getString("server");
		channel = "#" + chatPlugin.getConfig().getString("channel");

		runner = new IRCRunner();
		query = new ChannelQuery();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,runner,20,10);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,query,40,1200);

		//initial connection
		reconnect();
	}

	private void reconnect() {

		MonoChat.log("connecting to " + host + ":" + port + " with username " + nick);
		try {
			socket = new Socket(host,port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			writeLine("NICK " + nick);
			writeLine("USER " + nick + " 8 * : " + nick);

			MonoChat.log("connected!");

			new Thread(() -> {
				try {
					MonoChat.log("sleeping");
					Thread.sleep(5000l);
					MonoChat.log("Joining channel");
					writeLine("JOIN " + channel);

				} catch (InterruptedException e1) {
					chatPlugin.getLogger().log(Level.SEVERE,"sleep interruption",e1);
				}
			}).start();

		} catch (Exception e) {
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e1) {
				chatPlugin.getLogger().log(Level.SEVERE,"sleep interruption",e1);
			}
			chatPlugin.getLogger().log(Level.SEVERE,"failed to connect to irc server",e);
			reconnect();
		}

	}

	public void close() {
		try {
			socket.close();
		} catch (Exception e) {
			chatPlugin.getLogger().log(Level.SEVERE,"failed to close connection to irc server",e);
		}
	}

	@EventHandler
	public void catchChat(AsyncPlayerChatEvent event) {
		String user = event.getPlayer().getName();
		String message = event.getMessage();

		sendMessage(user,message);
	}

	private void writeLine(String line) {
		line += "\n";
		//MonoChat.log(line);
		try {
			out.write(line);
			out.flush();
		} catch (Exception e) {
			reconnect();
		}
	}

	private void sendMessage(String user, String message) {

		StringBuilder line = new StringBuilder("PRIVMSG ");
		line.append(" " + channel + " :");
		line.append(user);
		line.append(": ");
		line.append(message);
		writeLine(line.toString());
	}

	private class IRCRunner extends BukkitRunnable {

		@Override
		public void run() {
			//publish incoming chat
			while (true) {
				try {
					if (!in.ready()) {
						//MonoChat.log("not ready");
						return;
					}
					if (socket.isClosed()) {
						MonoChat.log("socket closed");
						reconnect();
						return;
					}
					String line = in.readLine();
					if (line == null) {
						MonoChat.log("invalid line received");
						continue;
					}
					MonoChat.log("DEBUG: " + line);
					if (line.startsWith("PING")) {
						//MonoChat.log("PING");
						writeLine(line.replace("PING", "PONG"));
					} else if (line.contains("PRIVMSG")){
						if (line.contains(".players")) {
							sendMessage("Online Players",listPlayers());
						} else {
							//:monofuel_!monofuel@AB30421C.7629B4F6.3CA8CF0A.IP PRIVMSG #minecraft :test test
							String user;
							String message;
							String[] split = line.split("!");
							user = split[0].substring(1, split[0].length());
							split = line.split(" ");
							message = split[3].substring(1,split[3].length());
							for (int i = 4; i < split.length; i++) {
								message += " " + split[i];
							}

							Bukkit.broadcastMessage("#IRC " + user + ": " + message);
						}

					}else if (line.contains(" 352 ")) {

						boolean inChan = false;
						do {
							if (line.contains(" 352 ") &&
								line.contains(nick) &&
								line.contains(channel)) inChan = true;
							line = in.readLine();
						} while (!line.contains(" 315 "));

						if (!inChan) {
							MonoChat.log("Joining channel");
							writeLine("JOIN " + channel);
						}
					}
					//MonoChat.log(line);
				} catch (IOException e) {

					reconnect();
				}
			}
		}

		private String listPlayers() {
			Player[] online;


			//comment whichever one you do not want to use
			//because dumbasses like to break API's in minor version changes.
			online = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
			//online = (Player[]) Bukkit.getOnlinePlayers();

			if (online.length == 0) {
				return "nobody";
			}
			String listPlayers = online[0].getName();
			for (int i = 1; i < online.length; i++) {
				listPlayers += "," + online[i].getName();
			}
			return listPlayers;
		}
	}

	private class ChannelQuery extends BukkitRunnable {

		@Override
		public void run() {
			writeLine("who");
		}
	}
}
