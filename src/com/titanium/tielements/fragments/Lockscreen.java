/*
 * Copyright (C) 2019 TitaniumOS
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

package com.titanium.tielements.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;
import com.titanium.support.preferences.SecureSettingMasterSwitchPreference;
import com.titanium.support.preferences.CustomSeekBarPreference;
import com.titanium.support.preferences.SystemSettingSeekBarPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

public class Lockscreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "Lockscreen";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private SecureSettingMasterSwitchPreference mVisualizerEnabled;
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";
    private static final String CLOCK_FONT_SIZE = "lockclock_font_size";
    private static final String DATE_FONT_SIZE  = "lockdate_font_size";
    private static final String LOCK_OWNERINFO_FONTS = "lock_ownerinfo_fonts";
    private static final String LOCKOWNER_FONT_SIZE = "lockowner_font_size";

    private PreferenceCategory mFODIconPickerCategory;
    private ListPreference mLockClockSelection;
    private ListPreference mTextClockAlign;
    private ListPreference mLockClockFonts;
    private ListPreference mLockDateFonts;
    private CustomSeekBarPreference mClockFontSize;
    private CustomSeekBarPreference mDateFontSize;
    private CustomSeekBarPreference mOwnerInfoFontSize;
    private ListPreference mLockOwnerInfoFonts;
    private SystemSettingSeekBarPreference mTextClockPadding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen);
        setRetainInstance(true);

        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null
                && !getResources().getBoolean(com.android.internal.R.bool.config_supportsInDisplayFingerprint)) {
            prefScreen.removePreference(mFODIconPickerCategory);

        // Lockscreen Clock
        mLockClockSelection = (ListPreference) findPreference(LOCKSCREEN_CLOCK_SELECTION);
        boolean mClockSelection = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 9
                || Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 10;
        mLockClockSelection.setOnPreferenceChangeListener(this);

        // Lockscreen Clock Fonts
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 28)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 28)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Clock Size
        mClockFontSize = (CustomSeekBarPreference) findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 54));
        mClockFontSize.setOnPreferenceChangeListener(this);

        // Text Clock Alignment
        mTextClockAlign = (ListPreference) findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setEnabled(mClockSelection);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = (SystemSettingSeekBarPreference) findPreference(TEXT_CLOCK_PADDING);
        boolean mTextClockAlignx = Settings.System.getIntForUser(resolver,
                    Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;
        mTextClockPadding.setEnabled(!mTextClockAlignx);

        // Lockscreen Date Size
        mDateFontSize = (CustomSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE, 18));
        mDateFontSize.setOnPreferenceChangeListener(this);

        // Lockscreen Owner Info Fonts
        mLockOwnerInfoFonts = (ListPreference) findPreference(LOCK_OWNERINFO_FONTS);
        mLockOwnerInfoFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_OWNERINFO_FONTS, 28)));
        mLockOwnerInfoFonts.setSummary(mLockOwnerInfoFonts.getEntry());
        mLockOwnerInfoFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Owner Info Size
        mOwnerInfoFontSize = (CustomSeekBarPreference) findPreference(LOCKOWNER_FONT_SIZE);
        mOwnerInfoFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKOWNER_FONT_SIZE, 18));
        mOwnerInfoFontSize.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TIELEMENTS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
       ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(),
		            LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) objValue));
            mLockClockFonts.setValue(String.valueOf(objValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mLockClockSelection) {
            boolean val = Integer.valueOf((String) objValue) == 9
                    || Integer.valueOf((String) objValue) == 10;
            mTextClockAlign.setEnabled(val);
            return true;
        } else if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) objValue));
            mLockDateFonts.setValue(String.valueOf(objValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mTextClockAlign) {
            boolean val = Integer.valueOf((String) objValue) == 1;
            mTextClockPadding.setEnabled(!val);
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
       } else if (preference == mLockOwnerInfoFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_OWNERINFO_FONTS,
                    Integer.valueOf((String) objValue));
            mLockOwnerInfoFonts.setValue(String.valueOf(objValue));
            mLockOwnerInfoFonts.setSummary(mLockOwnerInfoFonts.getEntry());
            return true;
        } else if (preference == mOwnerInfoFontSize) {
            int top = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKOWNER_FONT_SIZE, top*1);
            return true;
        }
        return false;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                ArrayList<SearchIndexableResource> result =
                        new ArrayList<SearchIndexableResource>();

                SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.lockscreen;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
    };

}
