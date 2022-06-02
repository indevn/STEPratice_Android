package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.view.View;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.res.Configuration;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.Log;
import android.text.Html;
import android.widget.Toast;


import com.friendlyarm.FriendlyThings.HardwareControler;
import com.friendlyarm.FriendlyThings.BoardType;

import android.os.Handler;
import android.os.Message;
import android.content.Context;

import androidx.annotation.Nullable;


public class Fix extends AppCompatActivity {


    private static final String TAG = "SerialPort";
    /*
     * 全局声明一下
     * 定义在下面第一次用到的地方有注释
     * */
//    private TextView fromTextView = null;
    private final int MAXLINES = 200;
    //    创建容量为---的字符串缓冲区，支撑字符串的更改


//    private StringBuilder remoteData = new StringBuilder(256 * MAXLINES);

    // NanoPC-T4 UART4
    private String devName = "/dev/ttyAMA3";
    private int speed = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int devfd = -1;
    private Timer timer = new Timer();

    @Override
    public void onDestroy() {
        mp.release();
        timer.cancel();
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


    // 串口调试
    private Button spButton;

    // 颜色传感
    private Button sensorButton;
    // 距离传感
    private Button disButton;

    // 风扇温度控制
    private Button windtemButton;

    private Button soundButton;
    // 舵机
    private Button moveAButton;
    private Button moveBButton;
    private int soundPlay = 0;
    private int windStatus = 0;
    private int windtemStatus = 0;
    private TextView tstContent;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix);

