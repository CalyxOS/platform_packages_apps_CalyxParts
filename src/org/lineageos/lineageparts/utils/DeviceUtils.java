/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-FileCopyrightText: 2017-2023 The LineageOS project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.utils;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import androidx.annotation.NonNull;

public class DeviceUtils {

    /* returns whether the device has a centered display cutout or not. */
    public static boolean hasCenteredCutout(Context context) {
        Display display = context.getDisplay();
        DisplayCutout cutout = display.getCutout();
        if (cutout != null) {
            Point realSize = new Point();
            display.getRealSize(realSize);

            switch (display.getRotation()) {
                case Surface.ROTATION_0: {
                    Rect rect = cutout.getBoundingRectTop();
                    return !(rect.left <= 0 || rect.right >= realSize.x);
                }
                case Surface.ROTATION_90: {
                    Rect rect = cutout.getBoundingRectLeft();
                    return !(rect.top <= 0 || rect.bottom >= realSize.y);
                }
                case Surface.ROTATION_180: {
                    Rect rect = cutout.getBoundingRectBottom();
                    return !(rect.left <= 0 || rect.right >= realSize.x);
                }
                case Surface.ROTATION_270: {
                    Rect rect = cutout.getBoundingRectRight();
                    return !(rect.top <= 0 || rect.bottom >= realSize.y);
                }
            }
        }
        return false;
    }

    public static int getDeviceKeys(Context context) {
        return context.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
    }

    public static int getDeviceWakeKeys(Context context) {
        return context.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareWakeKeys);
    }

    /* returns whether the device has power key or not. */
    public static boolean hasPowerKey() {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
    }

    /* returns whether the device has volume rocker or not. */
    public static boolean hasVolumeKeys(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_VOLUME) != 0;
    }

    public static boolean deviceSupportsFlashLight(@NonNull Context context) {
        CameraManager cameraManager = context.getSystemService(CameraManager.class);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null
                        && flashAvailable
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (CameraAccessException | AssertionError e) {
            // Ignore
        }
        return false;
    }

    public static boolean isEdgeToEdgeEnabled(Context context) {
        return NAV_BAR_MODE_GESTURAL == context.getResources().getInteger(
                com.android.internal.R.integer.config_navBarInteractionMode);
    }
}
