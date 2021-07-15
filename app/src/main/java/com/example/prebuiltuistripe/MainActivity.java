package com.example.prebuiltuistripe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button payButton;
    Button cardButton;
    Button cardButtontwo;
    Button cardButtonthree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payButton = (Button)findViewById(R.id.paybutton);
        cardButton = (Button)findViewById(R.id.cardButton);
        cardButtontwo = (Button)findViewById(R.id.cardButtontwo);
        cardButtonthree = (Button)findViewById(R.id.prebuiltmulti);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CheckoutActivity.class);
                startActivity(intent);
            }
        });

        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CardActivity.class);
                startActivity(intent);
            }
        });
        cardButtontwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CardLatestActivity.class);
                startActivity(intent);
            }
        });

        cardButtonthree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PrebuiltMulti.class);
                startActivity(intent);
            }
        });

    }
}