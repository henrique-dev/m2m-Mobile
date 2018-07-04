package br.com.phdev.faciltransferencia.connection;

import android.os.Build;
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
import br.com.phdev.faciltransferencia.managers.TransferManager;
import br.com.phdev.faciltransferencia.transfer.ArchiveInfo;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnProgressMadeListener;

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

    private InputStream in;
    private OutputStream out;

    private ArchiveInfo archiveInfo;

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
    private OnProgressMadeListener progressMadeListener;

    private boolean connected = false;

    public TCPServer(TransferManager context,
                     Connection.OnClientConnectionTCPStatusListener broadcastConnectAlert, OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
        this.guiConnectAlert = context;
        this.progressMadeListener = context;
        this.broadcastConnectAlert = broadcastConnectAlert;
    }

    public void setArchiveInfo(ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
    }

    public void setReceivingType(int receivingType) {
        this.receivingType = receivingType;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void close() {
        try {
            if (this.serverSocket!= null && !this.serverSocket.isClosed())this.serverSocket.close();
            if (this.socket != null && !this.socket.isClosed()) this.socket.close();
            if (this.out != null) this.out.close();
            if (this.in != null) this.in.close();
        } catch (Exception e) {
            Log.d(MainActivity.TAG, e.getMessage());
        }
        finally {
            this.onReadListener = null;
            this.guiConnectAlert = null;
            this.broadcastConnectAlert = null;
        }
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
            this.in = this.socket.getInputStream();

            while (true) {
                int currentProgress = 0;
                int totalDataReaded = 0;
                int dataRead = 0;
                byte[] finalBuffer;

                if (receivingType == RECEIVING_TYPE_MSG) {
                    Log.d(MainActivity.TAG, "Novo tamanho para o buffer: " + 512);
                    finalBuffer = new byte[512];
                    totalDataReaded += in.read(finalBuffer, totalDataReaded, finalBuffer.length - totalDataReaded);
                    TCPServer.this.onReadListener.onRead(finalBuffer, totalDataReaded, false);
                } else {
                    Log.d(MainActivity.TAG, "Recebendo arquivo: " + archiveInfo.getArchiveName());
                    long startTime = System.nanoTime();
                    if (archiveInfo.getFragmentsAmount() > 1) {
                        Log.d(MainActivity.TAG, "Recebendo arquivo fragmentado");

                        for (int i=0; i<archiveInfo.getFragmentsAmount(); i++) {
                            if (i == archiveInfo.getFragmentsAmount()-1 && archiveInfo.getLastFragmentLength() != 0) {
                                finalBuffer = new byte[archiveInfo.getLastFragmentLength()];
                            } else {
                                finalBuffer = new byte[archiveInfo.getFragmentLength()];
                            }
                            Log.d(MainActivity.TAG, "Novo tamanho para o buffer: " + finalBuffer.length);
                            while (totalDataReaded < finalBuffer.length) {
                                dataRead = in.read(finalBuffer, totalDataReaded, finalBuffer.length - totalDataReaded);
                                currentProgress += dataRead;
                                totalDataReaded += dataRead;
                                progressMadeListener.updateProgressBar(currentProgress);
                                this.socket.setSoTimeout(20000);
                            }
                            Log.d(MainActivity.TAG, "Fragmento " + (i+1) + " recebido. Tamanho: " + totalDataReaded);
                            this.onReadListener.onRead(finalBuffer, 0, true);
                            totalDataReaded = 0;
                        }
                        long finalTime = (System.nanoTime() - startTime) / 1000000;
                        Log.d(MainActivity.TAG, "Tempo total de transferência: " + finalTime + " milisegundos");
                        Log.d(MainActivity.TAG, "Taxa de transferencia média final: " +
                                (((double)archiveInfo.getArchiveLength() / (1024))/(((double)finalTime)/1000)) + " KB/s");

                        this.onReadListener.onRead(null, 0, true);
                    } else {
                        Log.d(MainActivity.TAG, "Novo tamanho para o buffer: " + archiveInfo.getArchiveLength());
                        finalBuffer = new byte[(int)archiveInfo.getArchiveLength()];
                        while (totalDataReaded < finalBuffer.length) {
                            dataRead = in.read(finalBuffer, totalDataReaded, finalBuffer.length - totalDataReaded);
                            currentProgress += dataRead;
                            totalDataReaded += dataRead;
                            progressMadeListener.updateProgressBar(currentProgress);
                            this.socket.setSoTimeout(20000);
                        }
                        long finalTime = (System.nanoTime() - startTime) / 1000000;
                        Log.d(MainActivity.TAG, "Tempo total de transferência: " + finalTime + " milisegundos");
                        Log.d(MainActivity.TAG, "Taxa de transferencia média final: " +
                                (((double)archiveInfo.getArchiveLength() / (1024))/(((double)finalTime)/1000)) + " KB/s");

                        this.onReadListener.onRead(finalBuffer, 0, false);
                    }
                }
                this.socket.setSoTimeout(0);
            }
        } catch (InterruptedIOException e) {
            if (this.guiConnectAlert != null)
                this.guiConnectAlert.onDisconnect("Falha ao conectar. Por favor tente novamente.");
            Log.d(MainActivity.TAG, e.getMessage() != null ? e.getMessage() : "");
        } catch (SocketException e) {
            if (this.guiConnectAlert != null)
                this.guiConnectAlert.onDisconnect("Conexão encerrada pelo host");
            Log.d(MainActivity.TAG, e.getMessage());
        } catch (IOException e) {
            if (this.guiConnectAlert != null)
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
