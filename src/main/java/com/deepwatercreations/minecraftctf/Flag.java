package com.deepwatercreations.minecraftctf;

import de.tr7zw.changeme.nbtapi.NBTItem;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.metadata.FixedMetadataValue;

import com.deepwatercreations.minecraftctf.MinecraftCTF;
import com.deepwatercreations.minecraftctf.Team;

public class Flag implements Listener{

	public static List<Flag> flagList = new ArrayList<Flag>();

	Block block;
	Block spawnBlock;
	Material bannerType;
	ItemStack item;
	Team team;

	public Flag(MinecraftCTF plugin, Location initLoc, Material bannerType, Team team){
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
		//We need to use an external library to set NBT data
		//or else two flags with the same name will count as the same flag.
		NBTItem nbtitem = new NBTItem(flagItem);
		nbtitem.setString(MinecraftCTF.TEAM_KEY, team.toString()); //TODO: I'd really rather have this be an int
									   //TODO: Make sure there aren't problems caused by
									   //	   using this same key for a non-flag
		flagItem = nbtitem.getItem();

		this.item = flagItem;
		this.team = team;

		Flag.flagList.add(this);
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

	//TODO:
	//Break portal frame block? How do I inform the players this is possible?
	//Check if flag is destroyed from: Despawn, BlockBurnedEvent(?), maybe just
	//	periodically.
	//Different placement and pickup behavior by team? Or, zone-aware?
}

