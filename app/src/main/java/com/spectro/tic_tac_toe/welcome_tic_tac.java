package com.spectro.tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class welcome_tic_tac extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_tic_tac);
        getSupportActionBar().hide();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000); // Sleep for 4000 milliseconds (4 seconds)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Code to be executed after the sleep duration
                    Intent intent = new Intent(welcome_tic_tac.this,Home.class);
                    startActivity(intent);
                    finish(); // Close the current activity if needed
                }
            }
        };

        thread.start(); // Start the thread after defining it
    }
    }
