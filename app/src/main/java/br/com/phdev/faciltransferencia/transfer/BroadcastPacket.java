package br.com.phdev.faciltransferencia.transfer;

import java.io.Serializable;

/*
 * Copyright (C) 2018 Paulo Henrique Gonçalves Bacelar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class BroadcastPacket implements Serializable {

    private int currentVersionId;
    private String alias;

    public BroadcastPacket(int currentVersionId, String alias) {
        this.currentVersionId = currentVersionId;
        this.alias = alias;
    }

    public int getCurrentVersionId() {
        return currentVersionId;
    }

    public void setCurrentVersionId(int currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

}