package com.spectro.tic_tac_toe;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

public class Pomodoro extends AppCompatActivity {

    private TextView timerDisplay, workDurationLabel;
    private SeekBar workDurationSeekBar;
    private ImageView clock_0;
    private Button startButton, pauseButton, resetButton, selectSoundButton, stopSoundButton;

    private Handler handler = new Handler();
    private Runnable runnable;
    private long workTimeInMillis;
    private long timeLeftInMillis;
    private boolean isRunning = false;
    private boolean isWorkTime = true;
    private MediaPlayer clickSoundPlayer;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Uri selectedSoundUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        clock_0 = findViewById(R.id.Clock_0);
        Glide.with(this)
                .asGif()
                .load(R.drawable.pomodoro)  // Reference to your GIF in res/drawable
                .into(clock_0);

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {

        timerDisplay = findViewById(R.id.timer_display);
        workDurationSeekBar = findViewById(R.id.work_duration_seekbar);
        workDurationLabel = findViewById(R.id.work_duration_label);
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        resetButton = findViewById(R.id.reset_button);
        selectSoundButton = findViewById(R.id.select_sound_button);
        stopSoundButton = findViewById(R.id.stop_sound_button);
        mediaPlayer = MediaPlayer.create(this, R.raw.win_sound);
        clickSoundPlayer = MediaPlayer.create(this, R.raw.rclick);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    private void playClickSound() {
        if (clickSoundPlayer != null) {
            clickSoundPlayer.start();
        }
    }

    private void setupListeners() {
        workDurationSeekBar.setOnSeekBarChangeListener(new DurationSeekBarChangeListener());

        startButton.setOnClickListener(v -> {
            playClickSound();
            animateButton(startButton);
            startTimer();
        });
        pauseButton.setOnClickListener(v -> {
            animateButton(pauseButton);
            playClickSound();
            pauseTimer();
        });
        resetButton.setOnClickListener(v -> {
            animateButton(resetButton);
            playClickSound();
            resetTimer();
        });
        selectSoundButton.setOnClickListener(v -> {
            animateButton(selectSoundButton);
            playClickSound();
            openFilePicker();
        });
        stopSoundButton.setOnClickListener(v -> {
            animateButton(stopSoundButton);
            playClickSound();
            stopSound();
        });
    }

    private class DurationSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            workDurationLabel.setText("Work Duration: " + progress + " mins");
            if (!isRunning) {
                workTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
                if (isWorkTime) {
                    timeLeftInMillis = workTimeInMillis;
                    updateTimerDisplay();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private void startTimer() {
        if (isRunning) return;

        long workMinutes = workDurationSeekBar.getProgress();
        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);

        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;

        isRunning = true;
        runnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeftInMillis <= 0) {
                    toggleTimer();
                    return;
                }
                timeLeftInMillis -= 1000;
                updateTimerDisplay();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void pauseTimer() {
        if (!isRunning) return;

        isRunning = false;
        handler.removeCallbacks(runnable);
    }

    private void resetTimer() {
        isRunning = false;
        handler.removeCallbacks(runnable);

        workTimeInMillis = TimeUnit.MINUTES.toMillis(workDurationSeekBar.getProgress());
        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;
        updateTimerDisplay();
    }

    private void toggleTimer() {
        isWorkTime = !isWorkTime;
        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;
        updateTimerDisplay();
        playNotification();
    }

    private void playNotification() {
        if (selectedSoundUri != null) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, selectedSoundUri);
            mediaPlayer.start();
        }
        if (vibrator != null) {
            vibrator.vibrate(1000);
        }
    }

