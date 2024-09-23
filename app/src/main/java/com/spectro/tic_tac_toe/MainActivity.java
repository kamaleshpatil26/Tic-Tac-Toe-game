package com.spectro.tic_tac_toe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private Button[] buttons = new Button[9];
    private boolean isX = true; // Track whose turn it is
    private MediaPlayer winSound;
    private MediaPlayer drawSound;
    private MediaPlayer clickSoundPlayer;
    private AdView bannerAdView;
    private InterstitialAd interstitialAd;
    private TextView statusTextView;
    private SharedPreferences sharedPreferences;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE);
        isSoundOn = sharedPreferences.getBoolean("sound_on", true);

        // Initialize AdMob SDK
        MobileAds.initialize(this, initializationStatus -> {});

        // Initialize Banner Ad
        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // Initialize Interstitial Ad
        loadInterstitialAd();

        // Initialize sounds
        winSound = MediaPlayer.create(this, R.raw.win_sound);
        drawSound = MediaPlayer.create(this, R.raw.draw_sound);
        clickSoundPlayer = MediaPlayer.create(this, R.raw.gamclick);

        // Initialize status text
        statusTextView = findViewById(R.id.statusTextView);

        // Initialize buttons
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < 9; i++) {
            String buttonID = "button" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(v -> onButtonClick((Button) v));
        }

        // Start Game Button
        Button startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(v -> {
            playClickSound();
            animateButton(startGameButton);
            startGame();
        });

        // Multiplayer Button
        Button multiplayerButton = findViewById(R.id.multiplayerButton);
        multiplayerButton.setOnClickListener(v -> {
            playClickSound();
            animateButton(multiplayerButton);
            showMultiplayerOptions();
        });

        // Sound Toggle Switch
//        Switch soundToggle = findViewById(R.id.soundToggle);
//        soundToggle.setChecked(isSoundOn);
//        soundToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            isSoundOn = isChecked;
//            sharedPreferences.edit().putBoolean("sound_on", isSoundOn).apply();
//        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit the game?")
                .setPositiveButton("Yes", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    private void animateButton(Button button) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        button.startAnimation(scaleAnimation);
    }

    private void playClickSound() {
        if (isSoundOn && clickSoundPlayer != null) {
            clickSoundPlayer.seekTo(0);
            clickSoundPlayer.start();
        }
    }

    private void onButtonClick(Button button) {
        if (button.getText().toString().equals("")) {
            button.setText(isX ? "X" : "O");
            playClickSound();
            provideHapticFeedback();

            if (checkForWinner()) {
                showResult("Player " + (isX ? "X" : "O") + " Wins!", true);
            } else if (isBoardFull()) {
                showResult("Draw!", false);
            } else {
                isX = !isX;
                statusTextView.setText(isX ? "X's Turn" : "O's Turn");
            }
        }
    }

    private void provideHapticFeedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void startGame() {
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }
        isX = true;
        provideHapticFeedback();
        statusTextView.setText("Game Started! X's Turn");

        // Enable start button again
        findViewById(R.id.startGameButton).setEnabled(true);
    }

    private void showMultiplayerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an opponent")
                .setSingleChoiceItems(new CharSequence[]{"Play with Robot", "Play with Friend"}, -1, (dialog, which) -> {
                    // Update status message to show selected option
                    String selectedOption = which == 0 ? "Play with Robot" : "Play with Friend";
                    statusTextView.setText("Selected: " + selectedOption);
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle selection based on user choice
                    int selectedOption = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    switch (selectedOption) {
                        case 0:
                            // Implement basic AI logic here
                            break;
                        case 1:
                            // Implement multiplayer logic here
                            break;
                    }
                });
        builder.create().show();
    }

    private boolean checkForWinner() {
        String[][] board = new String[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            board[row][col] = buttons[i].getText().toString();
        }

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].equals("")) {
                return true;
            }
            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].equals("")) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].equals("")) {
            return true;
        }
        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].equals("")) {
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (Button button : buttons) {
            if (button.getText().toString().equals("")) {
                return false;
            }
        }
        return true;
    }

    private void showResult(String message, boolean isWin) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);

        // Find the TextView in the custom layout
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText(message);

        // Add fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        messageTextView.startAnimation(fadeIn);

        if (isWin) {
            dialogView.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            dialogView.setBackgroundColor(getResources().getColor(R.color.red));
        }

        // Build and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    startGame();
                    // Show an ad after the game round ends
                    if (interstitialAd != null) {
                        interstitialAd.show(this);
                    } else {
                        startGame(); // Ensure game restarts if ad fails
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Play sound based on the result
        if (isWin) {
            playSound(winSound);
        } else {
            playSound(drawSound);
        }
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (isSoundOn && mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
            }
            mediaPlayer.start();
        }
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this, getString(R.string.full_screen_ad_unit_id), new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Load a new ad after the current one is dismissed
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Handle the error
                                loadInterstitialAd(); // Attempt to load another ad
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle ad load failure
                        interstitialAd = null;
                    }
                });
    }
}


