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

public final class MinecraftCTF extends JavaPlugin implements Listener{

	public int currentParticleIdx = 0;
	public Block flagA = null;
	public Block flagB = null;

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
				Block flagSpawnA = teamABaseLoc.getBlock();
				flagSpawnA.setType(Material.END_PORTAL_FRAME);
				flagA = teamABaseLoc.clone().add(0, 1, 0).getBlock();
				flagA.setType(Material.BLUE_BANNER);

				Location teamBBaseLoc = playerLoc.clone().add(-10, 0, 0);
				Block flagSpawnB = teamBBaseLoc.getBlock();
				flagSpawnB.setType(Material.END_PORTAL_FRAME);
				flagB = teamBBaseLoc.clone().add(0, 1, 0).getBlock();
				flagB.setType(Material.RED_BANNER);

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
		if(flagA != null){
			Location aLoc = flagA.getLocation();
			if(pLoc.getBlockX() == aLoc.getBlockX() &&
			   pLoc.getBlockY() == aLoc.getBlockY() &&
			   pLoc.getBlockZ() == aLoc.getBlockZ() &&
			   player.getInventory().contains(new ItemStack(Material.RED_BANNER))){
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
