package com.dgsw.coloring;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.dgsw.bt.BTService;
import com.dgsw.data.Constant;

public class ApplicationActivity extends Application {
    //--------------------------------------------------------------------------------
    //- Member Variable
    //--------------------------------------------------------------------------------
    private static final String 	    TAG    = "ApplicationActivity";

    public BTService btService;
    public Handler   workHandler;


    //----------------------------------------------------------------------------------------------
    //- Application's override Method
    //----------------------------------------------------------------------------------------------
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    //----------------------------------------------------------------------------------------------
    //- Get/Set Method
    //----------------------------------------------------------------------------------------------
    public BTService getBtService() {

        return btService;
    }

    public void setBtService(BTService btService) {

        this.btService = btService;
    }

    public Handler getWorkHandler() {
        return workHandler;
    }

    public void setWorkHandler(Handler workHandler) {
        this.workHandler = workHandler;
    }

    //- BT Service로 메시지 전송 --------------------------------------------------------------------
    public void sendMessage(String message) {

        if (btService.getState() != BTService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "sendMessage() -" + message);

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            btService.write(send);
        }
    }
}
