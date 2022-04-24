package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

public class DialogSeedFragment extends DialogFragment {

    ViewGroup dialog;
    EditText seed, setting;
    Button play;

    public DialogSeedFragment() {    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_seed_dialog, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = view.findViewById(R.id.edit_seed_settings);
        play = view.findViewById(R.id.play_seed_settings);
        seed = view.findViewById(R.id.text_seed);
        setting = view.findViewById(R.id.text_settings);
        if (MainActivity.darkMode)
            dialog.setBackgroundColor(getResources().getColor(R.color.dialogDark));
        seed.requestFocus();

        play.setOnClickListener(view0 -> {
            String try_seed = seed.getText().toString();
            if (!try_seed.matches("")) {
                try {
                    MainActivity.seed = Long.parseLong(try_seed);
                } catch (Exception ignored) {
                    Toast.makeText(getActivity(), "Invalid Seed", Toast.LENGTH_SHORT).show();
                }
            }
            String try_settings = setting.getText().toString();
            if (!try_settings.matches("")) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
                String[] settings = setting.getText().toString().split("((?<=[a-zA-Z])|(?=[a-zA-Z]))");
                try {
                    prefs.edit()
                            .putInt("undoPenalty", Integer.parseInt(settings[0])).putBoolean("inputAllEditable", settings[1].equals("T"))
                            .putString("startMax", settings[2]).putBoolean("stackSeparate", settings[3].equals("T"))
                            .putString("goalMax", settings[4]).putBoolean("preCalcGoal", settings[5].equals("T"))
                            .putString("inputMax", settings[6]).putBoolean("showPreCalc", settings[7].equals("T"))
                            .putString("base", settings[8]).putBoolean("addEnable", settings[9].equals("T"))
                            .putString("goalAmount", settings[10]).putBoolean("mulEnable", settings[11].equals("T"))
                            .putString("inputSize", settings[12]).putBoolean("subEnable", settings[13].equals("T"))
                            .putString("inputAmountEditable", settings[14]).putBoolean("divEnable", settings[15].equals("T"))
                            .putString("stackSize", settings[16]).putBoolean("allowUndo", settings[17].equals("T"))
                            .putString("cipher", settings[18]).putBoolean("prefixZero", settings[19].equals("T"))
                            .putString("anyMatch", settings[20]).putInt("scoreRatio", Integer.parseInt(settings[21])).apply();
                } catch (Exception ignored) {
                    Toast.makeText(getActivity(), "Invalid Settings: " + settings.length + "/22", Toast.LENGTH_SHORT).show();
                }
            }
            NavHostFragment.findNavController(DialogSeedFragment.this).navigate(R.id.GameFragment);
            dismiss();
        });
    }
}
