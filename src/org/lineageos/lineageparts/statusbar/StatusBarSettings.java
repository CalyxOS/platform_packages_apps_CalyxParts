/*
 * SPDX-FileCopyrightText: 2014-2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.statusbar;

import static org.lineageos.lineageparts.utils.ResourceUtils.isRtlMode;

import android.content.res.Resources;
import android.os.Bundle;

import lineageos.preference.LineageSystemSettingListPreference;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment {

    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private LineageSystemSettingListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

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

        // Adjust status bar preferences for RTL
        if (isRtlMode(getResources())) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
        } else {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries);
        }
    }
}
