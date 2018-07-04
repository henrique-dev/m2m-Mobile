package br.com.phdev.faciltransferencia.managers;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import br.com.phdev.faciltransferencia.MainActivity;
import br.com.phdev.faciltransferencia.connection.TCPServer;
import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.connection.interfaces.WriteListener;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.ArchiveInfo;
import br.com.phdev.faciltransferencia.transfer.FragmentArchive;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnObjectReceivedListener;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnProgressMadeListener;
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
public class TransferManager implements OnObjectReceivedListener, Connection.OnClientConnectionTCPStatusListener, TransferStatusListener, OnProgressMadeListener {

    private final String TAG = "myApp.TransferManager";
    private final long PERCENT_SPACE_ON_DISK_UNAVAILABLE = 5;
    public static final int CURRENT_ID_VERSION = 6;

    private MainActivity mainActivity;
    private ConnectionManager connectionManager;
    private WriteListener writeListener;

    private Archive currentReceiveArchive;
    private List<File> currentReceiveArchiveInFragments;

    public TransferManager(MainActivity mainActivity, String userName) {
        this.connectionManager = new ConnectionManager(this);
        this.connectionManager.startBroadcastSender(userName);
        this.connectionManager.startTCPServer();
        this.writeListener = this.connectionManager.getWriteListener();
        this.mainActivity = mainActivity;
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
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), archive.getLocalPath());
            if (!absolutePath.mkdirs())
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
                if (archive.getMasterPath() != null) {
                    archive.setName(archive.getMasterPath());
                    archive.setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + archive.getMasterPath());
                    archive.setLocalPath(null);
                } else
                    archive.setPath(pathAndName);
                new File(pathAndName).setLastModified(System.nanoTime());
                this.mainActivity.onSendComplete(archive);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            }
        } else
            Log.e(TAG, "Não é possivel armazenar!!!");
    }

    private void writeFragment(FragmentArchive fragmentArchive) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (absolutePath.mkdir())
                Log.d(TAG, "Diretorio criado");

            try {
                String pathAndName = absolutePath + "/" + fragmentArchive.getName();
                FileOutputStream fos = new FileOutputStream(pathAndName);
                fos.write(fragmentArchive.getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Fragmento criado com sucesso.");
                this.currentReceiveArchiveInFragments.add(new File(pathAndName));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            }
        } else
            Log.e(TAG, "Não é possivel armazenar!!!");
    }

    private void mergeFragments() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), currentReceiveArchive.getLocalPath());
            if (absolutePath.mkdir())
                Log.d(TAG, "Diretorio criado");

            try {
                String pathAndName = absolutePath + "/" + this.currentReceiveArchive.getName();
                FileOutputStream fos = new FileOutputStream(pathAndName);
                FileChannel outFile = fos.getChannel();
                for (File fragmentFile : this.currentReceiveArchiveInFragments) {
                    FileInputStream fis = new FileInputStream(fragmentFile.getPath());
                    FileChannel inFile = fis.getChannel();
                    inFile.transferTo(0, inFile.size(), outFile);
                    inFile.close();
                    fis.close();
                    fragmentFile.delete();
                }
                outFile.close();
                fos.flush();
                fos.close();

                if (currentReceiveArchive.getMasterPath() != null) {
                    currentReceiveArchive.setName(currentReceiveArchive.getMasterPath());
                    currentReceiveArchive.setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + currentReceiveArchive.getMasterPath());
                    currentReceiveArchive.setLocalPath(null);
                } else
                    currentReceiveArchive.setPath(pathAndName);

                new File(pathAndName).setLastModified(System.nanoTime());
                this.mainActivity.onSendComplete(currentReceiveArchive);
                Log.d(TAG, "Arquivo criado com sucesso.");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Erro ao salvar arquivo!" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Erro ao salvar arquivo!" + e.getMessage());
            }
        } else
            Log.e(TAG, "Não é possivel armazenar!!!");
    }

    private boolean haveSpace(long fileSize) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File absolutePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            long freeSpaceAvailable = absolutePath.getFreeSpace();
            long spaceAvailableForUs = freeSpaceAvailable - (long)(freeSpaceAvailable * (double)(PERCENT_SPACE_ON_DISK_UNAVAILABLE / 100));
            return fileSize <= spaceAvailableForUs;
        }
        return false;
    }

    @Override
    public void onObjectReceived(Object obj) {
        if (obj instanceof ArchiveInfo) {
            ArchiveInfo archiveInfo = (ArchiveInfo) obj;
            if (!haveSpace(archiveInfo.getArchiveLength())) {
                this.writeListener.write(getBytesFromObject("nospace"));
                this.mainActivity.noSpace();
                return;
            }
            this.currentReceiveArchive = new Archive();
            this.currentReceiveArchive.setName(archiveInfo.getArchiveName());
            this.currentReceiveArchive.setLocalPath(archiveInfo.getLocalPath());
            this.currentReceiveArchive.setMasterPath(archiveInfo.getMasterPath());
            this.connectionManager.setArchiveInfo(archiveInfo);
            this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_FILE);
            this.mainActivity.onSending((int)archiveInfo.getArchiveLength());
            if (archiveInfo.getFragmentsAmount() > 1) {
                currentReceiveArchiveInFragments = new ArrayList<>();
            }
            this.writeListener.write(getBytesFromObject("sm"));
        } else if (obj instanceof FragmentArchive) {
            FragmentArchive fragmentArchive = (FragmentArchive) obj;
            if (fragmentArchive.isLast()) {
                this.mergeFragments();
                this.currentReceiveArchive = null;
                this.connectionManager.setArchiveInfo(null);
                this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_MSG);
                this.writeListener.write(getBytesFromObject("cango"));
                this.currentReceiveArchiveInFragments.clear();
                this.currentReceiveArchiveInFragments = null;
            } else {
                fragmentArchive.setName(currentReceiveArchive.getName() + "_frg" + currentReceiveArchiveInFragments.size());
                writeFragment(fragmentArchive);
                this.writeListener.write(getBytesFromObject("smf"));
            }
        } else if (obj instanceof byte[]) {
            this.currentReceiveArchive.setBytes((byte[])obj);
            this.writeFile(this.currentReceiveArchive);
            this.currentReceiveArchive = null;
            this.connectionManager.setArchiveInfo(null);
            this.connectionManager.setConnectionReceivingType(TCPServer.RECEIVING_TYPE_MSG);
            this.writeListener.write(getBytesFromObject("cango"));
        }
    }

    @Override
    public void onDisconnect(String msg) {
        mainActivity.onDisconnect(msg);
    }

    @Override
    public void onConnect() {
        mainActivity.onConnect();
    }

    @Override
    public void updateProgressBar(int amount) {
        mainActivity.updateProgressBar(amount);
    }

    @Override
    public void onSending(int fileSize) {
        mainActivity.onSending(fileSize);
    }

    @Override
    public void onSendComplete(Archive archive) {
        mainActivity.onSendComplete(archive);
    }

    @Override
    public void noSpace() {
        mainActivity.noSpace();
    }
}
