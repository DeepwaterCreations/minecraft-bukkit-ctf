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
	public Scoreboard scoreboard;

	public int teamColorIdx = 0;

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
				scoreboard = getServer().getScoreboardManager().getMainScoreboard();
				
				//First, clear any persistent data
				for(Team t: scoreboard.getTeams()){
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

				Location playerLoc = player.getLocation();
				Location centerLoc = new Location(player.getWorld(), (double)playerLoc.getBlockX(), (double)playerLoc.getBlockY(), (double)playerLoc.getBlockZ());
				Location teamABaseLoc = centerLoc.clone().add(10, 0, 0);
				Location teamBBaseLoc = centerLoc.clone().add(-10, 0, 0);
				int teamZoneRadius = 3;
				Team teamA = createTeam("Zigzags", getValidTeamColors()[teamColorIdx++ % getValidTeamColors().length], teamABaseLoc, teamZoneRadius);
				Team teamB = createTeam("Curlicues", getValidTeamColors()[teamColorIdx++ % getValidTeamColors().length], teamBBaseLoc, teamZoneRadius);
				//TODO: Pick an appropriate height to spawn both flags at given the ground levels
				//	at the two locations.

				//Prompt players to register a team //TODO: Actually just randomize it for now, but they should get the option to choose
				List<Player> players = player.getWorld().getPlayers();
				Random rng = new Random();
				List<Team> teamList = new ArrayList<Team>(scoreboard.getTeams());
				for(Player p : players){
					Team team = teamList.get(rng.nextInt(teamList.size()));
					team.addEntry(p.getName());
					p.sendRawMessage("You've been assigned to team " + teamColoredText(team, team.getDisplayName()));
				}
				return true;

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
			return true;
		} else if(cmd.getName().equalsIgnoreCase("respawnFlags")){
			//TODO: Make this something players can vote on. If both teams agree, do it.
			for(Flag flag : Flag.flagList){
				flag.respawn();
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("teamlist")){
			if(scoreboard == null){
				sender.sendMessage("Game isn't initialized yet");
			} else if (scoreboard.getTeams().size() == 0){
				sender.sendMessage("There are no teams!");
			} else {
				sender.sendMessage("Teams:");
				for(Team team : scoreboard.getTeams()){
					String teamNameString = teamColoredText(team, team.getDisplayName());
					//If the display name doesn't match the regular name, we also want
					//to show the player the regular name so they can correctly refer to
					//the team in commands.
					if(!team.getDisplayName().equals(team.getName())){
						teamNameString += " (" + team.getName() + " )";
					}
					sender.sendMessage(teamNameString);
				}
			}
			return true;
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
						Score teamScore = this.scoreObjective.getScore(getTeamScoreName(playerTeam));
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

	public Team createTeam(String name, ChatColor color, Location teamBaseLoc, int teamZoneRadius){
		Team team = scoreboard.registerNewTeam(name);
		team.setColor(color);
		this.scoreObjective.getScore(getTeamScoreName(team)).setScore(0);
		new Flag(this, teamBaseLoc, getBannerForColor(color), team);
		new Zone(teamBaseLoc, teamZoneRadius, Zone.getParticleColorFromChatColor(team.getColor())).runTaskTimer(this, 0, 2);

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

	public ChatColor[] getValidTeamColors(){
		ChatColor[] validTeamColors = {ChatColor.AQUA, ChatColor.BLACK, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_GRAY, ChatColor.DARK_GREEN,
			ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED, 
			ChatColor.WHITE, ChatColor.YELLOW};
		return validTeamColors;
	}

	public String teamColoredText(Team team, String str){
		return team.getColor() + str;
	}

	public String getTeamScoreName(Team team){
		return team.getColor() + team.getName();
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
	//Flag can't go through nether/end portals
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
