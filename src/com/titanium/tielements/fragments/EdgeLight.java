/*
 * Copyright (C) 2018 TitaniumOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.titanium.tielements.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.titanium.support.colorpicker.ColorPickerPreference;
import com.titanium.support.preferences.CustomSeekBarPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

public class EdgeLight extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "EdgeLight";
    private static final String AMBIENT_LIGHT_COLOR = "ambient_light_color";
    private static final String AMBIENT_LIGHT_CUSTOM_COLOR = "ambient_light_custom_color";
    private static final String AMBIENT_LIGHT_DURATION = "ambient_light_duration";
    private static final String AMBIENT_LIGHT_REPEAT_COUNT = "ambient_light_repeat_count";

    private ListPreference mEdgeLightColorMode;
    private ColorPickerPreference mEdgeLightColor;
    private CustomSeekBarPreference mEdgeLightDuration;
    private CustomSeekBarPreference mEdgeLightRepeatCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.edge_light);

        mEdgeLightColorMode = (ListPreference) findPreference(AMBIENT_LIGHT_COLOR);
        int edgeLightColorMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_COLOR, 0, UserHandle.USER_CURRENT);
        mEdgeLightColorMode.setValue(String.valueOf(edgeLightColorMode));
        mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntry());
        mEdgeLightColorMode.setOnPreferenceChangeListener(this);

        mEdgeLightColor = (ColorPickerPreference) findPreference(AMBIENT_LIGHT_CUSTOM_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_CUSTOM_COLOR, 0xFF3980FF);
        mEdgeLightColor.setNewPreviewColor(edgeLightColor);
        String edgeLightColorHex = String.format("#%08x", (0xFF3980FF & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColor.setSummary(R.string.default_string);
        } else {
            mEdgeLightColor.setSummary(edgeLightColorHex);
        }
        mEdgeLightColor.setOnPreferenceChangeListener(this);

        mEdgeLightDuration = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_DURATION);
        int lightDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_DURATION, 2, UserHandle.USER_CURRENT);
        mEdgeLightDuration.setValue(lightDuration);
        mEdgeLightDuration.setOnPreferenceChangeListener(this);

        mEdgeLightRepeatCount = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_REPEAT_COUNT);
        int edgeLightRepeatCount = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, 0, UserHandle.USER_CURRENT);
        mEdgeLightRepeatCount.setValue(edgeLightRepeatCount);
        mEdgeLightRepeatCount.setOnPreferenceChangeListener(this);

        updateColorPrefs(edgeLightColorMode);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEdgeLightColorMode) {
            int edgeLightColorMode = Integer.valueOf((String) newValue);
            int index = mEdgeLightColorMode.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_COLOR, edgeLightColorMode, UserHandle.USER_CURRENT);
            mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntries()[index]);
            updateColorPrefs(edgeLightColorMode);
            return true;
        } else if (preference == mEdgeLightColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_CUSTOM_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightDuration) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_DURATION, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightRepeatCount) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int edgeLightColorMode) {
        if (mEdgeLightColor != null) {
            if (edgeLightColorMode == 3) {
                getPreferenceScreen().addPreference(mEdgeLightColor);
            } else {
                getPreferenceScreen().removePreference(mEdgeLightColor);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TIELEMENTS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                ArrayList<SearchIndexableResource> result =
                        new ArrayList<SearchIndexableResource>();

                SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.edge_light;
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
