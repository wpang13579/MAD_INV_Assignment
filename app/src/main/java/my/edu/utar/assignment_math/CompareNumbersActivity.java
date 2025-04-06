package my.edu.utar.assignment_math;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CompareNumbersActivity extends AppCompatActivity {

    // UI components
    private Button leftNumberButton, rightNumberButton, newQuestionButton, tryAgainButton;
    private ImageView resultImage;
    private TextView questionText, timerText;

    // Game variables
    private int leftNumber, rightNumber;
    private int score = 0;
    private int totalQuestions = 0;

    // Utilities
    private Random random;
    private MediaPlayer correctSound, incorrectSound;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_numbers);

        // Bind UI elements
        leftNumberButton = findViewById(R.id.leftNumberButton);
        rightNumberButton = findViewById(R.id.rightNumberButton);
        newQuestionButton = findViewById(R.id.newQuestionButton);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        resultImage = findViewById(R.id.resultImage);
        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);

        // Initialize sound and random number generator
        random = new Random();
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);

        // Set button listeners
        leftNumberButton.setOnClickListener(v -> checkAnswer(true));
        rightNumberButton.setOnClickListener(v -> checkAnswer(false));
        newQuestionButton.setOnClickListener(v -> generateNewQuestion());
        tryAgainButton.setOnClickListener(v -> resetTimerOnly());

        // Set initial UI state
        tryAgainButton.setVisibility(View.GONE);
        generateNewQuestion();

        // Start timer after 3 seconds delay
        new Handler().postDelayed(() -> startTimer(15), 500);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel(); // Stop the timer when exiting
            }
            finish(); // Return to MainActivity
        });

    }

    /**
     * Starts a countdown timer for the game.
     * Disables input and shows the score when time is up.
     */
    private void startTimer(int seconds) {
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Timer: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                timerText.setText("Time's up!");
                leftNumberButton.setEnabled(false);
                rightNumberButton.setEnabled(false);
                newQuestionButton.setVisibility(View.GONE);
                tryAgainButton.setVisibility(View.VISIBLE);
                showScorePopup();
            }
        }.start();
    }

    /**
     * Generates a new random comparison question.
     */
    private void generateNewQuestion() {
        leftNumberButton.setEnabled(true);
        rightNumberButton.setEnabled(true);
        tryAgainButton.setVisibility(View.GONE);
        newQuestionButton.setVisibility(View.VISIBLE);

        do {
            leftNumber = random.nextInt(990) + 10;
            rightNumber = random.nextInt(990) + 10;
        } while (leftNumber == rightNumber); // Ensure numbers are different

        leftNumberButton.setText(String.valueOf(leftNumber));
        rightNumberButton.setText(String.valueOf(rightNumber));
        questionText.setText("Which number is bigger?");
        resultImage.setVisibility(View.GONE);
    }

    /**
     * Checks if the selected answer is correct and updates score.
     */
    private void checkAnswer(boolean leftSelected) {
        boolean isCorrect = (leftSelected && leftNumber > rightNumber) ||
                (!leftSelected && rightNumber > leftNumber);

        if (isCorrect) {
            resultImage.setImageResource(R.drawable.correct_icon);
            correctSound.start();
            score++;
        } else {
            resultImage.setImageResource(R.drawable.incorrect_icon);
            incorrectSound.start();
        }

        totalQuestions++;
        resultImage.setVisibility(View.VISIBLE);
        leftNumberButton.setEnabled(false);
        rightNumberButton.setEnabled(false);
    }

    /**
     * Displays the current score in a pop-up dialog.
     */
    // Custom popup for CompareNumbersActivity
    private void showScorePopup() {
        if (isFinishing() || isDestroyed()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_score, null);

        TextView scoreText = popupView.findViewById(R.id.scoreMessage);
        scoreText.setText("Your score is: " + score + "/" + totalQuestions + " questions");

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        Button closeBtn = popupView.findViewById(R.id.closePopupButton);
        closeBtn.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void resetTimerOnly() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerText.setText("Timer: 15");
        new Handler().postDelayed(() -> startTimer(15), 500);

        leftNumberButton.setEnabled(true);
        rightNumberButton.setEnabled(true);
        tryAgainButton.setVisibility(View.GONE);
        newQuestionButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
