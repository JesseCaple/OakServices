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

import org.bukkit.plugin.java.JavaPlugin;

import com.ignoreourgirth.gary.oakcorelib.CommandPreprocessor;

public class OakServices extends JavaPlugin  {
	
	protected static final String xpBankPrefix = "!player_";
	
	public void onEnable() {
		Utils.log = getLogger();
		CommandPreprocessor.addExecutor(new Commands_Banking());
		CommandPreprocessor.addExecutor(new Commands_Blacksmith());
	}
	
	public void onDisable() {}
	
}
