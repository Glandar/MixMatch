package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.transitionseverywhere.Recolor;
import com.transitionseverywhere.TransitionManager;

import java.util.Locale;

public class ScoreFragment extends Fragment {

    View fragmentSecondLayout;
    Button replay_button, reseed_button, nextSeed_button;
    TextView score_textview, moves_textview, time_textview, seed_textview, settings_textview;
    String settings;
    int moves;
    float score, time;
    long seed;
    boolean darkMode;
    int[] colorScheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        moves = MainActivity.moves;
        time = MainActivity.time / 1000f;
        seed = MainActivity.seed;
        settings = MainActivity.settings;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        fragmentSecondLayout = inflater.inflate(R.layout.fragment_score, container, false);
        return fragmentSecondLayout;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // called before this fragment is navigated to
        float ratio = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt("scoreRatio", 100);
        settings += (int) ratio;
        score = ((float) moves * ratio + time * (100f - ratio)) / 100f;

        darkMode = MainActivity.darkMode;
        colorScheme = ((MainActivity) requireActivity()).getColorScheme();
        replay_button = fragmentSecondLayout.findViewById(R.id.replay);
        reseed_button = fragmentSecondLayout.findViewById(R.id.reSeed);
        nextSeed_button = fragmentSecondLayout.findViewById(R.id.nextSeed);
        score_textview = fragmentSecondLayout.findViewById(R.id.score_text);
        moves_textview = fragmentSecondLayout.findViewById(R.id.moves);
        time_textview = fragmentSecondLayout.findViewById(R.id.time);
        seed_textview = fragmentSecondLayout.findViewById(R.id.seed_text);
        settings_textview = fragmentSecondLayout.findViewById(R.id.settings_hash_text);

        score_textview.setText(String.format(Locale.getDefault(), "%.2f", score));
        moves_textview.setText(getString(R.string.moves, moves));
        time_textview.setText(getString(R.string.time, time));
        seed_textview.setText(getString(R.string.seed, seed));
        settings_textview.setText(getString(R.string.settings, settings));
        set_colors();

        replay_button.setOnClickListener(view1 -> {
            ((MainActivity) requireActivity()).setSeed(seed);
            NavHostFragment.findNavController(ScoreFragment.this).navigate(R.id.action_ScoreFragment_to_GameFragment);
        });

        reseed_button.setOnClickListener(view12 -> {
            ((MainActivity) requireActivity()).changeSeed();
            NavHostFragment.findNavController(ScoreFragment.this).navigate(R.id.action_ScoreFragment_to_GameFragment);
        });

        nextSeed_button.setOnClickListener(view1 -> {
            ((MainActivity) requireActivity()).setSeed(seed + 1);
            NavHostFragment.findNavController(ScoreFragment.this).navigate(R.id.action_ScoreFragment_to_GameFragment);
        });
    }

    private void set_colors(){
        score_textview.setTextColor(colorScheme[0]);
        moves_textview.setTextColor(colorScheme[0]);
        time_textview.setTextColor(colorScheme[0]);
        seed_textview.setTextColor(colorScheme[0]);
        settings_textview.setTextColor(colorScheme[0]);
        if (darkMode) {
            replay_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint_dark));
            replay_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background_dark));
            reseed_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint_dark));
            reseed_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background_dark));
            nextSeed_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint_dark));
            nextSeed_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background_dark));
        } else {
            replay_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint));
            replay_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background));
            reseed_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint));
            reseed_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background));
            nextSeed_button.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.button_tint));
            nextSeed_button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.image_button_background));
        }
    }

    public void dark_mode() {
        darkMode = !darkMode;
        colorScheme = ((MainActivity) requireActivity()).getColorScheme();
        TransitionManager.beginDelayedTransition((ViewGroup) fragmentSecondLayout, new Recolor().setDuration(400));
        set_colors();
    }
}