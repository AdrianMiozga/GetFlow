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

package org.wentura.getflow.applicationlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

import androidx.preference.PreferenceManager;

import org.wentura.getflow.Constants;
import org.wentura.getflow.MainActivity;

import java.util.HashSet;
import java.util.Set;

public class ApplicationLockService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!sharedPreferences.getBoolean(Constants.MASTER_LOCK_APPLICATION_SETTING, false)) {
            return;
        }

        if (!sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING, false)) {
            return;
        }

        if (sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false)) {
            return;
        }

        String packageName = String.valueOf(event.getPackageName());

        Set<String> applicationList =
                sharedPreferences.getStringSet(Constants.LOCKED_APPLICATIONS_LIST, new HashSet<>());

        if (applicationList.contains(packageName)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onInterrupt() {

    }
}
