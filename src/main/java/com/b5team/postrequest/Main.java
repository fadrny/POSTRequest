package com.b5team.postrequest;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {

    private static Settings settings;
    private static Main plugin;
    private static Logger logger;
    private boolean isRaining = false;
    private boolean isThundering = false;

	@Override
    public void onEnable() {
        plugin = this;
        logger = this.getLogger();

        // Load settings
        ConfigHandler configHandler = new ConfigHandler();
        try {
            settings = configHandler.loadSettings();
        } catch (Exception ex) {
            configHandler.generateConfig();
            logger.info("POSTRequest generated a config file. Go edit it!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Initialize weather state
        World world = Bukkit.getWorlds().get(0); 
        isRaining = world.hasStorm();
        isThundering = world.isThundering();

        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Schedule the task to run every 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                sendTimeAndWeather();
            }
        }.runTaskTimer(this, 0, 100); // 100 ticks = 5 seconds
    }

    @Override
    public void onDisable() {
        logger.info("POSTRequest is now disabled. You will no longer be capable to send HTTP/HTTPS POST requests.");
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.getWorld().equals(Bukkit.getWorlds().get(0))) { 
            isRaining = event.toWeatherState();
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.getWorld().equals(Bukkit.getWorlds().get(0))) { 
            isThundering = event.toThunderState();
        }
    }

    private void sendTimeAndWeather() {
        World world = Bukkit.getWorlds().get(0);
        long time = world.getTime();
        String weather = isThundering ? "thunder" : (isRaining ? "rain" : "clear");

        String[] args = new String[]{"time=" + time, "weather=" + weather};
        String url = Main.getSettings().getUrl();
        String protocol = Main.getSettings().getProtocol();
        String pwd = Main.getSettings().getPwd();

        try {
            Utils util = new Utils();
            String hash = util.encode(pwd);

            if (protocol.equals("https")) {
                HttpsPOSTRequest.sendRequest(url, hash, args);
            } else if (protocol.equals("http")) {
                HttpPOSTRequest.sendRequest(url, hash, args);
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Non-unicode characters found in your password. Change it and reload the server.");
            e.printStackTrace();
        }
    }


	@Override
	public boolean onCommand(CommandSender sender,
			Command cmd,
			String label,
			String[] args) {
		if(!this.isEnabled()){
            logger.log(Level.SEVERE, "POSTRequest is disabled. Restart the server to run commands.");
        } else if (cmd.getName().equalsIgnoreCase("pr")) {
        	String[] arg = args;
        	String url = Main.getSettings().getUrl();
        	String protocol = Main.getSettings().getProtocol();
        	String pwd = Main.getSettings().getPwd();
        	
        	try {
				Utils util = new Utils();
				String hash = util.encode(pwd);
				
				if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender || sender instanceof BlockCommandSender) {
	        		
					if (protocol == "https") {
	        			
						logger.info("Making HTTPS POST request...");
	        			HttpsPOSTRequest.sendRequest(url, hash, arg);
	        		} else if (protocol == "http") {
	        			
	        			logger.info("Making HTTP POST request...");
	        			HttpPOSTRequest.sendRequest(url, hash, arg);
	        		}
					
	        	} else if (sender instanceof Player) {
	        		
	        		Player plsender = (Player) sender;
	        		if (plsender.hasPermission("postrequest.pr.send")) {
	        			
	        			if (protocol == "https") {
		        			logger.info("Making HTTPS POST request...");
		        			HttpsPOSTRequest.sendRequest(url, hash, arg);
		        		} else if (protocol == "http") {
		        			
		        			logger.info("Making HTTP POST request...");
		        			HttpPOSTRequest.sendRequest(url, hash, arg);
		        		}
	        			
	        		} else {
	        			plsender.sendMessage("You are not allowed to use this command.");
	        		}
	        	}
				
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				logger.log(Level.SEVERE, "Non-unicode caracteres found on your password. Change it and reload the server.");
				e.printStackTrace();
			}
        }
		return false;
	}

    protected static Main getInstance() {
        return plugin;
    }

    protected static Logger getMainLogger() {
        return logger;
    }

    protected static Settings getSettings() {
        return settings;
    }
}