/*
 * Copyright (C) 2018-2020 The LineageOS Project
 * Copyright (C) 2021 The Calyx Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.lineageparts.security;

import android.os.Bundle;

import androidx.preference.ListPreference;

import lineageos.trust.TrustInterface;
import lineageos.providers.LineageSettings;

public class RestrictUsbSetting extends SettingsPreferenceFragment {
    private static final String TAG = "RestrictUsbSetting}";

    private ListPreference mUsbRestrictorPref;

    private TrustInterface mInterface;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        mInterface = TrustInterface.getInstance(getContext());

        addPreferencesFromResource(R.xml.restrict_usb_setting);
        mUsbRestrictorPref = findPreference("security_restrict_usb");
        mUsbRestrictorPref.setOnPreferenceChangeListener((p, v) ->
                onUsbRestrictorPrefChanged(Integer.parseInt((String) v)));
        setup();
    }

    private void setup() {
        if (!mInterface.hasUsbRestrictor()) {
            removePreference(mUsbRestrictorPref);
        }
    }

    private boolean onUsbRestrictorPrefChanged(Integer value) {
        LineageSettings.Global.putInt(getContext().getContentResolver(),
                LineageSettings.Global.TRUST_RESTRICT_USB, value);
        return true;
    }
}
