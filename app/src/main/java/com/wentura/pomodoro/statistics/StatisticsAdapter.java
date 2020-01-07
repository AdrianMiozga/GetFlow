package com.wentura.pomodoro.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wentura.pomodoro.R;
import com.wentura.pomodoro.Utility;

import java.util.List;

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

        holder.dateTextView.setText(statisticsItem.getDate());

        holder.completedWorkNumberTextView.setText(String.valueOf(statisticsItem.getCompletedWorks()));
        holder.completedWorkTimeTextView.setText(Utility.formatStatisticsTime(
                statisticsItem.getCompletedWorkTime()));

        holder.breakNumberTextView.setText(String.valueOf(statisticsItem.getBreaks()));
        holder.breakTimeTextView.setText(Utility.formatStatisticsTime(
                statisticsItem.getBreakTime()));

        holder.incompleteWorkNumberTextView.setText(String.valueOf(statisticsItem.getIncompleteWorks()));
        holder.incompleteWorkTimeTextView.setText(Utility.formatStatisticsTime(
                statisticsItem.getIncompleteWorkTime()));

        holder.totalWorkNumberTextView.setText(String.valueOf(statisticsItem.getCompletedWorks() + statisticsItem.getIncompleteWorks()));
        holder.totalWorkTimeTextView.setText(Utility.formatStatisticsTime(
                statisticsItem.getCompletedWorkTime() + statisticsItem.getIncompleteWorkTime()));
    }

    @Override
    public int getItemCount() {
        return statisticsItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        TextView completedWorkNumberTextView;
        TextView completedWorkTimeTextView;

        TextView breakNumberTextView;
        TextView breakTimeTextView;

        TextView incompleteWorkNumberTextView;
        TextView incompleteWorkTimeTextView;

        TextView totalWorkNumberTextView;
        TextView totalWorkTimeTextView;

        ViewHolder(View view) {
            super(view);

            dateTextView = view.findViewById(R.id.date);

            breakNumberTextView = view.findViewById(R.id.break_number);
            breakTimeTextView = view.findViewById(R.id.break_time);

            completedWorkNumberTextView = view.findViewById(R.id.work_number);
            completedWorkTimeTextView = view.findViewById(R.id.completed_work_time);

            incompleteWorkNumberTextView = view.findViewById(R.id.incomplete_work_number);
            incompleteWorkTimeTextView = view.findViewById(R.id.incomplete_work_time);

            totalWorkNumberTextView = view.findViewById(R.id.total_work_number);
            totalWorkTimeTextView = view.findViewById(R.id.total_work_time);
        }
    }
}
