package com.dgsw.coloring;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dgsw.bt.BTService;
import com.dgsw.data.Constant;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.regex.Pattern;


public class ColorHelper extends AppCompatActivity {
    //--------------------------------------------------------------------------------
    //- Member Variable
    //--------------------------------------------------------------------------------
    private static final String 	    TAG                             		= "ColorHelper";

    private Button                  select_from_color_wheel;        //색상원으로 색선택하는 버튼
    private Button                  select_from_hexadecimal;        //16진수로 색선택하는 버튼

    //rgb로 색선택 하는 버튼들
    private Button                  select_from_rgb_r, select_from_rgb_g, select_from_rgb_b;
    private Button                  recommend_color_wheel_1, recommend_color_wheel_2, recommend_color_wheel_3;
    private Button                  recommend_color_wheel_4, recommend_color_wheel_5, recommend_color_wheel_6;

    //색상 추천 버튼
    private Button                  recommend_hexadecimal_1, recommend_hexadecimal_2, recommend_hexadecimal_3;
    private Button                  recommend_hexadecimal_4, recommend_hexadecimal_5, recommend_hexadecimal_6;

    private BTService               mBTService                      		= null;
    private ApplicationActivity     mAppActivity;
    private int                     RValue, GValue, BValue;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.colorhelper);

        mAppActivity    = (ApplicationActivity)this.getApplicationContext();
        mBTService      = mAppActivity.getBtService();
        mBTService.setmHandler(mHandler);

        select_from_color_wheel = (Button)findViewById(R.id.select_from_color_wheel);
        select_from_color_wheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { testColorPickerDialog();
            }
        });

        select_from_hexadecimal = (Button)findViewById(R.id.select_from_hexadecimal);
        select_from_hexadecimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { hexShow(select_from_hexadecimal);
            }
        });

        select_from_rgb_r = (Button)findViewById(R.id.select_from_rgb_r);
        select_from_rgb_g = (Button)findViewById(R.id.select_from_rgb_g);
        select_from_rgb_b = (Button)findViewById(R.id.select_from_rgb_b);

        select_from_rgb_r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { rgbShow(select_from_rgb_r, "r");
            }
        });
        select_from_rgb_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { rgbShow(select_from_rgb_g, "g");
            }
        });
        select_from_rgb_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { rgbShow(select_from_rgb_b, "b");
            }
        });

        recommend_hexadecimal_1 = (Button)findViewById(R.id.recommend_hexadecimal_1);
        recommend_hexadecimal_2 = (Button)findViewById(R.id.recommend_hexadecimal_2);
        recommend_hexadecimal_3 = (Button)findViewById(R.id.recommend_hexadecimal_3);
        recommend_hexadecimal_4 = (Button)findViewById(R.id.recommend_hexadecimal_4);
        recommend_hexadecimal_5 = (Button)findViewById(R.id.recommend_hexadecimal_5);
        recommend_hexadecimal_6 = (Button)findViewById(R.id.recommend_hexadecimal_6);


        recommend_color_wheel_1 = (Button)findViewById(R.id.recommend_color_wheel_1);
        recommend_color_wheel_2 = (Button)findViewById(R.id.recommend_color_wheel_2);
        recommend_color_wheel_3 = (Button)findViewById(R.id.recommend_color_wheel_3);
        recommend_color_wheel_4 = (Button)findViewById(R.id.recommend_color_wheel_4);
        recommend_color_wheel_5 = (Button)findViewById(R.id.recommend_color_wheel_5);
        recommend_color_wheel_6 = (Button)findViewById(R.id.recommend_color_wheel_6);
    }


    public void getClick(View v){

       switch(v.getId())
       {
           case R.id.imageView3:
                mAppActivity.sendMessage("RCDATA");
                break;
       }
    }

    void testColorPickerDialog(){
        new ColorPickerDialog.Builder(this)
                .setTitle("ColorPicker Dialog")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                //setLayoutColor(envelope);
                                String hex = "#" + envelope.getHexCode().substring(2);
                                select_from_hexadecimal.setText(hex);
                                //select_from_color_wheel.setBackgroundColor(Color.parseColor(hex));
                                select_from_color_wheel.setBackgroundColor(envelope.getColor());
                                //Toast.makeText(getApplicationContext(), "rgb:  "+envelope.getArgb() + "hex : " + envelope.getHexCode() + "color" + envelope.getColor(), Toast.LENGTH_SHORT).show();
                                RValue = Integer.parseInt(envelope.getHexCode().substring(2,4), 16); //int r = Integer.parseInt(envelope.getHexCode().substring(2,4), 16);
                                GValue = Integer.parseInt(envelope.getHexCode().substring(4,6), 16);//int g = Integer.parseInt(envelope.getHexCode().substring(4,6), 16);
                                BValue = Integer.parseInt(envelope.getHexCode().substring(6), 16);//int b = Integer.parseInt(envelope.getHexCode().substring(6), 16);

                                select_from_rgb_r.setText("r: " + RValue);
                                select_from_rgb_g.setText("g: " + GValue);
                                select_from_rgb_b.setText("b: " + BValue);

                                Log.i(TAG, "testColorPickerDialog() ");
                                mAppActivity.sendMessage("CDATA");
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .attachAlphaSlideBar(true) // default is true. If false, do not show the AlphaSlideBar.
                .attachBrightnessSlideBar(true)  // default is true. If false, do not show the BrightnessSlideBar.
                .show();
    }
   // new ColorPickerDialog.Builder(this, AlertDialog.TH);
    void hexShow(final Button result)
    {
        final EditText edittext = new EditText(this);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        builder.setTitle("16진 색상코드 입력");
        builder.setMessage("예) rrggbb");

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(9);
        edittext.setFilters(new InputFilter[] {filterAlphabet});


        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!(edittext.length() == 6))
                        {
                            Toast.makeText(getApplicationContext(),"6자리만 입력이 가능합니다." ,Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            result.setText("#"+edittext.getText());

                        }

                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();

    }


    public InputFilter filterAlphabet = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Pattern ps = Pattern.compile("^[.a-zA-Z0-9]+$");
            if(!ps.matcher(source).matches()){
                return "";
            }
            return null;
        }
    };


    void rgbShow(final Button result, final String color)
    {
        final EditText edittext = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("rgb 색상코드 입력");
        builder.setMessage("예) 111, 222, 255");

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(9);
        edittext.setFilters(new InputFilter[] {filterAlphabet});


        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        int inputVal = Integer.parseInt(edittext.getText().toString());

                         if(inputVal < 0 || inputVal > 255){
                            Toast.makeText(getApplicationContext(), "0이상 255이하의 글자만 입력이 가능합니다", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            result.setText(color + ": " + edittext.getText());
                            if(color.equals("r"))
                               RValue = Integer.parseInt( edittext.getText().toString());
                            else if(color.equals("g"))
                                GValue = Integer.parseInt( edittext.getText().toString());
                            else if(color.equals("b"))
                                BValue = Integer.parseInt( edittext.getText().toString());

                            Log.i(TAG, "R = " + RValue + " G = " + GValue + " B = " + BValue);

                            select_from_color_wheel.setBackgroundColor(Color.rgb(RValue, GValue, BValue));
                        }

                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();

    }

    //- BT Service로 메시지 전송 --------------------------------------------------------------------
    private void checkReceiveData(String message) {

        Log.i(TAG, "checkReceiveData() - " + message);
        Log.i(TAG, "checkReceiveData() - message.indexOf(\"SOK\") : " + message.indexOf("SOK"));

        if(message.indexOf("SOK")>=0) {
            Log.i(TAG, ""+RValue+","+GValue+","+BValue);
            mAppActivity.sendMessage(""+RValue+","+GValue+","+BValue);
        }else  if(message.indexOf("ROK")>=0){
            mAppActivity.sendMessage("DONE");
        }else if(message.indexOf("N:")>=0){
            //- 색상명 UI에 추가하기
            select_from_color_wheel.setText(message.substring(2));
        }else{
            // JSONG 추천색상 리스트
        }
    }

    //--------------------------------------------------------------------------------
    //- Inner Class :
    //--------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------
    //- Member Method : BT통신 Thread & UI 연동 제어 핸들러
    //-----------------------------------------------------------------------------------------
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


