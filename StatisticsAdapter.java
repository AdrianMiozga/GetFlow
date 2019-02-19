package com.wentura.pomodoro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.ViewHolder> {

    private List<StatisticsItem> statisticsItems;

    StatisticsAdapter(List<StatisticsItem> statisticsItems) {
        this.statisticsItems = statisticsItems;
    }

    @NonNull
    @Override
    public StatisticsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View statisticsView = inflater.inflate(R.layout.item_statistic, parent, false);

        return new ViewHolder(statisticsView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsAdapter.ViewHolder holder, int position) {
        StatisticsItem statisticsItem = statisticsItems.get(position);

        Context context = holder.dateTextView.getContext();

        holder.completedWorksTextView.setText(context.getResources().getString(R.string.completed_works,
                statisticsItem.getCompletedWorks()));

        holder.completedBreaksTextView.setText(context.getResources().getString(R.string.completed_breaks,
                statisticsItem.getCompletedBreaks()));

        holder.dateTextView.setText(statisticsItem.getDate());
    }

    @Override
    public int getItemCount() {
        return statisticsItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView completedWorksTextView;
        TextView completedBreaksTextView;

        ViewHolder(View view) {
            super(view);

            dateTextView = view.findViewById(R.id.date);
            completedBreaksTextView = view.findViewById(R.id.completed_breaks);
            completedWorksTextView = view.findViewById(R.id.completed_works);
        }
    }
}
