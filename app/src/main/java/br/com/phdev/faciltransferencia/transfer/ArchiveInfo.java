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
public class ArchiveInfo implements Serializable{

    private String archiveName;
    private String masterPath;
    private String localPath;
    private long archiveLength;
    private int fragmentsAmount;
    private int fragmentLength;
    private int lastFragmentLength;

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public long getArchiveLength() {
        return archiveLength;
    }

    public void setArchiveLength(long archiveLength) {
        this.archiveLength = archiveLength;
    }

    public int getFragmentsAmount() {
        return fragmentsAmount;
    }

    public void setFragmentsAmount(int fragmentsAmount) {
        this.fragmentsAmount = fragmentsAmount;
    }

    public int getFragmentLength() {
        return fragmentLength;
    }

    public void setFragmentLength(int fragmentLength) {
        this.fragmentLength = fragmentLength;
    }

    public int getLastFragmentLength() {
        return lastFragmentLength;
    }

    public void setLastFragmentLength(int lastFragmentLength) {
        this.lastFragmentLength = lastFragmentLength;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getMasterPath() {
        return masterPath;
    }

    public void setMasterPath(String masterPath) {
        this.masterPath = masterPath;
    }

}

