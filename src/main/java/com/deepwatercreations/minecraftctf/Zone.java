package com.deepwatercreations.minecraftctf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	public Random rng;
	public Zone(Location center, int radius, Particle particle){
		this.center = center;
		this.radius = radius;
		this.particle = particle;
		this.rng = new Random();
		Zone.zoneList.add(this);
	}

	public void run(){
		World world = this.center.getWorld();

		float particleSize = 0.2f;

		Location loc;
		Location randomloc;
		for(int y = 0; y < 256; y++){
			double relativeY = (-this.center.getY()) + y;
			//Draw top and bottom sides
			for(int x = -radius; x <= radius; x++){
				loc = this.center.clone().add(x,relativeY,radius);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(this.rng.nextFloat(), this.rng.nextFloat(), 1);
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(Color.BLUE, particleSize));
				}
				loc = this.center.clone().add(x,relativeY,-radius);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(this.rng.nextFloat(), this.rng.nextFloat(), 0);
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(Color.BLUE, particleSize));
				}
			}
			//Draw left and right sides
			for(int z = -radius; z <= radius; z++){
				loc = this.center.clone().add(radius,relativeY, z);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(1, this.rng.nextFloat(), this.rng.nextFloat());
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(Color.BLUE, particleSize));
				}
				loc = this.center.clone().add(-radius,relativeY, z);
				for(int i = 0; i < 3; i++){
					randomloc = loc.clone().add(0, this.rng.nextFloat(), this.rng.nextFloat());
					world.spawnParticle(Particle.REDSTONE, randomloc, 1, new Particle.DustOptions(Color.BLUE, particleSize));
				}
			}
		}

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

	//TODO: isInBounds()
	//TODO: Draw only the outline
	//TODO: Team bounds vs play area bounds
}
