package com.redcodersgroup.numberblocks.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redcodersgroup.numberblocks.databinding.ItemLeaderboardRankBinding;
import com.redcodersgroup.numberblocks.games.LeaderboardEntry;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void setItems(List<LeaderboardEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardRankBinding binding = ItemLeaderboardRankBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardRankBinding binding;

        ViewHolder(ItemLeaderboardRankBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardEntry entry) {
            Theme theme = ThemeManager.getInstance().getCurrentTheme();

            binding.tvItemPlayerName.setText(entry.getDisplayName());
            binding.tvItemScore.setText(entry.getFormattedScore());
            binding.tvItemRank.setText(entry.getRank());

            // Rank coloring: 1 = Gold, 2 = Silver, 3 = Bronze, 4+ = Default
            int rankColor;
            String rank = entry.getRank();
            if ("1".equals(rank)) {
                rankColor = Color.parseColor("#F59E0B"); // Gold
            } else if ("2".equals(rank)) {
                rankColor = Color.parseColor("#9E9E9E"); // Silver
            } else if ("3".equals(rank)) {
                rankColor = Color.parseColor("#D97706"); // Bronze
            } else {
                rankColor = theme.isDark ? Color.parseColor("#383C48") : Color.parseColor("#E0DDD5");
            }
            binding.layoutRankBadge.setBackgroundTintList(ColorStateList.valueOf(rankColor));

            // Text colors & Theme
            int cardBg = theme.isDark ? theme.btnSurfaceColor : Color.parseColor("#FAF9F6");
            int strokeColor = theme.isDark ? theme.btnStrokeColor : Color.parseColor("#E3E1D8");
            binding.cardLeaderboardRow.setCardBackgroundColor(cardBg);
            binding.cardLeaderboardRow.setStrokeColor(entry.isCurrentPlayer() ? Color.parseColor("#F59E0B") : strokeColor);
            binding.cardLeaderboardRow.setStrokeWidth((int) ((entry.isCurrentPlayer() ? 1.8f : 1f) * binding.getRoot().getResources().getDisplayMetrics().density));

            binding.tvItemPlayerName.setTextColor(theme.textPrimaryColor);
            binding.tvItemScore.setTextColor(theme.textPrimaryColor);

            if (entry.isCurrentPlayer()) {
                binding.tvItemYouBadge.setVisibility(View.VISIBLE);
                binding.tvItemYouBadge.setTextColor(Color.parseColor("#F59E0B"));
            } else {
                binding.tvItemYouBadge.setVisibility(View.GONE);
            }
        }
    }
}
