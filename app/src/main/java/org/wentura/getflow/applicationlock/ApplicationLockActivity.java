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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationLockActivity extends AppCompatActivity {

    private SwitchCompat masterLockApplicationSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_applications);

        masterLockApplicationSwitch = findViewById(R.id.master_lock_application_switch);
        final RecyclerView recyclerView = findViewById(R.id.application_list);
        final ProgressBar loadingBar = findViewById(R.id.loading_bar);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        masterLockApplicationSwitch.setOnClickListener(view -> {
            SharedPreferences.Editor editPreferences =
                    sharedPreferences.edit();

            if (masterLockApplicationSwitch.isChecked()) {
                if (!isAccessibilityServiceEnabled(getApplicationContext(),
                        ApplicationLockService.class)) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(ApplicationLockActivity.this);
                    builder.setMessage(R.string.accessibility_not_enabled_dialog)
                            .setPositiveButton(R.string.dialog_go_to_settings, (dialog, id) ->
                                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, (dialog, id) ->
                                    masterLockApplicationSwitch.performClick()).show();
                }

                editPreferences.putBoolean(Constants.MASTER_LOCK_APPLICATION_SETTING, true);
                editPreferences.apply();

                recyclerView.setVisibility(View.VISIBLE);

                if (recyclerView.getAdapter() != null) {
                    return;
                }

                loadingBar.setVisibility(View.VISIBLE);

                new Thread(() -> {
                    final List<Application> applicationList = getApplicationList();

                    runOnUiThread(() -> {
                        loadingBar.setVisibility(View.GONE);

                        recyclerView.setHasFixedSize(true);

                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);

                        RecyclerView.Adapter<ApplicationAdapter.ViewHolder> adapter =
                                new ApplicationAdapter(getApplicationContext(), applicationList);
                        recyclerView.setAdapter(adapter);
                    });
                }).start();
            } else {
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);

                editPreferences.putBoolean(Constants.MASTER_LOCK_APPLICATION_SETTING, false);
                editPreferences.apply();
            }
        });

        if (masterLockApplicationSwitch.isChecked() !=
                sharedPreferences.getBoolean(Constants.MASTER_LOCK_APPLICATION_SETTING, false) &&
                isAccessibilityServiceEnabled(getApplicationContext(),
                        ApplicationLockService.class)) {
            masterLockApplicationSwitch.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (masterLockApplicationSwitch.isChecked() && !isAccessibilityServiceEnabled(this,
                ApplicationLockService.class)) {
            masterLockApplicationSwitch.performClick();
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (enabledServicesSetting == null) {
            return false;
        }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName)) {
                return true;
            }
        }
        return false;
    }

    private List<Application> getApplicationList() {
        final List<Application> applicationList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        Application application;
        ApplicationInfo applicationInfo;
        String applicationName;
        String thisAppPackageName = this.getPackageName();

        for (ResolveInfo resolveInfo : resolveInfoList) {
            applicationInfo = resolveInfo.activityInfo.applicationInfo;

            if (applicationInfo == null ||
                    !applicationInfo.enabled ||
                    applicationInfo.packageName.equals(thisAppPackageName)) {
                continue;
            }

            applicationName = applicationInfo.loadLabel(packageManager).toString().replaceAll("^\\s+", "");

            application =
                    new Application(applicationInfo.packageName, applicationName,
                            applicationInfo.loadIcon(packageManager));

            applicationList.add(application);
        }

        Collections.sort(applicationList);
        return applicationList;
    }
}
