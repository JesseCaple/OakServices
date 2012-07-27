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
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ignoreourgirth.gary.oakcorelib.CommandPreprocessor.OnCommand;
import com.ignoreourgirth.gary.oakcorelib.NPCUtils;
import com.ignoreourgirth.gary.oakcorelib.OakCoreLib;
import com.ignoreourgirth.gary.oakcorelib.StringFormats;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Commands_Banking {

	private static final String[] bankerNames = new String[] {"BankerA", "BankerB"};
	
	@OnCommand ("bank.money.balance")
	public void moneyBalance(Player player) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		long balanceAmmount = Math.round(OakCoreLib.getEconomy().getBalance(OakCoreLib.bankPrefix  + player.getName()));
		player.sendMessage("§eAccount balance: " + StringFormats.toCurrency(balanceAmmount));
	}
	
	@OnCommand (value="bank.money.deposit.all")
	public void moneyDepositAll(Player player) {
		moneyDeposit(player, OakCoreLib.getEconomy().getBalance(player.getName()));
	}
	
	@OnCommand ("bank.money.deposit")
	public void moneyDeposit(Player player, double amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		if (OakCoreLib.getEconomy().getBalance(player.getName()) >= amount) {
			OakCoreLib.getEconomy().bankWithdraw(player.getName(), amount);
			OakCoreLib.getEconomy().bankDeposit(OakCoreLib.bankPrefix + player.getName(), amount);
			player.sendMessage("§eDeposited " + StringFormats.toCurrency(amount) + " into your personal account.");
			player.sendMessage("§eNew Balance: " + StringFormats.toCurrency(OakCoreLib.getEconomy().getBalance(OakCoreLib.bankPrefix + player.getName())) +  ".");
		} else {
			player.sendMessage(Strings.notEnoughMoneyMessage);
		}
	}
	
	@OnCommand (value="bank.money.withdraw.all")
	public void moneyWithdrawAll(Player player) {
		moneyWithdraw(player, OakCoreLib.getEconomy().getBalance(OakCoreLib.bankPrefix + player.getName()));
	}
	
	@OnCommand ("bank.money.withdraw")
	public void moneyWithdraw(Player player, double amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		if (OakCoreLib.getEconomy().getBalance(OakCoreLib.bankPrefix + player.getName()) >= amount) {
			OakCoreLib.getEconomy().bankWithdraw(OakCoreLib.bankPrefix + player.getName(), amount);
			OakCoreLib.getEconomy().bankDeposit(player.getName(), amount);
			player.sendMessage("§eWithdrew " + StringFormats.toCurrency(amount) + " from your personal account.");
		} else {
			player.sendMessage(Strings.notEnoughMoneyInBankMessage);
		}
	}
	
	@OnCommand (value="bank.money.transfer", labels="PlayerName")
	public void moneyTransfer(Player player, String toName, double amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		double playerBalance = OakCoreLib.getEconomy().getBalance(OakCoreLib.bankPrefix + player.getName());
		if (playerBalance < amount) {
			amount = playerBalance;
		}
		Player toPlayer = Bukkit.getServer().getPlayer(toName);
		String toPlayerName = null;
		if (toPlayer != null) {
			toPlayerName = toPlayer.getName();
		} else {
			OfflinePlayer toOfflinePlayer = Bukkit.getServer().getOfflinePlayer(toName);
			if (toOfflinePlayer != null && toOfflinePlayer.hasPlayedBefore()) {
				toPlayerName = toOfflinePlayer.getName();
			}
		}
		if (toPlayerName != null) {
			if (toPlayerName.equals(player.getName())) {
				player.sendMessage("§4You can not transfer money to yourself, you idiot.");
			} else {
				OakCoreLib.getEconomy().bankWithdraw(OakCoreLib.bankPrefix + player.getName(), amount);
				OakCoreLib.getEconomy().bankDeposit(OakCoreLib.bankPrefix + toPlayerName, amount);
				if (toPlayer != null) {toPlayer.sendMessage("§e" + player.getName() + " wired " + StringFormats.toCurrency(amount) + " to your account.");}
				player.sendMessage("§eTransfered " + StringFormats.toCurrency(amount) + " to " + toPlayerName + ".");
			}
		} else {
			player.sendMessage("§4Player not found.");
		}
	}
	
	@OnCommand ("bank.town.balance")
	public void townBalance(Player player) {
		try {
			Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
			if (!resident.hasTown()) {
				player.sendMessage(Strings.mustHaveATownMessage);
				return;
			} 
			Town town = resident.getTown();
			
			if (!Utils.playerInTown(player, town)) {
				if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeInTownOrAtBankMessage)) return;
			}
			
			int townBalance = (int) town.getHoldingBalance();
			player.sendMessage("§eTown account balance: " + StringFormats.toCurrency(townBalance));
		} catch (TownyException ex) {
			TownyMessaging.sendErrorMsg(player, ex.getMessage());
		} catch (EconomyException ex) {
			TownyMessaging.sendErrorMsg(player, ex.getMessage());
		}
	}
	
	@OnCommand (value="bank.town.deposit.all")
	public void townDepositAll(Player player) {
		townDeposit(player, (int) Math.round(OakCoreLib.getEconomy().getBalance(player.getName())));
	}
	
	@OnCommand ("bank.town.deposit")
	public void townDeposit(Player player, int amount) {
		if (OakCoreLib.getEconomy().getBalance(player.getName()) >= amount) {
			try {
				Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
				if (!resident.hasTown()) {
					player.sendMessage(Strings.mustHaveATownMessage);
					return;
				} 
				
				Town town = resident.getTown();
				
				if (!Utils.playerInTown(player, town)) {
					if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeInTownOrAtBankMessage)) return;
				}

				double bankcap = TownySettings.getTownBankCap();
				if (bankcap > 0) {
					if (amount + town.getHoldingBalance() > bankcap){
						throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
					}	
				}

				if (amount < 0) {throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));}
				if (!resident.payTo(amount, town, "Town Deposit")) {throw new TownyException(TownySettings.getLangString("msg_insuf_funds"));}
				
				TownyMessaging.sendTownMessage(town, String.format(TownySettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, "town"));
				player.sendMessage("§eDeposited " + StringFormats.toCurrency(amount) + " into your town's account.");
			} catch (TownyException ex) {
				TownyMessaging.sendErrorMsg(player, ex.getMessage());
			} catch (EconomyException ex) {
				TownyMessaging.sendErrorMsg(player, ex.getMessage());
			}
		} else {
			player.sendMessage(Strings.notEnoughMoneyMessage);
		}
	}
	
	@OnCommand ("bank.town.withdraw")
	public void townWithdraw(Player player, int amount) {
		try {
			Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
			if (!resident.hasTown()) {
				player.sendMessage(Strings.mustHaveATownMessage);
				return;
			} 
			
			if (!TownySettings.getTownBankAllowWithdrawls()) {throw new TownyException(TownySettings.getLangString("msg_err_withdraw_disabled"));}
			if (amount < 0) {throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));}
			
			Town town = resident.getTown();
			int townBalance = (int) town.getHoldingBalance();
			
			if (!Utils.playerInTown(player, town)) {
				if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeInTownOrAtBankMessage)) return;
			}
			
			if (townBalance >= amount) {		
					town.withdrawFromBank(resident, amount);
					TownyMessaging.sendTownMessage(town, String.format(TownySettings.getLangString("msg_xx_withdrew_xx"), resident.getName(), amount, "town"));
					player.sendMessage("§eWithdrew " + StringFormats.toCurrency(amount) + " from your town's account.");
			} else {
				player.sendMessage(Strings.notEnoughMoneyInTown);
			}
			
		} catch (TownyException ex) {
			TownyMessaging.sendErrorMsg(player, ex.getMessage());
		} catch (EconomyException ex) {
			TownyMessaging.sendErrorMsg(player, ex.getMessage());
		}
	}
	
	@OnCommand ("bank.xp.balance")
	public void xpBalance(Player player) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		Utils.createXPAccountIfNull(player.getName());
		player.sendMessage("§eXP account balance: " + StringFormats.formatInteger(Utils.getXPBalance(player.getName())));
	}
	
	@OnCommand (value="bank.xp.deposit.all")
	public void xpDepositAll(Player player) {
		Utils.createXPAccountIfNull(player.getName());
		xpDeposit(player, OakCoreLib.getXPMP().getTotalXP(player));
	}
	
	@OnCommand ("bank.xp.deposit")
	public void xpDeposit(Player player, int amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		try {
			Utils.createXPAccountIfNull(player.getName());
			if (OakCoreLib.getXPMP().getTotalXP(player) < amount) {
				amount = OakCoreLib.getXPMP().getTotalXP(player);
				player.sendMessage("§eYou don't have that much XP. Depositing all instead.");
			}
			int currentBalance = Utils.getXPBalance(player.getName());
			OakCoreLib.getXPMP().addXP(player, amount * -1);
			PreparedStatement statement = OakCoreLib.getDB().prepareStatement("UPDATE oakbank_xp SET amount=? WHERE bankName=?;");
			statement.setInt(1, currentBalance + amount);
			statement.setString(2, OakServices.xpBankPrefix + player.getName());
			statement.executeUpdate();
			statement.close();
			player.sendMessage("§eDeposited " + StringFormats.formatInteger(amount) + " XP into your account.");
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	@OnCommand (value="bank.xp.withdraw.all")
	public void xpWithdrawAll(Player player) {
		Utils.createXPAccountIfNull(player.getName());
		xpWithdraw(player, Utils.getXPBalance(player.getName()));
	}
	
	@OnCommand ("bank.xp.withdraw")
	public void xpWithdraw(Player player, int amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		try {
			Utils.createXPAccountIfNull(player.getName());
			int currentBalance = Utils.getXPBalance(player.getName());
			if (currentBalance >= amount) {
				PreparedStatement statement = OakCoreLib.getDB().prepareStatement("UPDATE oakbank_xp SET amount=? WHERE bankName=?;");
				statement.setInt(1, currentBalance - amount);
				statement.setString(2, OakServices.xpBankPrefix + player.getName());
				statement.executeUpdate();
				statement.close();
				OakCoreLib.getXPMP().addXP(player, amount);
				player.sendMessage("§eWithdrew " + StringFormats.formatInteger(amount) + " XP from your account.");
			} else {
				player.sendMessage(Strings.notEnoughXPInBankMessage);
			}
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	@OnCommand (value="bank.xp.transfer", labels="PlayerName")
	public void xpTransfer(Player player, String toName, int amount) {
		if (!NPCUtils.isNear(player, bankerNames, Strings.mustBeAtBankerMessage)) return;
		try {
			Utils.createXPAccountIfNull(player.getName());
			int currentBalance = Utils.getXPBalance(player.getName());
			if (currentBalance >= amount) {
				
				Player toPlayer = Bukkit.getServer().getPlayer(toName);
				String toPlayerName = null;
				if (toPlayer != null) {
					toPlayerName = toPlayer.getName();
				} else {
					OfflinePlayer toOfflinePlayer = Bukkit.getServer().getOfflinePlayer(toName);
					if (toOfflinePlayer != null && toOfflinePlayer.hasPlayedBefore()) {
						toPlayerName = toOfflinePlayer.getName();
					}
				}
				if (toPlayerName != null) {
					if (toPlayerName.equals(player.getName())) {
						player.sendMessage("§4You can not transfer xp to yourself, you idiot.");
					} else {
						PreparedStatement statement = OakCoreLib.getDB().prepareStatement("UPDATE oakbank_xp SET amount=? WHERE bankName=?;");
						statement.setInt(1, currentBalance - amount);
						statement.setString(2, OakServices.xpBankPrefix + player.getName());
						statement.executeUpdate();
						statement.close();
						
						Utils.createXPAccountIfNull(toPlayerName);
						int currentBalance2 = Utils.getXPBalance(toPlayerName);
								
						statement = OakCoreLib.getDB().prepareStatement("UPDATE oakbank_xp SET amount=? WHERE bankName=?;");
						statement.setInt(1, currentBalance2 + amount);
						statement.setString(2, OakServices.xpBankPrefix + toPlayerName);
						statement.executeUpdate();
						statement.close();
						
						if (toPlayer != null) {toPlayer.sendMessage("§e" + player.getName() + " wired " + amount + " XP to your account.");}
						player.sendMessage("§eTransfered " + StringFormats.formatInteger(amount) + " XP to " + toPlayerName + ".");
					}
				} else {
					player.sendMessage("§4Player not found.");
				}
			} else {
				player.sendMessage(Strings.notEnoughXPInBankMessage);
			}
		} catch (SQLException ex) {
			Utils.log.log(Level.SEVERE, ex.getMessage());
		}
	}
	
}
