package com.deepwatercreations.minecraftctf.zones;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleBlock extends BukkitRunnable{

	Particle particle;
	float particleSize;
	Color color;
	Location loc;
	public ParticleBlock(Location loc, Color color, float particleSize){
		this.particle = Particle.REDSTONE;
		this.particleSize = particleSize;
		this.color = color;
		this.loc = loc;
	}

	public void run(){
		this.loc.getWorld().spawnParticle(this.particle, this.loc.clone().add(0.5, 0.5, 0.5), 1, new Particle.DustOptions(this.color, this.particleSize));
		this.loc.getWorld().spawnParticle(this.particle, this.loc.clone().add(0.5, 1.5, 0.5), 1, new Particle.DustOptions(this.color, this.particleSize));
	}

}

