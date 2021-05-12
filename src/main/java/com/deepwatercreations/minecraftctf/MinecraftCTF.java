package com.deepwatercreations.minecraftctf;

import de.tr7zw.changeme.nbtapi.NBTItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
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

import com.deepwatercreations.minecraftctf.Flag;
import com.deepwatercreations.minecraftctf.Zone;

public final class MinecraftCTF extends JavaPlugin implements Listener{

	public static String TEAM_KEY = "CTF_TEAM";

	public int currentParticleIdx = 0;
	public Objective scoreObjective;

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
			//TODO: Let this possibly be a server command (needs to find a location without relying on player)
			//TODO: Break this up into methods or something
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
				this.scoreObjective = board.registerNewObjective("score", "dummy", "Score");
				this.scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				this.scoreObjective.setRenderType(RenderType.INTEGER);

				Location playerLoc = player.getLocation();
				Location teamABaseLoc = playerLoc.clone().add(10, 0, 0);
				Location teamBBaseLoc = playerLoc.clone().add(-10, 0, 0);
				int teamZoneRadius = 3;
				Team teamA = createTeam(board, "Zigzags", ChatColor.BLUE, teamABaseLoc, teamZoneRadius);
				Team teamB = createTeam(board, "Curlicues", ChatColor.RED, teamBBaseLoc, teamZoneRadius);
				//TODO: Pick an appropriate height to spawn both flags at given the ground levels
				//	at the two locations.

				//Prompt players to register a team //TODO: Actually just randomize it for now, but they should get the option to choose
				List<Player> players = player.getWorld().getPlayers();
				Random rng = new Random();
				List<Team> teamList = new ArrayList<Team>(board.getTeams());
				for(Player p : players){
					Team team = teamList.get(rng.nextInt(teamList.size()));
					team.addEntry(p.getName());
					p.sendRawMessage("You've been assigned to team " + team.getDisplayName());
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
		} else if(cmd.getName().equalsIgnoreCase("respawnFlags")){
			//TODO: Make this something players can vote on. If both teams agree, do it.
			for(Flag flag : Flag.flagList){
				flag.respawn();
			}
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
					Team playerTeam = getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
					String playerTeamId = playerTeam.getName();
					String flagTeamId = flag.team.getName();
					//Check if it's their own flag and if they have an enemy flag in their inventory
					if(playerTeamId == flagTeamId && checkInventoryForEnemyFlag(player)){
						Score teamScore = this.scoreObjective.getScore(playerTeam.getName());
						List<ItemStack> carriedFlags = Flag.getFlagItemsFromInventory(player.getInventory());
						for(ItemStack carriedFlag : carriedFlags){
							teamScore.setScore(teamScore.getScore() + 1);
							NBTItem nbtitem = new NBTItem(carriedFlag);
							String teamName = nbtitem.getString(MinecraftCTF.TEAM_KEY);
							Flag.getFlagForTeamName(teamName).respawn();
							//TODO: Be very careful and check that the player isn't somehow
							//carrying the same flag they're also standing on?
							//It sounds stupid, but I'm wary of server glitches.
							//Then again, although it shouldn't award a point, it *should* respawn.
						}
						//TODO: Check for win (maybe even emit an event? Only if I have a genuine use for it - YNGNI)
					}
				   }
			}
		}
	}

	//TODO: Can I just get rid of this dumb function entirely?
	private boolean checkInventoryForEnemyFlag(Player player){
		Inventory inv = player.getInventory();
		String playerTeamId = getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName()).getName(); //TODO: Be a little careful and make sure we aren't checking players without teams
		for(ItemStack item : inv){
			if(item != null){
				NBTItem nbtitem = new NBTItem(item);
				//If I wanted to be safer, I could use a try/catch to make sure the value under the key
				//	is actually a string, but I think that's overkill for planned usage
				if(nbtitem.hasKey(MinecraftCTF.TEAM_KEY) && !nbtitem.getString(MinecraftCTF.TEAM_KEY).equals(playerTeamId)){
					return true;
				}
			}
		}
		return false;
	}

	public Team createTeam(Scoreboard board, String name, ChatColor color, Location teamBaseLoc, int teamZoneRadius){
		Team team = board.registerNewTeam(name);
		team.setColor(color);
		this.scoreObjective.getScore(team.getName()).setScore(0);
		new Flag(this, teamBaseLoc, getBannerForColor(color), team);
		new Zone(teamBaseLoc.getWorld(), teamBaseLoc, teamZoneRadius).runTaskTimer(this, 0, 1);

		return team;
	}

	public Material getBannerForColor(ChatColor color) throws IllegalArgumentException{
		switch(color){
			case AQUA:
				return Material.LIGHT_BLUE_BANNER;
			case BLACK:
				return Material.BLACK_BANNER;
			case BLUE:
				return Material.BLUE_BANNER;
			case DARK_AQUA:
				return Material.CYAN_BANNER;
			// case DARK_BLUE:
			// 	return Material._BANNER;
			case DARK_GRAY:
				return Material.GRAY_BANNER;
			case DARK_GREEN:
				return Material.GREEN_BANNER;
			case DARK_PURPLE:
				return Material.PURPLE_BANNER;
			// case DARK_RED:
			// 	return Material.%_BANNER;
			case GOLD:
				return Material.ORANGE_BANNER;
			case GRAY:
				return Material.LIGHT_GRAY_BANNER;
			case GREEN:
				return Material.LIME_BANNER;
			case LIGHT_PURPLE:
				return Material.MAGENTA_BANNER;
			case RED:
				return Material.RED_BANNER;
			case WHITE:
				return Material.WHITE_BANNER;
			case YELLOW:
				return Material.YELLOW_BANNER;
			default:
				throw new IllegalArgumentException(String.format("No matching banner for color %s", color.toString()));
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
	//Zones as barriers for first x minutes
	//Flags behave appropriately in zones
	//Recall/b command?
	//Turn off advancements (maybe just hide them from the other team?)
	//Sound effects on capture, pickup, etc
	//	Also particle effects
	//Compasses to find chunk with enemy flag
	//	Compasses wobble to mask flag movement across chunk boundaries?
	//Make flags visible through walls to owning team(?)
	//Team Whisper (/tw) command to chat with an entire team
	//	Make sure this doesn't already exist
	//Track extra stuff like kills, deaths, captures, etc?
}
