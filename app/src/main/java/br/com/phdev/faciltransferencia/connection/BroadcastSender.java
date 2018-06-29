package br.com.phdev.faciltransferencia.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import br.com.phdev.faciltransferencia.connection.interfaces.Connection;

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

            for (InetAddress address : addresses) {
                for (int i=0; i<20; i++) {
                    datagramSocket.send(new DatagramPacket(alias.getBytes(), alias.getBytes().length, address, SERVER_BROADCAST_PORT));
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
