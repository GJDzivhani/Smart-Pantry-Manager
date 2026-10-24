package com.richfield.smartpantry.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.ImageViewCompat;

/**
 * Paints the round, coloured ingredient icon: the category glyph in its colour, on a chip of the
 * same colour at about 17% strength. The ImageView needs the white oval background
 * (@drawable/bg_avatar), which is what gets tinted.
 */
public final class IngredientIcons {

    private static final int CHIP_ALPHA = 0x2B;

    private IngredientIcons() { }

    public static void bind(ImageView chip, String ingredientName) {
        Context ctx = chip.getContext();
        IngredientCategory category = IngredientCategory.of(ingredientName);
        int colour = ContextCompat.getColor(ctx, category.color);

        chip.setImageResource(category.icon);
        ImageViewCompat.setImageTintList(chip, ColorStateList.valueOf(colour));
        chip.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(colour, CHIP_ALPHA)));
    }
}
