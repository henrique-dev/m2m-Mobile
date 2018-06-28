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
public class Archive implements Serializable {

    private String name;
    private String path;
    private int statusTransfer = 0;
    private byte[] bytes;

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