/*******************************************************************************
 * Copyright (c) 2012 GaryMthrfkinOak (Jesse Caple).
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.ignoreourgirth.gary.oakservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ignoreourgirth.gary.oakcorelib.CommandPreprocessor.OnCommand;
import com.ignoreourgirth.gary.oakcorelib.NPCUtils;
import com.ignoreourgirth.gary.oakcorelib.OakCoreLib;
import com.ignoreourgirth.gary.oakcorelib.StringFormats;

public class Commands_Blacksmith {
	
	@OnCommand ("test.a")
	public void testA(Player player, int test) {
		Utils.getRepairPrice(player, true, true);
	}
	
	@OnCommand ("test.a")
	public void testA(Player player, int test, int test2) {
		Utils.getRepairPrice(player, true, true);
	}
	
	@OnCommand (value="blacksmith.forge", labels="ItemName")
	public void priceForge(Player player, String itemName) {
		if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
		itemName = Utils.formatItemName(itemName);
		int itemCost = Utils.getBaseCost(itemName);
		if (itemCost > -1) {
			String[] customData = Utils.getcustomData(itemName);
			if (customData[0] == null) {
				itemCost += 3000;
			}
			NPCUtils.sayAsNPC(player, "Blacksmith", "We'd charge " + StringFormats.toCurrency(itemCost) + " to make that." );
			NPCUtils.sayAsNPC(player, "Blacksmith", "Should we get started? (command + confirm)");
		} else {
			NPCUtils.sayAsNPC(player, "Blacksmith", "Sorry, we can't make that for you.");
		}
	}

	@OnCommand ("blacksmith.repair")
	public void priceRepair(Player player) {
		Utils.getRepairPrice(player, true, true);
	}
	
	@OnCommand (value="blacksmith.forge.confirm", labels="ItemName")
	public void forge(Player player, String itemName) {
		try {
			if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
			itemName = Utils.formatItemName(itemName);
			int itemCost = Utils.getBaseCost(itemName);
			if (itemCost > -1) {
				String[] customData = Utils.getcustomData(itemName);
				String dbMaterial = itemName;
				if (customData[0] == null) {
					itemCost += 3000;
				} else {
					dbMaterial = customData[0];
				}
				int forgeTime = Utils.getRepairTime(itemName);
				if (OakCoreLib.getEconomy().getBalance(player.getName()) >= itemCost) {
					OakCoreLib.getEconomy().bankWithdraw(player.getName(), itemCost);
				} else {
					player.sendMessage(Strings.notEnoughMoneyMessage);
					return;
				}

				PreparedStatement insertStatement = OakCoreLib.getDB().prepareStatement("INSERT INTO oakbank_repairqueue(player,itemName,enchantNames,enchantLevels,startedAt) VALUES (?, ?, ?, ?, ?);");
				insertStatement.setString(1, player.getName());
				insertStatement.setString(2, dbMaterial);
				insertStatement.setString(3, customData[1]);
				insertStatement.setString(4, customData[2]);
				insertStatement.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
				insertStatement.executeUpdate();
				insertStatement.close(); 
				
				NPCUtils.sayAsNPC(player, "Blacksmith", "Thank you. It will be about " + forgeTime + " minutes.");
			
			} else {
				NPCUtils.sayAsNPC(player, "Blacksmith", "Sorry, we can't make that for you.");
			}
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	@OnCommand ("blacksmith.repair.confirm")
	public void repair(Player player) {
		try {
			if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
			int basePrice = Utils.getRepairPrice(player, true, false);
			if (basePrice > -1) {
				int price = Utils.getFinalCost(player.getItemInHand());
				int repairTime = Utils.getRepairTime(player.getItemInHand().getType().toString());
				if (OakCoreLib.getEconomy().getBalance(player.getName()) >= price) {
					OakCoreLib.getEconomy().bankWithdraw(player.getName(), price);
				} else {
					player.sendMessage(Strings.notEnoughMoneyMessage);
					return;
				}
				int index = 0;
				String dbItemName = player.getItemInHand().getType().toString().toUpperCase();
				Set<Entry<Enchantment, Integer>> enchantments = player.getItemInHand().getEnchantments().entrySet();
				StringBuilder dbEnchantmentsLine = new StringBuilder();
				StringBuilder dbEnchantmentLevelsLine = new StringBuilder();
				for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
					if (index > 0) {
						dbEnchantmentsLine.append(",");
						dbEnchantmentLevelsLine.append(",");
					}
					dbEnchantmentsLine.append(enchantment.getKey().getName().toString());
					dbEnchantmentLevelsLine.append(enchantment.getValue().toString());
					index ++;
				}
				PreparedStatement insertStatement = OakCoreLib.getDB().prepareStatement("INSERT INTO oakbank_repairqueue(player,itemName,enchantNames,enchantLevels,startedAt) VALUES (?, ?, ?, ?, ?);");
				insertStatement.setString(1, player.getName());
				insertStatement.setString(2, dbItemName);
				insertStatement.setString(3, dbEnchantmentsLine.toString());
				insertStatement.setString(4, dbEnchantmentLevelsLine.toString());
				insertStatement.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
				insertStatement.executeUpdate();
				insertStatement.close();
				player.setItemInHand(new ItemStack(Material.AIR));
				NPCUtils.sayAsNPC(player, "Blacksmith", "Thank you. It will be about " + repairTime+ " minutes.");
			}
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	@OnCommand ("blacksmith.pickup")
	public void pickUp(Player player) {
		if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT id, startedAt, itemName, enchantNames, enchantLevels FROM oakbank_repairqueue WHERE player=?;");
			statement.setString(1, player.getName());
			ResultSet result = statement.executeQuery();
			int itemsNotReady = 0;
			int itemsGiven = 0;
			int itemsReadyButNoInv = 0;
			while (result.next()) {
				int rowID = result.getInt(1);
				Calendar readyAt = Calendar.getInstance();
				readyAt.setTime(result.getTimestamp(2));
				readyAt.add(Calendar.MINUTE, Utils.getRepairTime(result.getString(3)));
				boolean itemReady = (((int) ((readyAt.getTime().getTime() -  new java.util.Date().getTime()) / 1000)) < 1);
				if (itemReady) {
					int emptySlotID = player.getInventory().firstEmpty();
					if (emptySlotID > -1) {
						ItemStack blacksmithItem = new ItemStack(Material.getMaterial(result.getString(3)));
						String dbEnchantName = result.getString(4);
						if (dbEnchantName != null) {
							if (dbEnchantName.length() > 0) {
								if (!dbEnchantName.contains(",")) {
									blacksmithItem.addUnsafeEnchantment(Enchantment.getByName(result.getString(4)), Integer.parseInt(result.getString(5)));
								} else {
									String[] enchantNames = result.getString(4).split(",");
									String[] enchantLevels = result.getString(5).split(",");
									for(int i=0; i < enchantNames.length; i++) {
										blacksmithItem.addUnsafeEnchantment(Enchantment.getByName(enchantNames[i]), Integer.parseInt(enchantLevels[i]));
									}
								}
							}
						}
						PreparedStatement statement2 = OakCoreLib.getDB().prepareStatement("DELETE FROM oakbank_repairqueue WHERE id=?");
						statement2.setInt(1, rowID);
						statement2.executeUpdate();
						statement2.close();
						player.getInventory().addItem(blacksmithItem);
						itemsGiven ++;
					} else {
						itemsReadyButNoInv ++;
					}
				} else {
					itemsNotReady ++;
				}
			}
			result.close();
			statement.close();
			if (itemsGiven > 0 && itemsNotReady == 0) {
				NPCUtils.sayAsNPC(player, "Blacksmith", "The work order is finished.");
				NPCUtils.sayAsNPC(player, "Blacksmith", "Thank you and please come again.");
			} else if (itemsGiven > 0 && itemsNotReady > 0) {
				NPCUtils.sayAsNPC(player, "Blacksmith", "The work order is partially finished.");
				NPCUtils.sayAsNPC(player, "Blacksmith", "Please check back in a bit for the rest.");
			} else if (itemsGiven == 0 && itemsNotReady == 0 && itemsNotReady == 0) {
				NPCUtils.sayAsNPC(player, "Blacksmith", "You don't have a pending work order.");
				NPCUtils.sayAsNPC(player, "Blacksmith", "If you would like us to start one, let us know.");
			} else if (itemsNotReady > 0) {
				NPCUtils.sayAsNPC(player, "Blacksmith", "Sorry, it's not done yet.");
			}
			if (itemsReadyButNoInv > 0) {
				NPCUtils.sayAsNPC(player, "Blacksmith", "Please clear space in your inventory.");
			}
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	@OnCommand ("blacksmith.junk")
	public void clearInventoryInfo(Player player) {
		if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
		NPCUtils.sayAsNPC(player, "Blacksmith", "Clear entire inventory? (command + confirm).");
	}
	
	@OnCommand ("blacksmith.junk.confirm")
	public void clearInventory(Player player) {
		if (!NPCUtils.isNear(player, "Blacksmith", Strings.mustBeAtBlacksmithMessage)) return;
		player.getInventory().clear();
		NPCUtils.sayAsNPC(player, "Blacksmith", "Thank you for your scraps donation.");
	}
	
}
