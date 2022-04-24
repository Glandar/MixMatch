package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import java.util.stream.IntStream;

public class WindowFragment extends Fragment {

    View windowFragment;
    ImageView[] imageViews = new ImageView[4];

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        windowFragment = inflater.inflate(R.layout.fragment_windows, container, false);
        return windowFragment;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        imageViews[0] = view.findViewById(R.id.window_blue);
        imageViews[1] = view.findViewById(R.id.window_red);
        imageViews[2] = view.findViewById(R.id.window_green);
        imageViews[3] = view.findViewById(R.id.window_purple);

        if (MainActivity.darkMode) {
            imageViews[0].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_dtl_blue));
            imageViews[1].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_dtl_red));
            imageViews[2].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_dtl_green));
            imageViews[3].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_dtl_purple));
        } else {
            imageViews[0].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_ltd_blue));
            imageViews[1].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_ltd_red));
            imageViews[2].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_ltd_green));
            imageViews[3].setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.background_gradient_ltd_purple));
        }

        IntStream.range(0, 4).forEach(i -> imageViews[i].setOnClickListener(view1 -> {
            prefs.edit().putInt("theme", i).apply();
            ((MainActivity) requireActivity()).theme = i;
            ((MainActivity) requireActivity()).setTheme();
            ((MainActivity) requireActivity()).adaptOverlay("game");
            if (MainActivity.origin == 0) {
                NavHostFragment.findNavController(this).navigate(R.id.action_windowFragment_to_GameFragment);
            } else if (MainActivity.origin == 1) {
                NavHostFragment.findNavController(this).navigate(R.id.action_windowFragment_to_ScoreFragment);
            }
        }));
    }

    public void dark_mode() {
        for (ImageView imageView : imageViews) {
            ((TransitionDrawable) imageView.getDrawable()).reverseTransition(400);
        }
    }
}
