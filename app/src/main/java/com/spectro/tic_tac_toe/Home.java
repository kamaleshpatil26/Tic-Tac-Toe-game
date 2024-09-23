package com.spectro.tic_tac_toe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class Home extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView Game1 = findViewById(R.id.Game_1);
        ImageView Game2 = findViewById(R.id.Game_2);
//        ImageView Game3 = findViewById(R.id.Game_3);
//        ImageView Game4 = findViewById(R.id.Game_4);


        Glide.with(this)
                .asGif()
                .load(R.drawable.pomodoro)  // Reference to your GIF in res/drawable
                .into(Game2);

     Game1.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             // Create an Intent to start the other activity
             Intent intent = new Intent(Home.this, MainActivity.class);
             startActivity(intent);
         }
     });

     Game2.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             Intent intent = new Intent(Home.this,Pomodoro.class);
             startActivity(intent);
         }
     });

//        Game3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Home.this,Study_Music.class);
//                startActivity(intent);
//            }
//        });
//
//        Game4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Home.this,To_Do_Notes.class);
//                startActivity(intent);
//            }
//        });

    }
}