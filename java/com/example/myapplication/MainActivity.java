package com.example.myapplication;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static Boolean darkMode;
    public static int moves = 0;
    public static long time = 0;
    public static long seed = 0;
    public static String settings = "";
    public static int origin;

    NavHostFragment navHostFragment;
    FloatingActionButton fab, fab2;
    Random random = new Random();
    Toolbar toolbar;
    Window window;
    int[] colorScheme;
    int theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeSeed();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (getIntent().getBooleanExtra("mix_match.reset", false)) {
            prefs.edit().clear().apply();
        }
        darkMode = prefs.getBoolean("darkMode", false);
        theme = prefs.getInt("theme", 0);

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        window = getWindow();
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        fab = findViewById(R.id.fab);
        fab2 = findViewById(R.id.fab2);
        setTheme();

        fab.setOnClickListener(view -> {
            darkMode();
            prefs.edit().putBoolean("darkMode", darkMode).apply();
        });

        fab2.setOnClickListener(view -> {
            changeSeed();
            Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (fragment instanceof GameFragment) {
                ((GameFragment) fragment).restart();
            }
            if (fragment instanceof ScoreFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_ScoreFragment_to_GameFragment);
            }
        });

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            toolbar.setVisibility(View.GONE);
            fab.hide();
            fab2.setTranslationY(220);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (fragment instanceof SettingsFragment) {
            adaptOverlay("game");
            if (origin == 0) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_SettingsFragment_to_GameFragment);
            } else if (origin == 1) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_SettingsFragment_to_ScoreFragment);
            }
        } else if (fragment instanceof WindowFragment) {
            adaptOverlay("game");
            if (origin == 0) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_windowFragment_to_GameFragment);
            } else if (origin == 1) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_windowFragment_to_ScoreFragment);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

        if (id == R.id.action_settings) {
            adaptOverlay("settings");
            if (fragment instanceof GameFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_GameFragment_to_SettingsFragment);
                origin = 0;
            } else if (fragment instanceof ScoreFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_ScoreFragment_to_SettingsFragment);
                origin = 1;
            } else if (fragment instanceof WindowFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_windowFragment_to_SettingsFragment);
            }
            return true;
        } else if (id == R.id.action_reset_settings) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().clear().apply();
            prefs.edit().putBoolean("darkMode", darkMode).putInt("theme", theme).apply();
            if (fragment instanceof GameFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_GameFragment_to_GameFragment);
            }
            return true;
        } else if (id == R.id.action_play_from_seed) {
            showSeedDialog();
            return true;
        } else if (id == R.id.action_select_theme) {
            adaptOverlay("theme");
            if (fragment instanceof GameFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_GameFragment_to_windowFragment);
                origin = 0;
            } else if (fragment instanceof ScoreFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_ScoreFragment_to_windowFragment);
                origin = 1;
            } else if (fragment instanceof SettingsFragment) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_SettingsFragment_to_windowFragment);
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void darkMode() {
        darkMode = !darkMode;
        colorScheme = getColorScheme();
        window.setStatusBarColor(colorScheme[7]);
        fab.setBackgroundTintList(ColorStateList.valueOf(colorScheme[8]));
        fab2.setBackgroundTintList(ColorStateList.valueOf(colorScheme[8]));
        ((TransitionDrawable) toolbar.getBackground()).reverseTransition(400);
        ((TransitionDrawable) window.getDecorView().getBackground()).reverseTransition(400);
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (fragment instanceof GameFragment) {
            ((GameFragment) fragment).dark_mode();
        } else if (fragment instanceof ScoreFragment) {
            ((ScoreFragment) fragment).dark_mode();
        }else if (fragment instanceof WindowFragment) {
            ((WindowFragment) fragment).dark_mode();
        }
    }

    public void adaptOverlay(String location) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        switch (location) {
            case "game":
                fab.show();
                fab2.show();
                toolbar.setTitle("Mix and Match");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                break;
            case "settings":
                fab.hide();
                fab2.hide();
                toolbar.setTitle("Settings");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                break;
            case "theme":
                fab.show();
                fab2.hide();
                toolbar.setTitle("Select Theme");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                break;
        }
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            toolbar.setVisibility(View.GONE);
            fab.hide();
            fab2.setTranslationY(220);
        }
    }

    public void setTheme() {
        window.setBackgroundDrawableResource(getBackground());
        colorScheme = getColorScheme();
        toolbar.setBackground(new TransitionDrawable(new Drawable[]{new ColorDrawable(colorScheme[5]), new ColorDrawable(colorScheme[6])}));
        window.setStatusBarColor(colorScheme[7]);
        fab.setBackgroundTintList(ColorStateList.valueOf(colorScheme[8]));
        fab2.setBackgroundTintList(ColorStateList.valueOf(colorScheme[8]));
    }

    public int getBackground () {
        int windowBackground = (darkMode ? 4 : 0) + theme;
        if (windowBackground == 0) {
            return R.drawable.background_gradient_ltd_blue;
        } else if (windowBackground == 1) {
            return R.drawable.background_gradient_ltd_red;
        } else if (windowBackground == 2) {
            return R.drawable.background_gradient_ltd_green;
        } else if (windowBackground == 3) {
            return R.drawable.background_gradient_ltd_purple;
        } else if (windowBackground == 4) {
            return R.drawable.background_gradient_dtl_blue;
        } else if (windowBackground == 5) {
            return R.drawable.background_gradient_dtl_red;
        } else if (windowBackground == 6) {
            return R.drawable.background_gradient_dtl_green;
        } else {
            return R.drawable.background_gradient_dtl_purple;
        }
    }
    
    public int[] getColorScheme() {
        int colorScheme = (darkMode ? 4 : 0) + theme;
        if (colorScheme == 0) {
            return getResources().getIntArray(R.array.light_mode_blue);
        } else if (colorScheme == 1) {
            return getResources().getIntArray(R.array.light_mode_red);
        } else if (colorScheme == 2) {
            return getResources().getIntArray(R.array.light_mode_green);
        } else if (colorScheme == 3) {
            return getResources().getIntArray(R.array.light_mode_purple);
        } else if (colorScheme == 4) {
            return getResources().getIntArray(R.array.dark_mode_blue);
        } else if (colorScheme == 5) {
            return getResources().getIntArray(R.array.dark_mode_red);
        } else if (colorScheme == 6) {
            return getResources().getIntArray(R.array.dark_mode_green);
        } else {
            return getResources().getIntArray(R.array.dark_mode_purple);
        }
    }

    private void showSeedDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DialogSeedFragment dialogSeedFragment = new DialogSeedFragment();
        dialogSeedFragment.show(fm, "fragment_dialog_seed");
    }

    public void changeSeed() {
        seed = random.nextLong();
    }

    public void setSeed(long new_seed) {
        seed = new_seed;
    }
}