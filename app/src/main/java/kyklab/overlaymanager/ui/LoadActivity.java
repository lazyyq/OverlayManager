package kyklab.overlaymanager.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import kyklab.overlaymanager.R;
import kyklab.overlaymanager.utils.ThemeManager;
import projekt.andromeda.client.AndromedaClient;

public class LoadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ANDROMEDA_REQ_CODE_PERMISSION = 14045;

    private TextView mAndromedaWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        mAndromedaWarning = findViewById(R.id.andromedaWarningBody);
        ImageButton retryAndromeda = findViewById(R.id.retryAndromeda);
        retryAndromeda.setOnClickListener(this);

        ThemeManager.getInstance().init();

        AndromedaClient.INSTANCE.initialize(this);
        if (ContextCompat.checkSelfPermission(this, AndromedaClient.ACCESS_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) {
            // Already has permission
            startAppIfAndromedaActive();
        } else {
            mAndromedaWarning.setText(R.string.andromeda_denied);
            askAndromedaPermission();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.retryAndromeda:
                askAndromedaPermission();
                break;
        }
    }

    private void startAppIfAndromedaActive() {
        Log.e("ANDROMEDA", "run started");
        try {
            if (AndromedaClient.INSTANCE.isServerActive()) {
                Log.e("ANDROMEDA", "permission granted and server active");
                Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e("ANDROMEDA", "permission granted and server inactive");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e("ANDROMEDA", "crashed while run");
            mAndromedaWarning.setText(R.string.andromeda_crashed);
        }
    }

    private void askAndromedaPermission() {
        requestPermissions(
                new String[]{AndromedaClient.ACCESS_PERMISSION},
                ANDROMEDA_REQ_CODE_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ANDROMEDA_REQ_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startAppIfAndromedaActive();
            }
        }
    }
}
