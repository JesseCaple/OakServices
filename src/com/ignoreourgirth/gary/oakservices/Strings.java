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

import org.bukkit.ChatColor;

public class Strings {

	protected static final String notEnoughMoneyMessage = ChatColor.RED + "You lack the funds necessary to complete that action.";
	protected static final String notEnoughMoneyInBankMessage = ChatColor.RED + "Your account lacks the funds needed to complete that action.";
	protected static final String notEnoughXPInBankMessage = ChatColor.RED + "Your account lacks the XP needed to complete that action.";
	protected static final String notEnoughMoneyInTown = ChatColor.RED + "Your town lacks the funds needed to complete that action.";
	protected static final String mustHaveATownMessage = ChatColor.RED + "You must belong to a town in order to use this command.";
	protected static final String mustBeInTownOrAtBankMessage = ChatColor.RED + "You must be in front of a Banker or in your town.";
	protected static final String mustBeAtBlacksmithMessage = ChatColor.RED + "You must be in front of a Blacksmith.";
	protected static final String mustBeAtBankerMessage = ChatColor.RED + "You must be in front of a Banker.";
}
