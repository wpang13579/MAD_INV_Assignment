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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ComposeNumbersActivity extends AppCompatActivity {

    private TextView targetNumberText, selectedNumber1, selectedNumber2, correctAnswerText, timerText;
    private ImageView resultImage;
    private Button checkButton, newQuestionButton, clearButton, tryAgainButton;
    private Button[] numberButtons;

    private int targetNumber, part1 = -1, part2 = -1;
    private int correctPart1, correctPart2;
    private int score = 0, score2 = 0;

    private MediaPlayer correctSound, incorrectSound;
    private CountDownTimer countDownTimer;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_numbers);

        // Initialize UI components
        targetNumberText = findViewById(R.id.targetNumberText);
        selectedNumber1 = findViewById(R.id.selectedNumber1);
        selectedNumber2 = findViewById(R.id.selectedNumber2);
        correctAnswerText = findViewById(R.id.correctAnswerText);
        timerText = findViewById(R.id.timerText);
        resultImage = findViewById(R.id.resultImage);
        checkButton = findViewById(R.id.checkButton);
        newQuestionButton = findViewById(R.id.newQuestionButton);
        clearButton = findViewById(R.id.clearButton);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Set up sounds and random generator
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);
        random = new Random();

        // Initialize number buttons
        numberButtons = new Button[]{
                findViewById(R.id.number1Button), findViewById(R.id.number2Button),
                findViewById(R.id.number3Button), findViewById(R.id.number4Button),
                findViewById(R.id.number5Button), findViewById(R.id.number6Button)
        };

        // Set click listeners for number selection
        for (Button button : numberButtons) {
            button.setOnClickListener(v -> selectNumber(Integer.parseInt(button.getText().toString())));
        }

        // Button actions
        clearButton.setOnClickListener(v -> resetSelection());
        checkButton.setOnClickListener(v -> checkAnswer());
        newQuestionButton.setOnClickListener(v -> generateNewQuestion());
        tryAgainButton.setOnClickListener(v -> restartCurrentQuestion());

        // Handle back navigation with timer cancel
        backButton.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            finish();
        });

        // Start with a question and delay timer to allow user to read
        generateNewQuestion();
        tryAgainButton.setVisibility(View.GONE);
        new Handler().postDelayed(() -> startTimer(15), 500);
    }

    /** Starts the countdown timer for the question */
    private void startTimer(int seconds) {
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Timer: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Time's up!");
                showScorePopup();
                tryAgainButton.setVisibility(View.VISIBLE);
                checkButton.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);
                newQuestionButton.setVisibility(View.GONE);
            }
        }.start();
    }

    /** Generates a new random number question */
    private void generateNewQuestion() {
        resetSelection();
        resultImage.setVisibility(View.GONE);

        // Show all number buttons
        for (Button button : numberButtons) {
            button.setVisibility(View.VISIBLE);
        }

        // Generate a target number and correct parts
        targetNumber = random.nextInt(990) + 10;
        targetNumberText.setText(String.valueOf(targetNumber));
        correctPart1 = random.nextInt(targetNumber - 1) + 1;
        correctPart2 = targetNumber - correctPart1;

        // Fill number options including correct and distractors
        List<Integer> numberOptions = new ArrayList<>();
        numberOptions.add(correctPart1);
        numberOptions.add(correctPart2);
        while (numberOptions.size() < 6) {
            int fake = random.nextInt(999);
            if (!numberOptions.contains(fake)) {
                numberOptions.add(fake);
            }
        }

        // Shuffle and assign to buttons
        Collections.shuffle(numberOptions);
        for (int i = 0; i < numberButtons.length; i++) {
            numberButtons[i].setText(String.valueOf(numberOptions.get(i)));
            numberButtons[i].setVisibility(View.VISIBLE);
        }

        clearButton.setVisibility(View.VISIBLE);
        checkButton.setVisibility(View.VISIBLE);
    }

    /** Handles selection of a number button */
    private void selectNumber(int num) {
        if (part1 == -1) {
            part1 = num;
            selectedNumber1.setText(String.valueOf(part1));
        } else if (part2 == -1) {
            part2 = num;
            selectedNumber2.setText(String.valueOf(part2));
            checkButton.setEnabled(true);
        }
    }

    /** Resets selected numbers and clears UI feedback */
    private void resetSelection() {
        part1 = part2 = -1;
        selectedNumber1.setText("?");
        selectedNumber2.setText("?");
        checkButton.setEnabled(false);
        resultImage.setVisibility(View.GONE);
        correctAnswerText.setVisibility(View.GONE);
    }

    /** Validates the answer and provides visual feedback */
    private void checkAnswer() {
        boolean isCorrect = (part1 + part2 == targetNumber);
        score2++;

        if (isCorrect) {
            score++;
            resultImage.setImageResource(R.drawable.correct_icon);
            correctSound.start();
        } else {
            resultImage.setImageResource(R.drawable.incorrect_icon);
            incorrectSound.start();
        }

        resultImage.setVisibility(View.VISIBLE);
        correctAnswerText.setVisibility(View.VISIBLE);
        correctAnswerText.setText(isCorrect ?
                "Correct! 🎉 " + part1 + " + " + part2 :
                "Wrong! The correct answer is: " + correctPart1 + " + " + correctPart2);

        clearButton.setVisibility(View.GONE);
        checkButton.setVisibility(View.GONE);

        // Hide all number buttons
        for (Button button : numberButtons) {
            button.setVisibility(View.GONE);
        }
    }

    /** Restarts the current question without generating a new one */
    private void restartCurrentQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setText("Timer: 15");
        for (Button button : numberButtons) {
            button.setVisibility(View.VISIBLE);
        }

        checkButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
        newQuestionButton.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.GONE);

        resetSelection();

        // Restart timer instantly
        new Handler().postDelayed(() -> startTimer(15), 500);
    }

    /** Shows a custom popup with score */
    private void showScorePopup() {
        if (isFinishing() || isDestroyed()) return;
        for (Button button : numberButtons) {
            button.setVisibility(View.GONE);
        }


        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_score, null);

        TextView scoreText = popupView.findViewById(R.id.scoreMessage);
        scoreText.setText("You got " + score + " out of " + score2 + "!");

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

    /** Cancels timer to avoid leaks if activity is destroyed */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
