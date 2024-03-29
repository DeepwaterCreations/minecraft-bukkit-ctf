package com.deepwatercreations.minecraftctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.deepwatercreations.minecraftctf.zones.Zone;

public class CTFTeam{

	public static final ChatColor[] validTeamColors = {
		ChatColor.RED, ChatColor.BLUE, 
		ChatColor.WHITE, ChatColor.BLACK, 
		ChatColor.YELLOW, ChatColor.DARK_GRAY,
	       	ChatColor.AQUA, ChatColor.DARK_PURPLE,
	       	ChatColor.GOLD, ChatColor.GRAY, 
		ChatColor.DARK_AQUA, ChatColor.GREEN, 
		ChatColor.LIGHT_PURPLE, ChatColor.DARK_GREEN 	
	};

	public static Map<String, CTFTeam> teamDict = new HashMap<String, CTFTeam>();

	public static CTFTeam getTeamOfPlayer(Player player){
		for(CTFTeam team : teamDict.values()){
			if(team.hasPlayer(player)){
				return team;
			}
		}
		return null;
	}

	public String name;
	public String scoreName;
	public ChatColor color;

	public Scoreboard scoreboard;
	public Team scoreboardTeam;

	public Location teamBaseLoc;
	public Zone zone;
	public Flag flag;

	public CTFTeam(String name, ChatColor color, MinecraftCTF plugin, Scoreboard scoreboard, Objective scoreObjective, Location teamBaseLoc, int teamZoneRadius){
		this.name = name;
		this.color = color;
		this.scoreName = color + name;

		this.scoreboard = scoreboard;
		this.scoreboardTeam = scoreboard.registerNewTeam(name);
		scoreboardTeam.setColor(color);
		scoreObjective.getScore(this.scoreName).setScore(0);

		this.teamBaseLoc = teamBaseLoc;
		this.zone = new Zone(plugin, teamBaseLoc, teamZoneRadius, Zone.getParticleColorFromChatColor(color));
		this.flag = new Flag(plugin, teamBaseLoc, color, this);

		CTFTeam.teamDict.put(name, this);
	}

	public boolean hasPlayer(Player player){
		Team sTeam = scoreboard.getEntryTeam(player.getName());
		if(sTeam != null){
			return sTeam.equals(this.scoreboardTeam);
		} else {
			return false;
		}
	}

	public void addPlayer(Player player){
		this.scoreboardTeam.addEntry(player.getName());
	}

}

