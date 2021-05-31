package com.deepwatercreations.minecraftctf.zones;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class ParticleWall implements Listener{
	
	Color color;
	int displayDist;

	Location endA, endB;
	int parallelMax, parallelMin;  //Coordinates of endpoints along the parallel axis
        int perpendicular; //Coordinate of both endpoints along the perpendicular axis
	Axis parallelAxis; //Which axis the wall is parallel to

	Map<Vector, Long> lastDisplay;
	long displayDelay = 3;

	Plugin plugin;

	public ParticleWall(Plugin plugin, Location endA, Location endB, Color color){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		this.color = color;
		this.displayDist = 16;
		this.endA = endA;
		this.endB = endB;

		if(endA.getBlockX() != endB.getBlockX() && endA.getBlockZ() == endB.getBlockZ()){
			this.parallelMin = Math.min(endA.getBlockX(), endB.getBlockX());
			this.parallelMax = Math.max(endA.getBlockX(), endB.getBlockX());
			this.perpendicular = endA.getBlockZ();
			this.parallelAxis = Axis.X;

		} else if (endA.getBlockZ() != endB.getBlockZ() && endA.getBlockX() == endB.getBlockX()){
			this.parallelMin = Math.min(endA.getBlockZ(), endB.getBlockZ());
			this.parallelMax = Math.max(endA.getBlockZ(), endB.getBlockZ());
			this.perpendicular = endA.getBlockX();
			this.parallelAxis = Axis.Z;
		}

		this.lastDisplay = new HashMap<Vector, Long>();

		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		//We have two endpoints of a line. 
		//We know the player's location. We can get the difference from the perpendicular coordinate to know if we should
		//display anything and the player's parallel coordinate to know where to draw the wall.
		Player player = event.getPlayer();
		Location playerLoc = player.getLocation();
		int playerll = this.parallelAxis == Axis.X ? playerLoc.getBlockX() : playerLoc.getBlockZ();
		int playerT = this.parallelAxis == Axis.X ? playerLoc.getBlockZ() : playerLoc.getBlockX();
		if(playerll > this.parallelMin && playerll < this.parallelMax){
			//player is aligned with the wall at some distance
			int playerDist = Math.abs(playerT - this.perpendicular);
			if(playerDist < this.displayDist){
				World world = player.getWorld();

				//Get the location of the block adjacent to the player: 
				//Same parallel coordinates as the player, same perpendicular coordinates as the wall.
				//Also give it the player's Y value.
				int blockX = this.parallelAxis == Axis.X ? playerll : this.perpendicular;
				int blockZ = this.parallelAxis == Axis.X ? this.perpendicular : playerll;
				Location loc = new Location(world, blockX, playerLoc.getBlockY(), blockZ);

				//Don't display particles unless it's been long enough since the last time
				long dTime = world.getTime() - this.lastDisplay.getOrDefault(loc.toVector(), 0l);
				if(dTime > this.displayDelay){
					((BukkitRunnable) new ParticleBlock(loc, this.color, 2.0f)).runTaskAsynchronously(plugin);
					this.lastDisplay.put(loc.toVector(), playerLoc.getWorld().getTime());
				}
			}
		}
	}
}
