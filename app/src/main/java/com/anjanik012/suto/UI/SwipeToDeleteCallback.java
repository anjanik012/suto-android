package com.anjanik012.suto.UI;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.anjanik012.suto.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private HostListAdapter adapter;
    private Drawable icon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(HostListAdapter adapter) {
        super(0, ItemTouchHelper.LEFT /*| ItemTouchHelper.RIGHT*/);
        this.adapter = adapter;
        icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.baseline_delete_white_18dp);
        background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.item_delete_red));
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        adapter.deleteItem(position);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
//        if (dX > 0) { // Swiping to the right
//            int iconLeft = itemView.getLeft() + iconMargin;
//            int iconRight = itemView.getLeft() + iconMargin+ icon.getIntrinsicWidth();
//            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
//            background.setBounds(itemView.getLeft(), itemView.getTop(),
//                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
//                    itemView.getBottom());
//
//        } else
        if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }
}
