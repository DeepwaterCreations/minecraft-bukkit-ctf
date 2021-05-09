package com.deepwatercreations.minecraftctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
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
		return this.block.getLocation();
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		Block broken = event.getBlock();
		if(broken.getX() == this.block.getX() &&
		   broken.getY() == this.block.getY() &&
		   broken.getZ() == this.block.getZ()){
			event.getPlayer().sendMessage("You broke a flag");
		}
	}

}

