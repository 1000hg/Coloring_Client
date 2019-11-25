package com.dgsw.coloring;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dgsw.bt.BTService;
import com.dgsw.data.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ImageHelper_2 extends AppCompatActivity {
    //----------------------------------------------------------------------------------------------
    //- Member Variable
    //----------------------------------------------------------------------------------------------
    private static final String 	     TAG                      = "ImageHelper_2";
    private static final String           TAG_2 = "imagebyte";

    private ImageView                   mImgView;
    private int                         mType                   = Constant.CAM_TYPE;
    private Intent                      mNowIntent;
    private BTService                   mBTService              = null;
    private ApplicationActivity         mAppActivity;
    private Bitmap                          image;
    private byte[]                            imageByte, imageByte_2;

    //----------------------------------------------------------------------------------------------
    //- AppCompatActivity's Override Method
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagehelper_2);

        mAppActivity    = (ApplicationActivity)this.getApplicationContext();
        mBTService      = mAppActivity.getBtService();
        mBTService.setmHandler(mHandler);

        mNowIntent = getIntent();
        mType = mNowIntent.getIntExtra("TYPE", Constant.CAM_TYPE);
        mImgView = findViewById(R.id.imgView);

        //--- 촬영 사진 또는 선택 이미지 뷰에 출력
        if(mType == Constant.CAM_TYPE) {
            Bundle extras = mNowIntent.getExtras();
            if (extras != null) {
                image = (Bitmap) extras.get("image");
                if (image != null) {
                    mImgView.setImageBitmap(image);
                    mAppActivity.sendMessage("ISEND");
                    //--------------convert Bitmap to ByteArray--------------//

                    //--------send ByteArray-------//
                }
            }
        }
        else if(mType == Constant.GAL_TYPE) {
            Uri imgUri = Uri.parse(mNowIntent.getStringExtra("image"));
            mImgView.setImageURI(imgUri);
            mAppActivity.sendMessage("ISEND");
            //--------------convert ImageUri to ByteArray--------------//
            imageByte_2 = convertImageToByte(imgUri);
            Log.i(TAG, "byteArray =>" + imageByte_2.toString());
            //--------send ByteArray-------//
            //mBTService.write(imageByte_2);
        }
    }

    //----------------------------------------------------------------------------------------------
    //- Custom Method
    //----------------------------------------------------------------------------------------------

    public void clickFunc(View v)
    {
        switch(v.getId())
        {
            case R.id.backBTN:
                 startActivity(new Intent(ImageHelper_2.this, ImageHelper.class));
                 finish();
                 break;

            case R.id.saveBTN:
                 // 현재 이미지뷰 데이터 저장
                 saveImage();
                 break;
        }
    }

    //- BT Service로 메시지 전송 --------------------------------------------------------------------
    private void checkReceiveData(String message) {

        Log.i(TAG, "checkReceiveData() - " + message);
        Log.i(TAG, "checkReceiveData() - message.indexOf(\"SOK\") : " + message.indexOf("SOK"));

        if (message.indexOf("SOK") >= 0) {
            byte[] send = getBytesFromBitmap(image);

        } else if (message.indexOf("ROK") >= 0) {
            mAppActivity.sendMessage("DONE");
        } else if (message.indexOf("IMG") >= 0) {
            //- 색상명 UI에 추가하기
        }
    }

    //-----------------------get byte function (camera Ver.)------------------------//
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress(Bitmap.CompressFormat.JPEG ,100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }

    //------------------------get byte function (gallery Ver.)-----------------------//
    public byte[] convertImageToByte(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void saveImage() {
        mImgView.buildDrawingCache();
        Bitmap bm=mImgView.getDrawingCache();
        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            File root = new File(Environment.getExternalStorageDirectory() + File.separator + "ImageHelper" + File.separator);
            root.mkdirs();
            File sdImageMainDirectory = new File(root, System.currentTimeMillis() + ".png");
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(("file://" + outputFileUri.getPath())));
            sendBroadcast(intent);
            Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
    }

    //----------------------------------------------------------------------------------------------
    //- Inner Class :
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //- Member Method : BT통신 Thread & UI 연동 제어 핸들러
    //----------------------------------------------------------------------------------------------
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg){
            switch (msg.what)
            {
                case Constant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG,"readMessage :"+readMessage);
                    checkReceiveData(readMessage);
                    break;
            }
        }
    };
}




