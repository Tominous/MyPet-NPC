/*
 * This file is part of MyPet-NPC
 *
 * Copyright © 2011-2018 Keyle
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

public class DummyShopTrait extends Trait {
    private String shop = null;

    public DummyShopTrait() {
        super("mypet-shop");
    }

    public void load(DataKey key) throws NPCLoadException {
        shop = key.getString("shop", null);
    }

    public void save(DataKey key) {
        key.setString("shop", this.shop);
    }
}