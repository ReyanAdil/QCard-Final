package com.qcard.ui.card;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.listener.CardSelectedListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardHolder> {

    private final List<QCard> mItems;
    private CardSelectedListener mCardClickListener;
    private CardSelectedListener mCardEditSelectedListener;

    public CardListAdapter(@NonNull List<QCard> items) {
        mItems = items;
    }

    public CardListAdapter() {
        mItems = new ArrayList<>();
    }

    public void add(QCard item) {
        int lastPosition = mItems.size();
        mItems.add(item);
        notifyItemInserted(lastPosition);
    }

    public void addAll(List<QCard> items) {
        int lastPosition = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(lastPosition, items.size());
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        QCard item = getItem(position);
        if (item != null) {
            holder.bind(item);
        }
    }

    @Nullable
    private QCard getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<QCard> getItems() {
        return mItems;
    }

    public void clear() {
        int size = mItems.size();
        mItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void remove(QCard item) {
        int index = mItems.indexOf(item);
        if (index != -1) {
            mItems.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void setCardSelectedListener(CardSelectedListener listener) {
        mCardClickListener = listener;
    }

    public void setOnCardEditSelectedListener(CardSelectedListener listener) {
        mCardEditSelectedListener = listener;
    }

    class CardHolder extends RecyclerView.ViewHolder {

        private final TextView mTextViewName;
        private final TextView mTextViewDefinition;
        private final ImageView mImageViewTerm;
        private final ImageButton mButtonRemembered;
        private final ImageButton mButtonMoreOptions;

        public CardHolder(@NonNull View itemView) {
            super(itemView);

            mTextViewName = itemView.findViewById(R.id.tvName);
            mTextViewDefinition = itemView.findViewById(R.id.tvDefinition);
            mImageViewTerm = itemView.findViewById(R.id.ivTerm);
            mButtonRemembered = itemView.findViewById(R.id.btnRemembered);
            mButtonMoreOptions = itemView.findViewById(R.id.ivMoreOptions);

            itemView.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCardClickListener != null) {
                        mCardClickListener.onCardSelected(getItem(getAdapterPosition()));
                    }
                }
            });
        }


        public void bind(@NonNull QCard item) {
            mTextViewName.setText(item.getTerm());
            mTextViewDefinition.setText(item.getDefinition());

            if (item.isRemembered()) {
                mButtonRemembered.setImageResource(R.drawable.ic_baseline_thumb_up_24);
            } else {
                mButtonRemembered.setImageResource(R.drawable.ic_outline_thumb_up_24);
            }

            mButtonRemembered.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore.getInstance()
                            .collection(GlobalData.COL_CARD)
                            .document(item.getId())
                            .update("remembered", !item.isRemembered())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    item.setRemembered(!item.isRemembered());
                                    notifyItemChanged(getAdapterPosition());
                                }
                            });
                }
            });

            mButtonMoreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showOptions(view, item);
                }
            });

            if (item.getTermImage() != null && !item.getTermImage().isEmpty()) {
                FirebaseStorage.getInstance().getReference(item.getTermImage())
                        .getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d("TAG", "onComplete: " + uri);
                                Picasso.get().load(uri)
                                        .fit()
                                        .into(mImageViewTerm);
                            }
                        });
            }
        }

        private void showOptions(View view, QCard item) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenu().add(R.string.edit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (mCardEditSelectedListener != null) {
                        mCardEditSelectedListener.onCardSelected(item);
                    }
                    return true;
                }
            });
            popupMenu.getMenu().add(R.string.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FirebaseFirestore.getInstance().collection(GlobalData.COL_CARD).document(item.getId()).delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        remove(item);
                                    } else {
                                        Toast.makeText(view.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    return true;
                }
            });
            popupMenu.show();
        }
    }
}