package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (MainActivity.darkMode) {
            view.setBackgroundColor(Color.argb(120, 120, 120, 120));
        }
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        bindNumbersOnly("startMax");
        bindNumbersOnly("goalMax");
        bindNumbersOnly("inputMax");
        bindNumbersOnly("base");
        bindNumbersOnly("goalAmount");
        bindNumbersOnly("inputSize");
        bindNumbersOnly("inputAmountEditable");
        bindNumbersOnly("stackSize");
    }

    private void bindNumbersOnly(String key) {
        EditTextPreference editTextPreference = findPreference(key);
        if (editTextPreference != null) {
            editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
    }
}