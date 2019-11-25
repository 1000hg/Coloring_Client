package com.dgsw.coloring;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View.OnTouchListener;

import com.dgsw.bt.BTService;
import com.dgsw.data.Constant;

public class MainActivity extends AppCompatActivity {
    //--------------------------------------------------------------------------------
    //- Member Variable
    //--------------------------------------------------------------------------------
    private static final String 	         TAG                             		= "MainActivity";

    private static final int              REQUEST_ENABLE_BT                   = 10;
    private static final int    	         REQUEST_CONNECT_DEVICE 		    	= 1;

    private LinearLayout                    L1, L2;
    private ImageButton                     mBtBTN, ColorHelper, ImageHelper;

    private OnTouchListener                 TouchLayout;

    private Resources                       mAppRes;
    private String              	    	  mConnectedDeviceName            		= null;
    private BluetoothAdapter                mBluetoothAdapter               		= null;
    private BTService                       mBTService                      		= null;
    private Boolean						  mBTOK  									= false;
    private ProgressDialog                  mProgress;
    private ApplicationActivity             mAppActivity;

    String sendFinePermission = new String(Manifest.permission.ACCESS_FINE_LOCATION);
    String sendBluetoothPermission = new String(Manifest.permission.BLUETOOTH);

    //--------------------------------------------------------------------------------
    //- Override Method
    //--------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int cameraPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int galleryPermissionCheck =  ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int bluetoothFinePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int bluetoothPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH);

        if (cameraPermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 0);
        }
        if (galleryPermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if(bluetoothPermissionCheck == PackageManager.PERMISSION_DENIED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, sendBluetoothPermission)) {
                Toast.makeText(this, "앱 실행을 위해서는 블루투스 관리 권한을 설정해야 합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH}, 4);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH}, 4);
            }
        }

        if(bluetoothFinePermissionCheck == PackageManager.PERMISSION_DENIED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, sendFinePermission)) {
                Toast.makeText(this, "앱 실행을 위해서는 블루투스 관리 권한을 설정해야 합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH}, 4);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH}, 4);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "++ ON START ++");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mBTService == null) {
                initUI();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case 2 :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("OK", "OK");
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK)  connectDevice(data);
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initUI();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //- Custom Method
    //----------------------------------------------------------------------------------------------
    private void initUI(){
        mAppRes         = this.getResources();
        mAppActivity    = (ApplicationActivity)this.getApplicationContext();
        mBTService      = new BTService(this);
        mAppActivity.setBtService(mBTService);
        mBTService.setmHandler(mHandler);

        L1 = (LinearLayout)findViewById(R.id.LinearLayout1);
        L2 = (LinearLayout)findViewById(R.id.LinearLayout2);

        ColorHelper = (ImageButton)findViewById(R.id.colorHelper);
        ImageHelper = (ImageButton)findViewById(R.id.imageHelper);
        TouchLayout = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    ColorHelper.setImageResource(R.drawable.colorhelper_gray);
                    ImageHelper.setImageResource(R.drawable.image_helper_gray);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    ColorHelper.setImageResource(R.drawable.color_helper_icon);
                    ImageHelper.setImageResource(R.drawable.imagehelpericon);
                }
                return true;
            }
        };

        L1.setOnTouchListener(TouchLayout);
        L2.setOnTouchListener(TouchLayout);

        mBtBTN = findViewById(R.id.bluetooth);
        mBtBTN.setBackgroundResource(R.drawable.selector_btn_bt);
        mBtBTN.setSelected(false);
    }

    public void moveFunc(View v) {
        switch (v.getId())
        {
            case R.id.colorHelper :
                startActivity(new Intent(MainActivity.this, ColorHelper.class));
                break;

            case R.id.imageHelper :
                startActivity(new Intent(MainActivity.this, ImageHelper.class));
                break;

            case R.id.bluetooth:
                startActivityForResult(new Intent(this, DeviceListActivity.class),REQUEST_CONNECT_DEVICE);
                break;
            case R.id.about:
                Log.i("TAG", "OK");
                Intent intent = new Intent(MainActivity.this, aboutDevelop.class);
                startActivity(intent);
                break;
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device =  mBluetoothAdapter.getRemoteDevice(address);
        mBTService.connect(device);
    }

    //--------------------------------------------------------------------------------
    //- Inner Class :
    //--------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------
    //- Member Method : BT통신 Thread & UI 연동 제어 핸들러
    //-----------------------------------------------------------------------------------------
    private final Handler mHandler = new Handler() {

        private ProgressDialog mDialog;

        @Override
        public void handleMessage(Message msg){
            switch (msg.what)
            {
                case Constant.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    if(mDialog != null && mDialog.isShowing())  mDialog.dismiss();

                    switch (msg.arg1)
                    {
                        case BTService.STATE_CONNECTED:
                            Log.i(TAG, "BTService.STATE_CONNECTED");
                            mBtBTN.setSelected(true);
                            if(mProgress != null) mProgress.dismiss();
                            mBTOK= true;
                            break;

                        case BTService.STATE_CONNECTING:   mBtBTN.setSelected(false);  showProgressDialog("Bluetooth");    mBTOK= false;	break;
                        case BTService.STATE_LISTEN:       mBtBTN.setSelected(false);  if(mProgress != null) mProgress.dismiss(); mBTOK= false;	break;
                        case BTService.STATE_NONE:         mBtBTN.setSelected(false);  if(mProgress != null) mProgress.dismiss(); mBTOK= false;	break;
                    }
                    break;

                case Constant.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constant.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;

                case Constant.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constant.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //- 상태 진행 창 ------------------------------------------------------
    private void showProgressDialog(String szType){

        Log.i(TAG,"showProgressDialog()");

        mProgress = new ProgressDialog(this, R.style.ProgressDialog);
        mProgress.setCancelable(false);
        mProgress.setMessage(" [ " +szType+" ] " +mAppRes.getString(R.string.title_connecting));
        mProgress.show();
    }
}