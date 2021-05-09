package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftCTF extends JavaPlugin{

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
			return true;
		}
		

		//If the command isn't valid, return false so that help is displayed
		return false;
	}
}
