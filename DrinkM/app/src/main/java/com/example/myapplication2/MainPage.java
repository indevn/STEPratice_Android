package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class MainPage extends AppCompatActivity {

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
    private int ifNext = 0;
    private Timer timer = new Timer();

    @Override
    public void onDestroy () {
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



    private Button confirmButton;
    private Button sensButton;
    private TextView drinktype;

    private int drinkStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        /*打开串口，返回如果为-1则打开串口失败,成功返回文件描述符*/
        devfd = HardwareControler.openSerialPort(devName, speed, dataBits, stopBits);
        if (devfd >= 0) {
//            如果打开串口成功，每半秒钟执行一次task
            timer.schedule(task, 0, 500);
        } else {
//            如果是这样就是串口打开失败，会在接受区显示文字
            devfd = -1;
//            fromTextView.append("Fail to open " + devName + "!");

//            fun commonDialog(view: View) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("坏了")
                    .setMessage("串口加载失败")
                    .create();
            dialog.show();
        }


        confirmButton = (Button)findViewById(R.id.confirm_button);
        sensButton = (Button)findViewById(R.id.sens_button);
        drinktype = (TextView)findViewById(R.id.drinkType);

        drinktype.setText("检测中……");
        sensButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(MainPage.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .create();
                int rc = HardwareControler.write(devfd, "rc".getBytes());

                if (rc > 0) {
                    drinktype.setText("识别中");
                } else {
                    dialog.setTitle("出错啦");
                    dialog.setMessage("请求信息发送失败");
                }

            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                switch (drinkStatus){
                    case 0:
                        AlertDialog dialog = new AlertDialog.Builder(MainPage.this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("请重试")
                                .setMessage("未检测到饮料类型")
                                .create();
                        dialog.show();
                        break;
                    case 1:
                        AlertDialog dialog2 = new AlertDialog.Builder(MainPage.this)
                                .setIcon(R.mipmap.ic_launcher)
                                .create();
                        int oa = HardwareControler.write(devfd, "oa".getBytes());
                        if(oa>0) {
                            dialog2.setTitle("请稍等");
                            dialog2.setMessage("正在出饮料…");
                        }else{
                            dialog2.setTitle("出错啦");
                            dialog2.setMessage("请求信息发送失败");
                        }
                        dialog2.show();

                        drinktype.setText("可口可乐");
                        break;
                    case 2:
                        AlertDialog dialog3 = new AlertDialog.Builder(MainPage.this)
                                .setIcon(R.mipmap.ic_launcher)
                                .create();

                        int ob = HardwareControler.write(devfd, "ob".getBytes());

                        if(ob>0) {
                            dialog3.setTitle("请稍等");
                            dialog3.setMessage("正在出饮料…");
                        }else{
                            dialog3.setTitle("出错啦");
                            dialog3.setMessage("请求信息发送失败");
                        }
                        dialog3.show();
                        drinktype.setText("维他豆奶");
                        break;
                    default:
                        break;
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
                        byte[] buf = new byte[BUFSIZE];
//                        从缓冲区读出要查询的文件的512个字节内的东西，成功返回读取的字节数
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
                            str = str.substring(0,2);

                            if(str.equals("ca")){
                                drinkStatus=1;
                                drinktype.setText("可口可乐");
                            }else if(str.equals("cb")){
                                drinkStatus=2;
                                drinktype.setText("维他豆奶");
                            }else if(str.equals("ok")){
                                AlertDialog dialog = new AlertDialog.Builder(MainPage.this)
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle("成功")
                                        .setMessage("尽快拿取饮料")
                                        .create();
                                dialog.show();
                            }
//                            ((ScrollView) findViewById(R.id.scroolView)).fullScroll(View.FOCUS_DOWN);
                        }else{
                            AlertDialog dialog = new AlertDialog.Builder(MainPage.this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle("警告")
                                    .setMessage("接收到未知字符")
                                    .create();
                            dialog.show();
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
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