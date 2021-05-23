package com.deepwatercreations.minecraftctf;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
// import org.bukkit.scoreboard.Team;

public class CTFCommandExecutor implements TabExecutor{

	public MinecraftCTF plugin;
	public CTFCommandExecutor(MinecraftCTF plugin){
		this.plugin = plugin;
	}

	 @Override
	 public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("respawnFlags")){
			//TODO: Make this something players can vote on. If both teams agree, do it.
			for(Flag flag : Flag.flagsByTeamname.values()){
				flag.respawn();
			}
			return true;
		//TODO: Add team command handler
		//	...which will let me have it autocomplete team names for one thing
		} else if(cmd.getName().equalsIgnoreCase("teamlist")){
			if(plugin.scoreboard == null){
				sender.sendMessage("Game isn't initialized yet");
			} else if (CTFTeam.teamDict.isEmpty() || plugin.scoreboard.getTeams().size() == 0){
				sender.sendMessage("There are no teams!");
			} else {
				sender.sendMessage("Teams:");
				// for(Team team : plugin.scoreboard.getTeams()){
				for(CTFTeam team : CTFTeam.teamDict.values()){
					// String teamNameString = teamColoredText(team, team.getDisplayName());
					//If the display name doesn't match the regular name, we also want
					//to show the player the regular name so they can correctly refer to
					//the team in commands.
					// if(!team.getDisplayName().equals(team.getName())){
					// 	teamNameString += " (" + team.getName() + " )";
					// }
					sender.sendMessage(team.scoreName);
				}
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("teamjoin")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				if(args.length > 0){
					if(plugin.scoreboard == null){
						sender.sendMessage("Game isn't initialized yet");
					} else if (CTFTeam.teamDict.isEmpty() || plugin.scoreboard.getTeams().size() == 0){
						sender.sendMessage("There are no teams!");
					//TODO: Check if they're already on another team
					} else{
						String playerChoice = args[0];
						for(String teamname : CTFTeam.teamDict.keySet()){
							if(teamname.toLowerCase().startsWith(playerChoice.toLowerCase())){
								CTFTeam team = CTFTeam.teamDict.get(teamname);
								team.addPlayer(player);
								plugin.getServer().broadcastMessage(String.format("%s has joined team %s", player.getName(), team.scoreName));
								return true;
							}
						}
					}
				} else {
					player.sendMessage("Usage: /teamjoin [team name]");
				}
				player.sendMessage("Type '/teamlist' to see the list of teams.");
				return true;
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

	 @Override
	 public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		//TODO: This
		return null;
	 }

	 public static String teamColoredText(CTFTeam team, String str){
		 return team.color + str;
	 }
}
