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

    public BroadcastSender() {

    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {

            List<InetAddress> addresses = new ArrayList<>();

            socket = new DatagramSocket();
            socket.setBroadcast(true);

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = networkInterfaces.nextElement();
                if (netInterface.isVirtual())
                    continue;
                System.out.println(netInterface.getName());
                List<InterfaceAddress> interfaces = netInterface.getInterfaceAddresses();

                for (InterfaceAddress address : interfaces) {
                    //System.out.println("        Endereço de host: " + address.getAddress().getHostAddress());
                    InetAddress broadcastAddress = address.getBroadcast();
                    if (broadcastAddress != null) {
                      //  System.out.println("        Endereço de broadcast: " + broadcastAddress.getHostAddress());
                        addresses.add(broadcastAddress);
                    } else {
                        //System.out.println("        Endereço de broadcast: Sem endereço de broadcast");
                    }
                }
            }

            String msg = "PauloHenrique\n";

            this.sendingBroadcast = true;

            for (InetAddress address : addresses) {
                for (int i=0; i<20; i++) {
                    socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, SERVER_BROADCAST_PORT));
                    sleep(500);
                    if (!this.sendingBroadcast)
                        break;
                }
            }

            socket.close();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
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
