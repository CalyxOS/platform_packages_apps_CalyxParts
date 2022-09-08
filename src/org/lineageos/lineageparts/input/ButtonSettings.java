/*
 * Copyright (C) 2016 The CyanogenMod project
 *               2017-2022 The LineageOS project
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

package org.lineageos.lineageparts.input;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY;

import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import static com.android.systemui.shared.recents.utilities.Utilities.isTablet;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;
import org.lineageos.lineageparts.search.BaseSearchIndexProvider;
import org.lineageos.lineageparts.search.Searchable;
import org.lineageos.lineageparts.utils.DeviceUtils;
import org.lineageos.lineageparts.utils.TelephonyUtils;
import org.lineageos.internal.util.ScreenType;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

public class ButtonSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Searchable {
    private static final String TAG = "SystemSettings";

    /*
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen";
    private static final String KEY_VOLUME_ANSWER_CALL = "volume_answer_call";
    */
    private static final String KEY_NAVIGATION_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    /*
    private static final String KEY_NAVIGATION_BACK_LONG_PRESS = "navigation_back_long_press";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    */
    private static final String KEY_TORCH_LONG_PRESS_POWER_GESTURE =
            "torch_long_press_power_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT =
            "torch_long_press_power_timeout";
    /*
    private static final String KEY_CLICK_PARTIAL_SCREENSHOT =
            "click_partial_screenshot";
    private static final String KEY_NAV_BAR_INVERSE = "sysui_nav_bar_inverse";
    private static final String KEY_ENABLE_TASKBAR = "enable_taskbar";
    */

    private static final String CATEGORY_POWER = "power_key";
    /*
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_NAVBAR = "navigation_bar_category";
    private static final String CATEGORY_EXTRAS = "extras_category";
    */

    /*
    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mNavigationArrowKeys;
    private ListPreference mNavigationBackLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;
    private SwitchPreference mPowerEndCall;
    */
    private ListPreference mTorchLongPressPowerTimeout;
    /*
    private SwitchPreference mNavBarInverse;
    private SwitchPreference mEnableTaskbar;

    private PreferenceCategory mNavigationPreferencesCat;
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        /*
        final Resources res = getResources();
        */
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean hasPowerKey = DeviceUtils.hasPowerKey();
        /*
        final boolean hasVolumeKeys = DeviceUtils.hasVolumeKeys(getActivity());

        final boolean showVolumeWake = DeviceUtils.canWakeUsingVolumeKeys(getActivity());
        */

        final PreferenceCategory powerCategory = prefScreen.findPreference(CATEGORY_POWER);
        /*
        final PreferenceCategory volumeCategory = prefScreen.findPreference(CATEGORY_VOLUME);
        */
        final PreferenceCategory extrasCategory = prefScreen.findPreference(CATEGORY_EXTRAS);

        /*
        // Power button ends calls.
        mPowerEndCall = findPreference(KEY_POWER_END_CALL);
        */

        // Long press power while display is off to activate torchlight
        SwitchPreference torchLongPressPowerGesture =
                findPreference(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
        final int torchLongPressPowerTimeout = LineageSettings.System.getInt(resolver,
                LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout = initList(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT,
                torchLongPressPowerTimeout);

        /*
        mNavigationPreferencesCat = findPreference(CATEGORY_NAVBAR);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action backLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action homeLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);

        // Navigation bar arrow keys while typing
        mNavigationArrowKeys = findPreference(KEY_NAVIGATION_ARROW_KEYS);

        // Navigation bar back long press
        mNavigationBackLongPressAction = initList(KEY_NAVIGATION_BACK_LONG_PRESS,
                backLongPressAction);

        // Navigation bar home long press
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction);

        // Navigation bar home double tap
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction);

        // Navigation bar app switch long press
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);
        */

        if (hasPowerKey) {
            /*
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
            */
            if (!DeviceUtils.deviceSupportsFlashLight(getActivity())) {
                powerCategory.removePreference(torchLongPressPowerGesture);
                powerCategory.removePreference(mTorchLongPressPowerTimeout);
            }
        }
        if (!hasPowerKey || powerCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(powerCategory);
        }

        /*
        if (hasVolumeKeys) {
            if (!showVolumeWake) {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_ANSWER_CALL));
            }

            int cursorControlAction = Settings.System.getInt(resolver,
                    "volume_key_cursor_control", 0);
            mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);
        } else {
            extrasCategory.removePreference(findPreference(KEY_CLICK_PARTIAL_SCREENSHOT));
        }
        if (!hasVolumeKeys || volumeCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(volumeCategory);
        }

        SwitchPreference volumeWakeScreen = findPreference(KEY_VOLUME_WAKE_SCREEN);
        SwitchPreference volumeMusicControls = findPreference(KEY_VOLUME_MUSIC_CONTROLS);

        if (volumeWakeScreen != null) {
            if (volumeMusicControls != null) {
                volumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN);
                volumeWakeScreen.setDisableDependentsState(true);
            }
        }

        mNavBarInverse = findPreference(KEY_NAV_BAR_INVERSE);

        mEnableTaskbar = findPreference(KEY_ENABLE_TASKBAR);
        if (mEnableTaskbar != null) {
            if (!isTablet(getContext()) || !hasNavigationBar()) {
                mNavigationPreferencesCat.removePreference(mEnableTaskbar);
            } else {
                mEnableTaskbar.setOnPreferenceChangeListener(this);
                mEnableTaskbar.setChecked(LineageSettings.System.getInt(resolver,
                        LineageSettings.System.ENABLE_TASKBAR,
                        isTablet(getContext()) ? 1 : 0) == 1);
                toggleTaskBarDependencies(mEnableTaskbar.isChecked());
            }
        }

        List<Integer> unsupportedValues = new ArrayList<>();
        List<String> entries = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.hardware_keys_action_entries)));
        List<String> values = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.hardware_keys_action_values)));

        // hide split screen option unconditionally - it doesn't work at the moment
        // once someone gets it working again: hide it only for low-ram devices
        // (check ActivityManager.isLowRamDeviceStatic())
        unsupportedValues.add(Action.SPLIT_SCREEN.ordinal());

        for (int unsupportedValue: unsupportedValues) {
            entries.remove(unsupportedValue);
            values.remove(unsupportedValue);
        }

        String[] actionEntries = entries.toArray(new String[0]);
        String[] actionValues = values.toArray(new String[0]);

        mNavigationBackLongPressAction.setEntries(actionEntries);
        mNavigationBackLongPressAction.setEntryValues(actionValues);

        mNavigationHomeLongPressAction.setEntries(actionEntries);
        mNavigationHomeLongPressAction.setEntryValues(actionValues);

        mNavigationHomeDoubleTapAction.setEntries(actionEntries);
        mNavigationHomeDoubleTapAction.setEntryValues(actionValues);

        mNavigationAppSwitchLongPressAction.setEntries(actionEntries);
        mNavigationAppSwitchLongPressAction.setEntryValues(actionValues);
        */
    }

    /*
    @Override
    public void onResume() {
        super.onResume();

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
        }
    }
    */

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    /*
    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }
    */

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        /*
        if (preference == mVolumeKeyCursorControl) {
            handleSystemListChange(mVolumeKeyCursorControl, newValue,
                    "volume_key_cursor_control");
            return true;
        } else
        */
        if (preference == mTorchLongPressPowerTimeout) {
            handleListChange(mTorchLongPressPowerTimeout, newValue,
                    LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT);
            return true;
        /*
        } else if (preference == mEnableTaskbar) {
            toggleTaskBarDependencies((Boolean) newValue);
            if ((Boolean) newValue && is2ButtonNavigationEnabled(getContext())) {
                // Let's switch to gestural mode if user previously had 2 buttons enabled.
                setButtonNavigationMode(NAV_BAR_MODE_GESTURAL_OVERLAY);
            }
            LineageSettings.System.putInt(getContentResolver(),
                    LineageSettings.System.ENABLE_TASKBAR, ((Boolean) newValue) ? 1 : 0);
            return true;
        */
        }
        return false;
    }

    /*
    private static boolean is2ButtonNavigationEnabled(Context context) {
        return NAV_BAR_MODE_2BUTTON == context.getResources().getInteger(
                com.android.internal.R.integer.config_navBarInteractionMode);
    }

    private static void setButtonNavigationMode(String overlayPackage) {
        IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        try {
            overlayManager.setEnabledExclusiveInCategory(overlayPackage, UserHandle.USER_CURRENT);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void toggleTaskBarDependencies(boolean enabled) {
        enablePreference(mNavigationArrowKeys, !enabled);
        enablePreference(mNavBarInverse, !enabled);
        enablePreference(mNavigationBackLongPressAction, !enabled);
        enablePreference(mNavigationHomeLongPressAction, !enabled);
        enablePreference(mNavigationHomeDoubleTapAction, !enabled);
        enablePreference(mNavigationAppSwitchLongPressAction, !enabled);
    }

    private void enablePreference(Preference pref, boolean enabled) {
        if (pref != null) {
            pref.setEnabled(enabled);
        }
    }
    */

    private static boolean hasNavigationBar() {
        boolean hasNavigationBar = false;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            hasNavigationBar = windowManager.hasNavigationBar(Display.DEFAULT_DISPLAY);
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }
        return hasNavigationBar;
    }

    /*
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }
    */

    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final Set<String> result = new ArraySet<String>();

            /*
            if (!TelephonyUtils.isVoiceCapable(context)) {
                result.add(KEY_POWER_END_CALL);
                result.add(KEY_VOLUME_ANSWER_CALL);
            }

            if (!DeviceUtils.hasVolumeKeys(context)) {
                result.add(CATEGORY_VOLUME);
                result.add(KEY_VOLUME_ANSWER_CALL);
                result.add(KEY_VOLUME_KEY_CURSOR_CONTROL);
                result.add(KEY_VOLUME_MUSIC_CONTROLS);
                result.add(KEY_VOLUME_WAKE_SCREEN);
                result.add(KEY_CLICK_PARTIAL_SCREENSHOT);
            } else if (!DeviceUtils.canWakeUsingVolumeKeys(context)) {
                result.add(KEY_VOLUME_WAKE_SCREEN);
            }
            */

            if (!DeviceUtils.deviceSupportsFlashLight(context)) {
                result.add(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
                result.add(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);
            }

            if (hasNavigationBar()) {
                if (DeviceUtils.isEdgeToEdgeEnabled(context)) {
                    result.add(KEY_NAVIGATION_ARROW_KEYS);
                    /*
                    result.add(KEY_NAVIGATION_HOME_LONG_PRESS);
                    result.add(KEY_NAVIGATION_HOME_DOUBLE_TAP);
                    result.add(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS);
                } else if (DeviceUtils.isSwipeUpEnabled(context)) {
                    result.add(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS);
                    */
                }
            }
            return result;
        }
    };
}
