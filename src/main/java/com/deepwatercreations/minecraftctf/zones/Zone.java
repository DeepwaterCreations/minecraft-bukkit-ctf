package com.deepwatercreations.minecraftctf.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class Zone{

	public static List<Zone> zoneList = new ArrayList<Zone>();

	public Location center;
	public int radius;

	double xMin;
	double xMax;
	double zMin;
	double zMax;

	ParticleWall northEdge;
	ParticleWall southEdge;
	ParticleWall westEdge;
	ParticleWall eastEdge;
	
	// public Particle particle;
	public Color color;
	public Random rng;

	public Zone(Plugin plugin, Location center, int radius, Color color){
		this.center = center;
		this.radius = radius;

		this.xMin = this.center.getX() - this.radius;
		this.xMax = this.center.getX() + this.radius;
		this.zMin = this.center.getZ() - this.radius;
		this.zMax = this.center.getZ() + this.radius;

		this.color = color;
		World world = this.center.getWorld();
		this.northEdge = new ParticleWall(plugin,
						  new Location(world, this.xMin, 0, this.zMin), 
						  new Location(world, this.xMax, 0, this.zMin), 
						  this.color);
		this.southEdge = new ParticleWall(plugin,
						  new Location(world, this.xMin, 0, this.zMax), 
						  new Location(world, this.xMax, 0, this.zMax), 
						  this.color);
		this.westEdge = new ParticleWall(plugin,
						 new Location(world, this.xMin, 0, this.zMin), 
						 new Location(world, this.xMin, 0, this.zMax), 
						 this.color);
		this.eastEdge = new ParticleWall(plugin,
						 new Location(world, this.xMax, 0, this.zMin), 
						 new Location(world, this.xMax, 0, this.zMax), 
						 this.color);

		this.rng = new Random();
		Zone.zoneList.add(this);
	}

	public boolean isInBounds(Location loc){
		return (loc.getX() > this.xMin &&
			loc.getX() < this.xMax &&
			loc.getZ() > this.zMin &&
			loc.getZ() < this.zMax);
	}

	public Location getClosestInBounds(Location loc){
		loc = loc.clone();

		if(loc.getX() > this.xMax){
			loc.setX(Math.floor(this.xMax));
		} else if(loc.getX() < this.xMin){
			loc.setX(Math.ceil(this.xMin));
		}
		if(loc.getZ() > this.zMax){
			loc.setZ(Math.floor(this.zMax));
		} else if (loc.getZ() < this.zMin){
			loc.setZ(Math.ceil(this.zMin));
		}
		return loc;		
	}

	public static void resetList(){
		Zone.zoneList = new ArrayList<Zone>();
	}

	public static Color getParticleColorFromChatColor(ChatColor color){
		switch(color){
			case AQUA:
				return Color.AQUA;
			case BLACK:
				return Color.BLACK;
			case BLUE:
				return Color.BLUE;
			case DARK_AQUA:
				return Color.TEAL;
			// case DARK_BLUE: //No banner for this color
			// 	return Color.NAVY;
			case DARK_GRAY:
				return Color.GRAY;
			case DARK_GREEN:
				return Color.GREEN;
			case DARK_PURPLE:
				return Color.PURPLE;
			case DARK_RED: //No banner for this color
				return Color.MAROON;
			case GOLD:
				return Color.ORANGE;
			case GRAY:
				return Color.SILVER;
			case GREEN:
				return Color.LIME;
			case LIGHT_PURPLE:
				return Color.FUCHSIA;
			case RED:
				return Color.RED;
			case WHITE:
				return Color.WHITE;
			case YELLOW:
				return Color.YELLOW;
			default:
				throw new IllegalArgumentException(String.format("No matching particle color for chat color %s", color.toString()));
		}

	}

	//TODO: Team bounds vs play area bounds
	//TODO: Handle ender pearling across the boundary.
}
