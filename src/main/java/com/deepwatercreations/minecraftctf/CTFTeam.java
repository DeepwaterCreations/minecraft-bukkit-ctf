package com.deepwatercreations.minecraftctf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scoreboard.Team;

public class CTFTeam{

	private static int maxid = 0;
	public static List<CTFTeam> teamList = new ArrayList<CTFTeam>();

	private final int id;
	public String name;

	public CTFTeam(String name){
		this.id = CTFTeam.maxid++;	
		this.name = name;
		CTFTeam.teamList.add(this);
	}

	public int getId(){
		return this.id;
	}
}
