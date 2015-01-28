/*
 * This file is part of MyPet-NPC
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet-NPC is licensed under the GNU Lesser General Public License.
 *
 * MyPet-NPC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet-NPC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.keyle.mypet.npc.traits;

import de.Keyle.MyPet.util.hooks.Economy;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MyPetWalletTrait extends Trait {
    @Persist
    double privateWallet = 0.0D;
    @Persist
    String walletTypeName = WalletType.Private.name();
    public WalletType walletType = WalletType.Private;
    @Persist
    public String accountName = "";

    public static enum WalletType {
        Private, Owner, Bank, None;

        public static WalletType getByName(String name) {
            for (WalletType walletType : values()) {
                if (walletType.name().equalsIgnoreCase(name)) {
                    return walletType;
                }
            }
            return null;
        }
    }

    public MyPetWalletTrait() {
        super("mypet-wallet");
    }

    public void setWalletType(WalletType newType) {
        walletType = newType;
        walletTypeName = newType.name();
    }

    public void setAccount(String accountName) {
        this.accountName = accountName;
    }

    public boolean deposit(double amount) {
        if (amount <= 0.0D) {
            return false;
        }
        switch (walletType) {
            case Private:
                this.privateWallet += amount;
                return true;
            case Owner:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Owner\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId()), amount).transactionSuccess();
            case Bank:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Bank\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().isBankOwner(accountName, Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId())).transactionSuccess() && Economy.getEconomy().bankDeposit(accountName, amount).transactionSuccess();
            case None:
                return true;
        }
        return false;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0.0D) {
            return false;
        }

        switch (walletType) {
            case Private:
                if (amount > this.privateWallet) {
                    return false;
                }
                this.privateWallet -= amount;
                return true;
            case Owner:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Owner\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId()), amount).transactionSuccess();
            case Bank:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Bank\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().isBankOwner(accountName, Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId())).transactionSuccess() && Economy.getEconomy().bankWithdraw(accountName, amount).transactionSuccess();
            case None:
                return true;
        }
        return false;
    }

    public boolean has(double amount) {
        if (amount <= 0.0D) {
            return false;
        }
        switch (walletType) {
            case Private:
                return this.privateWallet >= amount;
            case Owner:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Owner\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().has(Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId()), amount);
            case Bank:
                if (!Economy.canUseEconomy()) {
                    MyPetLogger.write(ChatColor.RED + "The MyPet-Wallet trait needs an economy plugin to use the \"Bank\" wallet type! (NPC: " + this.getNPC().getId() + ")", "MyPet-NPC");
                    return false;
                }
                return Economy.getEconomy().isBankOwner(accountName, Bukkit.getOfflinePlayer(this.npc.getTrait(Owner.class).getOwnerId())).transactionSuccess() && Economy.getEconomy().bankHas(accountName, amount).transactionSuccess();
            case None:
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MyPetWalletTrait{walletType: " + walletTypeName + ", privateWallet: " + String.format("%1.4f", privateWallet) + ", account: " + accountName + "}";
    }
}