// imp
//package com.spectro.tic_tac_toe;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.VibrationEffect;
//import android.os.Vibrator;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.GridLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//
//public class MainActivity extends AppCompatActivity {
//
//    private Button[] buttons = new Button[9];
//    private boolean isX = true; // Track whose turn it is
//    private MediaPlayer winSound;
//    private MediaPlayer drawSound;
//    private MediaPlayer clickSoundPlayer;
//    private AdView bannerAdView;
//    private InterstitialAd interstitialAd;
//    private TextView statusTextView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize AdMob SDK
//        MobileAds.initialize(this, initializationStatus -> {});
//
//        // Initialize Banner Ad
//        bannerAdView = findViewById(R.id.bannerAdView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        bannerAdView.loadAd(adRequest);
//
//        // Initialize Interstitial Ad
//        loadInterstitialAd();
//
//        // Initialize sounds
//        winSound = MediaPlayer.create(this, R.raw.win_sound);
//        drawSound = MediaPlayer.create(this, R.raw.draw_sound);
//        clickSoundPlayer = MediaPlayer.create(this, R.raw.gamclick);
//
//        // Initialize status text
//        statusTextView = findViewById(R.id.statusTextView);
//
//        // Initialize buttons
//        GridLayout gridLayout = findViewById(R.id.gridLayout);
//        for (int i = 0; i < 9; i++) {
//            String buttonID = "button" + (i + 1);
//            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
//            buttons[i] = findViewById(resID);
//            buttons[i].setOnClickListener(v -> onButtonClick((Button) v));
//        }
//
//        // Start Game Button
//        Button startGameButton = findViewById(R.id.startGameButton);
//        startGameButton.setOnClickListener(v -> {
//            playClickSound();
//            animateButton(startGameButton);
//            startGame();
//        });
//
//        // Multiplayer Button
//        Button multiplayerButton = findViewById(R.id.multiplayerButton);
//        multiplayerButton.setOnClickListener(v -> {
//            playClickSound();
//            animateButton(multiplayerButton);
//            showMultiplayerOptions();
//        });
//    }
//
//    @Override
//    public void onBackPressed() {
//        new AlertDialog.Builder(this)
//                .setMessage("Do you want to exit the game?")
//                .setPositiveButton("Yes", (dialog, which) -> super.onBackPressed())
//                .setNegativeButton("No", null)
//                .show();
//    }
//
//    private void animateButton(Button button) {
//        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
//        button.startAnimation(scaleAnimation);
//    }
//
//    private void playClickSound() {
//        if (clickSoundPlayer != null) {
//            clickSoundPlayer.seekTo(0);
//            clickSoundPlayer.start();
//        }
//    }
//
//    private void onButtonClick(Button button) {
//        if (button.getText().toString().equals("")) {
//            button.setText(isX ? "X" : "O");
//            playClickSound();
//            provideHapticFeedback();
//
//            if (checkForWinner()) {
//                showResult("Player " + (isX ? "X" : "O") + " Wins!", true);
//            } else if (isBoardFull()) {
//                showResult("Draw!", false);
//            } else {
//                isX = !isX;
//                statusTextView.setText(isX ? "X's Turn" : "O's Turn");
//            }
//        }
//    }
//
//    private void provideHapticFeedback() {
//        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        if (vibrator != null && vibrator.hasVibrator()) {
//            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
//        }
//    }
//
//    private void startGame() {
//        for (Button button : buttons) {
//            button.setText("");
//            button.setEnabled(true);
//        }
//        isX = true;
//        provideHapticFeedback();
//        statusTextView.setText("Game Started! X's Turn");
//
//        // Enable start button again
//        findViewById(R.id.startGameButton).setEnabled(true);
//    }
//
//    private void showMultiplayerOptions() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose an opponent")
//                .setItems(new CharSequence[]{"Play with Robot", "Play with Friend"}, (dialog, which) -> {
//                    switch (which) {
//                        case 0:
//                            // Implement basic AI logic here
//                            break;
//                        case 1:
//                            // Implement multiplayer logic here
//                            break;
//                    }
//                });
//        builder.create().show();
//    }
//
//    private boolean checkForWinner() {
//        String[][] board = new String[3][3];
//        for (int i = 0; i < 9; i++) {
//            int row = i / 3;
//            int col = i % 3;
//            board[row][col] = buttons[i].getText().toString();
//        }
//
//        // Check rows and columns
//        for (int i = 0; i < 3; i++) {
//            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].equals("")) {
//                return true;
//            }
//            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].equals("")) {
//                return true;
//            }
//        }
//
//        // Check diagonals
//        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].equals("")) {
//            return true;
//        }
//        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].equals("")) {
//            return true;
//        }
//
//        return false;
//    }
//
//    private boolean isBoardFull() {
//        for (Button button : buttons) {
//            if (button.getText().toString().equals("")) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void showResult(String message, boolean isWin) {
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
//
//        // Find the TextView in the custom layout
//        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
//        messageTextView.setText(message);
//
//        // Add fade-in animation
//        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
//        messageTextView.startAnimation(fadeIn);
//
//        if (isWin) {
//            dialogView.setBackgroundColor(getResources().getColor(R.color.green));
//        } else {
//            dialogView.setBackgroundColor(getResources().getColor(R.color.red));
//        }
//
//        // Build and show the AlertDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setView(dialogView)
//                .setPositiveButton("OK", (dialog, which) -> {
//                    startGame();
//                    // Show an ad after the game round ends
//                    if (interstitialAd != null) {
//                        interstitialAd.show(this);
//                    } else {
//                        startGame(); // Ensure game restarts if ad fails
//                    }
//                });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // Play sound based on the result
//        if (isWin) {
//            playSound(winSound);
//        } else {
//            playSound(drawSound);
//        }
//    }
//
//    private void playSound(MediaPlayer mediaPlayer) {
//        if (mediaPlayer != null) {
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.prepareAsync();
//            }
//            mediaPlayer.start();
//        }
//    }
//
//    private void loadInterstitialAd() {
//        InterstitialAd.load(this, getString(R.string.full_screen_ad_unit_id), new AdRequest.Builder().build(),
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd ad) {
//                        interstitialAd = ad;
//                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                // Load a new ad after the current one is dismissed
//                                loadInterstitialAd();
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(AdError adError) {
//                                // Handle the error
//                                loadInterstitialAd(); // Attempt to load another ad
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle ad load failure
//                        interstitialAd = null;
//                    }
//                });
//    }
//}


//
//package com.spectro.tic_tac_toe;
//
//import android.app.AlertDialog;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.GridLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//
//public class MainActivity extends AppCompatActivity {
//
//    private Button[] buttons = new Button[9];
//    private boolean isX = true; // Track whose turn it is
//    private MediaPlayer winSound;
//    private MediaPlayer drawSound;
//    private MediaPlayer clickSoundPlayer;
//    private AdView bannerAdView;
//    private InterstitialAd interstitialAd;
//    private long lastAdTime;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize AdMob SDK
//        MobileAds.initialize(this, initializationStatus -> {});
//
//        // Initialize Banner Ad
//        bannerAdView = findViewById(R.id.bannerAdView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        bannerAdView.loadAd(adRequest);
//
//        // Initialize Interstitial Ad
//        loadInterstitialAd();
//
//        // Initialize sounds
//        winSound = MediaPlayer.create(this, R.raw.win_sound);
//        drawSound = MediaPlayer.create(this, R.raw.draw_sound);
//        clickSoundPlayer = MediaPlayer.create(this, R.raw.gamclick);
//
//        // Initialize buttons
//        GridLayout gridLayout = findViewById(R.id.gridLayout);
//        for (int i = 0; i < 9; i++) {
//            String buttonID = "button" + (i + 1);
//            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
//            buttons[i] = findViewById(resID);
//            playClickSound();
//            buttons[i].setOnClickListener(v -> {
//                onButtonClick((Button) v);
//            });
//        }
//
//        // Start Game Button
//        Button startGameButton = findViewById(R.id.startGameButton);
//        startGameButton.setOnClickListener(v -> {
//            playClickSound();
//            animateButton(startGameButton);
//            startGame();
//        });
//
////        // Leaderboard Button
////        Button leaderboardButton = findViewById(R.id.leaderboardButton);
////        leaderboardButton.setOnClickListener(v -> {
////
////            showLeaderboard();
////        });
//
//        // Multiplayer Button
//        Button multiplayerButton = findViewById(R.id.multiplayerButton);
//        multiplayerButton.setOnClickListener(v -> {
//            playClickSound();
//            animateButton(multiplayerButton);
//            showMultiplayerOptions();
//        });
//
//        // Display fullscreen ad periodically
//        lastAdTime = System.currentTimeMillis();
//        displayAdPeriodically();
//    }
//    private void animateButton(Button button) {
//
//        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
//        button.startAnimation(scaleAnimation);
//    }
//    private void playClickSound() {
//        if (clickSoundPlayer != null) {
//            if (clickSoundPlayer.isPlaying()) {
//                clickSoundPlayer.stop();
//                clickSoundPlayer.prepareAsync();
//            }
//            clickSoundPlayer.start();
//        }
//    }
//
//    private void onButtonClick(Button button) {
//        if (button.getText().toString().equals("")) {
//            button.setText(isX ? "X" : "O");
//            if (checkForWinner()) {
//                showResult("Player " + (isX ? "X" : "O") + " Wins!", true);
//            } else if (isBoardFull()) {
//                showResult("Draw!", false);
//            }
//            isX = !isX;
//        }
//    }
//
//    private void startGame() {
//        for (Button button : buttons) {
//            button.setText("");
//        }
//        isX = true;
//    }
//
//    private void showLeaderboard() {
//        // Implement leaderboard functionality here
//    }
//
//    private void showMultiplayerOptions() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose an opponent")
//                .setItems(new CharSequence[]{"Play with Robot", "Play with Friend"}, (dialog, which) -> {
//                    switch (which) {
//                        case 0:
//                            // Play with Robot
//                            break;
//                        case 1:
//                            // Play with Friend
//                            break;
//                    }
//                });
//        builder.create().show();
//    }
//
//    private boolean checkForWinner() {
//        String[][] board = new String[3][3];
//        for (int i = 0; i < 9; i++) {
//            int row = i / 3;
//            int col = i % 3;
//            board[row][col] = buttons[i].getText().toString();
//        }
//
//        // Check rows and columns
//        for (int i = 0; i < 3; i++) {
//            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].equals("")) {
//                return true;
//            }
//            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].equals("")) {
//                return true;
//            }
//        }
//
//        // Check diagonals
//        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].equals("")) {
//            return true;
//        }
//        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].equals("")) {
//            return true;
//        }
//
//        return false;
//    }
//
//    private boolean isBoardFull() {
//        for (Button button : buttons) {
//            if (button.getText().toString().equals("")) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void showResult(String message, boolean isWin) {
//        // Inflate the custom layout for the dialog
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
//
//        // Find the TextView in the custom layout
//        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
//
//        // Set the message and background color based on the result
//        messageTextView.setText(message);
//        if (isWin) {
//            dialogView.setBackgroundColor(getResources().getColor(R.color.green));
//        } else {
//            dialogView.setBackgroundColor(getResources().getColor(R.color.red));
//        }
//
//        // Build and show the AlertDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setView(dialogView)
//                .setPositiveButton("OK", (dialog, which) -> {
//                    startGame();
//                    if (interstitialAd != null) {
//                        interstitialAd.show(this);
//                    }
//                    displayAdPeriodically();
//                });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // Play sound based on the result
//        if (isWin) {
//            winSound.start();
//        } else {
//            drawSound.start();
//        }
//    }
//
//    private void displayAdPeriodically() {
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastAdTime >= 5 * 60 * 1000) { // 5 minutes
//            if (interstitialAd != null) {
//                interstitialAd.show(this);
//            }
//            lastAdTime = currentTime;
//        }
//    }
//
//    private void loadInterstitialAd() {
//        InterstitialAd.load(this, getString(R.string.full_screen_ad_unit_id), new AdRequest.Builder().build(),
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd ad) {
//                        interstitialAd = ad;
//                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                // Load a new ad when the current one is dismissed
//                                loadInterstitialAd();
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(AdError adError) {
//                                // Handle the error
//                            }
//
//                            @Override
//                            public void onAdShowedFullScreenContent() {
//                                // Called when ad is shown
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
//                        // Handle the error
//                    }
//                });
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (winSound.isPlaying()) {
//            winSound.pause();
//        }
//        if (drawSound.isPlaying()) {
//            drawSound.pause();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (winSound != null) {
//            winSound.release();
//        }
//        if (drawSound != null) {
//            drawSound.release();
//        }
//        if (clickSoundPlayer != null) {
//            clickSoundPlayer.release();
//        }
//    }
//}
//
//
