package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.transitionseverywhere.Recolor;
import com.transitionseverywhere.TransitionManager;

import java.util.Arrays;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class GameFragment extends Fragment {

    String settings, cipher_type, matchAny;
    Random random = new Random();
    View fragmentFirstLayout;
    Button undo_button;
    TextView[] textViews;
    ImageButton[] imageButtons = new ImageButton[4];
    ProgressBar[] progressBars;
    ConstraintLayout[] constraintLayouts;
    int rank = 0;
    int[] bounds = new int[3];
    int[] prefix_zero = new int[3];
    boolean[] buttonEnabled = new boolean[5];
    int moves, base, goals, inputSize, inputAmountEditable, redoNumber, undoPenalty, goal_height, adjusted_height, first_div, second_div;
    int[] numbers, colorScheme, stackSize, startStackSize, lastAction;
    int[][] goalStack;
    boolean darkMode, inputAllEditable, sepStacks = true, preCalc, showPreCalc, undone, cipher;
    long time, seed;
    char[] cipher_map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        settings_update();
        startup();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        fragmentFirstLayout = inflater.inflate(R.layout.fragment_game, container, false);
        return fragmentFirstLayout;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // This method is called twice for some reason
        darkMode = MainActivity.darkMode;
        colorScheme = ((MainActivity) requireActivity()).getColorScheme();

        if (first_div != 48) {
            ((Guideline) view.findViewById(R.id.input_line)).setGuidelinePercent(first_div/100f);
        }
        if (second_div != 64) {
            ((Guideline) view.findViewById(R.id.button_line)).setGuidelinePercent(second_div/100f);
        }

        imageButtons[0] = view.findViewById(R.id.button_add);
        imageButtons[1] = view.findViewById(R.id.button_multiply);
        imageButtons[2] = view.findViewById(R.id.button_subtract);
        imageButtons[3] = view.findViewById(R.id.button_divide);
        undo_button = view.findViewById(R.id.button_undo);
        for (int i = 0; i < imageButtons.length; i++) {
            imageButtons[i].setEnabled(buttonEnabled[i]);
        }
        if (!buttonEnabled[4]) {
            undo_button.setVisibility(View.GONE);
        }
        set_button_colors();

        textViews = new TextView[2 * goals + inputSize];
        progressBars = new ProgressBar[goals + 1];
        progressBars[0] = view.findViewById(R.id.progress_bar_full);
        constraintLayouts = new ConstraintLayout[goals];
        final ViewGroup layoutGoals = view.findViewById(R.id.goal_container);

        Context context = requireContext();
        int resource = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resource > 0) {
            goal_height = (context.getResources().getDisplayMetrics().heightPixels - context.getResources().getDimensionPixelSize(resource)) * adjusted_height / 625;
        } else {
            goal_height = context.getResources().getDisplayMetrics().heightPixels * adjusted_height / 625;
        }
        IntStream.range(0, goals). forEach(i -> {
            constraintLayouts[i] = (ConstraintLayout) getLayoutInflater().inflate(R.layout.item_goal, layoutGoals, false);
            textViews[i] = constraintLayouts[i].findViewById(R.id.output);
            textViews[i].setText(cipher(numbers[i], base, cipher, cipher_map, 0));
            textViews[i].setOnClickListener(click -> {
                if (rank >= goals) {
                    textViews[rank].setTextColor(colorScheme[1]);
                } else {
                    textViews[rank].setTextColor(colorScheme[0]);
                    if (matchAny.equals("i") || matchAny.equals("g")) {
                        for (int j = goals; j < 2 * goals; j++) {
                            if (numbers[rank] == numbers[j]) {
                                textViews[rank].setTextColor(colorScheme[3]);
                                break;
                            }
                        }
                    } else if (numbers[rank] == numbers[rank + goals]) {
                        textViews[rank].setTextColor(colorScheme[3]);
                    }
                }
                rank = i;
                textViews[rank].setTextColor(colorScheme[2]);
            });
            textViews[i + goals] = constraintLayouts[i].findViewById(R.id.goal);
            textViews[i + goals].setText(cipher(numbers[i + goals], base, cipher, cipher_map, 1));
            textViews[i + goals].setTextColor(colorScheme[4]);
            if (showPreCalc) {
                textViews[i + goals].setOnClickListener(click -> {
                    PopUpFragment popUpClass = new PopUpFragment();
                    popUpClass.showPopupWindow(click, goalStack, !sepStacks, stackSize, goals, base, ((MainActivity) requireActivity()).getBackground(), cipher, cipher_map);
                });
            }
            progressBars[i + 1] = constraintLayouts[i].findViewById(R.id.progress_bar);
            constraintLayouts[i].getLayoutParams().height = goal_height;
            constraintLayouts[i].setLayoutParams(constraintLayouts[i].getLayoutParams());
            layoutGoals.addView(constraintLayouts[i]);
        });

        if (stackSize[0] == 0){
            for (ProgressBar progressBar : progressBars){
                progressBar.setVisibility(View.GONE);
            }
        } else if (!sepStacks) {
            for (int i = 0; i < goals; i++) {
                progressBars[i + 1].setVisibility(View.GONE);
            }
            progressBars[0].setVisibility(View.VISIBLE);
            progressBars[0].setMax(startStackSize[0] * 100);
        } else {
            for (int i = 0; i < goals; i++) {
                progressBars[i + 1].setVisibility(View.VISIBLE);
                progressBars[i + 1].setMax(startStackSize[i] * 100);
            }
            progressBars[0].setVisibility(View.GONE);
        }
        if (!Arrays.equals(startStackSize, stackSize)) {
            IntStream.range(0, goals). forEach(i -> progressBars[i + 1].post(() -> progressBars[i + 1].setProgress((startStackSize[i] - stackSize[i]) * 100)));
        }
        match();
        color_update();

        final ViewGroup layoutInput = view.findViewById(R.id.input_container);
        moveInputViews(getLayoutInflater(), layoutInput);

        imageButtons[0].setOnClickListener(view1 -> {
            lastAction = new int[]{rank, numbers[rank], numbers[goals * 2]};
            numbers[rank] += numbers[goals * 2];
            new_output();
            TransitionManager.beginDelayedTransition(layoutInput);
            moveInputViews(getLayoutInflater(), layoutInput);
        });

        imageButtons[1].setOnClickListener(view12 -> {
            lastAction = new int[]{rank, numbers[rank], numbers[goals * 2]};
            numbers[rank] *= numbers[goals * 2];
            new_output();
            TransitionManager.beginDelayedTransition(layoutInput);
            moveInputViews(getLayoutInflater(), layoutInput);
        });

        imageButtons[2].setOnClickListener(view13 -> {
            lastAction = new int[]{rank, numbers[rank], numbers[goals * 2]};
            numbers[rank] -= numbers[goals * 2];
            new_output();
            TransitionManager.beginDelayedTransition(layoutInput);
            moveInputViews(getLayoutInflater(), layoutInput);
        });

        imageButtons[3].setOnClickListener(view14 -> {
            lastAction = new int[]{rank, numbers[rank], numbers[goals * 2]};
            if (numbers[goals * 2] != 0) {
                numbers[rank] /= numbers[goals * 2];
                new_output();
                TransitionManager.beginDelayedTransition(layoutInput);
                moveInputViews(getLayoutInflater(), layoutInput);
            } else {
                Toast.makeText(requireActivity(), "Zero division is not allowed", Toast.LENGTH_SHORT).show();
            }
        });

        undo_button.setOnClickListener(view15 -> {
            undo();
            TransitionManager.beginDelayedTransition(layoutInput);
            moveInputViews(getLayoutInflater(), layoutInput);
        });
    }

    private void startup() {
        seed = MainActivity.seed;
        random.setSeed(seed);
        undone = false;
        moves = 0;
        numbers = new int[2 * goals + inputSize];
        for (int i = 0; i < goals; i++) {
            numbers[i] = random.nextInt(bounds[0]);
        }
        for (int i = goals; i < 2 * goals; i++) {
            numbers[i] = random.nextInt(bounds[1]);
        }
        for (int i = 2 * goals; i < numbers.length; i++) {
            numbers[i] = random.nextInt(bounds[2]) + 1;
        }
        if (preCalc) {
            if (!sepStacks) {
                goalStack = new int[2][startStackSize[0] + goals];
                for (int i = 0; i < startStackSize[0]; i++) {
                    goalStack[0][i] = random.nextInt(bounds[1]);
                    goalStack[1][i] = 0;
                }
                for (int i = 0; i < goals; i++) {
                    goalStack[0][startStackSize[0] + i] = numbers[i + goals];
                    goalStack[1][startStackSize[0] + i] = startStackSize[0] + i;
                }
            } else {
                goalStack = new int[goals][startStackSize[0] + 1];
                for (int i = 0; i < goals; i++) {
                    for (int j = 0; j < startStackSize[0]; j++) {
                        goalStack[i][j] = random.nextInt(bounds[1]);
                    }
                }
                for (int i = 0; i < goals; i++) {
                    goalStack[i][startStackSize[0]] = numbers[i + goals];
                }
            }
        }
        if (cipher) {
            cipher_map = create_cipher(cipher_type);
        }
        time = System.currentTimeMillis();
    }

    private void moveInputViews(LayoutInflater inflater, @NonNull ViewGroup layout) {
        layout.removeAllViews();
        for (int i = goals * 2; i < textViews.length; i++) {
            textViews[i] = (TextView) inflater.inflate(R.layout.item_textview, layout, false);
            textViews[i].setText(cipher(numbers[i], base, cipher, cipher_map, 2));
            textViews[i].setTextSize(0, goal_height);
            textViews[i].setTextColor(colorScheme[1]);
            textViews[i].setAlpha((float) Math.max(1 - ((float)(i - goals * 2))/inputSize, 0.3));
            ViewCompat.setTransitionName(textViews[i], Integer.toString(moves + i));
            layout.addView(textViews[i]);
        }

        if (rank > goals * 2) {
            textViews[rank].setTextColor(colorScheme[2]);
        }

        IntStream.range(goals * 2 + 1, goals * 2 + inputAmountEditable).forEach( i ->
                textViews[i].setOnClickListener(view -> {
                    if (rank >= goals) {
                        textViews[rank].setTextColor(colorScheme[1]);
                    } else {
                        textViews[rank].setTextColor(colorScheme[0]);
                        if (matchAny.equals("i") || matchAny.equals("g")) {
                            for (int j = goals; j < 2 * goals; j++) {
                                if (numbers[rank] == numbers[j]) {
                                    textViews[rank].setTextColor(colorScheme[3]);
                                    break;
                                }
                            }
                        } else if (numbers[rank] == numbers[rank + goals]) {
                            textViews[rank].setTextColor(colorScheme[3]);
                        }
                    }
                    rank = i;
                    textViews[rank].setTextColor(colorScheme[2]);
                }));
    }

    private void match() {
        for (int i = goals; i < 2 * goals; i++) {
            search_match_goal(i);
        }
    }

    private void search_match_input(int rank){
        if (matchAny.equals("i") || matchAny.equals("g")) {
            for (int i = goals; i < 2 * goals; i++) {
                if (numbers[rank] == numbers[i]) {
                    handle_match(rank, i);
                }
            }
        } else {
            if (numbers[rank] == numbers[rank + goals]) {
                handle_match(rank, rank + goals);
            }
        }
    }

    private void search_match_goal(int rank){
        if (matchAny.equals("i") || matchAny.equals("g")){
            for (int i = 0; i < goals; i++) {
                if (numbers[rank] == numbers[i]) {
                    handle_match(i, rank);
                }
            }
        } else {
            if (numbers[rank] == numbers[rank - goals]) {
                handle_match(rank - goals, rank);
            }
        }
    }

    private void handle_match(int match, int matchTo) {
        int stack = matchAny.equals("g") ? matchTo - goals : match;
        if (!sepStacks && stackSize[0] > 0) {
            stackSize[0]--;
            ObjectAnimator.ofInt(progressBars[0], "progress", (startStackSize[0] - stackSize[0]) * 100).setDuration(1000).start();
            if (preCalc) {
                numbers[matchTo] = goalStack[0][stackSize[0]];
                goalStack[1][stackSize[0]] = -1;
                goalStack[1][goalStack[1][startStackSize[0] + stack]] = -2;
                goalStack[1][startStackSize[0] + stack] = stackSize[0];
            } else {
                numbers[matchTo] = random.nextInt(bounds[1]);
            }
            undo_button.setEnabled(false);
            textViews[matchTo].setText(cipher(numbers[matchTo], base, cipher, cipher_map, 1));
            search_match_goal(matchTo);
        } else if (sepStacks && stackSize[stack] > 0) {
            stackSize[stack]--;
            ObjectAnimator.ofInt(progressBars[stack + 1], "progress", (startStackSize[stack] - stackSize[stack]) * 100).setDuration(1000).start();
            if (preCalc) {
                numbers[matchTo] = goalStack[stack][stackSize[stack]];
            } else {
                numbers[matchTo] = random.nextInt(bounds[1]);
            }
            undo_button.setEnabled(false);
            textViews[matchTo].setText(cipher(numbers[matchTo], base, cipher, cipher_map, 1));
            search_match_goal(matchTo);
        } else {
            if (preCalc && !sepStacks && goalStack[1][startStackSize[0] + stack] >= 0) {
                goalStack[1][goalStack[1][startStackSize[0] + stack]] = -2;
            }
            textViews[match].setTextColor(colorScheme[3]);
            check_complete();
        }
    }

    private void check_complete() {
        boolean completed = true;
        if (matchAny.equals("i")) {
            int matches = 0;
            for (int i = 0; i < goals; i++) {
                for (int j = goals; j < 2 * goals; j++) {
                    if (numbers[i] == numbers[j]) {
                        matches++;
                        break;
                    }
                }
                if (matches <= i) {
                    completed = false;
                    break;
                }
            }
        } else if (matchAny.equals("g")) {
            int matches = 0;
            for (int i = goals; i < 2 * goals; i++) {
                for (int j = 0; j < goals; j++) {
                    if (numbers[i] == numbers[j]) {
                        matches++;
                        break;
                    }
                }
                if (matches <= i - goals) {
                    completed = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < goals; i++) {
                if (numbers[i] != numbers[goals + i]) {
                    completed = false;
                    break;
                }
            }
        }
        if (completed) {
            time = System.currentTimeMillis() - time;
            MainActivity.moves = moves;
            MainActivity.time = time;
            MainActivity.seed = seed;
            MainActivity.settings = settings;
            NavHostFragment.findNavController(GameFragment.this).navigate(R.id.action_GameFragment_to_ScoreFragment);
        }
    }
    
    private void new_output() {
        moves++;
        undo_button.setEnabled(true);
        if (rank < goals) {
            textViews[rank].setText(cipher(numbers[rank], base, cipher, cipher_map, 0));
            search_match_input(rank);
        }
        if (numbers.length > goals * 2)
            System.arraycopy(numbers, goals * 2 + 1, numbers, goals * 2, numbers.length - goals * 2 - 1);
        if (undone){
            numbers[numbers.length - 1] = redoNumber;
        } else {
            numbers[numbers.length - 1] = random.nextInt(bounds[2]) + 1;
        }
        undone = false;
    }

    private String cipher(int number, int base, boolean cipher, char[] cipher_map, int bound) {
        StringBuilder text = new StringBuilder(Integer.toString(number, base));
        while (text.length() < prefix_zero[bound]) {
            text.insert(0, '0');
        }
        if (!cipher) {
            return text.toString();
        }
        char[] digits = text.toString().toCharArray();
        for (int i = number < 0 ? 1 : 0; i < digits.length; i++) {
            digits[i] = cipher_map[Integer.parseInt(Character.toString(digits[i]), base)];
        }
        return String.valueOf(digits);
    }

    private char[] create_cipher(String cipher) {
        int c;
        char[] map = new char[base];
        switch (cipher) {
            case "c":
                c = random.nextInt(base);
                for (int i = 0; i < base; i ++) {
                    map[i] = Character.forDigit((i + c) % base, base);
                }
                return map;
            case "p":
                SortedSet<Integer> p = new TreeSet<>();
                for (int i = 0; i < base; i ++) {
                    if (p.contains(i)) {
                        continue;
                    }
                    c = random.nextInt(base - p.size());
                    for (Integer prev : p) {
                        if (prev > c) {
                            break;
                        }
                        c++;
                    }
                    p.add(i);
                    p.add(c);
                    map[i] = Character.forDigit(c, base);
                    map[c] = Character.forDigit(i, base);
                }
                return map;
            case "r":
                SortedSet<Integer> r = new TreeSet<>();
                for (int i = 0; i < base; i ++) {
                    c = random.nextInt(base - i);
                    for (Integer prev : r) {
                        if (prev > c) {
                            break;
                        }
                        c++;
                    }
                    r.add(c);
                    map[i] = Character.forDigit(c, base);
                }
                return map;
            default:
                for (int i = 0; i < base; i ++) {
                    map[i] = Character.forDigit(i, base);
                }
                return map;
        }
    }

    private void undo(){
        moves--;
        time -= (long) undoPenalty * 100L;
        undone = true;
        redoNumber = numbers[numbers.length - 1];
        if (numbers.length >= goals * 2 + 1)
            System.arraycopy(numbers, goals * 2, numbers, goals * 2 + 1, numbers.length - goals * 2 - 1);
        numbers[lastAction[0]] = lastAction[1];
        if (lastAction[0] < goals){
            textViews[lastAction[0]].setText(cipher(numbers[lastAction[0]], base, cipher, cipher_map, 0));
        }
        numbers[goals * 2] = lastAction[2];
        color_update();
        undo_button.setEnabled(false);
    }

    public void restart() {
        stackSize = startStackSize.clone();
        startup();
        moveInputViews(getLayoutInflater(), fragmentFirstLayout.findViewById(R.id.input_container));
        for (int i = 0; i < 2 * goals; i++) {
            textViews[i].setText(cipher(numbers[i], base, cipher, cipher_map, i / goals));
        }
        for (ProgressBar progressBar : progressBars) {
            progressBar.setProgress(0);
        }
        match();
        color_update();
        if (undo_button.isEnabled()) {
            undo_button.setEnabled(false);
        }
    }

    private void set_button_colors() {
        if (darkMode) {
            for (ImageButton imageButton : imageButtons) {
                imageButton.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint_dark));
                imageButton.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background_dark));
            }
            undo_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint_dark));
            undo_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background_dark));
        } else {
            for (ImageButton imageButton : imageButtons) {
                imageButton.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint));
                imageButton.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background));
            }
            undo_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint));
            undo_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background));
        }
    }

    public void dark_mode() {
        darkMode = !darkMode;
        set_button_colors();
        colorScheme = ((MainActivity) requireActivity()).getColorScheme();
        Recolor recolor = new Recolor();
        recolor.setDuration(400);
        TransitionManager.beginDelayedTransition((ViewGroup) fragmentFirstLayout, recolor);
        for (int i = goals; i < goals * 2; i++) {
            textViews[i].setTextColor(colorScheme[4]);
        }
        for (int i = goals * 2; i < textViews.length; i++) {
            textViews[i].setTextColor(colorScheme[1]);
        }
        color_update();
    }

    public void color_update() {
        for (int i = 0; i < goals; i++) {
            textViews[i].setTextColor(colorScheme[0]);
            if (matchAny.equals("i") || matchAny.equals("g")) {
                for (int j = goals; j < 2 * goals; j++) {
                    if (numbers[i] == numbers[j]) {
                        textViews[i].setTextColor(colorScheme[3]);
                        break;
                    }
                }
            } else if (numbers[i] == numbers[i + goals]) {
                textViews[i].setTextColor(colorScheme[3]);
            }
        }
        textViews[rank].setTextColor(colorScheme[2]);
    }

    private void settings_update() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        undoPenalty = prefs.getInt("undoPenalty", 0);
        bounds[0] = Integer.parseInt(prefs.getString("startMax", "50"));
        bounds[1] = Integer.parseInt(prefs.getString("goalMax", "1000"));
        bounds[2] = Integer.parseInt(prefs.getString("inputMax", "10"));
        base = Integer.parseInt(prefs.getString("base", "10"));
        cipher_type = prefs.getString("cipher", "n");
        cipher = !cipher_type.equals("n");
        if (prefs.getBoolean("prefixZero", false)) {
            for (int i = 0; i < prefix_zero.length; i++) {
                prefix_zero[i] = Integer.toString(bounds[i] - 1, base).length();
            }
        } else {
            Arrays.fill(prefix_zero, 0);
        }
        goals = Integer.parseInt(prefs.getString("goalAmount", "3"));
        inputSize = Integer.parseInt(prefs.getString("inputSize","3"));
        matchAny = prefs.getString("anyMatch", "l");
        inputAllEditable = prefs.getBoolean("inputAllEditable",true);
        inputAmountEditable = inputAllEditable ? inputSize : Integer.parseInt(prefs.getString("inputAmountEditable", Integer.toString(inputSize)));
        sepStacks = !prefs.getBoolean("stackSeparate", true);
        preCalc = prefs.getBoolean("preCalcGoal",false);
        showPreCalc = prefs.getBoolean("showPreCalc",false);
        stackSize = new int[goals];
        stackSize[0] = Integer.parseInt(prefs.getString("stackSize","0"));
        for (int i = 1; i < stackSize.length; i++) {
            stackSize[i] = stackSize[0];
        }
        startStackSize = stackSize.clone();
        buttonEnabled[0] = prefs.getBoolean("addEnable", true);
        buttonEnabled[1] = prefs.getBoolean("mulEnable", true);
        buttonEnabled[2] = prefs.getBoolean("subEnable", true);
        buttonEnabled[3] = prefs.getBoolean("divEnable", true);
        buttonEnabled[4] = prefs.getBoolean("allowUndo", true);
        adjusted_height = prefs.getInt("textSize", 100);
        first_div = prefs.getInt("inputLine", 48);
        second_div = prefs.getInt("buttonLine", 64);
        settings = undoPenalty + letter(inputAllEditable) + bounds[0] + letter(sepStacks) + bounds[1] + letter(preCalc) + bounds[2] + letter(showPreCalc) + base
                 + letter(buttonEnabled[0]) + goals + letter(buttonEnabled[1]) + inputSize + letter(buttonEnabled[2]) + prefs.getString("inputAmountEditable", "0")
                 + letter(buttonEnabled[3]) + stackSize[0] + letter(buttonEnabled[4]) + cipher_type + letter(prefs.getBoolean("prefixZero", false)) + matchAny;
    }

    private String letter(boolean B) {
        return B? "T" : "F";
    }
}