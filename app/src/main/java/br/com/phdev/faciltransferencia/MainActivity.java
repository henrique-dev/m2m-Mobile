package br.com.phdev.faciltransferencia;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.phdev.faciltransferencia.connection.interfaces.Connection;
import br.com.phdev.faciltransferencia.customviews.AboutDialogFragment;
import br.com.phdev.faciltransferencia.customviews.ArchiveAdapter;
import br.com.phdev.faciltransferencia.customviews.HelpDialogFragment;
import br.com.phdev.faciltransferencia.managers.TransferManager;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.interfaces.OnProgressMadeListener;
import br.com.phdev.faciltransferencia.transfer.interfaces.TransferStatusListener;
import phdev.com.br.faciltransferencia.R;

public class MainActivity extends AppCompatActivity implements Connection.OnClientConnectionTCPStatusListener, TransferStatusListener, OnProgressMadeListener {

    public static final String TAG = "MyApp";
    private final int PERMISSIONS_NEEDED = 0;

    private TransferManager transferManager;

    private ViewGroup mainView;
    private ViewGroup connectedView;

    private ProgressBar progressBar_connecting;
    private Button button;
    private EditText editText;

    private BottomNavigationView bottomNavigationView;

    private ProgressBar progressBar_receiving;

    private ListView listViewArchives;
    private ArchiveAdapter archiveAdapter;

    private List<Archive> archivesList;
    private Archive lastArchive;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_receive_files: {
                    ViewGroup nrf = (ViewGroup) findViewById(R.id.connectedReceivedFiles);
                    ViewGroup nfb = (ViewGroup) findViewById(R.id.connectedFileBrowser);
                    nrf.setVisibility(View.VISIBLE);
                    nfb.setVisibility(View.INVISIBLE);
                    return true;
                }
                case R.id.navigation_file_browser: {
                    ViewGroup nrf = (ViewGroup) findViewById(R.id.connectedReceivedFiles);
                    ViewGroup nfb = (ViewGroup) findViewById(R.id.connectedFileBrowser);
                    nfb.setVisibility(View.VISIBLE);
                    nrf.setVisibility(View.INVISIBLE);
                    return true;
                }
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        this.mainView = (ViewGroup) findViewById(R.id.mainView);
        this.mainView.setVisibility(View.VISIBLE);
        this.mainView.setAlpha(1f);
        this.connectedView = (ViewGroup) findViewById(R.id.connectedView);
        this.connectedView.setAlpha(0f);
        this.connectedView.setVisibility(View.GONE);

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
        this.progressBar_connecting = (ProgressBar) findViewById(R.id.progressBar_connecting);
        this.button = (Button) findViewById(R.id.button_connect);
        this.button.requestFocus();
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.button.setEnabled(false);
                MainActivity.this.progressBar_connecting.setVisibility(View.VISIBLE);
                String userName = MainActivity.this.editText.getText().toString();
                if (userName.equals(""))
                    userName = Build.MODEL;
                MainActivity.this.transferManager = new TransferManager(MainActivity.this, userName);
            }
        });
        this.editText = (EditText) findViewById(R.id.editText_alias);
        this.editText.setText(Build.MODEL);

        this.progressBar_receiving = (ProgressBar) findViewById(R.id.progressBar_receiving);
        this.progressBar_receiving.setVisibility(View.INVISIBLE);

        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this.mOnNavigationItemSelectedListener);

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int internetPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
            int writePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (internetPermissions != PackageManager.PERMISSION_GRANTED || writePermissions != PackageManager.PERMISSION_GRANTED
                    || readPermissions != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_NEEDED);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connected, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_connected_action_about:
                new AboutDialogFragment().show(getFragmentManager(), TAG);
                return true;
            case R.id.menu_connected_action_help:
                new HelpDialogFragment().show(getFragmentManager(), TAG);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_NEEDED:
                if (grantResults.length > 2 && grantResults[0] != PackageManager.PERMISSION_GRANTED
                        && grantResults[1] != PackageManager.PERMISSION_GRANTED
                        && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    super.onBackPressed();
                }
        }
    }

    private void fadeToConnected() {
        connectedView.setVisibility(View.VISIBLE);
        connectedView.animate().alpha(1f).setDuration(400).setListener(null);
        mainView.animate().alpha(0f).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainView.setVisibility(View.GONE);
                Snackbar.make(MainActivity.this.mainView, "Conectado", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void fadeToMain() {
        mainView.setVisibility(View.VISIBLE);
        progressBar_connecting.setVisibility(View.GONE);
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
            onDisconnect("Conexão encerrada pelo usuário");
        else
            super.onBackPressed();
    }

    @Override
    public synchronized void onDisconnect(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fadeToMain();
                MainActivity.this.button.setEnabled(true);
                Snackbar.make(MainActivity.this.mainView, msg, Snackbar.LENGTH_LONG).setAction("", null).show();
            }
        });
    }

    @Override
    public void onConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fadeToConnected();
                MainActivity.this.archivesList = new ArrayList<>();
                MainActivity.this.lastArchive = new Archive();
                MainActivity.this.archiveAdapter = new ArchiveAdapter(MainActivity.this, MainActivity.this.archivesList);
                MainActivity.this.listViewArchives.setAdapter(MainActivity.this.archiveAdapter);
            }
        });
    }

    @Override
    public void onSending(final int fileSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.progressBar_receiving.setVisibility(View.VISIBLE);
                MainActivity.this.progressBar_receiving.setProgress(0);
                MainActivity.this.progressBar_receiving.setMax(fileSize);
            }
        });
    }

    @Override
    public void updateProgressBar(final int amount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.progressBar_receiving.setProgress(amount);
            }
        });
    }

    @Override
    public void onSendComplete(final Archive archive) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!lastArchive.getMasterPath().equals(archive.getMasterPath())) {
                    try {
                        archiveAdapter.add(archive);
                        String ext = MimeTypeMap.getFileExtensionFromUrl(archive.getPath());
                        MediaScannerConnection.scanFile(MainActivity.this, new String[]{archive.getPath()},
                                new String[]{ext}, null);
                        archiveAdapter.notifyDataSetChanged();
                        lastArchive = archive;
                    } catch (Exception e) {
                    }
                }

                MainActivity.this.progressBar_receiving.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void noSpace() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(MainActivity.this.mainView, "Espaço insuficiente", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }
}
