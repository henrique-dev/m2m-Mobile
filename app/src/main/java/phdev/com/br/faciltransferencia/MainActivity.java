package phdev.com.br.faciltransferencia;

import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.SizeInfo;
import phdev.com.br.faciltransferencia.connection.BroadcastSender;
import phdev.com.br.faciltransferencia.connection.OnConnectedListener;
import phdev.com.br.faciltransferencia.connection.OnFailedConnection;
import phdev.com.br.faciltransferencia.connection.OnReadListener;
import phdev.com.br.faciltransferencia.connection.TCPServer;

public class MainActivity extends AppCompatActivity implements OnReadListener, OnConnectedListener, OnFailedConnection {

    public static final String TAG = "MyApp";

    private TCPServer server;
    private BroadcastSender broadcastSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabConnect = (FloatingActionButton) findViewById(R.id.fab_connect);
        fabConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.info_view);
                textView.setVisibility(View.INVISIBLE);
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                server = new TCPServer(MainActivity.this);
                server.start();
                broadcastSender = new BroadcastSender();
                broadcastSender.start();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Object getObjectFromBytes(byte[] buffer, int bufferSize) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, bufferSize);
        ObjectInput in = null;
        Object obj = null;

        try {
            in = new ObjectInputStream(bais);
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Classe não encontrada. " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Falha na leitura. " + e.getMessage());
            e.printStackTrace();
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

    public static byte[] getBytesFromObject(Object obj) {
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
            //File absolutPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "teste");
            File absolutPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!absolutPath.mkdir())
                Log.d(TAG, "Direotrio não criado!");
            else
                Log.d(TAG, "Diretorio criado");

            try {
                //FileOutputStream fos = openFileOutput(absolutPath, MODE_WORLD_READABLE);
                FileOutputStream fos = new FileOutputStream(absolutPath + "/" + file.getName());
                fos.write(file.getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Arquivo criado com sucesso.");
                //Toast info = Toast.makeText(this, "Arquivo salvo", Toast.LENGTH_SHORT);
                //info.show();

                server.write(getBytesFromObject("cango"));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Falha ao salvar o arquivo. " + e.getMessage());
            }
        } else
            Log.e(TAG, "Não é possivel armazenar!!!");
    }

    @Override
    public int onRead(byte[] buffer, int bufferSize) {
        Object obj = getObjectFromBytes(buffer, bufferSize);

        if (obj instanceof SizeInfo) {
            SizeInfo sf = (SizeInfo) obj;
            return sf.getSize();
        } else if (obj instanceof Archive) {
            Archive fileReceived = (Archive)obj;
            writeFile(fileReceived);
            return 512;
        }
        return 512;
        /*
        if (bufferSize <= 50) {
            msg = (String)getObjectFromBytes(buffer, bufferSize);
            if (msg.contains("<bs>") && msg.endsWith("</bs>")) {
                String sub = msg.substring(4, msg.length() - 5);
                int newBufferSize = Integer.parseInt(sub);
                //Log.d(TAG, "Tamanho do arquuvo a ser recebido: " + newBufferSize);
                server.write(getBytesFromObject("sm"));
                return newBufferSize*-1;
            }
        } else {

            Object obj = getObjectFromBytes(buffer, bufferSize);

            if (obj != null) {
                try {
                    Archive fileReceived = (Archive) getObjectFromBytes(buffer, bufferSize);
                    //Log.d(TAG, "Arquivo recebido!\nNome do arquivo: " + fileReceived.getName() + "\nTamanho do arquivo: " + fileReceived.getBytes().length);
                    writeFile(fileReceived);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast info = Toast.makeText(MainActivity.this, "Falha ao aslvar o arquivo, por favor informe o erro ao desenvolvedor.", Toast.LENGTH_LONG);
                            info.show();
                        }
                    });
                    e.printStackTrace();
                }
            } else
                Log.e(TAG, "Algo deu errado. Objeto recebido vazio");
            return 50;
        }
        Log.d(TAG, "Mensagem recebida: " + msg);
        return 50;
        */
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_connect);
                fab.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onFailedConnection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast info = Toast.makeText(MainActivity.this, "Falha ao conectar, tente novamente", Toast.LENGTH_LONG);
                info.show();
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_connect);
                fab.setVisibility(View.VISIBLE);
                TextView textView = (TextView) findViewById(R.id.info_view);
                textView.setVisibility(View.VISIBLE);
                server = null;
                broadcastSender = null;
            }
        });
    }
}
