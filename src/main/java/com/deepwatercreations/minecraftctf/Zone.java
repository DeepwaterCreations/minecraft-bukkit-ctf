package com.deepwatercreations.minecraftctf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class Zone extends BukkitRunnable{

	public static List<Zone> zoneList = new ArrayList<Zone>();

	public Location center;
	public int radius;
	public Particle particle;
	public Color color;
	public Random rng;
	public Zone(Location center, int radius, Color color){
		this.center = center;
		this.radius = radius;
		this.color = color;
		this.rng = new Random();
		Zone.zoneList.add(this);
	}

	public void run(){
		World world = this.center.getWorld();

		float particleSize = 0.2f;

		//TODO: Refactor this.
		Location loc;
		Location randomloc;
		for(int y = 0; y < 256; y++){
			double relativeY = (-this.center.getY()) + y;
			//Draw top and bottom sides
			for(int x = -radius; x <= radius; x++){
				loc = this.center.clone().add(x,relativeY,radius);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(this.rng.nextFloat(), this.rng.nextFloat(), 1);
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(color, particleSize));
				}
				loc = this.center.clone().add(x,relativeY,-radius);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(this.rng.nextFloat(), this.rng.nextFloat(), 0);
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(color, particleSize));
				}
			}
			//Draw left and right sides
			for(int z = -radius; z <= radius; z++){
				loc = this.center.clone().add(radius,relativeY, z);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(1, this.rng.nextFloat(), this.rng.nextFloat());
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(color, particleSize));
				}
				loc = this.center.clone().add(-radius,relativeY, z);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(0, this.rng.nextFloat(), this.rng.nextFloat());
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(color, particleSize));
				}
			}
		}

	}

	public boolean isInBounds(Location loc){
		double xMin = this.center.getX() - this.radius;
		double xMax = this.center.getX() + this.radius;
		double zMin = this.center.getZ() - this.radius;
		double zMax = this.center.getZ() + this.radius;
		return (loc.getX() > xMin &&
			loc.getX() < xMax &&
			loc.getZ() > zMin &&
			loc.getZ() < zMax);
	}

	public void setParticle(Particle particle){
		this.particle = particle;
	}

	public static void resetList(){
		for(Zone z : Zone.zoneList){
			if(!z.isCancelled()){
				z.cancel();
			}
		}
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
			// case DARK_RED: //No banner for this color
			// 	return Color.MAROON;
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

	//TODO: isInBounds()
	//TODO: Draw only the outline
	//TODO: Team bounds vs play area bounds
}
