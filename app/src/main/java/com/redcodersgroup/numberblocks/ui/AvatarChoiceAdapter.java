package com.redcodersgroup.numberblocks.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redcodersgroup.numberblocks.databinding.ItemAvatarChoiceBinding;
import com.redcodersgroup.numberblocks.profile.AvatarManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import java.util.List;

public class AvatarChoiceAdapter extends RecyclerView.Adapter<AvatarChoiceAdapter.ViewHolder> {

    public interface OnAvatarSelectListener {
        void onAvatarSelected(AvatarManager.AvatarItem item);
    }

    private final List<AvatarManager.AvatarItem> items;
    private String selectedAvatarId;
    private final OnAvatarSelectListener listener;

    public AvatarChoiceAdapter(List<AvatarManager.AvatarItem> items, String initialSelectedId, OnAvatarSelectListener listener) {
        this.items = items;
        this.selectedAvatarId = initialSelectedId;
        this.listener = listener;
    }

    public void setSelectedAvatarId(String avatarId) {
        this.selectedAvatarId = avatarId;
        notifyDataSetChanged();
    }

    public String getSelectedAvatarId() {
        return selectedAvatarId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvatarChoiceBinding binding = ItemAvatarChoiceBinding.inflate(
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAvatarChoiceBinding binding;

        ViewHolder(ItemAvatarChoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AvatarManager.AvatarItem item) {
            Theme theme = ThemeManager.getInstance().getCurrentTheme();
            float density = binding.getRoot().getResources().getDisplayMetrics().density;

            binding.ivAvatarIcon.setImageResource(item.drawableResId);

            boolean isSelected = item.id.equalsIgnoreCase(selectedAvatarId);

            int cardBg = theme.isDark ? theme.btnSurfaceColor : Color.parseColor("#FAF9F6");
            int strokeColor = isSelected ? Color.parseColor("#F59E0B") : (theme.isDark ? theme.btnStrokeColor : Color.parseColor("#E3E1D8"));
            int strokeWidth = isSelected ? (int) (2.5f * density) : (int) (1f * density);

            binding.cardAvatarItem.setCardBackgroundColor(cardBg);
            binding.cardAvatarItem.setStrokeColor(strokeColor);
            binding.cardAvatarItem.setStrokeWidth(strokeWidth);
            binding.cardAvatarItem.setCardElevation(isSelected ? 2.5f * density : 0f);

            binding.layoutAvatarCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            binding.cardAvatarItem.setOnClickListener(v -> {
                selectedAvatarId = item.id;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onAvatarSelected(item);
                }
            });
        }
    }
}
