package phdev.com.br.faciltransferencia.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import br.com.phdev.faciltransferencia.transfer.SizeInfo;
import phdev.com.br.faciltransferencia.MainActivity;

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

    private Socket socket;

    private OutputStream out;

    private int bufferSize = BUFFER_MSG_SIZE;

    private final int RECEIVING_FILE = 0;
    private final int RECEIVING_MSG = 1;
    private int receivingType = RECEIVING_MSG;

    private final int RECEIVING_STATUS_WAITING = 0;
    private final int RECEIVING_STATUS_SENDING = 1;
    private final int RECEIVING_STATUS_READY = 2;
    private int receivingStatus = RECEIVING_STATUS_WAITING;

    private OnFailedConnection failedConnection;
    private OnReadListener onReadListener;
    private OnConnectedListener connectedListener;

    public TCPServer(MainActivity mainActivity) {
        this.onReadListener = mainActivity;
        this.connectedListener = mainActivity;
        this.failedConnection = mainActivity;
    }

    @Override
    public void run () {
        try {
            Log.d(MainActivity.TAG, "Esperando conexão");
            this.socket = new ServerSocket(TRANSFER_PORT).accept();
            Log.d(MainActivity.TAG, "Conectado ao servidor");
            this.connectedListener.onConnected();

            this.out = this.socket.getOutputStream();
            InputStream in = this.socket.getInputStream();

            while (true) {
                int totalDataReaded = 0;
                int dataReaded;
                int bufferReaded;
                boolean msg = false;

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
                    if (receivingType == RECEIVING_MSG) {
                        try {
                            if (MainActivity.getObjectFromBytes(finalBuffer, totalDataReaded) != null) {
                                for (int i=0; i<totalDataReaded; i++) {
                                    Log.d(MainActivity.TAG, "msg data: " + finalBuffer[i]);
                                }
                                SizeInfo sf = (SizeInfo)MainActivity.getObjectFromBytes(finalBuffer, totalDataReaded);
                                bufferSize = sf.getSize();
                                Log.e(MainActivity.TAG, "Tamanho do arquivo a ser recebido: " + sf.getSize());
                                receivingType = RECEIVING_FILE;
                                break;
                            }
                        } catch (Exception e) {
                            Log.d(MainActivity.TAG, e.getMessage());
                        }
                    }
                }
                if (receivingType == RECEIVING_FILE && receivingStatus == RECEIVING_STATUS_READY) {
                    bufferSize = this.onReadListener.onRead(finalBuffer, totalDataReaded);
                    //Log.d(MainActivity.TAG, "Enviando mensagem de arquivo recebido...");
                    //write(MainActivity.getBytesFromObject("cango"));
                    //Log.d(MainActivity.TAG, "Mensagem de arquivo recebido enviada!");
                    receivingType = RECEIVING_MSG;
                    receivingStatus = RECEIVING_STATUS_WAITING;
                }
                if (receivingType == RECEIVING_FILE && receivingStatus == RECEIVING_STATUS_WAITING) {
                    receivingStatus = RECEIVING_STATUS_READY;
                    write(MainActivity.getBytesFromObject("sm"));
                }
            }
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.getMessage());
        } finally {
            try {
                if (this.socket != null)
                    this.socket.close();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                this.socket = null;
                Log.e(MainActivity.TAG, "Erro ao criar socket TCP");
                this.failedConnection.onFailedConnection();
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {
            //Log.d(MainActivity.TAG, "Tamanho da msg: " + bytes.length);
            this.out.write(bytes);
            this.out.flush();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Falha ao enviar dados. " + e.getMessage());
        }
    }
}
