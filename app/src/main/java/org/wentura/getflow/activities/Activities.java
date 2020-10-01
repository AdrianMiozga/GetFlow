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

package org.wentura.getflow.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;
import org.wentura.getflow.Utility;
import org.wentura.getflow.database.Activity;
import org.wentura.getflow.database.Database;

public class Activities extends AppCompatActivity {

    private static Database database;
    private static ActivitiesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        database = Database.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.activities_list);
        FloatingActionButton addActivity = findViewById(R.id.add_activity_button);

        addActivity.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.activity_name);

            EditText editText = new EditText(this);

            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            editText.requestFocus();

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().isEmpty() ||
                            editable.toString().length() > Constants.MAX_ACTIVITY_NAME_LENGTH) {
                        editText.getRootView().findViewById(android.R.id.button1).setEnabled(false);
                    } else {
                        editText.getRootView().findViewById(android.R.id.button1).setEnabled(true);
                    }
                }
            });

            builder.setView(getLinearLayoutWithMargins(editText));

            builder.setPositiveButton(getString(R.string.OK), null);

            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.show();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(v ->
                    Database.databaseExecutor.execute(() -> {
                        boolean isNameOccupied = database.activityDao().isNameOccupied(editText.getText().toString());

                        if (isNameOccupied) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, R.string.activity_with_that_name_exists, Toast.LENGTH_LONG)
                                            .show());
                        } else {
                            dialog.dismiss();

                            database.activityDao().insertActivity(new Activity(editText.getText().toString()));

                            adapter = new ActivitiesAdapter(this, database.activityDao().getAll());
                            runOnUiThread(() -> {
                                recyclerView.setAdapter(adapter);
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                            });
                        }
                    }));

            Window window = dialog.getWindow();

            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Database.databaseExecutor.execute(() ->
                adapter = new ActivitiesAdapter(this, database.activityDao().getAll()));

        Database.databaseExecutor.execute(() ->
                recyclerView.setAdapter(adapter));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private LinearLayout getLinearLayoutWithMargins(EditText input) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) Utility.convertDpToPixel(20, this), 0,
                (int) Utility.convertDpToPixel(20, this), 0);

        input.setLayoutParams(layoutParams);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

        linearLayout.addView(input, layoutParams);
        return linearLayout;
    }
}
