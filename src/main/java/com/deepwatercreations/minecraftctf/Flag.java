package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Flag implements Listener{

	Block block;
	Block spawnBlock;
	Material bannerType;
	ItemStack item;

	public Flag(MinecraftCTF plugin, Location initLoc, Material bannerType, int team){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.block = initLoc.getBlock();
		this.block.setType(bannerType);

		this.spawnBlock = initLoc.clone().subtract(0,1,0).getBlock();
		this.spawnBlock.setType(Material.END_PORTAL_FRAME);
		EndPortalFrame blockData = (EndPortalFrame) Material.END_PORTAL_FRAME.createBlockData();
		blockData.setEye(true);
		this.spawnBlock.setBlockData(blockData);

		this.bannerType = bannerType;

		//Make the flag item
		ItemStack flagItem = new ItemStack(this.bannerType);
		ItemMeta meta = flagItem.getItemMeta();
		meta.setDisplayName("Team " + "UNDEFINED" + " Flag");
		flagItem.setItemMeta(meta);
		this.item = flagItem;
	}

	public Location getLocation(){
		if(this.block != null){
			return this.block.getLocation();
		} else {
			return null;
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		Block broken = event.getBlock();
		if(this.getLocation() != null &&
		   broken.getX() == this.block.getX() &&
		   broken.getY() == this.block.getY() &&
		   broken.getZ() == this.block.getZ()){
			event.getPlayer().sendMessage("You broke a flag");
			
			//Stop this event from dropping a natural flag
			event.setDropItems(false);

			//Drop the custom flag right onto the player's location to make it
			//a little harder to build stuff that makes it impossible to pick
			//up the flag: //TODO: don't do this outside of team zones?
			Player player = event.getPlayer();
			World world = player.getWorld();
			world.dropItem(player.getLocation(), this.item);
			
			//Set the block to null, since the flag's not there anymore:
			this.block = null;
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(event.getItemInHand().equals(this.item)){
			event.getPlayer().sendMessage("You placed a flag");

			//Set the new block location
			this.block = event.getBlockPlaced();
		}
	}

}

