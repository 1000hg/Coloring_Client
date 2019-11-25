package com.dgsw.coloring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dgsw.data.Constant;


public class ImageHelper extends AppCompatActivity {
    //----------------------------------------------------------------------------------------------
    //- Member Variable
    //----------------------------------------------------------------------------------------------
    private static final String 	TAG                 = "ImageHelper";

    private static final int    CAMERA_REQUEST     = 100;
    private static final int    GALLERY_REQUEST    = 101;

    //----------------------------------------------------------------------------------------------
    //- AppCompatActivity's Override Method
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagehelper);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST){
            if(resultCode == RESULT_OK)
            {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                Intent camINT = new Intent(ImageHelper.this, ImageHelper_2.class);
                camINT.putExtra("TYPE", Constant.CAM_TYPE);
                camINT.putExtra("image", thumbnail);
                startActivity(camINT);
                finish();
            }else{
                Log.i(TAG, " 사진 촬영 후 선택하지 않음");
                finish();
            }
        }else  if (requestCode == GALLERY_REQUEST) {

            if(resultCode == RESULT_OK)
            {
                try {
                    Uri imgUri = data.getData();
                    Intent gallINT = new Intent(ImageHelper.this, ImageHelper_2.class);
                    gallINT.putExtra("TYPE", Constant.GAL_TYPE);
                    gallINT.putExtra("image", imgUri.toString());
                    startActivity(gallINT);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "해당 이미지 파일 경로가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }else{
                finish();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    //- Custom Method
    //----------------------------------------------------------------------------------------------
    public void moveFunc(View v) {
        switch(v.getId())
        {
            case R.id.goCamera:
                 startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
                 break;

            case R.id.goGallery:
                 Intent intent = new Intent();
                 intent.setType("image/*");
                 intent.setAction(Intent.ACTION_GET_CONTENT);
                 startActivityForResult(intent, GALLERY_REQUEST);
                 break;
        }
    }
}

