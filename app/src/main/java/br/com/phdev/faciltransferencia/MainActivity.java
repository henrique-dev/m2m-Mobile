package br.com.phdev.faciltransferencia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView textView;
    private EditText editText;

    private ListView listViewArchives;

    private List<Archive> archivesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mainView = (ViewGroup) findViewById(R.id.mainView);
        this.connectedView = (ViewGroup) findViewById(R.id.connectedView);
        this.connectedView.setAlpha(1f);

        this.listViewArchives = (ListView) findViewById(R.id.listView);
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
        this.textView = (TextView) findViewById(R.id.textView_alias);
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
