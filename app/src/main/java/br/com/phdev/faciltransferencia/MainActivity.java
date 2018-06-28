package br.com.phdev.faciltransferencia;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.managers.TransferManager;
import phdev.com.br.faciltransferencia.R;
import br.com.phdev.faciltransferencia.connection.BroadcastSender;
import br.com.phdev.faciltransferencia.connection.TCPServer;

public class MainActivity extends AppCompatActivity implements Connection.OnClientConnectionTCPStatusListener {

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

                TransferManager transferManager = new TransferManager(MainActivity.this);

                //server = new TCPServer(MainActivity.this);
                //server.start();
                //broadcastSender = new BroadcastSender();
                //broadcastSender.start();

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

    @Override
    public void onDisconnect(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast info = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
                info.show();
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_connect);
                fab.setVisibility(View.VISIBLE);
                TextView textView = (TextView) findViewById(R.id.info_view);
                textView.setVisibility(View.VISIBLE);
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
                server = null;
                broadcastSender = null;
            }
        });
    }

    @Override
    public void onConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast info = Toast.makeText(MainActivity.this, "Conectado", Toast.LENGTH_LONG);
                info.show();
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_connect);
                fab.setVisibility(View.INVISIBLE);
            }
        });
    }
}
