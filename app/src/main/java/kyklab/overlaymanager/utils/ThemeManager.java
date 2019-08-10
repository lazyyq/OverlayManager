package kyklab.overlaymanager.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import kyklab.overlaymanager.App;
import kyklab.overlaymanager.R;

public class ThemeManager {
    public static final int THEME_AUTO = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    private static final String KEY_THEME = "key_theme";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private ThemeManager() {
        pref = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = pref.edit();
        editor.apply();
    }

    public static ThemeManager getInstance() {
        return Holder.INSTANCE;
    }

    public static int getCurrentTheme() {
        return AppCompatDelegate.getDefaultNightMode();
    }

    public void init() {
        int prevTheme = pref.getInt(KEY_THEME, THEME_AUTO);
        setTheme(prevTheme);
    }

    public void setTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        editor.putInt(KEY_THEME, mode).apply();
    }

    public void switchTheme() {
        int cur = getCurrentTheme();
        switch (cur) {
            case THEME_AUTO:
                Toast.makeText(App.getContext(), R.string.theme_light, Toast.LENGTH_SHORT).show();
                setTheme(THEME_LIGHT);
                break;
            case THEME_LIGHT:
                Toast.makeText(App.getContext(), R.string.theme_dark, Toast.LENGTH_SHORT).show();
                setTheme(THEME_DARK);
                break;
            case THEME_DARK:
            default:
                Toast.makeText(App.getContext(), R.string.theme_auto, Toast.LENGTH_SHORT).show();
                setTheme(THEME_AUTO);
                break;
        }
    }

    private static class Holder {
        public static final ThemeManager INSTANCE = new ThemeManager();
    }
}
