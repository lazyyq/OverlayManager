package kyklab.overlaymanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import projekt.andromeda.client.AndromedaClient;

public class LoadActivity extends AppCompatActivity {
    private static final int ANDROMEDA_REQ_CODE_PERMISSION = 14045;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        AndromedaClient.INSTANCE.initialize(this);
        if (ContextCompat.checkSelfPermission(this, AndromedaClient.ACCESS_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) {
            // Already has permission
            startAppIfAndromedaActive();
        } else {
            requestPermissions(
                    new String[]{AndromedaClient.ACCESS_PERMISSION},
                    ANDROMEDA_REQ_CODE_PERMISSION
            );
        }
    }

    private final Runnable serverInactiveRunnable = new Runnable() {
        @Override
        public void run() {
            new AlertDialog.Builder(mContext)
                    .setMessage("Andromeda crashed, probably Andromeda server is inactive at the moment.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }
    };

    private final Runnable startAppRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("ANDROMEDA", "run started");
            try {
                if (AndromedaClient.INSTANCE.isServerActive()) {
                    Log.e("ANDROMEDA", "permission granted and server active");
                    Intent intent = new Intent(mContext, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("ANDROMEDA", "permission granted and server inactive");
                    runOnUiThread(serverInactiveRunnable);
//                    new Thread(serverInactiveRunnable).start();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e("ANDROMEDA", "crashed while run");
                runOnUiThread(serverInactiveRunnable);
//                new Thread(serverInactiveRunnable).start();
            }
        }
    };

    private void startAppIfAndromedaActive() {
        new Thread(startAppRunnable).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ANDROMEDA_REQ_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startAppIfAndromedaActive();
            } else {
                // Permission denied
                new AlertDialog.Builder(this)
                        .setMessage("Andromeda access permission denied")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();
            }
        }
    }
}
