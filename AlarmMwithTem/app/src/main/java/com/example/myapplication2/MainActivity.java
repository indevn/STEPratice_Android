package com.example.myapplication2;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private Button cnMode;
//    private Button enMode;
    private ImageButton tsMode;
    private TextView helloword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent intent = null;
                intent = new Intent(MainActivity.this, Login.class);
                intent.putExtra("usrname","0");

                startActivity(intent);
            }
        });
    }
}