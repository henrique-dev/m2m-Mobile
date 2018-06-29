package br.com.phdev.faciltransferencia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.customviews.ArchiveAdapter;
import br.com.phdev.faciltransferencia.managers.TransferManager;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.interfaces.TransferStatusListener;
import phdev.com.br.faciltransferencia.R;
import br.com.phdev.faciltransferencia.connection.BroadcastSender;
import br.com.phdev.faciltransferencia.connection.TCPServer;

public class MainActivity extends AppCompatActivity implements Connection.OnClientConnectionTCPStatusListener, TransferStatusListener {

    public static final String TAG = "MyApp";

    private TransferManager transferManager;

    private ViewGroup mainView;
    private ViewGroup connectedView;

    private ProgressBar progressBar;
    private Button button;
    private EditText editText;

    private ListView listViewArchives;

    private List<Archive> archivesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teste_activity);

        this.mainView = (ViewGroup) findViewById(R.id.mainView);
        this.connectedView = (ViewGroup) findViewById(R.id.connectedView);
        this.connectedView.setAlpha(1f);

        this.listViewArchives = (ListView) findViewById(R.id.listView);
        this.listViewArchives.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Archive archive = (Archive) adapterView.getItemAtPosition(i);
                    File file = new File(archive.getPath());
                    Uri uri = Uri.fromFile(file);
                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                    String fts = file.toString();
                    if (fts.contains(".doc") || fts.contains(".docx"))
                        openFileIntent.setDataAndType(uri, "application/msword");
                    else if (fts.contains(".pdf"))
                        openFileIntent.setDataAndType(uri, "application/pdf");
                    else if (fts.contains(".xls") || fts.contains(".xlsx"))
                        openFileIntent.setDataAndType(uri, "application/vnd.ms-excel");
                    else if (fts.contains(".rtf"))
                        openFileIntent.setDataAndType(uri, "application/rtf");
                    else if (fts.contains(".wav") || fts.contains(".mp3"))
                        openFileIntent.setDataAndType(uri, "audio/x-wav");
                    else if (fts.contains(".gif"))
                        openFileIntent.setDataAndType(uri, "image/gif");
                    else if (fts.contains(".jpg") || fts.contains(".jpeg") || fts.contains(".png"))
                        openFileIntent.setDataAndType(uri, "image/jpeg");
                    else if (fts.contains(".txt"))
                        openFileIntent.setDataAndType(uri, "text/plain");
                    else if (fts.contains(".3gp") || fts.contains(".mpg") || fts.contains(".mpeg") || fts.contains(".mpe") || fts.contains(".mp4"))
                        openFileIntent.setDataAndType(uri, "video/*");
                    else
                        openFileIntent.setDataAndType(uri, "/*");
                    openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(openFileIntent);

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.button = (Button) findViewById(R.id.button_connect);
        this.button.requestFocus();
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.progressBar.setVisibility(View.VISIBLE);
                String userName = MainActivity.this.editText.getText().toString();
                MainActivity.this.transferManager = new TransferManager(MainActivity.this, userName);
            }
        });
        this.editText = (EditText) findViewById(R.id.editText_alias);
        this.editText.clearFocus();

    }

    private void fadeToConnected() {
        connectedView.setVisibility(View.VISIBLE);
        connectedView.animate().alpha(1f).setDuration(400).setListener(null);
        mainView.animate().alpha(0f).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainView.setVisibility(View.GONE);
            }
        });
    }

    private void fadeToMain() {
        mainView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        mainView.animate().alpha(1f).setDuration(400).setListener(null);
        connectedView.animate().alpha(0f).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                connectedView.setVisibility(View.GONE);
                transferManager.close();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.transferManager != null)
            this.transferManager.close();
    }

    @Override
    public void onBackPressed() {
        if (this.mainView.getAlpha() == 0)
            fadeToMain();
        else
            super.onBackPressed();
    }

    @Override
    public void onDisconnect(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fadeToMain();
                Toast info = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
                info.show();
            }
        });
    }

    @Override
    public void onConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fadeToConnected();
                Toast info = Toast.makeText(MainActivity.this, "Conectado", Toast.LENGTH_LONG);
                info.show();
                MainActivity.this.archivesList = MainActivity.this.transferManager.getArchivesList();
            }
        });
    }

    @Override
    public void onSending() {
    }

    @Override
    public void onSendComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArchiveAdapter archiveAdapter = new ArchiveAdapter(MainActivity.this, MainActivity.this.archivesList);
                MainActivity.this.listViewArchives.setAdapter(archiveAdapter);
            }
        });
    }
}
