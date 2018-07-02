package br.com.phdev.faciltransferencia.managers;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.connection.TCPServer;
import br.com.phdev.faciltransferencia.connection.interfaces.WriteListener;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.ArchiveInfo;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnObjectReceivedListener;
import br.com.phdev.faciltransferencia.transfer.interfaces.TransferStatusListener;

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
public class TransferManager implements OnObjectReceivedListener, Serializable {

    private final String TAG = "myApp.TransferManager";

    private TransferStatusListener transferStatusListener;
    private ConnectionManager connectionManager;
    private WriteListener writeListener;

    private List<Archive> archives;

    private Archive currentReceiveArchive;

    public TransferManager(MainActivity context, String userName) {
        this.connectionManager = new ConnectionManager(context,this);
        this.connectionManager.startBroadcastSender(userName);
        this.connectionManager.startTCPServer();
        this.writeListener = this.connectionManager.getWriteListener();
        this.transferStatusListener = context;
        this.archives = new ArrayList<>();
    }

    public List<Archive> getArchivesList() {
        return this.archives;
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

    private void writeFile(Archive archive) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!absolutePath.mkdir())
                Log.d(TAG, "Direotrio não criado!");
            else
                Log.d(TAG, "Diretorio criado");

            try {
                String pathAndName = absolutePath + "/" + archive.getName();
                FileOutputStream fos = new FileOutputStream(pathAndName);
                fos.write(archive.getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Arquivo criado com sucesso.");
                archive.setBytes(null);
                archive.setPath(pathAndName);
                this.archives.add(archive);
                this.transferStatusListener.onSendComplete();
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
    public void onObjectReceived(Object obj) {
        if (obj instanceof ArchiveInfo) {
            ArchiveInfo archiveInfo = (ArchiveInfo) obj;
            this.currentReceiveArchive = new Archive();
            this.currentReceiveArchive.setName(archiveInfo.getArchiveName());
            this.connectionManager.setArchiveInfo(archiveInfo);
            this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_FILE);
            this.writeListener.write(getBytesFromObject("sm"));
        } else if (obj instanceof Archive) {
            Archive fileReceived = (Archive)obj;
            writeFile(fileReceived);
        } else if (obj instanceof byte[]) {
            this.currentReceiveArchive.setBytes((byte[])obj);
            this.writeFile(this.currentReceiveArchive);
            this.currentReceiveArchive = null;
            this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_MSG);
            this.writeListener.write(getBytesFromObject("cango"));
        }
    }
}
