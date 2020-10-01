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

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private final List<Application> applicationList;
    private final SharedPreferences sharedPreferences;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView applicationName;
        final ImageView applicationIcon;
        final SwitchCompat lockApplicationSwitch;

        ViewHolder(View view) {
            super(view);
            applicationName = view.findViewById(R.id.application_name);
            applicationIcon = view.findViewById(R.id.application_icon);
            lockApplicationSwitch = view.findViewById(R.id.lock_application_switch);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            lockApplicationSwitch.performClick();
        }
    }

    public ApplicationAdapter(Context context, List<Application> applicationList) {
        this.applicationList = applicationList;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.application_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ApplicationAdapter.ViewHolder holder, final int position) {
        holder.applicationName.setText(applicationList.get(position).getApplicationName());
        holder.applicationIcon.setImageDrawable(applicationList.get(position).getApplicationIcon());

        Set<String> applicationList = sharedPreferences.getStringSet(Constants.LOCKED_APPLICATIONS_LIST,
                new HashSet<>());

        if (applicationList.contains(this.applicationList.get(position).getPackageName())) {
            holder.lockApplicationSwitch.setChecked(true);
        }

        holder.lockApplicationSwitch.setOnClickListener(view -> {
            SharedPreferences.Editor editPreferences = sharedPreferences.edit();

            if (holder.lockApplicationSwitch.isChecked()) {
                applicationList.add(ApplicationAdapter.this.applicationList.get(position).getPackageName());
            } else {
                applicationList.remove(ApplicationAdapter.this.applicationList.get(position).getPackageName());
            }
            editPreferences.putStringSet(Constants.LOCKED_APPLICATIONS_LIST, applicationList).apply();
        });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
