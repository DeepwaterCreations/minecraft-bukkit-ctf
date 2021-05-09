package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class Zone extends BukkitRunnable{

	public World world;
	public Location center;
	public int radius;
	public Zone(World world, Location center, int radius){
		this.world = world;
		this.center = center;
		this.radius = radius;
	}

	public void run(){
		//Move the cursor down to the bottom of the world and the corner of the zone
		Location loc = this.center.clone().subtract(radius,this.center.getY(),radius);

		for(int y = 0; y < 256; y++){
			for(int x = -radius; x <= radius; x++){
				for(int z = -radius; z <= radius; z++){
					world.spawnParticle(Particle.SUSPENDED, loc, 1);
					loc.add(0,0,1);
				}
				loc.add(1,0,-((radius * 2)+1));
			}
			loc.add(-((radius * 2) + 1), 1, 0);
		}

	}

	//TODO: isInBounds()
	//TODO: Draw only the outline
	//TODO: Team bounds vs play area bounds
}