    private void stopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateTimerDisplay() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60;
        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        audioPickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> audioPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedAudioUri = result.getData().getData();
                    if (selectedAudioUri != null) {
                        selectedSoundUri = selectedAudioUri;
                        Toast.makeText(Pomodoro.this, "Music file selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void animateButton(Button button) {

        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        button.startAnimation(scaleAnimation);
    }

    @Override
    protected void onDestroy() {
        stopSound(); // Stop sound if playing
        if (clickSoundPlayer != null) {
            clickSoundPlayer.release();
            clickSoundPlayer = null;
        }
        super.onDestroy();
    }
}

//package com.spectro.tic_tac_toe;
//
//import android.content.Context;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Vibrator;
//import android.provider.MediaStore;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//
//import java.util.concurrent.TimeUnit;
//
//public class Pomodoro extends AppCompatActivity {
//
//    private TextView timerDisplay, workDurationLabel;
//    private ImageView Game3;
//    private SeekBar workDurationSeekBar;
//    private Button startButton, pauseButton, resetButton, selectSoundButton, stopSoundButton;
//
//    private Handler handler = new Handler();
//    private Runnable runnable;
//    private long workTimeInMillis;
//    private long timeLeftInMillis;
//    private boolean isRunning = false;
//    private boolean isWorkTime = true;
//
//    private MediaPlayer mediaPlayer;
//    private Vibrator vibrator;
//    private Uri selectedSoundUri;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pomodoro);
//
//        Game3 = findViewById(R.id.Game_3);
//
//        Glide.with(this)
//                .asGif()
//                .load(R.drawable.pomodoro)  // Reference to your GIF in res/drawable
//                .into(Game3);
//
//        initializeUI();
//        setupListeners();
//    }
//
//    private void initializeUI() {
//        timerDisplay = findViewById(R.id.timer_display);
//        workDurationSeekBar = findViewById(R.id.work_duration_seekbar);
//        workDurationLabel = findViewById(R.id.work_duration_label);
//        startButton = findViewById(R.id.start_button);
//        pauseButton = findViewById(R.id.pause_button);
//        resetButton = findViewById(R.id.reset_button);
//        selectSoundButton = findViewById(R.id.select_sound_button);
//        stopSoundButton = findViewById(R.id.stop_sound_button);
//        mediaPlayer = MediaPlayer.create(this, R.raw.win_sound);
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//    }
//
//    private void setupListeners() {
//        workDurationSeekBar.setOnSeekBarChangeListener(new DurationSeekBarChangeListener());
//
//        startButton.setOnClickListener(v -> startTimer());
//        pauseButton.setOnClickListener(v -> pauseTimer());
//        resetButton.setOnClickListener(v -> resetTimer());
//        selectSoundButton.setOnClickListener(v -> openFilePicker());
//        stopSoundButton.setOnClickListener(v -> stopSound());
//    }
//
//    private class DurationSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            workDurationLabel.setText("Work Duration: " + progress + " mins");
//            if (!isRunning) {
//                workTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
//                if (isWorkTime) {
//                    timeLeftInMillis = workTimeInMillis;
//                    updateTimerDisplay();
//                }
//            }
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {}
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {}
//    }
//
//    private void startTimer() {
//        if (isRunning) return;
//
//        long workMinutes = workDurationSeekBar.getProgress();
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
//
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;
//
//        isRunning = true;
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (timeLeftInMillis <= 0) {
//                    toggleTimer();
//                    return;
//                }
//                timeLeftInMillis -= 1000;
//                updateTimerDisplay();
//                handler.postDelayed(this, 1000);
//            }
//
//        };
//        handler.post(runnable);
//    }
//
//    private void pauseTimer() {
//        if (!isRunning) return;
//
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//    }
//
//    private void resetTimer() {
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workDurationSeekBar.getProgress());
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;
//        updateTimerDisplay();
//    }
//
//    private void toggleTimer() {
//        isWorkTime = !isWorkTime;
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : workTimeInMillis;
//        updateTimerDisplay();
//        playNotification();
//    }
//
//    private void playNotification() {
//        if (selectedSoundUri != null) {
//            if (mediaPlayer != null) {
//                mediaPlayer.release();
//            }
//            mediaPlayer = MediaPlayer.create(this, selectedSoundUri);
//            mediaPlayer.start();
//        }
//        if (vibrator != null) {
//            vibrator.vibrate(1000);
//        }
//    }
//
//    private void stopSound() {
//        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//    }
//
//    private void updateTimerDisplay() {
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60;
//        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//        audioPickerLauncher.launch(intent);
//    }
//
//    private final ActivityResultLauncher<Intent> audioPickerLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                    Uri selectedAudioUri = result.getData().getData();
//                    if (selectedAudioUri != null) {
//                        selectedSoundUri = selectedAudioUri;
//                        Toast.makeText(Pomodoro.this, "Sound file selected", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//    @Override
//    protected void onDestroy() {
//        stopSound(); // Stop sound if playing
//        super.onDestroy();
//    }
//}


//package com.spectro.tic_tac_toe;
//
//import android.content.Context;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Vibrator;
//import android.provider.MediaStore;
//import android.widget.Button;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.concurrent.TimeUnit;
//
//public class Pomodoro extends AppCompatActivity {
//
//    private TextView timerDisplay, workDurationLabel, breakDurationLabel;
//    private SeekBar workDurationSeekBar, breakDurationSeekBar;
//    private Button startButton, pauseButton, resetButton, selectSoundButton;
//
//    private Handler handler = new Handler();
//    private Runnable runnable;
//    private long workTimeInMillis;
//    private long breakTimeInMillis;
//    private long timeLeftInMillis;
//    private boolean isRunning = false;
//    private boolean isWorkTime = true;
//
//    private MediaPlayer mediaPlayer;
//    private Vibrator vibrator;
//    private Uri selectedSoundUri;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pomodoro);
//
//        initializeUI();
//        setupListeners();
//
//        if (savedInstanceState != null) {
//            restoreState(savedInstanceState);
//        }
//    }
//
//    private void initializeUI() {
//        timerDisplay = findViewById(R.id.timer_display);
//        workDurationSeekBar = findViewById(R.id.work_duration_seekbar);
//        breakDurationSeekBar = findViewById(R.id.break_duration_seekbar);
//        workDurationLabel = findViewById(R.id.work_duration_label);
//        breakDurationLabel = findViewById(R.id.break_duration_label);
//        startButton = findViewById(R.id.start_button);
//        pauseButton = findViewById(R.id.pause_button);
//        resetButton = findViewById(R.id.reset_button);
//        selectSoundButton = findViewById(R.id.select_sound_button);
//        mediaPlayer = MediaPlayer.create(this, R.raw.win_sound);
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//    }
//
//    private void setupListeners() {
//        workDurationSeekBar.setOnSeekBarChangeListener(new DurationSeekBarChangeListener(true));
//        breakDurationSeekBar.setOnSeekBarChangeListener(new DurationSeekBarChangeListener(false));
//
//        startButton.setOnClickListener(v -> startTimer());
//        pauseButton.setOnClickListener(v -> pauseTimer());
//        resetButton.setOnClickListener(v -> resetTimer());
//        selectSoundButton.setOnClickListener(v -> openFilePicker());
//    }
//
//    private class DurationSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
//        private final boolean isWorkSeekBar;
//
//        DurationSeekBarChangeListener(boolean isWorkSeekBar) {
//            this.isWorkSeekBar = isWorkSeekBar;
//        }
//
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            if (isWorkSeekBar) {
//                workDurationLabel.setText("Work Duration: " + progress + " mins");
//                if (!isRunning && isWorkTime) {
//                    workTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
//                    timeLeftInMillis = workTimeInMillis;
//                    updateTimerDisplay();
//                }
//            } else {
//                breakDurationLabel.setText("Break Duration: " + progress + " mins");
//                if (!isRunning && !isWorkTime) {
//                    breakTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
//                    timeLeftInMillis = breakTimeInMillis;
//                    updateTimerDisplay();
//                }
//            }
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {}
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {}
//    }
//
//    private void startTimer() {
//        if (isRunning) return;
//
//        long workMinutes = workDurationSeekBar.getProgress();
//        long breakMinutes = breakDurationSeekBar.getProgress();
//
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
//        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakMinutes);
//
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
//
//        isRunning = true;
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (timeLeftInMillis <= 0) {
//                    toggleTimer();
//                    return;
//                }
//                timeLeftInMillis -= 1000;
//                updateTimerDisplay();
//                handler.postDelayed(this, 1000);
//            }
//        };
//        handler.post(runnable);
//    }
//
//    private void pauseTimer() {
//        if (!isRunning) return;
//
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//    }
//
//    private void resetTimer() {
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workDurationSeekBar.getProgress());
//        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakDurationSeekBar.getProgress());
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
//        updateTimerDisplay();
//    }
//
//    private void toggleTimer() {
//        isWorkTime = !isWorkTime;
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
//        updateTimerDisplay();
//        playNotification();
//    }
//
//    private void playNotification() {
//        if (selectedSoundUri != null) {
//            if (mediaPlayer != null) {
//                mediaPlayer.release();
//            }
//            mediaPlayer = MediaPlayer.create(this, selectedSoundUri);
//            mediaPlayer.start();
//        }
//        if (vibrator != null) {
//            vibrator.vibrate(1000);
//        }
//    }
//
//    private void updateTimerDisplay() {
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60;
//        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//        audioPickerLauncher.launch(intent);
//    }
//
//    private final ActivityResultLauncher<Intent> audioPickerLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                    Uri selectedAudioUri = result.getData().getData();
//                    if (selectedAudioUri != null) {
//                        selectedSoundUri = selectedAudioUri;
//                        Toast.makeText(Pomodoro.this, "Sound file selected", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putLong("workTimeInMillis", workTimeInMillis);
//        outState.putLong("breakTimeInMillis", breakTimeInMillis);
//        outState.putLong("timeLeftInMillis", timeLeftInMillis);
//        outState.putBoolean("isRunning", isRunning);
//        outState.putBoolean("isWorkTime", isWorkTime);
//        if (selectedSoundUri != null) {
//            outState.putString("selectedSoundUri", selectedSoundUri.toString());
//        }
//    }
//
//    private void restoreState(Bundle savedInstanceState) {
//        workTimeInMillis = savedInstanceState.getLong("workTimeInMillis");
//        breakTimeInMillis = savedInstanceState.getLong("breakTimeInMillis");
//        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
//        isRunning = savedInstanceState.getBoolean("isRunning");
//        isWorkTime = savedInstanceState.getBoolean("isWorkTime");
//        if (savedInstanceState.containsKey("selectedSoundUri")) {
//            selectedSoundUri = Uri.parse(savedInstanceState.getString("selectedSoundUri"));
//        }
//        updateTimerDisplay();
//        if (isRunning) {
//            startTimer();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//        super.onDestroy();
//    }
//}


//package com.spectro.tic_tac_toe;
//
//import android.content.Context;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Vibrator;
//import android.provider.MediaStore;
//import android.widget.Button;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.concurrent.TimeUnit;
//
//public class Pomodoro extends AppCompatActivity {
//
//    private TextView timerDisplay, workDurationLabel, breakDurationLabel;
//    private SeekBar workDurationSeekBar, breakDurationSeekBar;
//    private Button startButton, pauseButton, resetButton, selectSoundButton;
//
//    private Handler handler = new Handler();
//    private Runnable runnable;
//    private long workTimeInMillis;
//    private long breakTimeInMillis;
//    private long timeLeftInMillis;
//    private boolean isRunning = false;
//    private boolean isWorkTime = true;
//
//    private MediaPlayer mediaPlayer;
//    private Vibrator vibrator;
//    private Uri selectedSoundUri;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pomodoro);
//
//        timerDisplay = findViewById(R.id.timer_display);
//        workDurationSeekBar = findViewById(R.id.work_duration_seekbar);
//        breakDurationSeekBar = findViewById(R.id.break_duration_seekbar);
//        workDurationLabel = findViewById(R.id.work_duration_label);
//        breakDurationLabel = findViewById(R.id.break_duration_label);
//        startButton = findViewById(R.id.start_button);
//        pauseButton = findViewById(R.id.pause_button);
//        resetButton = findViewById(R.id.reset_button);
//        selectSoundButton = findViewById(R.id.select_sound_button);
//        mediaPlayer = MediaPlayer.create(this, R.raw.win_sound);
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//
//        setupSeekBarListeners();
//
//        startButton.setOnClickListener(v -> startTimer());
//
//        pauseButton.setOnClickListener(v -> pauseTimer());
//
//        resetButton.setOnClickListener(v -> resetTimer());
//
//        selectSoundButton.setOnClickListener(v -> openFilePicker());
//    }
//
//    private void setupSeekBarListeners() {
//        workDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                workDurationLabel.setText("Work Duration: " + progress + " mins");
//                if (!isRunning) {
//                    workTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
//                    if (isWorkTime) {
//                        timeLeftInMillis = workTimeInMillis;
//                        updateTimerDisplay();
//                    }
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//        breakDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                breakDurationLabel.setText("Break Duration: " + progress + " mins");
//                if (!isRunning) {
//                    breakTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
//                    if (!isWorkTime) {
//                        timeLeftInMillis = breakTimeInMillis;
//                        updateTimerDisplay();
//                    }
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//    }
//
//    private void startTimer() {
//        if (isRunning) return;
//
//        long workMinutes = workDurationSeekBar.getProgress();
//        long breakMinutes = breakDurationSeekBar.getProgress();
//
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
//        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakMinutes);
//
//        if (isWorkTime) {
//            timeLeftInMillis = workTimeInMillis;
//        } else {
//            timeLeftInMillis = breakTimeInMillis;
//        }
//
//        isRunning = true;
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (timeLeftInMillis <= 0) {
//                    toggleTimer();
//                    return;
//                }
//
//                timeLeftInMillis -= 1000;
//                updateTimerDisplay();
//                handler.postDelayed(this, 1000);
//            }
//        };
//        handler.post(runnable);
//    }
//
//    private void pauseTimer() {
//        if (!isRunning) return;
//
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//    }
//
//    private void resetTimer() {
//        isRunning = false;
//        handler.removeCallbacks(runnable);
//
//        long workMinutes = workDurationSeekBar.getProgress();
//        long breakMinutes = breakDurationSeekBar.getProgress();
//
//        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
//        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakMinutes);
//
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
//        updateTimerDisplay();
//    }
//
//    private void toggleTimer() {
//        isWorkTime = !isWorkTime;
//        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
//        updateTimerDisplay();
//        playNotification();
//    }
//
//    private void playNotification() {
//        if (selectedSoundUri != null) {
//            if (mediaPlayer != null) {
//                mediaPlayer.release();
//            }
//            mediaPlayer = MediaPlayer.create(this, selectedSoundUri);
//            mediaPlayer.start();
//        }
//        if (vibrator != null) {
//            vibrator.vibrate(1000);
//        }
//    }
//
//    private void updateTimerDisplay() {
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60;
//        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//        audioPickerLauncher.launch(intent);
//    }
//
//    private final ActivityResultLauncher<Intent> audioPickerLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                    Uri selectedAudioUri = result.getData().getData();
//                    if (selectedAudioUri != null) {
//                        selectedSoundUri = selectedAudioUri;
//                        Toast.makeText(Pomodoro.this, "Sound file selected", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//    @Override
//    protected void onDestroy() {
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//        super.onDestroy();
//    }
//}

//
////package com.spectro.tic_tac_toe;
////import android.content.Context;
////import android.content.Intent;
////import android.media.MediaPlayer;
////import android.net.Uri;
////import android.os.Bundle;
////import android.os.Handler;
////import android.os.Vibrator;
////import android.provider.MediaStore;
////import android.view.View;
////import android.widget.Button;
////import android.widget.SeekBar;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.activity.result.ActivityResultLauncher;
////import androidx.activity.result.contract.ActivityResultContracts;
////import androidx.appcompat.app.AppCompatActivity;
////import java.util.concurrent.TimeUnit;
////
////public class Pomodoro extends AppCompatActivity {
////
////    private TextView timerDisplay, workDurationLabel, breakDurationLabel;
////    private SeekBar workDurationSeekBar, breakDurationSeekBar;
////    private Button startButton, pauseButton, resetButton, selectSoundButton;
////
////    private Handler handler = new Handler();
////    private Runnable runnable;
////    private long workTimeInMillis;
////    private long breakTimeInMillis;
////    private long timeLeftInMillis;
////    private boolean isRunning = false;
////    private boolean isWorkTime = true;
////
////    private MediaPlayer mediaPlayer;
////    private Vibrator vibrator;
////    private Uri selectedSoundUri; // Store selected sound URI
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_pomodoro);
////
////        timerDisplay = findViewById(R.id.timer_display);
////        workDurationSeekBar = findViewById(R.id.work_duration_seekbar);
////        breakDurationSeekBar = findViewById(R.id.break_duration_seekbar);
////        workDurationLabel = findViewById(R.id.work_duration_label);
////        breakDurationLabel = findViewById(R.id.break_duration_label);
////        startButton = findViewById(R.id.start_button);
////        pauseButton = findViewById(R.id.pause_button);
////        resetButton = findViewById(R.id.reset_button);
////        selectSoundButton = findViewById(R.id.select_sound_button); // Add this button in XML
////        mediaPlayer = MediaPlayer.create(this, R.raw.win_sound); // Add your sound file to res/raw
////        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
////
////        setupSeekBarListeners();
////
////        startButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                startTimer();
////            }
////        });
////
////        pauseButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                pauseTimer();
////            }
////        });
////
////        resetButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                resetTimer();
////            }
////        });
////    }
////
////    private void setupSeekBarListeners() {
////        workDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////            @Override
////            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                workDurationLabel.setText("Work Duration: " + progress + " mins");
////                if (!isRunning) {
////                    workTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
////                    if (isWorkTime) {
////                        timeLeftInMillis = workTimeInMillis;
////                        updateTimerDisplay();
////                    }
////                }
////            }
////
////            @Override
////            public void onStartTrackingTouch(SeekBar seekBar) {
////            }
////
////            @Override
////            public void onStopTrackingTouch(SeekBar seekBar) {
////            }
////        });
////
////        breakDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////            @Override
////            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                breakDurationLabel.setText("Break Duration: " + progress + " mins");
////                if (!isRunning) {
////                    breakTimeInMillis = TimeUnit.MINUTES.toMillis(progress);
////                    if (!isWorkTime) {
////                        timeLeftInMillis = breakTimeInMillis;
////                        updateTimerDisplay();
////                    }
////                }
////            }
////
////            @Override
////            public void onStartTrackingTouch(SeekBar seekBar) {
////            }
////
////            @Override
////            public void onStopTrackingTouch(SeekBar seekBar) {
////            }
////        });
////    }
////
////    private void startTimer() {
////        if (isRunning) return; // Timer is already running
////
////        // Set time based on current SeekBar values
////        long workMinutes = workDurationSeekBar.getProgress();
////        long breakMinutes = breakDurationSeekBar.getProgress();
////
////        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
////        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakMinutes);
////
////        if (isWorkTime) {
////            timeLeftInMillis = workTimeInMillis;
////        } else {
////            timeLeftInMillis = breakTimeInMillis;
////        }
////
////        isRunning = true;
////        runnable = new Runnable() {
////            @Override
////            public void run() {
////                if (timeLeftInMillis <= 0) {
////                    toggleTimer();
////                    return;
////                }
////
////                timeLeftInMillis -= 1000; // Reduce 1 second
////                updateTimerDisplay();
////                handler.postDelayed(this, 1000); // Run every second
////            }
////        };
////        handler.post(runnable);
////    }
////
////    private void pauseTimer() {
////        if (!isRunning) return; // Timer is not running
////
////        isRunning = false;
////        handler.removeCallbacks(runnable);
////    }
////
////    private void resetTimer() {
////        isRunning = false;
////        handler.removeCallbacks(runnable);
////
////        // Set time based on current SeekBar values
////        long workMinutes = workDurationSeekBar.getProgress();
////        long breakMinutes = breakDurationSeekBar.getProgress();
////
////        workTimeInMillis = TimeUnit.MINUTES.toMillis(workMinutes);
////        breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakMinutes);
////
////        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
////        updateTimerDisplay();
////    }
////
////    private void toggleTimer() {
////        isWorkTime = !isWorkTime; // Switch between work and break time
////        timeLeftInMillis = isWorkTime ? workTimeInMillis : breakTimeInMillis;
////        updateTimerDisplay();
////        playNotification();
////    }
////
////    private void playNotification() {
////        if (selectedSoundUri != null) {
////            mediaPlayer = MediaPlayer.create(this, selectedSoundUri);
////            mediaPlayer.start();
////        }
////        if (vibrator != null) {
////            vibrator.vibrate(1000); // Vibrate for 500 milliseconds
////        }
////    }
////
////    private void updateTimerDisplay() {
////        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis);
////        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60;
////        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
////    }
////
////    private void openFilePicker() {
////        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
////        audioPickerLauncher.launch(intent);
////    }
////
////    private final ActivityResultLauncher<Intent> audioPickerLauncher =
////            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
////                if (result.getResultCode() == RESULT_OK) {
////                    Uri selectedAudioUri = result.getData().getData();
////                    selectedSoundUri = selectedAudioUri;
////                    Toast.makeText(Pomodoro.this, "Sound file selected", Toast.LENGTH_SHORT).show();
////                }
////            });
////    }