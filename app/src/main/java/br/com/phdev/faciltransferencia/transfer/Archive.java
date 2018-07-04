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
public class Archive {

    public enum FILE_TYPE {VIDEO, IMAGE, MUSIC, DOCUMENT, UNDEFINED}

    private String name;
    private String path;
    private String masterPath;
    private String localPath;
    private int statusTransfer = 0;
    private byte[] bytes;

    public Archive() {
        this.masterPath = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void setStatusTranfer(int status) {
        this.statusTransfer = status;
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

    public static FILE_TYPE checkFileExtension(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif"))
            return FILE_TYPE.IMAGE;
        else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".3gp") || fileName.endsWith(".mkv"))
            return FILE_TYPE.VIDEO;
        else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".midi"))
            return FILE_TYPE.MUSIC;
        else if (fileName.endsWith(".doc") || fileName.endsWith(".txt") || fileName.endsWith(".docx") || fileName.endsWith(".pdf"))
            return FILE_TYPE.DOCUMENT;
        else
            return FILE_TYPE.UNDEFINED;
    }

    @Override
    public String toString() {
        switch (statusTransfer) {
            case 0:
                return "AGUARDANDO PARA ENVIAR! | " + this.name;
            case 1:
                return "ENVIANDO... | " + this.name;
            case 2:
                return "ENVIO COMPLETO! | " + this.name;
            default:
                return "Erro no arquivo";
        }
    }

}