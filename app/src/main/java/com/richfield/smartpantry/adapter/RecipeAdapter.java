package com.richfield.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter for the Suggested Recipes screen (both the strict list and "Almost there"). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<MatchResult> results = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setResults(List<MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final MatchResult m = results.get(position);
        final Recipe r = m.getRecipe();
        Context ctx = h.itemView.getContext();

        h.tvName.setText(r.getName());
        h.tvMeta.setText(r.getPrepMinutes() + " min  \u2022  " + r.getIngredients().size() + " ingredients");

        int missing = m.missingCount();
        if (m.isFullMatch()) {
            h.tvStatus.setText("Ready to cook");
            h.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.colorSuccess));
            h.tvStatus.setBackgroundResource(R.drawable.bg_pill_success);
        } else if (missing <= 2) {
            h.tvStatus.setText("Missing: " + m.missingSummary());
            h.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.colorWarning));
            h.tvStatus.setBackgroundResource(R.drawable.bg_pill_warning);
        } else {
            h.tvStatus.setText("Missing " + missing + " ingredients");
            h.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.textSecondary));
            h.tvStatus.setBackgroundResource(R.drawable.bg_pill_neutral);
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClicked(r));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvMeta, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvMeta = itemView.findViewById(R.id.tvRecipeMeta);
            tvStatus = itemView.findViewById(R.id.tvRecipeStatus);
        }
    }
}
