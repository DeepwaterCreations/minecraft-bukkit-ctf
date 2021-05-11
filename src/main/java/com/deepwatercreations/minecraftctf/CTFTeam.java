package com.deepwatercreations.minecraftctf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class CTFTeam{

	private static int maxid = 0;
	public static List<CTFTeam> teamList = new ArrayList<CTFTeam>();

	private final int id;
	public String name;
	public Team scoreboardTeam;

	public CTFTeam(Scoreboard board, Objective scoreObjective, String name){
		this.id = CTFTeam.maxid++;	
		this.name = name;
		CTFTeam.teamList.add(this);
		this.scoreboardTeam = board.registerNewTeam(name);
		scoreObjective.getScore(name).setScore(0);
		
	}

	public int getId(){
		return this.id;
	}

	public void addPlayer(Player player){
		this.scoreboardTeam.addEntry(player.getName());
	}

	public Set<String> getEntries(){
		return this.scoreboardTeam.getEntries();
	}
}
