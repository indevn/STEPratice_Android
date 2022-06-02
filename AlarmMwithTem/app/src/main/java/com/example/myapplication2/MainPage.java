package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


import com.friendlyarm.FriendlyThings.HardwareControler;

import android.os.Handler;
import android.os.Message;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainPage extends AppCompatActivity {

    private static final String TAG = "SerialPort";
    /*
     * 全局声明一下
     * 定义在下面第一次用到的地方有注释
     * */
//    private TextView fromTextView = null;
    private final int MAXLINES = 200;
    //    创建容量为---的字符串缓冲区，支撑字符串的更改

    // NanoPC-T4 UART4
    private String devName = "/dev/ttyAMA3";
    private int speed = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int devfd = -1;
    private int ifNext = 0;
    private Timer timer = new Timer();
    static final int TIME_DIALOG_ID = 0;


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


    private Button alarmButton;
    private Button switchWindButton;
    private TextView hourText;
    private TextView minuteText;
    private TextView windtemText;
    private TextView statusText;
    private int windStatus = 2;
//    private AlarmManager alarmManager;
//    static final int TIME_DIALOG_ID = 0;
    private int mHour = 0;
    private int mMinute = 0;
    AlarmManager alarmManager;
    private PendingIntent pi = null;
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Intent intent = new Intent(MainPage.this, wake.class);
        pi = PendingIntent.getActivity(MainPage.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Intent lstIntent = getIntent();
//        String str = intent.getStringExtra("usrname");

//            statusText.setText(str);

        alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);

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


        alarmButton = (Button) findViewById(R.id.alarm_set);
        switchWindButton = (Button) findViewById(R.id.switch_button);
        windtemText = findViewById(R.id.windtemText);
        hourText = findViewById(R.id.textViewHour);
        minuteText = findViewById(R.id.textViewMinute);
        statusText = findViewById(R.id.statusText);


        final int[] hour = {0};
        final int[] minutea = {0};
        int mHourOfDay, mMinute;
//        Integer minute;
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
//        switchWindButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int rc = 0;
//                switch (windStatus) {
//
//                    case 0:
//                        rc = HardwareControler.write(devfd, "x".getBytes());
//                        windtemText.setText("风扇温度：⬛⬜⬜⬜");
//                        windStatus++;
//                        break;
//                    case 1:
//                        rc = HardwareControler.write(devfd, "y".getBytes());
//
//                        windtemText.setText("风扇温度：⬛⬛⬜⬜");
//                        windStatus++;
//
//                        break;
//                    case 2:
//                        rc = HardwareControler.write(devfd, "z".getBytes());
//
//                        windtemText.setText("风扇温度：⬛⬛⬛⬜");
//                        windStatus++;
//
//                        break;
//                    case 3:
//                        rc = HardwareControler.write(devfd, "w".getBytes());
//
//                        windtemText.setText("风扇温度：⬛⬛⬛⬛");
//                        windStatus=0;
//                        break;
//                    default:
//                        break;
//                }
//                if (rc < 0) {
//                    statusText.setText("请求失败，请检查串口连接");
//                }
//            }
//        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute,
                        true);
        }
        return null;
    }
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY,hourOfDay);
            c.set(Calendar.MINUTE,minute);
            c.set(Calendar.SECOND,0);
            mHour = hourOfDay;
            mMinute = minute;
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            Log.e("HEHE",c.getTimeInMillis()+"");   //这里的时间是一个unix时间戳
            updateDisplay();

        }
    };
    private void updateDisplay() {
        hourText.setText(String.valueOf(mHour));
        minuteText.setText(String.valueOf(mMinute));
        switch(windStatus){
            case 1:
                mp = MediaPlayer.create(MainPage.this, R.raw.c1);
                mp.start();
                break;
            case 2:
                mp = MediaPlayer.create(MainPage.this, R.raw.c2);
                mp.start();
                break;
            case 3:
                mp = MediaPlayer.create(MainPage.this, R.raw.c3);
                mp.start();
                break;
            case 4:
                mp = MediaPlayer.create(MainPage.this, R.raw.c4);
                mp.start();
                break;
        }
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
                            String str = new String(buf, 0, retSize);
                            str = str.substring(0, 2);
                            if (str.equals("sd")) {
                                statusText.setText("风扇温度设置成功！");
                            }
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