        /*打开串口，返回如果为-1则打开串口失败,成功返回文件描述符*/
        devfd = HardwareControler.openSerialPort(devName, speed, dataBits, stopBits);
        if (devfd >= 0) {
//            如果打开串口成功，每半秒钟执行一次task
            timer.schedule(task, 0, 500);
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
//        windButton = findViewById(R.id.windbutton);
//        windtemButton = findViewById(R.id.windtembutton);
//
//        windButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int rc;
//                if (windStatus == 0) {
//                    rc = HardwareControler.write(devfd, "wy".getBytes());
//                    windStatus = 1;
//                } else {
//                    rc = HardwareControler.write(devfd, "wn".getBytes());
//                    windStatus = 0;
//                }
//                if (rc > 0) {
//                    tstContent.setText("正在请求风扇改变状态");
//
//                } else {
//                    AlertDialog dialog = new AlertDialog.Builder(Fix.this)
//                            .setIcon(R.mipmap.ic_launcher)
//                            .setTitle("出错啦")
//                            .setMessage("请求信息发送失败")
//                            .create();
//                    dialog.show();
//                }
//            }
//        });

//        windtemButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int rc;
//                switch (windtemStatus) {
//                    case 0:
//                        rc = HardwareControler.write(devfd, "x".getBytes());
//                        tstContent.setText("目前风扇触发温度：⬛⬜⬜⬜");
//                        windtemStatus++;
//                        if (rc > 0) {
//                            tstContent.setText("正在请求风扇");
//                        } else {
//                            AlertDialog dialog = new AlertDialog.Builder(Fix.this)
//                                    .setIcon(R.mipmap.ic_launcher)
//                                    .setTitle("出错啦")
//                                    .setMessage("请求信息发送失败")
//                                    .create();
//                            dialog.show();
//                        }
//                        break;
//                    case 1:
//                        rc = HardwareControler.write(devfd, "y".getBytes());
//                        tstContent.setText("目前风扇触发温度：⬛⬛⬜⬜");
//                        windtemStatus++;
//                        if (rc > 0) {
//                            tstContent.setText("正在请求风扇");
//                        } else {
//                            AlertDialog dialog = new AlertDialog.Builder(Fix.this)
//                                    .setIcon(R.mipmap.ic_launcher)
//                                    .setTitle("出错啦")
//                                    .setMessage("请求信息发送失败")
//                                    .create();
//                            dialog.show();
//                        }
//                        break;
//                    case 2:
//                        rc = HardwareControler.write(devfd, "z".getBytes());
//                        tstContent.setText("目前风扇触发温度：⬛⬛⬛⬜");
//                        windtemStatus++;
//                        if (rc > 0) {
//                            tstContent.setText("正在请求风扇");
//                        } else {
//                            AlertDialog dialog = new AlertDialog.Builder(Fix.this)
//                                    .setIcon(R.mipmap.ic_launcher)
//                                    .setTitle("出错啦")
//                                    .setMessage("请求信息发送失败")
//                                    .create();
//                            dialog.show();
//                        }
//                        break;
//                    case 3:
//                        rc = HardwareControler.write(devfd, "w".getBytes());
//                        tstContent.setText("目前风扇触发温度：⬛⬛⬛⬛");
//                        windtemStatus++;
//                        if (rc > 0) {
//                            tstContent.setText("正在请求风扇");
//                        } else {
//                            AlertDialog dialog = new AlertDialog.Builder(Fix.this)
//                                    .setIcon(R.mipmap.ic_launcher)
//                                    .setTitle("出错啦")
//                                    .setMessage("请求信息发送失败")
//                                    .create();
//                            dialog.show();
//                        }
//                        break;
//
//                }
//
//            }
//        });
        tstContent = findViewById(R.id.testTextView);
        spButton = findViewById(R.id.spbutton);
        spButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                intent = new Intent(Fix.this, SerialPort_Test.class);
                startActivity(intent);
            }
        });

        sensorButton = findViewById(R.id.sensorbutton);
        sensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rc = HardwareControler.write(devfd, "rc".getBytes());

                if (rc > 0) {
                    tstContent.setText("正在请求颜色传感器");
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(Fix.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("出错啦")
                            .setMessage("请求信息发送失败")
                            .create();
                    dialog.show();
                }
            }
        });

        disButton = findViewById(R.id.disbutton);
        disButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rc = HardwareControler.write(devfd, "rd".getBytes());

                if (rc > 0) {
                    tstContent.setText("正在请求距离传感器");
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(Fix.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("出错啦")
                            .setMessage("请求信息发送失败")
                            .create();
                    dialog.show();

                }
            }
        });
        moveAButton = findViewById(R.id.movebutton);
        moveAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rc = HardwareControler.write(devfd, "oa".getBytes());

                if (rc > 0) {
                    tstContent.setText("正在请求舵机A");

                } else {
                    AlertDialog dialog = new AlertDialog.Builder(Fix.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("出错啦")
                            .setMessage("请求信息发送失败")
                            .create();
                    dialog.show();
                }
            }
        });
        mp = MediaPlayer.create(Fix.this, R.raw.a);

        soundButton = findViewById(R.id.soundbutton);
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(soundPlay==0){
                    soundPlay=1;
                    mp.start();
                }else{
                    mp.pause();
                    soundPlay=0;
                }


            }

        });
    }

    private final int BUFSIZE = 512;
    //    创建一个512长度的byte数组
    //    private byte[] buf = new byte[BUFSIZE];

    private Handler handler = new Handler() {
        /*
         * MessageQueue是一个队列，存放着要发送的消息
         * 当上一个消息被发送，就会继续执行looper，继续取走下一个message，交给Handler
         * Handler收到消息后调用handleMessage发送消息
         * 这个方法是用来接受消息的，接受从MCU上发来的消息
         * */
        public void handleMessage(Message msg) {
            /*msg.what是用户定义的消息代码，便于接收消息*/
//            你用捏嘛switch呢
            switch (msg.what) {
                case 1:
                    /*如果fd有数据可读，返回1, 如果没有数据可读，返回0，出错时返回-1*/
                    if (HardwareControler.select(devfd, 0, 0) == 1) {
//                        从缓冲区读出要查询的文件的512个字节内的东西，成功返回读取的字节数
                        byte[] buf = new byte[BUFSIZE];
                        int retSize = HardwareControler.read(devfd, buf, BUFSIZE);
                        if (retSize > 0) {
                            //                        如果读出来了的东西
                            /*
                             * bytes- 要解码为字符的字节
                             * offset- 要解码的第一个字节的索引
                             * length- 要解码的字节数
                             * 把缓冲区里面存在的东西都解码
                             * */
                            String str = new String(buf, 0, retSize);
                            //                            使用remoteData保存所有要发送的东西
                            str = str.substring(0, 2);

                            // 颜色1反馈
                            if (str.equals("ca")) {
                                tstContent.setText("检测到颜色信息A");
                            }
                            // 颜色2反馈
                            else if (str.equals("cb")) {
                                tstContent.setText("检测到颜色信息B");
                            }
                            // 舵机操作反馈
                            else if (str.equals("ok")) {
                                tstContent.setText("舵机调试成功");
                            }
                            // 距离内反馈
                            else if (str.equals("dy")) {
                                tstContent.setText("在距离范围内");
                            }
                            // 距离外反馈
                            else if (str.equals("dn")) {
                                tstContent.setText("不在距离范围内");
                            }
                            // 风扇开启反馈
                            else if (str.equals("od")) {
                                tstContent.setText("已经开启风扇");
                            }
                            // 风扇关闭反馈
                            else if (str.equals("cd")) {
                                tstContent.setText("已经关闭风扇");
                            }
                            // 温度成功设置反馈
                            else if (str.equals("sd")) {
                                tstContent.setText("成功设置启动温度");
                            }
                        }
                        break;
                    }
                    super.handleMessage(msg);
            }
        }

    };

        //    定时器的回调函数
        private TimerTask task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };


}