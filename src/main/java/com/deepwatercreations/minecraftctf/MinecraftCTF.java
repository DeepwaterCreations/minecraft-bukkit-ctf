package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MinecraftCTF extends JavaPlugin{

	public int currentParticleIdx = 0;

	@Override
	public void onEnable(){

	}

	@Override
	public void onDisable(){

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("init")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				Location playerLoc = player.getLocation();

				Location teamABaseLoc = playerLoc.clone().add(3, 0, 0);
				Block flagA = teamABaseLoc.getBlock();
				flagA.setType(Material.BLUE_BANNER);

				Location teamBBaseLoc = playerLoc.clone().add(-3, 0, 0);
				Block flagB = teamBBaseLoc.getBlock();
				flagB.setType(Material.RED_BANNER);

				int teamZoneRadius = 3;

				Location boundaryCenterLoc = player.getLocation();
				new BukkitRunnable(){
					public void run(){
						Location loc = boundaryCenterLoc.clone().subtract(teamZoneRadius,0,teamZoneRadius); //Move the location to the corner
						loc.subtract(0,loc.getY(),0); //Move the location down to the bottom of the world
						for(int y = 0; y < 256; y++){
							for(int x = -teamZoneRadius; x <= teamZoneRadius; x++){
								for(int z = -teamZoneRadius; z <= teamZoneRadius; z++){
									// player.getWorld().spawnParticle(Particle.values()[currentParticleIdx], loc, 1);
									player.getWorld().spawnParticle(Particle.SUSPENDED, loc, 1);
									loc.add(0,0,1);
								}
								loc.add(1,0,-((teamZoneRadius * 2)+1));
							}
							loc.add(-((teamZoneRadius * 2) + 1), 1, 0);
						}
					}

				}.runTaskTimer(this, 0, 1);
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("ne")){
			this.currentParticleIdx = (this.currentParticleIdx + 1) % Particle.values().length;
			while(Particle.values()[this.currentParticleIdx] == Particle.REDSTONE ||
			   Particle.values()[this.currentParticleIdx] == Particle.ITEM_CRACK ||
			   Particle.values()[this.currentParticleIdx] == Particle.BLOCK_CRACK ||
			   Particle.values()[this.currentParticleIdx] == Particle.MOB_APPEARANCE){
				this.currentParticleIdx = (this.currentParticleIdx + 1) % Particle.values().length;
			   }
			sender.sendMessage(Particle.values()[this.currentParticleIdx].toString());
		}
		

		//If the command isn't valid, return false so that help is displayed
		return false;
	}
}
