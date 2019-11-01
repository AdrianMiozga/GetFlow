package com.wentura.pomodoro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        Context context = holder.dateTextView.getContext();

        holder.worksNumberTextView.setText(String.valueOf(statisticsItem.getCompletedWorks()));
        holder.breaksNumberTextView.setText(String.valueOf(statisticsItem.getBreaks()));
        holder.dateTextView.setText(statisticsItem.getDate());
        holder.completedWorkTimeTextView.setText(Utility.formatStatisticsTime(context, statisticsItem.getCompletedWorkTime()));
        holder.breakTimeTextView.setText(Utility.formatStatisticsTime(context, statisticsItem.getBreakTime()));
    }

    @Override
    public int getItemCount() {
        return statisticsItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView worksNumberTextView;
        TextView breaksNumberTextView;
        TextView completedWorkTimeTextView;
        TextView breakTimeTextView;

        ViewHolder(View view) {
            super(view);

            dateTextView = view.findViewById(R.id.date);
            breaksNumberTextView = view.findViewById(R.id.breaks_number);
            worksNumberTextView = view.findViewById(R.id.works_number);
            breakTimeTextView = view.findViewById(R.id.break_time);
            completedWorkTimeTextView = view.findViewById(R.id.completed_work_time);
        }
    }
}
