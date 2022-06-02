package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.friendlyarm.FriendlyThings.HardwareControler;

public class wake extends AppCompatActivity {

    // NanoPC-T4 UART4
    private String devName = "/dev/ttyAMA3";
    private int speed = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int devfd = -1;

    private Button silBtn;
    private Button retBtn;
    MediaPlayer mp;
    @Override
    public void onDestroy() {
        mp.release();
        if (devfd != -1) {
            /*
             * 如果串口是打开状态（打开状态devfd是相应的文件描述符）
             * 当页面销毁的时候要记得关闭这个串口
             * */
            HardwareControler.close(devfd);
            devfd = -1;
        }

        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake);

        devfd = HardwareControler.openSerialPort(devName, speed, dataBits, stopBits);
        if (devfd >= 0) {
//            如果打开串口成功，每半秒钟执行一次task
//            timer.schedule(task, 0, 500);
        } else {
//            如果是这样就是串口打开失败，会在接受区显示文字
            devfd = -1;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("坏了")
                    .setMessage("串口加载失败")
                    .create();
            dialog.show();
        }

        silBtn = findViewById(R.id.silbutton);
        retBtn = findViewById(R.id.returnbutton);

        mp = MediaPlayer.create(wake.this, R.raw.qc);
        mp.start();
        int rc = HardwareControler.write(devfd, "l".getBytes());
        if(rc<0){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("坏了")
                    .setMessage("数据发送失败")
                    .create();
            dialog.show();
        }
        silBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.release();
            }
        });
        retBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                intent = new Intent(wake.this, MainPage.class);
                startActivity(intent);
            }
        });
    }
}