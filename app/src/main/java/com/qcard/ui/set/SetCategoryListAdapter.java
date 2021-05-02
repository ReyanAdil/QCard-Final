package com.qcard.ui.set;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.qcard.R;
import com.qcard.data.model.QCategory;
import com.qcard.listener.CategorySelectedListener;

import java.util.ArrayList;
import java.util.List;

public class SetCategoryListAdapter extends RecyclerView.Adapter<SetCategoryListAdapter.SetCategoryHolder> {

    private final List<QCategory> mItems;
    private CategorySelectedListener mCategoryItemClickListener;

    public SetCategoryListAdapter(@NonNull List<QCategory> items) {
        mItems = items;
    }

    public SetCategoryListAdapter() {
        mItems = new ArrayList<>();
    }

    public void add(QCategory item) {
        int lastPosition = mItems.size();
        mItems.add(item);
        notifyItemInserted(lastPosition);
    }

    public void addAll(List<QCategory> items) {
        int lastPosition = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(lastPosition, items.size());
    }

    @NonNull
    @Override
    public SetCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SetCategoryHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_category, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SetCategoryHolder holder, int position) {
        QCategory item = getItem(position);
        holder.bind(item);
    }

    @Nullable
    private QCategory getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void clear() {
        int size = mItems.size();
        mItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void setOnItemClickListener(CategorySelectedListener listener) {
        mCategoryItemClickListener = listener;
    }

    class SetCategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextViewName;
        private final View mViewColor;

        public SetCategoryHolder(@NonNull View itemView) {
            super(itemView);

            mTextViewName = itemView.findViewById(R.id.tvName);
            mViewColor = itemView.findViewById(R.id.viewCategoryColor);

            itemView.getRootView().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCategoryItemClickListener.onCategorySelected(getItem(getAdapterPosition()));
        }

        public void bind(@Nullable QCategory item) {
            if (item != null) {
                mTextViewName.setText(item.getName());
                mViewColor.setBackgroundColor(item.getColor());
            } else {
                mTextViewName.setText("All");
                mViewColor.setBackgroundColor(Color.TRANSPARENT);
            }

        }
    }
}