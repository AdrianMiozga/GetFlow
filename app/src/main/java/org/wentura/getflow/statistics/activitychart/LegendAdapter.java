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

package org.wentura.getflow.statistics.activitychart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.wentura.getflow.R;
import org.wentura.getflow.Utility;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.ViewHolder> {

    private final List<LegendItem> legendNames;
    private String longestTimeString;

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView legendName;
        final TextView percent;
        final TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            legendName = itemView.findViewById(R.id.legend_name);
            percent = itemView.findViewById(R.id.percent);
            time = itemView.findViewById(R.id.time_text_view);
        }
    }

    public LegendAdapter(List<LegendItem> legendNames) {
        this.legendNames = legendNames;

        Optional<LegendItem> stream =
                legendNames.stream().max(Comparator.comparingInt(LegendItem::getTimeLength));

        stream.ifPresent(legendItem -> longestTimeString = legendItem.getTime());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.legend_item_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.legendName.getContext();

        holder.time.setWidth((int) holder.time.getPaint().measureText(longestTimeString));

        holder.legendName.setText(legendNames.get(position).getName());
        holder.percent.setText(Utility.formatPieChartLegendPercent(context, legendNames.get(position).getPercent()));
        holder.time.setText(legendNames.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return legendNames.size();
    }
}
