/*
 * SPDX-FileCopyrightText: 2014-2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.statusbar;

import static org.lineageos.lineageparts.utils.ResourceUtils.isRtlMode;

import android.content.Intent;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.preference.PreferenceCategory;

import com.android.settingslib.fuelgauge.BatteryUtils;

import lineageos.preference.LineageSecureSettingListPreference;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment {

    private static final String CATEGORY_BATTERY = "status_bar_battery_key";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String QS_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String QS_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private static final int QS_BRIGHTNESS_SLIDER_HIDDEN = 0;

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private LineageSecureSettingListPreference mQsBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mQsShowAutoBrightness;
    private LineageSystemSettingListPreference mQuickPulldown;

    private PreferenceCategory mStatusBarBatteryCategory;

    private boolean mBatteryPresent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        LineageSystemSettingListPreference statusBarBattery =
                findPreference(STATUS_BAR_BATTERY_STYLE);
        statusBarBattery.setOnPreferenceChangeListener((preference, newValue) -> {
            enableStatusBarBatteryDependents(Integer.parseInt((String) newValue));
            return true;
        });
        enableStatusBarBatteryDependents(statusBarBattery.getIntValue(2));

        Intent intent = BatteryUtils.getBatteryIntent(getContext());
        if (intent != null) {
            mBatteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
        }
        mStatusBarBatteryCategory = getPreferenceScreen().findPreference(CATEGORY_BATTERY);

        mQsShowAutoBrightness = findPreference(QS_SHOW_AUTO_BRIGHTNESS);
        mQsBrightnessSliderPosition = findPreference(QS_BRIGHTNESS_SLIDER_POSITION);
        LineageSecureSettingListPreference qsShowBrightnessSlider =
                findPreference(QS_SHOW_BRIGHTNESS_SLIDER);
        qsShowBrightnessSlider.setOnPreferenceChangeListener((preference, newValue) -> {
            enableQuickSettingsBrightnessSliderDependents(Integer.parseInt((String) newValue));
            return true;
        });
        enableQuickSettingsBrightnessSliderDependents(qsShowBrightnessSlider.getIntValue(1));

        mQuickPulldown = findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setSummaryProvider(preference -> {
            int value = Integer.parseInt(
                    ((LineageSystemSettingListPreference) preference).getValue());
            Resources res = preference.getContext().getResources();

            switch (value) {
                case PULLDOWN_DIR_NONE:
                    return res.getString(R.string.status_bar_quick_qs_pulldown_off);
                case PULLDOWN_DIR_LEFT:
                case PULLDOWN_DIR_RIGHT:
                    int side = (value == PULLDOWN_DIR_LEFT) ^ isRtlMode(res)
                            ? R.string.status_bar_quick_qs_pulldown_summary_left
                            : R.string.status_bar_quick_qs_pulldown_summary_right;

                    return res.getString(R.string.status_bar_quick_qs_pulldown_summary,
                            res.getString(side));
            }
            return "";
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final String curIconBlacklist = Settings.Secure.getString(getContext().getContentResolver(),
                ICON_BLACKLIST);

        if (!mBatteryPresent ||
                TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
            getPreferenceScreen().removePreference(mStatusBarBatteryCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarBatteryCategory);
        }

        // Adjust status bar preferences for RTL
        if (isRtlMode(getResources())) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
        } else {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries);
        }
    }

    private void enableQuickSettingsBrightnessSliderDependents(int showBrightnessSlider) {
        boolean enabled = showBrightnessSlider != QS_BRIGHTNESS_SLIDER_HIDDEN;

        mQsBrightnessSliderPosition.setEnabled(enabled);
        mQsShowAutoBrightness.setEnabled(enabled);
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }
}
