package com.deepwatercreations.minecraftctf;

import de.tr7zw.changeme.nbtapi.NBTItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
import org.bukkit.util.Vector;

import com.deepwatercreations.minecraftctf.CTFCommandExecutor;
import com.deepwatercreations.minecraftctf.Flag;
import com.deepwatercreations.minecraftctf.zones.*;

public final class MinecraftCTF extends JavaPlugin implements Listener{

	public static String TEAM_KEY = "CTF_TEAM";

	public int currentParticleIdx = 0;
	public Objective scoreObjective;
	public Scoreboard scoreboard;

	public int teamColorIdx = 0;

	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		CTFCommandExecutor commandExecutor = new CTFCommandExecutor(this);
		getCommand("respawnFlags").setExecutor(commandExecutor);
		getCommand("teamlist").setExecutor(commandExecutor);
		getCommand("teamjoin").setExecutor(commandExecutor);
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
				Location centerLoc = new Location(player.getWorld(), (double)playerLoc.getBlockX(), (double)playerLoc.getBlockY(), (double)playerLoc.getBlockZ());
				Vector axis = player.getFacing().getDirection();
				init(centerLoc, axis, (4 * 16));
			} else {
				//TODO: Get center location from arguments...?
				//	Or pick it if none are provided.
			}
			return true;
		}
		return false;
	}

	public void init(Location centerLoc, Vector axis, int zonelength){
		this.scoreboard = getServer().getScoreboardManager().getMainScoreboard();

		//First, clear any persistent data
		for(Team t: scoreboard.getTeams()){ //TODO: Handle this in CTFTeam?
			t.unregister();
		}
		for(Objective o: scoreboard.getObjectives()){
			o.unregister();
		}
		Flag.resetList();
		Zone.resetList();

		//Set up the scoreboard
		this.scoreObjective = scoreboard.registerNewObjective("score", "dummy", "Score");
		this.scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.scoreObjective.setRenderType(RenderType.INTEGER);

		//pick/setup a game field
		// findGameFieldLocation(centerLoc.getWorld(), 0, 0);

		int margin = 1;
		int distToTeamZoneCenter = zonelength + zonelength + (zonelength/2);
		int teamZoneRadius = (zonelength/2);
		Vector centerToBase = axis.clone().multiply(distToTeamZoneCenter);
		World world = centerLoc.getWorld();
		Location teamABaseLoc = world.getHighestBlockAt(centerLoc.clone().add(centerToBase)).getLocation().add(0,1,0);
		Location teamBBaseLoc = world.getHighestBlockAt(centerLoc.clone().subtract(centerToBase)).getLocation().add(0,1,0);
		CTFTeam teamA = new CTFTeam("Zigzags", 
					    CTFTeam.validTeamColors[teamColorIdx++ % CTFTeam.validTeamColors.length], 
					    this,
					    this.scoreboard, 
					    this.scoreObjective,
					    teamABaseLoc,
					    teamZoneRadius);
		CTFTeam teamB = new CTFTeam("Curlicues", 
					    CTFTeam.validTeamColors[teamColorIdx++ % CTFTeam.validTeamColors.length], 
					    this,
					    this.scoreboard, 
					    this.scoreObjective,
					    teamBBaseLoc,
					    teamZoneRadius);
		//TODO: Pick an appropriate height to spawn both flags at given the ground levels
		//	at the two locations.

		//Prompt players to register a team
		getServer().broadcastMessage("Choose a team by typing '/teamjoin [team name]' into chat.");

		//Make a game zone
		//TODO: Rectangular instead of square?
		GameBoundsZone gameBounds = new GameBoundsZone(centerLoc, (zonelength + zonelength + zonelength + margin), this);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		Location pLoc = player.getLocation();

		//Check if they've standing on their own flag
		CTFTeam ownTeam = CTFTeam.getTeamOfPlayer(player);
		if(ownTeam != null){
			Flag ownFlag = Flag.getFlagForTeamName(ownTeam.name);
			if(ownFlag != null && ownFlag.getLocation() != null){
				Location fLoc = ownFlag.getLocation();
				if(pLoc.getBlockX() == fLoc.getBlockX() &&
				   pLoc.getBlockY() == fLoc.getBlockY() &&
				   pLoc.getBlockZ() == fLoc.getBlockZ()){
					if(!ownTeam.zone.isInBounds(pLoc)) {
						player.sendMessage("Can't score outside your team zone"); 
					} else {
						//They're standing on their own flag's block and in their own zone, so check if they can score.
						Score ownTeamScore = this.scoreObjective.getScore(ownTeam.scoreName);
						List<ItemStack> carriedFlags = Flag.getFlagItemsFromInventory(player.getInventory());
						//Score for each flag in their inventory. We assume if they're standing on their own
						//team's flag block, that flag probably isn't also in their inventory, so every flag they're carrying
						//is a flag they can score off of.
						for(ItemStack carriedFlag : carriedFlags){
							//Increase the player's team's score
							ownTeamScore.setScore(ownTeamScore.getScore() + 1);
							//Figure out whose flag it is
							NBTItem nbtitem = new NBTItem(carriedFlag);
							String flagTeamName = nbtitem.getString(MinecraftCTF.TEAM_KEY);
							Flag cappedFlag = Flag.getFlagForTeamName(flagTeamName);
							CTFTeam cappedFlagTeam = cappedFlag.team;
							//Respawn it
							cappedFlag.respawn();
							//and spread the news!
							getServer().broadcastMessage(String.format("%s captured team %s's flag!", player.getName(), CTFCommandExecutor.teamColoredText(cappedFlagTeam, cappedFlagTeam.name)));
							getServer().broadcastMessage(CTFCommandExecutor.teamColoredText(ownTeam, String.format("TEAM %s SCORES!", ownTeam.name)));
							//TODO: Sound effects, particle effects?
						}
						//TODO: Check for win (maybe even emit an event? Only if I have a genuine use for it - YNGNI)
					}
				}
			} 
		} 
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Player player = event.getEntity();
		//Spawn them at their base if they don't have a different spawn set
		if(player.getBedSpawnLocation() == null){
			CTFTeam team = CTFTeam.getTeamOfPlayer(player);
			Flag flag = Flag.getFlagForTeamName(team.name);
			//TODO: Check the two blocks that are spawn targets and turn them into air if they're not
			//	a flag.
			if(flag.spawnBlock != null){
				player.setBedSpawnLocation(flag.spawnBlock.getLocation().add(0,1,0), true);
			} else {
				player.setBedSpawnLocation(flag.initLoc, true);
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
	
	//TODO:
	//Flag items dropped from the flag's attached block being broken count as real flag items
	//Zones as barriers for first x minutes
	//Flags behave appropriately in zones
	//Flag can't go through nether/end portals
	//Flag spawn can be moved (but can't leave team zone)
	//Sneaking, running, names, flag/carrier visibility
	//Team spawnpoints
	//Prisons
	//	What happens when prison beds are broken?
	//Turn off advancements (maybe just hide them from the other team?)
	//Recall/b command?
	//Sound effects on capture, pickup, etc
	//	Also particle effects
	//Compasses to find chunk with enemy flag
	//	Compasses wobble to mask flag movement across chunk boundaries?
	//Make flags visible through walls to owning team(?)
	//Team Whisper (/tw) command to chat with an entire team
	//	Make sure this doesn't already exist
	//Track extra stuff like kills, deaths, captures, etc?
	//Team votes
}
