package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Location spawnLocation;
    private Location auctionLocation;
    private Location sellLocation;
    private Location crystalPvpLocation;
    private List<ItemStack> firstJoinKit;
    private List<ItemStack> crystalKit;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        loadConfigurations();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawn").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getCommand("reload").setExecutor(this);
    }

    private void loadConfigurations() {
        World world = Bukkit.getWorld(config.getString("spawn.world"));
        spawnLocation = new Location(
            world,
            config.getDouble("spawn.x"),
            config.getDouble("spawn.y"),
            config.getDouble("spawn.z")
        );

        auctionLocation = new Location(
            world,
            config.getDouble("auction-area.x"),
            config.getDouble("auction-area.y"),
            config.getDouble("auction-area.z")
        );

        sellLocation = new Location(
            world,
            config.getDouble("sell-area.x"),
            config.getDouble("sell-area.y"),
            config.getDouble("sell-area.z")
        );

        crystalPvpLocation = new Location(
            world,
            config.getDouble("crystal-pvp-area.x"),
            config.getDouble("crystal-pvp-area.y"),
            config.getDouble("crystal-pvp-area.z")
        );

        firstJoinKit = new ArrayList<>();
        for (String item : config.getStringList("spawn.first-join-kit")) {
            firstJoinKit.add(new ItemStack(Material.valueOf(item)));
        }

        crystalKit = new ArrayList<>();
        for (String item : config.getStringList("spawn.crystal-kit")) {
            crystalKit.add(new ItemStack(Material.valueOf(item)));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("spawn")) {
            player.teleport(spawnLocation);
            player.sendMessage(ChatColor.GOLD + config.getString("spawn.welcome-message"));
            return true;
        } else if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("stablesmp.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            spawnLocation = player.getLocation();
            config.set("spawn.world", spawnLocation.getWorld().getName());
            config.set("spawn.x", spawnLocation.getX());
            config.set("spawn.y", spawnLocation.getY());
            config.set("spawn.z", spawnLocation.getZ());
            saveConfig();
            player.sendMessage(ChatColor.GOLD + "Spawn location set.");
            return true;
        } else if (command.getName().equalsIgnoreCase("reload")) {
            if (!player.hasPermission("stablesmp.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            reloadConfig();
            config = getConfig();
            loadConfigurations();
            player.sendMessage(ChatColor.GOLD + "Configuration reloaded.");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.teleport(spawnLocation);
            player.getInventory().addItem(firstJoinKit.toArray(new ItemStack[0]));
            player.sendMessage(ChatColor.GOLD + config.getString("spawn.welcome-message"));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getLastDamageCause() != null && player.getLastDamageCause().getEntity() instanceof Player) {
            player.teleport(spawnLocation);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(spawnLocation);
    }

    @EventHandler
    public void onPlayerJoinForCrystalKit(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("stablesmp.crystalkit")) {
            player.getInventory().addItem(crystalKit.toArray(new ItemStack[0]));
        }
    }
}