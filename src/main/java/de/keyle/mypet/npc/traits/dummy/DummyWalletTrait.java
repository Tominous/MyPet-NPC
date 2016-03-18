/*
 * This file is part of MyPet-NPC
 *
 * Copyright (C) 2011-2016 Keyle
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

package de.keyle.mypet.npc.traits.dummy;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class DummyWalletTrait extends Trait {
    private double credit = 0.0D;
    private String type = "Private";
    private String account = "";

    public DummyWalletTrait() {
        super("mypet-wallet");
    }

    public void load(DataKey key) throws NPCLoadException {
        type = key.getString("walletTypeName", key.getString("type", "Private"));
        account = key.getString("accountName", key.getString("account", ""));
        credit = key.getDouble("privateWallet", key.getDouble("credit", 0D));
    }

    public void save(DataKey key) {
        if (key.getString("walletTypeName") != null && !key.getString("walletTypeName").isEmpty()) {
            key.removeKey("walletTypeName");
        }
        if (key.getString("accountName") != null && !key.getString("accountName").isEmpty()) {
            key.removeKey("accountName");
        }
        if (key.getString("privateWallet") != null && !key.getString("privateWallet").isEmpty()) {
            key.removeKey("privateWallet");
        }

        key.setString("type", this.type);
        key.setString("account", this.account);
        key.setDouble("credit", this.credit);
    }
}