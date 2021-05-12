package com.deepwatercreations.minecraftctf;

import de.tr7zw.changeme.nbtapi.NBTItem;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Team;

import com.deepwatercreations.minecraftctf.MinecraftCTF;

public class Flag implements Listener{

	public static List<Flag> flagList = new ArrayList<Flag>();

	public static String FLAG_KEY = "CTF_FLAG"; //for setting the flag flag
							//TODO: It occurs to me that I can make the 
							//	existing key "TEAM_FLAG_KEY" and just
							//	assume anything that has it is a flag.

	Block block;
	Block spawnBlock;
	Material bannerType;
	ItemStack item;
	Team team;
	Location initLoc;

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
		this.team = team;
		this.initLoc = initLoc;

		//Make the flag item
		ItemStack flagItem = new ItemStack(this.bannerType);
		ItemMeta meta = flagItem.getItemMeta();
		meta.setDisplayName(this.team.getDisplayName());
		flagItem.setItemMeta(meta);
		//We need to use an external library to set NBT data
		//or else two flags with the same name will count as the same flag.
		NBTItem nbtitem = new NBTItem(flagItem);
		nbtitem.setString(MinecraftCTF.TEAM_KEY, team.getName()); //TODO: Consider using a more specific key name
		nbtitem.setBoolean(Flag.FLAG_KEY, true); //This just means "this is a flag".
		flagItem = nbtitem.getItem();
		this.item = flagItem;

		Flag.flagList.add(this);
	}

	public Location getLocation(){
		if(this.block != null){
			return this.block.getLocation();
		} else {
			return null;
		}
	}

	public void respawn(){
		World world = this.initLoc.getWorld();
		if(this.block != null){
			this.block.setType(Material.AIR);
			//TODO: Check if spawn block still exists and if not, respawn it too
		}
		for(Player player : world.getPlayers()){
			Inventory inv = player.getInventory();
			for(ItemStack item : inv){
				if(item != null && !(item.getType().equals(Material.AIR))){
					NBTItem nbtitem = new NBTItem(item);
					if(nbtitem.hasKey(MinecraftCTF.TEAM_KEY) && nbtitem.getString(MinecraftCTF.TEAM_KEY).equals(this.team.getName())){
						item.setAmount(0);
					}
				}
			}
		}
		for(Item itemEntity : world.getEntitiesByClass(Item.class)){
			ItemStack item = itemEntity.getItemStack();
			if(item != null && !(item.getType().equals(Material.AIR))){
				NBTItem nbtitem = new NBTItem(item);
				if(nbtitem.hasKey(MinecraftCTF.TEAM_KEY) && nbtitem.getString(MinecraftCTF.TEAM_KEY).equals(this.team.getName())){
					item.setAmount(0);
				}
			}
		}
		this.block = this.spawnBlock.getRelative(0,1,0);
		this.block.setType(this.bannerType);
		//TODO: Refactor this biz.
		//TODO: Respawn flags that are in chests and the like
		//TODO: Separate despawn and spawn methods? I might want to spawn flags without
		//	checking for despawn if, frex, one falls into lava.
		//TODO: Check to make sure the enemy flag isn't on the spawn block when respawning
		//	(It'll replace the block, but when the flag is broken, both flags will drop.)
	}

	/* BLOCK EVENTS */

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		//TODO: Make this work for if the block the banner is attached to gets broken also
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
		if(this.item != null && event.getItemInHand().equals(this.item)){
			event.getPlayer().sendMessage("You placed a flag");

			//Set the new block location
			this.block = event.getBlockPlaced();
		}
	}

	/* ITEM ENTITY EVENTS */

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event){
		Item spawnItem = event.getEntity();
		ItemStack spawnItemStack = spawnItem.getItemStack();
		if(this.item != null &&
		   spawnItemStack.isSimilar(this.item) &&
		   Flag.sWhatThisItemStackIs(spawnItemStack)){
			UUID throwerId = spawnItem.getThrower();
			if(throwerId != null){
				Entity thrower = spawnItem.getServer().getEntity(throwerId);
				thrower.sendMessage("You chucked a flag down");
			}
		}

	}

	//Stops flag item entities from despawning after 5 minutes
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event){
		Item spawnItem = event.getEntity();
		ItemStack spawnItemStack = spawnItem.getItemStack();
		if(this.item != null &&
		   spawnItemStack.isSimilar(this.item) && 
		   Flag.sWhatThisItemStackIs(spawnItemStack)){
			event.setCancelled(true); //Item exists for 5 more minutes
		}
	}

	//Respawns flag when it's destroyed
	@EventHandler
	public void onItemDamage(EntityDamageEvent event){
		if(event.getEntityType().equals(EntityType.DROPPED_ITEM)){
			   Item damagedItem = (Item) event.getEntity();
			   ItemStack damagedItemStack = damagedItem.getItemStack();
			   if(this.item != null &&
			      damagedItemStack.isSimilar(this.item) && 
			      Flag.sWhatThisItemStackIs(damagedItemStack) &&
			      event.getFinalDamage() > 0 &&
			      !event.isCancelled()){
				   damagedItem.remove();
				   this.respawn(); 
			      }
		}
	}

	public static boolean sWhatThisItemStackIs(ItemStack stack){
		if(stack != null){
			NBTItem nbtitem = new NBTItem(stack);
			return nbtitem.hasKey(Flag.FLAG_KEY);
		} else {
			return false;
		}
	}

	public static List<ItemStack> getFlagItemsFromInventory(Inventory inv){
		List<ItemStack> flags = new ArrayList<ItemStack>();
		for(ItemStack item : inv){
			if(item != null){
				NBTItem nbtitem = new NBTItem(item);
				if(nbtitem.hasKey(Flag.FLAG_KEY)){
					flags.add(item);
				}		
			}
		}
		return flags;
	}

	public static Flag getFlagForTeamName(String teamName){
		for(Flag flag : Flag.flagList){
			String name2 = flag.team.getName();
			if(teamName.equals(name2)){
				return flag;
			}
		}
		return null;
	}

	public static void resetList(){
		for(Flag flag : Flag.flagList){
			flag.respawn();
			if(flag.item != null){
				flag.item = null;
			}
			if(flag.block != null){
				flag.block.setType(Material.AIR);
				flag.block = null;
			}
			if(flag.spawnBlock != null){
				flag.spawnBlock.setType(Material.AIR);
				flag.spawnBlock = null;
			}
		}
		Flag.flagList = new ArrayList<Flag>();
	}

	//TODO:
	//Break portal frame block? How do I inform the players this is possible?
	//Check if flag is destroyed from: Despawn, BlockBurnedEvent(?), maybe just
	//	periodically.
	//Different placement and pickup behavior by team? Or, zone-aware?
}

