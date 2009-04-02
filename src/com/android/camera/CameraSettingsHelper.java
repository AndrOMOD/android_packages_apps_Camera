package com.android.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;

public class CameraSettingsHelper {
    private static final int FIRST_REQUEST_CODE = 100;
    private static final int NOT_FOUND = -1;

    public static final String KEY_RECORD_LOCATION =
            "pref_camera_recordlocation_key";
    public static final String KEY_VIDEO_QUALITY =
            "pref_camera_videoquality_key";
    public static final String KEY_VIDEO_DURATION =
            "pref_camera_video_duration_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_coloreffect_key";
    public static final String KEY_WHITE_BALANCE =
            "pref_camera_whitebalance_key";

    // max mms video duration in seconds.
    public static final int MMS_VIDEO_DURATION =
            SystemProperties.getInt("ro.media.enc.lprof.duration", 60);

    private static final boolean DEFAULT_VIDEO_QUALITY_VALUE = true;

    //MMS video length
    private static final int DEFAULT_VIDEO_DURATION_VALUE = -1;
    private static final String TAG = "CameraSettingsHelper";


    private final Context mContext;
    private final Parameters mParameters;
    private final PreferenceScreen mScreen;

    public CameraSettingsHelper(Activity activity, Parameters parameters) {
        mContext = activity;
        mParameters = parameters;
        PreferenceManager manager =
                new PreferenceManager(activity, FIRST_REQUEST_CODE);
        mScreen = manager.createPreferenceScreen(activity);
        manager.inflateFromResource(
                activity, R.xml.camera_preferences, mScreen);
        initPreference(mScreen);
    }

    public PreferenceScreen getPreferenceScreen() {
        return mScreen;
    }

    private void setDefaultIfNull(String key, int strRes) {
        ListPreference pref = (ListPreference) mScreen.findPreference(key);
        if (pref.getValue() == null) pref.setValue(mContext.getString(strRes));
    }

    private void initPreference(PreferenceScreen screen) {

        ListPreference videoDuration =
                (ListPreference) screen.findPreference(KEY_VIDEO_DURATION);
        ListPreference pictureSize =
                (ListPreference) screen.findPreference(KEY_PICTURE_SIZE);
        ListPreference whiteBalance =
                (ListPreference) screen.findPreference(KEY_WHITE_BALANCE);
        ListPreference colorEffect =
                (ListPreference) screen.findPreference(KEY_COLOR_EFFECT);

        // Modify video duration settings.
        // The first entry is for MMS video duration, and we need to fill in the
        // device-dependent value (in seconds).
        CharSequence[] entries = videoDuration.getEntries();
        entries[0] = String.format(entries[0].toString(), MMS_VIDEO_DURATION);

        // Filter out unsupported settings / options
        filterUnsupportedOptions(screen, pictureSize,
                sizeListToStringList(mParameters.getSupportedPictureSizes()));
        filterUnsupportedOptions(screen,
                whiteBalance, mParameters.getSupportedWhiteBalance());
        filterUnsupportedOptions(screen,
                colorEffect, mParameters.getSupportedColorEffects());

        setDefaultIfNull(
                KEY_JPEG_QUALITY, R.string.pref_camera_jpegquality_default);
        setDefaultIfNull(
                KEY_FOCUS_MODE, R.string.pref_camera_focusmode_default);
    }

    private boolean removePreference(PreferenceGroup group, Preference remove) {
        if (group.removePreference(remove)) return true;

        for (int i = 0; i < group.getPreferenceCount(); i++) {
            final Preference child = group.getPreference(i);
            if (child instanceof PreferenceGroup) {
                if (removePreference((PreferenceGroup) child, remove)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void filterUnsupportedOptions(PreferenceScreen screen,
            ListPreference pref, List<String> supported) {

        // Remove the preference if the parameter is not supported.
        if (supported == null) {
            removePreference(screen, pref);
            return;
        }

        // Prepare setting entries and entry values.
        CharSequence[] allEntries = pref.getEntries();
        CharSequence[] allEntryValues = pref.getEntryValues();
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (int i = 0, len = allEntryValues.length; i < len; i++) {
            if (supported.indexOf(allEntryValues[i].toString()) != NOT_FOUND) {
                entries.add(allEntries[i]);
                entryValues.add(allEntryValues[i]);
            }
        }

        // Set entries and entry values to list preference.
        pref.setEntries(entries.toArray(new CharSequence[entries.size()]));
        pref.setEntryValues(entryValues.toArray(
                new CharSequence[entryValues.size()]));

        // Set the value to the first entry if it is invalid.
        String value = pref.getValue();
        if (pref.findIndexOfValue(value) == NOT_FOUND) {
            pref.setValueIndex(0);
        }
    }

    private static List<String> sizeListToStringList(List<Size> sizes) {
        ArrayList<String> list = new ArrayList<String>();
        for (Size size : sizes) {
            list.add(String.format("%dx%d", size.width, size.height));
        }
        return list;
    }
}