package com.example.myapplication2;
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


public class MainActivity extends AppCompatActivity {

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
    public void onDestroy() {
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

    private Button cnMode;
//    private Button enMode;
    private ImageButton tsMode;
    private TextView helloword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//            }
        }


        helloword = findViewById(R.id.helloo);
        tsMode = findViewById(R.id.imageButton);

        tsMode.setOnClickListener(new View.OnClickListener(){
            @Override
                public void onClick(View v){
                        Intent intent = null;
                        intent = new Intent(MainActivity.this, Fix.class);
                        startActivity(intent);
            }
        });

        cnMode = findViewById(R.id.cnbutton);

        cnMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ifNext=1;
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("出错啦")
                        .setMessage("请求信息发送失败")
                        .create();
                int rc = HardwareControler.write(devfd, "rd".getBytes());

                if (rc > 0) {
                    helloword.setText("检测中");
                } else {
                    dialog.show();
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


                            if(str.equals("dy")){
                                if(ifNext==1){
                                    Intent intent = null;
                                    intent = new Intent(MainActivity.this,MainPage.class);
                                    startActivity(intent);
                                }
                            }else if(str.equals("dn")){
                                helloword.setText("未检测到人");
                            }else{
                                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle("警告")
                                        .setMessage("接收到未知字符")
                                        .create();
                                dialog.show();
                            }
//                            ((ScrollView) findViewById(R.id.scroolView)).fullScroll(View.FOCUS_DOWN);
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
//
//import android.app.Activity;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ScrollView;
//import android.widget.TextView;
//import android.util.Log;
//import android.text.Html;
//import android.widget.Toast;
//import java.util.Timer;
//import java.util.TimerTask;
//import com.friendlyarm.FriendlyThings.HardwareControler;
//import com.friendlyarm.FriendlyThings.BoardType;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.content.Context;
//import android.content.Intent;
//
//
//public class MainActivity extends Activity implements OnClickListener
//{
//    private static final String TAG = "SerialPort";
//    private TextView fromTextView = null;
//    private EditText toEditor = null;
//    private final int MAXLINES = 200;
//    private StringBuilder remoteData = new StringBuilder(256 * MAXLINES);
//
//    // NanoPC-T4 UART3
//    private String devName = "/dev/ttyAMA3";// usart3:ttyAMA3
//    private int speed = 115200;
//    private int dataBits = 8;
//    private int stopBits = 1;
//    private int devfd = -1;
//
//    @Override
//    public void onDestroy() {
//        timer.cancel();
//        if (devfd != -1) {
//            HardwareControler.close(devfd);
//            devfd = -1;
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            setContentView(R.layout.serialport_dataprocessview_landscape);
//        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setContentView(R.layout.serialport_dataprocessview);
//        }
//
//        String winTitle = devName + "," + speed + "," + dataBits + "," + stopBits;
//        setTitle(winTitle);
//
//        ((Button)findViewById(R.id.sendButton)).setOnClickListener(this);
//
//        fromTextView = (TextView)findViewById(R.id.fromTextView);
//        toEditor = (EditText)findViewById(R.id.toEditor);
//
//        /* no focus when begin */
//        toEditor.clearFocus();
//        toEditor.setFocusable(false);
//        toEditor.setFocusableInTouchMode(true);
//
//        devfd = HardwareControler.openSerialPort( devName, speed, dataBits, stopBits );
//        if (devfd >= 0) {
//            timer.schedule(task, 0, 500);
//        } else {
//            devfd = -1;
//            fromTextView.append("Fail to open " + devName + "!");
//        }
//    }
//
//    private final int BUFSIZE = 512;
//    private byte[] buf = new byte[BUFSIZE];
//    private Timer timer = new Timer();
//    private Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    if (HardwareControler.select(devfd, 0, 0) == 1) {
//                        int retSize = HardwareControler.read(devfd, buf, BUFSIZE);
//                        if (retSize > 0) {
//                            String str = new String(buf, 0, retSize);
//                            remoteData.append(str);
//
//                            //Log.d(TAG, "#### LineCount: " + fromTextView.getLineCount() + ", remoteData.length()=" + remoteData.length());
//                            if (fromTextView.getLineCount() > MAXLINES) {
//                                int nLineCount = fromTextView.getLineCount();
//                                int i = 0;
//                                for (i = 0; i < remoteData.length(); i++) {
//                                    if (remoteData.charAt(i) == '\n') {
//                                        nLineCount--;
//
//                                        if (nLineCount <= MAXLINES) {
//                                            break;
//                                        }
//                                    }
//                                }
//                                remoteData.delete(0, i);
//                                //Log.d(TAG, "#### remoteData.delete(0, " + i + ")");
//                                fromTextView.setText(remoteData.toString());
//                            } else {
//                                fromTextView.append(str);
//                            }
//
//                            ((ScrollView)findViewById(R.id.scroolView)).fullScroll(View.FOCUS_DOWN);
//                        }
//                    }
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };
//    private TimerTask task = new TimerTask() {
//        public void run() {
//            Message message = new Message();
//            message.what = 1;
//            handler.sendMessage(message);
//        }
//    };
//
//    public void onClick(View v)
//    {
//        switch (v.getId()) {
//            case R.id.sendButton:
//                String str = toEditor.getText().toString();
//                if (str.length() > 0) {
//                    if (str.charAt(str.length()-1) != '\n') {
//                        str = str + "\n";
//                    }
//                    int ret = HardwareControler.write(devfd, str.getBytes());
//                    if (ret > 0) {
//                        toEditor.setText("");
//
//                        str = ">>> " + str;
//                        if (remoteData.length() > 0) {
//                            if (remoteData.charAt(remoteData.length()-1) != '\n') {
//                                remoteData.append('\n');
//                                fromTextView.append("\n");
//                            }
//                        }
//                        remoteData.append(str);
//                        fromTextView.append(str);
//
//                        ((ScrollView)findViewById(R.id.scroolView)).fullScroll(View.FOCUS_DOWN);
//                    } else {
//                        Toast.makeText(this,"Fail to send!",Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                break;
//        }
//    }
//}
