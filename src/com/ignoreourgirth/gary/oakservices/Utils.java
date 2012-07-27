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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ignoreourgirth.gary.oakcorelib.NPCUtils;
import com.ignoreourgirth.gary.oakcorelib.OakCoreLib;
import com.ignoreourgirth.gary.oakcorelib.StringFormats;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Utils {

	protected static Logger log;  
	
	public static String formatItemName(String itemName) {
		if (itemName.endsWith("LEGS")) itemName.replace("LEGS", "LEGGINGS");
		if (itemName.endsWith("PANTS")) itemName.replace("PANTS", "LEGGINGS");
		if (itemName.endsWith("LEGINGS")) itemName.replace("LEGINGS", "LEGGINGS");
		if (itemName.endsWith("BOOT")) itemName.replace("BOOT", "BOOTS");
		if (itemName.endsWith("HELM")) itemName.replace("HELM", "HELMET");
		return itemName;
	}
	
	public static int getFinalCost(ItemStack stack) {
		int baseCost = getBaseCost(stack.getType().toString());
		int recalculatedCost = baseCost;
		for (Map.Entry<Enchantment, Integer> enchantment : stack.getEnchantments().entrySet()) {
			recalculatedCost += baseCost * Math.pow(1.73, enchantment.getValue());
			if (enchantment.getKey().equals(Enchantment.SILK_TOUCH)) recalculatedCost += 15000;
			if (enchantment.getKey().equals(Enchantment.ARROW_INFINITE)) recalculatedCost += 70000;	
		}
		return recalculatedCost;
	}
	
	public static int getBaseCost(String itemName) {
		int returnValue = -1;
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT Money FROM oakbank_blacksmithitems WHERE ItemName=?;");
			statement.setString(1, itemName);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				returnValue = result.getInt(1);
			}
			result.close();
			statement.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage());
		}
		return returnValue;
	}
	
	public static String[] getcustomData(String itemName) {
		String[] returnValue = new String[] {null, null, null};
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT customItem, customEnchantNames, customEnchantLevels  FROM oakbank_blacksmithitems WHERE ItemName=?;");
			statement.setString(1, itemName);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				returnValue = new String[] {result.getString(1), result.getString(2), result.getString(3)};
			}
			if (returnValue[1] == "") returnValue[1] = null;
			result.close();
			statement.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage());
		}
		return returnValue;
	}
	
	public static int getRepairTime(String itemName) {
		int returnValue = -1;
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT TimeInMinutes FROM oakbank_blacksmithitems WHERE ItemName=?;");
			statement.setString(1, itemName);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				returnValue = result.getInt(1);
			}
			result.close();
			statement.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage());
		}
		return returnValue;
	}
	
	public static int getRepairPrice(Player player, boolean showBill, boolean showErrors) {
		if(player.getItemInHand().getType() == Material.AIR) {
			if (showErrors) NPCUtils.sayAsNPC(player, "Blacksmith", "Please show us what you would like to repair.");
			return -1;
		}
		int repairCost = getBaseCost(player.getItemInHand().getType().toString());
		if (repairCost == -1) {
			if (showErrors) NPCUtils.sayAsNPC(player, "Blacksmith", "Sorry, we don't repair that sort of thing.");
			return -1;
		} else {
			if (showBill) NPCUtils.sayAsNPC(player, "Blacksmith", "We'd charge " + 
					StringFormats.toCurrency(getFinalCost(player.getItemInHand())) + " to repair that.");
			if (showBill) NPCUtils.sayAsNPC(player, "Blacksmith", "Should we get started? (command + confirm)");
			return repairCost;
		}
	}
	
	public static boolean playerInTown(Player player, Town town) {
		TownBlock townBlock = TownyUniverse.getTownBlock(player.getLocation());
		if (townBlock != null) {
			try {
				return townBlock.getTown().getName().equalsIgnoreCase(town.getName());
			} catch (NotRegisteredException ex) {
				log.log(Level.SEVERE, ex.getMessage());
			}
		}
		return false;
	}
	
	public static void createXPAccountIfNull(String playerName) {
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT bankName FROM oakbank_xp WHERE bankName=?;");
			statement.setString(1, OakServices.xpBankPrefix + playerName);
			ResultSet result = statement.executeQuery();
			if (!result.next()) {
				PreparedStatement insertStatement = OakCoreLib.getDB().prepareStatement("INSERT INTO oakbank_xp(bankName,amount) VALUES (?, ?);");
				insertStatement.setString(1, OakServices.xpBankPrefix + playerName);
				insertStatement.setInt(2, 0);
				insertStatement.executeUpdate();
				insertStatement.close();
			}
			result.close();
			statement.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	public static int getXPBalance(String playerName) {
		try {
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("SELECT amount FROM minecraft.oakbank_xp WHERE bankName=?;");
			statement.setString(1, OakServices.xpBankPrefix + playerName);
			ResultSet result = statement.executeQuery();
			result.next();
			int balance = result.getInt(1);
			result.close();
			statement.close();
			return balance;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage());
		}
		return 0;
	}
	
}
