package com.developmethis.partyflashlight.partyflashlight;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.hardware.Camera;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
// HANDLER
private final static int DO_UPDATE_TEXT = 0;
    private final static int DO_THAT = 1;
    private final Handler myHandler = new Handler();

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private Camera.Parameters params;
    private Camera camera;
    Timer timer;
    int speed;
    private boolean isFlashOn;
    private boolean hasFlash;
    private Button share;
    private ImageView up,down;
    LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
            // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

        /*
		 * First check if device is supporting flashlight or not
		 */
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.show();
            return;
        }
        // get the camera
        getCamera();
        partyStart(300);

		/*
		 * Switch button click event to toggle flash on/off
		 */
        up = (ImageView)findViewById(R.id.up);
        down=(ImageView)findViewById(R.id.down);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                timer.purge();
                    speed=100;
                partyStart(speed);
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                timer.purge();
                speed+=100;
                partyStart(speed);
            }
        });





        /// Share Intent listner
        share = (Button)findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Please Download This Applicattion");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,"Share this with friends"));
            }
        });
    }

    private void UpdateGUI() {
        myHandler.post(runnable_interact);
    }
    //creating runnable
    final Runnable runnable_interact = new Runnable() {
        public void run() {
            Random rand = new Random();
            layout = (LinearLayout)findViewById(R.id.background);
            layout.setBackgroundColor(Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));

        }
    };
    /* ________________Main PARTY FUNCTION__________________*/

    public void partyStart(int totalspeed){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(isFlashOn){turnOffFlash();}
                else if(!isFlashOn){turnOnFlash();}
                UpdateGUI();
            }
        },100,totalspeed);
    }

    /*______________ TORCH AND CAMERA FEATURES HERE ____ */

    /*
	 * Get the camera
	 */
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error.Open", e.getMessage());
            }
        }
    }

    /*
     * Turning On flash
     */
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;


        }

    }

    /*
     * Turning Off flash
     */
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // on pause turn off the flash
        if (camera != null) {
            camera.release();
            camera = null;
        }

        super.onPause();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on resume turn on the flash
        getCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // on starting the app get the camera params
        getCamera();
    }

    @Override
    protected void onStop() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onStop();
        // on stop release the camera

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    recreate();
                } else {
                    Toast toast = Toast.makeText(this,"You Didn't Allowed us to access your Tourch",Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
        }
    }
 }
