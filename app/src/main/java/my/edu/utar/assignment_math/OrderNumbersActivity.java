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
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class OrderNumbersActivity extends AppCompatActivity {

    private TextView instructionTextView;
    private List<TextView> numberTextViews;
    private List<Button> numberButtons;
    private Button ascendingButton;
    private Button descendingButton;
    private Button checkButton;
    private Button newQuestionButton;
    private ImageView resultImageView;

    private List<Integer> originalNumbers;
    private List<Integer> userOrderedNumbers;
    private boolean isAscendingOrder;
    private Random random;
    private MediaPlayer correctSound;
    private MediaPlayer incorrectSound;
    private int selectedPositionIndex;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private int score = 0, totalQuestions = 0;
    private Button tryAgainButton;
    private TextView correctOrderText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_numbers);

        // Initialize views
        instructionTextView = findViewById(R.id.instructionTextView);


        // Initialize Buttons
        numberButtons = new ArrayList<>();
        numberButtons.add(findViewById(R.id.number1Button));
        numberButtons.add(findViewById(R.id.number2Button));
        numberButtons.add(findViewById(R.id.number3Button));
        numberButtons.add(findViewById(R.id.number4Button));
        numberButtons.add(findViewById(R.id.number5Button));

        ascendingButton = findViewById(R.id.ascendingButton);
        descendingButton = findViewById(R.id.descendingButton);
        checkButton = findViewById(R.id.checkButton);
        newQuestionButton = findViewById(R.id.newQuestionButton);
        resultImageView = findViewById(R.id.resultImage);
        timerText = findViewById(R.id.timerText);
        new Handler().postDelayed(() -> startTimer(15), 500);
        correctOrderText = findViewById(R.id.correctOrderText);

        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setVisibility(View.GONE);

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Button button : numberButtons) {
                    button.setVisibility(View.VISIBLE);
                }
                // Cancel and restart timer only (without changing question)
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                timerText.setText("Timer: 15");
                checkButton.setVisibility(View.VISIBLE);
                newQuestionButton.setVisibility(View.VISIBLE);
                tryAgainButton.setVisibility(View.GONE);
                new Handler().postDelayed(() -> startTimer(15),500);
                generateNewQuestion();
            }
        });

        // Initialize random number generator
        random = new Random();

        // Initialize sound effects
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);

        // Initialize variables
        originalNumbers = new ArrayList<>();
        userOrderedNumbers = new ArrayList<>();
        selectedPositionIndex = -1;

        // Set click listeners for ascending/descending buttons
        ascendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAscendingOrder = true;
                instructionTextView.setText("Put numbers in order from SMALLEST to BIGGEST");
                descendingButton.setVisibility(View.GONE);
                ascendingButton.setEnabled(false);
                descendingButton.setEnabled(false);
                enableNumberButtons(true);
            }
        });

        descendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAscendingOrder = false;
                instructionTextView.setText("Put numbers in order from BIGGEST to SMALLEST");
                ascendingButton.setVisibility(View.GONE);
                ascendingButton.setEnabled(false);
                descendingButton.setEnabled(false);
                enableNumberButtons(true);
            }
        });

        // Set click listeners for number buttons
        for (int i = 0; i < numberButtons.size(); i++) {
            final int buttonIndex = i;
            numberButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPositionIndex == -1) {
                        // First selection - highlight the position
                        selectedPositionIndex = buttonIndex;
                        numberButtons.get(buttonIndex).setBackgroundResource(R.drawable.selected_button);
                    } else {
                        // Second selection - swap the numbers
                        int temp = originalNumbers.get(selectedPositionIndex);
                        originalNumbers.set(selectedPositionIndex, originalNumbers.get(buttonIndex));
                        originalNumbers.set(buttonIndex, temp);

                        // Reset button appearances
                        numberButtons.get(selectedPositionIndex).setBackgroundResource(R.drawable.round_button);

                        // Update button text
                        updateButtonText();

                        // Reset selection
                        selectedPositionIndex = -1;
                    }
                }
            });
        }

        // Set click listener for check button
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });

        // Set click listener for new question button
        newQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewQuestion();
            }
        });

        // Generate first question
        generateNewQuestion();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel(); // Stop the timer when exiting
            }
            finish(); // Return to MainActivity
        });


    }

    private void startTimer(int seconds) {
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Timer: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                timerText.setText("Time's up!");
                enableNumberButtons(false);
                checkButton.setVisibility(View.GONE);
                newQuestionButton.setVisibility(View.GONE);
                tryAgainButton.setVisibility(View.VISIBLE);
                showScorePopup();
            }
        }.start();
    }

    private void generateNewQuestion() {
        originalNumbers.clear();
        userOrderedNumbers.clear();
        correctOrderText.setVisibility(View.GONE);

        for (Button button : numberButtons) {
            button.setVisibility(View.VISIBLE);
        }
        checkButton.setVisibility(View.VISIBLE);

        // Use a HashSet to store unique numbers
        HashSet<Integer> uniqueNumbers = new HashSet<>();

        // Generate 5 unique random single-digit numbers (0-9)
        while (uniqueNumbers.size() < 5) {
            int newNumber = random.nextInt(10); // Generates numbers from 0 to 9
            uniqueNumbers.add(newNumber); // Ensures uniqueness
        }

        // Convert HashSet to List
        originalNumbers.addAll(uniqueNumbers);

        selectedPositionIndex = -1;
        updateButtonText();
        ascendingButton.setEnabled(true);
        descendingButton.setEnabled(true);
        enableNumberButtons(false);
        checkButton.setEnabled(true);
        instructionTextView.setText("Choose how to order the numbers:");
        resultImageView.setVisibility(View.GONE);

        ascendingButton.setVisibility(View.VISIBLE);
        descendingButton.setVisibility(View.VISIBLE);
    }

    private void updateButtonText() {
        for (int i = 0; i < numberButtons.size(); i++) {
            numberButtons.get(i).setText(String.valueOf(originalNumbers.get(i)));
        }
    }

    private void enableNumberButtons(boolean enable) {
        for (Button button : numberButtons) {
            button.setEnabled(enable);
        }
    }

    private void checkAnswer() {
        // Create a copy of the current order
        List<Integer> currentOrder = new ArrayList<>(originalNumbers);

        // Create correct order
        List<Integer> correctOrder = new ArrayList<>(originalNumbers);
        if (isAscendingOrder) {
            Collections.sort(correctOrder);
        } else {
            Collections.sort(correctOrder, Collections.reverseOrder());
        }

        // Check if current order matches correct order
        boolean isCorrect = currentOrder.equals(correctOrder);

        // Show result
        if (isCorrect) {
            resultImageView.setImageResource(R.drawable.correct_icon);
            correctSound.start();
            score++;
        } else {
            resultImageView.setImageResource(R.drawable.incorrect_icon);
            incorrectSound.start();
        }
        totalQuestions++;

        resultImageView.setVisibility(View.VISIBLE);
        enableNumberButtons(false);
        checkButton.setEnabled(false);

        for (Button button : numberButtons) {
            button.setVisibility(View.GONE);
        }

        // Show the correct order as text
        StringBuilder correctText = new StringBuilder("✅ Correct order is: ");
        for (int num : correctOrder) {
            correctText.append(num).append(" ");
        }

        correctOrderText.setText(correctText.toString().trim());
        correctOrderText.setVisibility(View.VISIBLE);
        checkButton.setVisibility(View.GONE);
        correctOrderText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media players
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (incorrectSound != null) {
            incorrectSound.release();
            incorrectSound = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void showScorePopup() {
        if (isFinishing() || isDestroyed()) return;
        for (Button button : numberButtons) {
            button.setVisibility(View.GONE);
        }

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




}