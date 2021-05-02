package com.qcard.ui.set;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCategory;
import com.qcard.data.model.QSet;
import com.qcard.listener.SetSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetListAdapter.SetHolder> {

    private final List<QSet> mItems;
    private SetSelectedListener mSetSelectedListener;

    public SetListAdapter(@NonNull List<QSet> items) {
        mItems = items;
    }

    public SetListAdapter() {
        mItems = new ArrayList<>();
    }

    public void add(QSet item) {
        int lastPosition = mItems.size();
        mItems.add(item);
        notifyItemInserted(lastPosition);
    }

    public void addAll(List<QSet> items) {
        int lastPosition = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(lastPosition, items.size());
    }

    @NonNull
    @Override
    public SetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SetHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_set, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SetHolder holder, int position) {
        QSet item = getItem(position);
        if (item != null) {
            holder.bind(item);
        }
    }

    @Nullable
    private QSet getItem(int position) {
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

    public void setSetSelectedListener(SetSelectedListener listener) {
        mSetSelectedListener = listener;
    }

    class SetHolder extends RecyclerView.ViewHolder {

        private final TextView mTextViewName;
        private final TextView mTextViewDescription;
        private final View mViewColor;

        public SetHolder(@NonNull View itemView) {
            super(itemView);

            mTextViewName = itemView.findViewById(R.id.tvName);
            mTextViewDescription = itemView.findViewById(R.id.tvDescription);
            mViewColor = itemView.findViewById(R.id.viewCategoryColor);

            itemView.findViewById(R.id.btnPractice).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PracticeActivity.class);
                    intent.putExtra("set", getItem(getAdapterPosition()));
                    v.getContext().startActivity(intent);
                }
            });

            itemView.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSetSelectedListener.onSetSelected(getItem(getAdapterPosition()));
                }
            });
        }

        public void bind(@NonNull QSet item) {
            mTextViewName.setText(item.getName());
            if (item.getDescription().isEmpty()) {
                mTextViewDescription.setVisibility(View.GONE);
            } else {
                mTextViewDescription.setVisibility(View.VISIBLE);
            }
            mTextViewDescription.setText(item.getDescription());

            if (item.getCategoryId() != null && !item.getCategoryId().isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection(GlobalData.COL_CATEGORY)
                        .document(item.getCategoryId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                QCategory cat = documentSnapshot.toObject(QCategory.class);
                                mViewColor.setBackgroundColor(cat.getColor());
                            }
                        });
            } else {
                mViewColor.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}