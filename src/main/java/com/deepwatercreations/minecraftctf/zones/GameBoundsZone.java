package com.deepwatercreations.minecraftctf.zones;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.deepwatercreations.minecraftctf.Flag;
import com.deepwatercreations.minecraftctf.MinecraftCTF;
import com.deepwatercreations.minecraftctf.zones.Zone;

public class GameBoundsZone extends Zone implements Listener{

	public GameBoundsZone(Location center, int radius, MinecraftCTF plugin){
		super(plugin, center, radius, Color.MAROON);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		//We don't want players to be able to stand out of bounds, pick up nearby
		//flag items, and run away with them. So we don't care whether they start oob.
		//Just whether they end up there.
		Location to = event.getTo();
		if(!this.isInBounds(to)){
			Location from = event.getFrom();
			Player player = event.getPlayer();
			Inventory playerInv = player.getInventory();
			//check their inventory for flag items and force them to drop:
			List<ItemStack> flags = Flag.getFlagItemsFromInventory(playerInv);
			//How do we do this?
			//I think that first we remove them from the player's inventory,
			//second we have the world drop them. 
			Location dropLoc;
			if(this.isInBounds(from)){
				dropLoc = from.clone();
			} else {
				dropLoc = this.getClosestInBounds(to);
			}
			for(ItemStack flag : flags){
				playerInv.remove(flag);
				player.getWorld().dropItem(dropLoc, flag);
			}
		}
	}
}
