package br.com.phdev.faciltransferencia.managers;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;

import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.connection.BroadcastSender;
import br.com.phdev.faciltransferencia.connection.interfaces.OnReadListener;
import br.com.phdev.faciltransferencia.connection.TCPServer;
import br.com.phdev.faciltransferencia.connection.interfaces.WriteListener;
import br.com.phdev.faciltransferencia.transfer.ArchiveInfo;
import br.com.phdev.faciltransferencia.transfer.FragmentArchive;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnObjectReceivedListener;

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
public class ConnectionManager implements OnReadListener{

    private BroadcastSender broadcastSender;
    private TCPServer TCPServer;
    private OnObjectReceivedListener onObjectReceivedListener;

    ConnectionManager(MainActivity context, OnObjectReceivedListener onObjectReceivedListener) {
        this.onObjectReceivedListener = onObjectReceivedListener;
        this.broadcastSender = new BroadcastSender();
        this.TCPServer = new TCPServer(context, this.broadcastSender, this);
    }

    public void startBroadcastSender(String userName) {
        this.broadcastSender.setAlias(userName);
        this.broadcastSender.start();
    }

    public void startTCPServer() {
        this.TCPServer.start();
    }

    public void close() {
        this.TCPServer.close();
        this.broadcastSender.close();
        this.onObjectReceivedListener = null;
    }

    public WriteListener getWriteListener() {
        return TCPServer;
    }

    public void setArchiveInfo(ArchiveInfo archiveInfo) {
        this.TCPServer.setArchiveInfo(archiveInfo);
    }

    public void setConnectionReceivingType(int receivingType) {
        TCPServer.setReceivingType(receivingType);
    }

    @Override
    public void onRead(byte[] buffer, int bufferSize, boolean fragment){
        if (bufferSize > 0)
            this.onObjectReceivedListener.onObjectReceived(getObjectFromBytes(buffer, bufferSize));
        else {
            if (fragment) {
                if (buffer == null)
                    this.onObjectReceivedListener.onObjectReceived(new FragmentArchive(null, true));
                else
                    this.onObjectReceivedListener.onObjectReceived(new FragmentArchive(buffer, false));
            }
            else
                this.onObjectReceivedListener.onObjectReceived(buffer);
        }
    }

    private Object getObjectFromBytes(byte[] buffer, int bufferSize) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, bufferSize);
        ObjectInput in = null;
        Object obj = null;

        try {
            in = new ObjectInputStream(bais);
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            Log.e(MainActivity.TAG + ".ConnectionManager", e.getMessage());
        } catch (IOException e) {
            Log.e(MainActivity.TAG + ".ConnectionManager", e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                bais.close();
            } catch (Exception e) {
            }
        }
        return obj;
    }

}
