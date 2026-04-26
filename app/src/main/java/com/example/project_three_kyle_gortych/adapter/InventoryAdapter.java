package com.example.project_three_kyle_gortych.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_three_kyle_gortych.R;
import com.example.project_three_kyle_gortych.db.InventoryItem;

/**
 * RecyclerView adapter for the inventory grid. Uses {@link ListAdapter} +
 * {@link DiffUtil} so we can feed it new lists from Room's LiveData and it
 * will animate only the rows that actually changed.
 *
 * <h3>Spinner-in-RecyclerView gotcha</h3>
 * A Spinner fires {@code onItemSelected} both when the user picks a value
 * <i>and</i> when we programmatically restore the current value in
 * {@code onBindViewHolder}. To avoid writing an unchanged quantity back to
 * the database on every bind, each ViewHolder tracks a {@code userTouched}
 * flag that flips to {@code true} only after a real touch event on the
 * spinner.
 */
public class InventoryAdapter
        extends ListAdapter<InventoryItem, InventoryAdapter.ViewHolder> {

    /** Callback interface used by the hosting activity. */
    public interface Listener {
        /** Fires when the user picks a new quantity from the spinner. */
        void onQuantityChanged(InventoryItem item, int newQty);
        /** Fires when the user taps the row's delete button. */
        void onDelete(InventoryItem item);
    }

    private final Listener listener;
    private final int[] quantityOptions;

    public InventoryAdapter(@NonNull Listener listener,
                            @NonNull int[] quantityOptions) {
        super(DIFF);
        this.listener = listener;
        this.quantityOptions = quantityOptions;
    }

    /** Compares two items so the ListAdapter knows which rows to animate. */
    private static final DiffUtil.ItemCallback<InventoryItem> DIFF =
            new DiffUtil.ItemCallback<InventoryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull InventoryItem a,
                                               @NonNull InventoryItem b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull InventoryItem a,
                                                  @NonNull InventoryItem b) {
                    return a.quantity == b.quantity && a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final InventoryItem item = getItem(position);

        h.tvName.setText(item.name);

        // Reset the "touched" flag whenever we rebind so the programmatic
        // spinner restore below doesn't masquerade as a user edit.
        h.userTouched = false;

        int spinnerIdx = indexOfQuantity(item.quantity);
        if (spinnerIdx < 0) {
            // If an older row holds a quantity not in the preset list, show
            // the first option rather than crashing. A subsequent user pick
            // will realign it.
            spinnerIdx = 0;
        }
        h.spinner.setSelection(spinnerIdx, false);

        h.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (!h.userTouched) return;
                int newQty = quantityOptions[pos];
                if (newQty != item.quantity) {
                    listener.onQuantityChanged(item, newQty);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    /** Returns the index of {@code qty} in the preset array, or -1. */
    private int indexOfQuantity(int qty) {
        for (int i = 0; i < quantityOptions.length; i++) {
            if (quantityOptions[i] == qty) return i;
        }
        return -1;
    }

    /** View holder for one inventory row. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final Spinner spinner;
        final Button btnDelete;

        /** Flips to true only on a real touch event against the spinner. */
        boolean userTouched = false;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            spinner = itemView.findViewById(R.id.spinnerQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Returning false means the touch still reaches the spinner's
            // built-in click handler; we're only using this as a signal.
            spinner.setOnTouchListener((v, ev) -> {
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    userTouched = true;
                }
                return false;
            });
        }
    }
}