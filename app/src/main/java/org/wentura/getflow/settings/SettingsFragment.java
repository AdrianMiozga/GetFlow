/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.wentura.getflow.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;
import org.wentura.getflow.applicationlock.ApplicationLockActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(Constants.APPLICATION_LOCK_PREFERENCE)) {
            Intent intent = new Intent(getContext(), ApplicationLockActivity.class);
            preference.setIntent(intent);
        }
        return super.onPreferenceTreeClick(preference);
    }
}
