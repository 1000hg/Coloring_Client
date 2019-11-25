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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dgsw.bt.BTService;
import com.dgsw.data.Constant;
import java.util.ArrayList;

public class IntroActivity extends AppCompatActivity {
    //----------------------------------------------------------------------------------------------
    //- Member Variable
    //----------------------------------------------------------------------------------------------
    private static final String 	        TAG                             		= "IntroActivity";
    public  static final int    	        REQUEST_PEERMISSION 			        = 1000;
    private static final int    	        REQUEST_CONNECT_DEVICE 		    	= 2000;
    private static final int    	        REQUEST_ENABLE_BT 					= 3000;

    //- Permission 체크 ----------------------------------------------------------------------------
    private int     				          mPermissionCheck 			= -1;

    private String              	    	  mConnectedDeviceName            		= null;
    private BluetoothAdapter                mBluetoothAdapter               		= null;
    private BTService                       mBTService                      		= null;
    private Boolean						  mBTOK  									= false;
    private ProgressDialog  		    	  mProgress;
    private ImageButton                     mBtBTN, ColorHelper, ImageHelper;
    private Resources                       mAppRes;
    private LinearLayout                    L1, L2;
    private View.OnTouchListener            TouchLayout;
    private ImageView                       mSelectIMG;

    private ApplicationActivity             mAppActivity;

    private String                          mPermissions[]={  Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                Manifest.permission.CAMERA,
                                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                                Manifest.permission.BLUETOOTH};

    //----------------------------------------------------------------------------------------------
    //- Override Method
    //----------------------------------------------------------------------------------------------
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

        //- 갤러리, 카메라, 블루투스 사용 권한 요청
        checkPermission(mPermissions);
    }

    @Override
    public void onStart() {
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
    public synchronized void onResume() {
        super.onResume();
        Log.i(TAG, "+ ON RESUME +");

        if (mBTService != null) {
            if (mBTService.getState() == BTService.STATE_NONE) {
                mBTService.start();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy()...1.");
        try {
            Thread.sleep(3000);
            Log.i(TAG, ".....END");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onDestroy()....2");

        if(mProgress != null) mProgress.dismiss();
        //if(mBTService != null) mBTService.stop();
        if(mProgress != null) mProgress.dismiss();

        Log.i(TAG, "--- ON DESTROY ---");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult " + resultCode);
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK)  connectDevice(data);
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initUI();
                } else {
                    Log.i(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }



    //- 권한 관련 요청 처리 결과 -------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_PEERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.i(TAG, "접근 권한 허용");
                }else{
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //- Bluetooth 기기 검색이 가능하도록 권한 허용 요청 --------------------------------------------
    private void checkPermission(String[] permission){
        ArrayList<String> permissionList = new ArrayList<String>();

        for(int i=0; i<permission.length; i++)
        {
            String curPermission = permission[i];
            int permissionCheck = ContextCompat.checkSelfPermission(this, curPermission);
            if(permissionCheck != getPackageManager().PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, curPermission) == false){
                    permissionList.add(curPermission);
                }
            }
        }
        String[] targets = new String[permissionList.size()];
        permissionList.toArray(targets);

        if(targets.length>0)
            ActivityCompat.requestPermissions(this, targets, REQUEST_PEERMISSION);
    }


    //----------------------------------------------------------------------------------------------
    //- Member Method : Cumstom Method
    //----------------------------------------------------------------------------------------------
    //- UI 초기화 ----------------------------------------------------------------------------------
    private void initUI() {
        Log.i(TAG, "initUI()");

        mAppRes         = this.getResources();
        mAppActivity    = (ApplicationActivity)this.getApplicationContext();
        mBTService      = new BTService(this);
        mAppActivity.setBtService(mBTService);
        mBTService.setmHandler(mHandler);

        mBtBTN = (ImageButton)findViewById(R.id.bluetooth);
        mBtBTN.setBackgroundResource(R.drawable.selector_btn_bt);
        mBtBTN.setSelected(false);

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

    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device =  mBluetoothAdapter.getRemoteDevice(address);
        mBTService.connect(device);
    }



    //- 권한 관련 요청 처리 결과 -------------------------------------------------------------------
    public void moveFunc(View v) {
        switch (v.getId())
        {
            case R.id.colorHelper :
                 if(mBTOK == true)
                    startActivity(new Intent(IntroActivity.this, ColorHelper.class));
                 else
                     Toast.makeText(this, "블루투스 연결을 해야합니다.", Toast.LENGTH_SHORT).show();
                 break;

            case R.id.imageHelper :
                //if(mBTOK == true)
                    startActivity(new Intent(IntroActivity.this, ImageHelper.class));
                //else
                   //Toast.makeText(this, "블루투스 연결을 해야합니다.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bluetooth:
                 startActivityForResult(new Intent(this, DeviceListActivity.class),REQUEST_CONNECT_DEVICE);
                 break;
            case R.id.about:
                Log.i("TAG", "OK");
                Intent intent = new Intent(IntroActivity.this, aboutDevelop.class);
                startActivity(intent);
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //- Inner Class :
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //- Member Method : BT통신 Thread & UI 연동 제어 핸들러
    //----------------------------------------------------------------------------------------------
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


    //- 상태 진행 창 -------------------------------------------------------------------------------
    private void showProgressDialog(String szType){

        Log.i(TAG,"showProgressDialog()");
        mProgress = new ProgressDialog(this, R.style.ProgressDialog);
        mProgress.setCancelable(false);
        mProgress.setMessage(" [ " +szType+" ] " +mAppRes.getString(R.string.title_connecting));
        mProgress.show();
    }
}
