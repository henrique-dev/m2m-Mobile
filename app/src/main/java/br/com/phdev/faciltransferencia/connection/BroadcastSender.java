package br.com.phdev.faciltransferencia.connection;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.managers.TransferManager;
import br.com.phdev.faciltransferencia.transfer.BroadcastPacket;

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
public class BroadcastSender extends Thread implements Connection.OnClientConnectionTCPStatusListener {

    public static final int SERVER_BROADCAST_PORT = 6012;

    private boolean sendingBroadcast = false;

    private DatagramSocket datagramSocket;

    private String alias;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void close() {
        this.datagramSocket.close();
    }

    private byte[] getBytesFromObject(BroadcastPacket obj) {
        if (obj == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;

        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                baos.close();
            } catch (Exception e) {
            }
        }
        return bytes;
    }

    @Override
    public void run() {
        this.datagramSocket = null;
        try {

            List<InetAddress> addresses = new ArrayList<>();

            this.datagramSocket = new DatagramSocket();
            this.datagramSocket.setBroadcast(true);

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = networkInterfaces.nextElement();
                if (netInterface.isVirtual())
                    continue;
                System.out.println(netInterface.getName());
                List<InterfaceAddress> interfaces = netInterface.getInterfaceAddresses();

                for (InterfaceAddress address : interfaces) {
                    InetAddress broadcastAddress = address.getBroadcast();
                    if (broadcastAddress != null)
                        addresses.add(broadcastAddress);
                }
            }

            this.sendingBroadcast = true;
            byte[] bytesToSend = getBytesFromObject(new BroadcastPacket(TransferManager.CURRENT_ID_VERSION, alias));
            Log.d(MainActivity.TAG, bytesToSend.length + "");

            for (InetAddress address : addresses) {
                for (int i=0; i<20; i++) {
                    //datagramSocket.send(new DatagramPacket(alias.getBytes(), alias.getBytes().length, address, SERVER_BROADCAST_PORT));
                    datagramSocket.send(new DatagramPacket(bytesToSend, bytesToSend.length, address, SERVER_BROADCAST_PORT));
                    sleep(500);
                    if (!this.sendingBroadcast)
                        break;
                }
            }

            this.datagramSocket.close();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
    }

    @Override
    public void onDisconnect(String msg) {

    }

    @Override
    public void onConnect() {
        this.sendingBroadcast = false;
    }
}
