package br.com.phdev.faciltransferencia.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.connection.interfaces.OnReadListener;
import br.com.phdev.faciltransferencia.connection.interfaces.WriteListener;
import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.exceptions.DisconnectException;

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
public class TCPServer extends Thread implements WriteListener {

    public static final int TRANSFER_PORT = 10011;
    private final int BUFFER_MSG_SIZE = 512;

    private ServerSocket serverSocket;
    private Socket socket;

    private OutputStream out;

    private int bufferSize = BUFFER_MSG_SIZE;

    public static final int RECEIVING_TYPE_FILE = 0;
    public static final int RECEIVING_TYPE_MSG = 1;
    private int receivingType = RECEIVING_TYPE_MSG;

    private final int RECEIVING_STATUS_WAITING = 0;
    private final int RECEIVING_STATUS_SENDING = 1;
    private final int RECEIVING_STATUS_READY = 2;
    private int receivingStatus = RECEIVING_STATUS_WAITING;

    private OnReadListener onReadListener;
    private Connection.OnClientConnectionTCPStatusListener guiConnectAlert;
    private Connection.OnClientConnectionTCPStatusListener broadcastConnectAlert;

    private boolean connected = false;

    public TCPServer(Connection.OnClientConnectionTCPStatusListener guiConnectAlert,
                     Connection.OnClientConnectionTCPStatusListener broadcastConnectAlert, OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
        this.guiConnectAlert = guiConnectAlert;
        this.broadcastConnectAlert = broadcastConnectAlert;
    }

    public void setReceivingType(int receivingType) {
        this.receivingType = receivingType;
    }

    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void run () {
        try {
            Log.d(MainActivity.TAG, "Esperando conexão");
            {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(10000);
                serverSocket.bind(new InetSocketAddress(TRANSFER_PORT));
                this.socket = serverSocket.accept();
                this.serverSocket.close();
            }
            Log.d(MainActivity.TAG, "Conectado ao servidor");
            this.guiConnectAlert.onConnect();
            this.broadcastConnectAlert.onConnect();
            this.connected = true;
            this.out = this.socket.getOutputStream();
            InputStream in = this.socket.getInputStream();

            while (true) {
                int totalDataReaded = 0;
                int dataReaded;

                Log.d(MainActivity.TAG, "Novo tamanho para o buffer: " + bufferSize);
                byte[] buffer = new byte[bufferSize];
                byte[] finalBuffer = new byte[bufferSize];

                Log.d(MainActivity.TAG, "getReceiveBufferSize: " + socket.getReceiveBufferSize());

                while (totalDataReaded < buffer.length) {
                    dataReaded = in.read(buffer);
                    for (int i=0; i<dataReaded; i++) {
                        finalBuffer[totalDataReaded + i] = buffer[i];
                    }
                    totalDataReaded += dataReaded;
                    if (receivingType == RECEIVING_TYPE_MSG) {
                        try {
                            bufferSize = TCPServer.this.onReadListener.onRead(finalBuffer, totalDataReaded);
                            Log.e(MainActivity.TAG, "Tamanho do arquivo a ser recebido: " + bufferSize);
                            break;
                        } catch (Exception e) {
                            Log.d(MainActivity.TAG, e.getMessage());
                        }
                    }
                }
                if (receivingType == RECEIVING_TYPE_FILE && receivingStatus == RECEIVING_STATUS_READY) {
                    bufferSize = this.onReadListener.onRead(finalBuffer, totalDataReaded);
                    receivingType = RECEIVING_TYPE_MSG;
                    receivingStatus = RECEIVING_STATUS_WAITING;
                }
                if (receivingType == RECEIVING_TYPE_FILE && receivingStatus == RECEIVING_STATUS_WAITING) {
                    receivingStatus = RECEIVING_STATUS_READY;
                }
            }
        } catch (InterruptedIOException e) {
            this.guiConnectAlert.onDisconnect("Falha ao conectar. Por favor tente novamente.");
            Log.d(MainActivity.TAG, e.getMessage() != null ? e.getMessage() : "");
        } catch (SocketException e) {
            this.guiConnectAlert.onDisconnect("Conexão encerrada pelo host");
            Log.d(MainActivity.TAG, e.getMessage());
        } catch (IOException e) {
            this.guiConnectAlert.onDisconnect(e.getMessage());
            Log.d(MainActivity.TAG, e.getMessage());
        } finally {
            try {
                if (this.socket != null) {
                    this.socket.close();
                }
                if (this.serverSocket != null && !this.serverSocket.isClosed())
                    this.serverSocket.close();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                this.socket = null;
                this.connected = false;
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {
            this.out.write(bytes);
            this.out.flush();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Falha ao enviar dados. " + e.getMessage());
        }
    }
}
