package com.deepwatercreations.minecraftctf;

import de.tr7zw.changeme.nbtapi.NBTItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.deepwatercreations.minecraftctf.CTFTeam;
import com.deepwatercreations.minecraftctf.Flag;
import com.deepwatercreations.minecraftctf.Zone;

public final class MinecraftCTF extends JavaPlugin implements Listener{

	public static String TEAM_KEY = "CTF_TEAM";

	public int currentParticleIdx = 0;

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

				//Set up the scoreboard
				Scoreboard board = getServer().getScoreboardManager().getMainScoreboard();
				//First, clear any persistent data
				for(Team t: board.getTeams()){
					t.unregister();
				}
				for(Objective o: board.getObjectives()){
					o.unregister();
				}
				Objective objective = board.registerNewObjective("score", "dummy", "Score");
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				objective.setRenderType(RenderType.INTEGER);
				CTFTeam zigzags = new CTFTeam(board, objective, "Zigzags");
				CTFTeam curlicues = new CTFTeam(board, objective, "Curlicues");

				Location playerLoc = player.getLocation();

				int teamZoneRadius = 3;

				Location teamABaseLoc = playerLoc.clone().add(10, 0, 0);
				new Flag(this, teamABaseLoc, Material.BLUE_BANNER, zigzags);
				new Zone(player.getWorld(), teamABaseLoc, teamZoneRadius).runTaskTimer(this, 0, 1);
				//TODO: Pick an appropriate height to spawn both flags at given the ground levels
				//	at the two locations.

				Location teamBBaseLoc = playerLoc.clone().add(-10, 0, 0);
				new Flag(this, teamBBaseLoc, Material.RED_BANNER, curlicues);
				new Zone(player.getWorld(), teamBBaseLoc, teamZoneRadius).runTaskTimer(this, 0, 1);

				//Prompt players to register a team //TODO: Actually just randomize it for now, but they should get the option to choose
				List<Player> players = player.getWorld().getPlayers();
				Random rng = new Random();
				List<CTFTeam> teamList = CTFTeam.teamList;
				for(Player p : players){
					CTFTeam team = teamList.get(rng.nextInt(teamList.size()));
					p.setMetadata(MinecraftCTF.TEAM_KEY, new FixedMetadataValue(this, team.name));
					p.sendRawMessage("You've been assigned to team " + team.name);
					//Let's also enable the scoreboard
					p.setScoreboard(board);
				}

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
		//TODO: Commands for
		//	Changing team
		//	Resetting flags/score/so forth
		//	Cleanup all game objects/metainfo?
		//	Defect to the other team? 
		

		//If the command isn't valid, return false so that help is displayed
		return false;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		//Check if they've standing on a flag
		Player player = event.getPlayer();
		Location pLoc = player.getLocation();
		for(Flag flag : Flag.flagList){
			if(flag.getLocation() != null){
				Location fLoc = flag.getLocation();
				if(pLoc.getBlockX() == fLoc.getBlockX() &&
				   pLoc.getBlockY() == fLoc.getBlockY() &&
				   pLoc.getBlockZ() == fLoc.getBlockZ()){
					int playerTeamId = player.getMetadata(MinecraftCTF.TEAM_KEY).get(0).asInt();
					int flagTeamId = flag.team.getId();
					//Check if it's their own flag and if they have an enemy flag in their inventory
					if(playerTeamId == flagTeamId && checkInventoryForEnemyFlag(player)){
						player.sendRawMessage("You probably scored maybe!");
					}
				   }
			}
		}
	}

	private boolean checkInventoryForEnemyFlag(Player player){
		Inventory inv = player.getInventory();
		int playerTeamId = player.getMetadata(MinecraftCTF.TEAM_KEY).get(0).asInt(); //TODO: Be a little careful and make sure we aren't checking players without teams
		for(ItemStack item : inv){
			if(item != null){
				NBTItem nbtitem = new NBTItem(item);
				//If I wanted to be safer, I could use a try/catch to make sure the value under the key
				//	is actually a string, but I think that's overkill for planned usage
				if(nbtitem.hasKey(MinecraftCTF.TEAM_KEY) && !nbtitem.getInteger(MinecraftCTF.TEAM_KEY).equals(playerTeamId)){
					return true;
				}
			}
		}
		return false;
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
	
	//TODO:
	//Recall/b command?
	//Turn off advancements (maybe just hide them from the other team?)
	//Sound effects on capture, pickup, etc
	//	Also particle effects
	//Compasses to find chunk with enemy flag
	//	Compasses wobble to mask flag movement across chunk boundaries?
	//Make flags visible through walls to owning team(?)
}
