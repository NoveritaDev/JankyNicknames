package me.Noverita.JankyNicknames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JankyNicknames extends JavaPlugin implements CommandExecutor, Listener {
    @Override
    public void onEnable() {
        Bukkit.getLogger().info("Enabled " + this.getName());
        this.getCommand("rpname").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);

        File yourFile = new File("./plugins/nicknames.txt");
        try {
            yourFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabled " + this.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try (BufferedReader br = new BufferedReader(new FileReader("./plugins/nicknames.txt"))) {
            String line = br.readLine();

            while (line != null) {
                String[] values = line.split(",");
                if (values[0].strip().equals(player.getName())) {
                    setNickname(values[1].strip(), event.getPlayer());
                    break;
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean setNickname(String name, Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getEntryTeam(player.getName());

        if (team == null) {
            team = board.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }

        if (name.length() > 20) {
            return false;
        }
        team.setPrefix(name + " (");
        team.setSuffix(")");

        player.setDisplayName(name);

        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get("./plugins/nicknames.txt"), StandardCharsets.UTF_8));

            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).split(",")[0].strip().equals(player.getName())) {
                    fileContent.set(i, player.getName() + "," + name);
                    break;
                }
            }

            Files.write(Paths.get("./plugins/nicknames.txt"), fileContent, StandardCharsets.UTF_8);
            return true;

        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getEntryTeam(player.getName());

        if (team != null) {
            team.unregister();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1) {
                String name = String.join(" ",args);
                return setNickname(name, player);
            } else {
                Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                Team team = board.getEntryTeam(player.getName());
                if (team != null) {
                    team.unregister();
                }
                player.setDisplayName(player.getName());
            }
            return true;
        }
        return false;
    }
}
