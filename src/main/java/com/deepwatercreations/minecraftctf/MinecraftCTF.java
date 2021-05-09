package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.deepwatercreations.minecraftctf.Zone;
import com.deepwatercreations.minecraftctf.Flag;

public final class MinecraftCTF extends JavaPlugin implements Listener{

	public int currentParticleIdx = 0;
	public Flag flagA = null;
	public Flag flagB = null;

	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
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

				Location teamABaseLoc = playerLoc.clone().add(10, 0, 0);
				flagA = new Flag(this, teamABaseLoc, Material.BLUE_BANNER, 0);
				//TODO: Pick an appropriate height to spawn both flags at given the ground levels
				//	at the two locations.

				Location teamBBaseLoc = playerLoc.clone().add(-10, 0, 0);
				flagB = new Flag(this, teamBBaseLoc, Material.RED_BANNER, 1);

				int teamZoneRadius = 3;

				new Zone(player.getWorld(), teamABaseLoc, teamZoneRadius).runTaskTimer(this, 0, 1);
				new Zone(player.getWorld(), teamBBaseLoc, teamZoneRadius).runTaskTimer(this, 0, 1);
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

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		//Check if they've carried the enemy flag to their own flag
		Player player = event.getPlayer();
		Location pLoc = player.getLocation();
		if(flagA != null && flagA.getLocation() != null){
			Location aLoc = flagA.getLocation();
			if(pLoc.getBlockX() == aLoc.getBlockX() &&
			   pLoc.getBlockY() == aLoc.getBlockY() &&
			   pLoc.getBlockZ() == aLoc.getBlockZ() &&
			   player.getInventory().contains(flagB.item)){
				player.sendRawMessage("You probably scored maybe!");
			}
		}
	}

	//EVENTS TO CATCH:
	//ItemDespawnEvent
	//EntityDropItemEvent
	//EntityPickupItemEvent
	//EntityPlaceEvent
	//AsyncPlayerPreLoginEvent (to restore team identity and so forth?)
	//PlayerDropItemEvent
	//PlayerQuitEvent
	//PlayerRespawnEvent (to spawn at team base or prison)
	//PlayerToggleSneakEvent (for flag visibility complexity?)	
	//PlayerToggleSprintEvent ("")
}
