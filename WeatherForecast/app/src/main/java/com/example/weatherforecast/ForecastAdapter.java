package com.example.weatherforecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private List<ForecastModel.ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastModel.ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastModel.ForecastItem item = forecastList.get(position);
        String date = item.getDtTxt().split(" ")[0];
        holder.tvDate.setText(formatDate(date));
        holder.tvTemp.setText(String.format("%.0f°C", item.getMain().getTemp()));
        holder.tvCondition.setText(item.getWeather().get(0).getDescription());

        String iconUrl = "https://openweathermap.org/img/wn/" + item.getWeather().get(0).getIcon() + "@2x.png";
        Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.ivIcon);
        fadeInView(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp, tvCondition;
        ImageView ivIcon;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat to = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            return to.format(from.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void fadeInView(View view) {
        if (view != null) {
            AlphaAnimation anim = new AlphaAnimation(0f, 1f);
            anim.setDuration(700);
            view.startAnimation(anim);
        }
    }
} 