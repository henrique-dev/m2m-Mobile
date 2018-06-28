package br.com.phdev.faciltransferencia.managers;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.connection.TCPServer;
import br.com.phdev.faciltransferencia.connection.interfaces.WriteListener;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.SizeInfo;
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
public class TransferManager implements OnObjectReceivedListener {

    private final String TAG = "myApp.TransferManager";

    private ConnectionManager connectionManager;
    private WriteListener writeListener;

    public TransferManager(MainActivity context) {
        this.connectionManager = new ConnectionManager(context,this);
        this.connectionManager.startBroadcastSender();
        this.connectionManager.startTCPServer();
        this.writeListener = this.connectionManager.getWriteListener();
    }

    public void close() {
        this.connectionManager.close();
    }

    private byte[] getBytesFromObject(Object obj) {
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
            Log.e(TAG, e.getMessage());
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

    private void writeFile(Archive file) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!absolutePath.mkdir())
                Log.d(TAG, "Direotrio não criado!");
            else
                Log.d(TAG, "Diretorio criado");

            try {
                FileOutputStream fos = new FileOutputStream(absolutePath + "/" + file.getName());
                fos.write(file.getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Arquivo criado com sucesso.");
                this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_MSG);
                this.writeListener.write(getBytesFromObject("cango"));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            }
        } else
            Log.e(TAG, "Não é possivel armazenar!!!");
    }

    @Override
    public int onObjectReceived(Object obj) {
        if (obj instanceof SizeInfo) {
            SizeInfo sf = (SizeInfo) obj;
            this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_FILE);
            this.writeListener.write(getBytesFromObject("sm"));
            return sf.getSize();
        } else if (obj instanceof Archive) {
            Archive fileReceived = (Archive)obj;
            writeFile(fileReceived);
            return 512;
        }
        return 512;
    }
}
