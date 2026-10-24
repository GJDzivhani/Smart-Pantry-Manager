package com.richfield.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.IngredientIcons;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter that binds pantry rows (from SQLite) to item_pantry.xml cards. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean alertsEnabled = true;
    private int alertDays = 3;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setAlertSettings(boolean enabled, int days) {
        this.alertsEnabled = enabled;
        this.alertDays = days;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        Context ctx = h.itemView.getContext();

        String displayName = IngredientMatcher.capitalise(item.getName());
        h.tvName.setText(displayName);
        IngredientIcons.bind(h.ivIcon, item.getName());
        h.tvQuantity.setText(IngredientMatcher.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        Long days = item.getDaysUntilExpiry();
        if (days == null) {
            h.tvExpiry.setText("No expiry date");
            h.tvStatus.setVisibility(View.GONE);
        } else {
            h.tvExpiry.setText("Expires " + item.getExpiryDisplay());
            if (days < 0) {
                show(h.tvStatus, "Expired", ContextCompat.getColor(ctx, R.color.colorDanger),
                        R.drawable.bg_pill_danger);
            } else if (alertsEnabled && days <= alertDays) {
                String msg = days == 0 ? "Expires today"
                        : "Expires in " + days + (days == 1 ? " day" : " days");
                show(h.tvStatus, msg, ContextCompat.getColor(ctx, R.color.colorWarning),
                        R.drawable.bg_pill_warning);
            } else {
                h.tvStatus.setVisibility(View.GONE);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    private void show(TextView tv, String text, int color, int background) {
        tv.setText(text);
        tv.setTextColor(color);
        tv.setBackgroundResource(background);
        tv.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName, tvQuantity, tvExpiry, tvStatus;
        final ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
