package com.qcard.ui.card;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.listener.CardSelectedListener;
import com.qcard.ui.set.SetDetailActivity;

public class CardsFragment extends Fragment {

    private static final int REQUEST_EDIT_CARD = 348;

    private CardListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private ProgressDialog mProgressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cards, container, false);
        mProgressBar = root.findViewById(R.id.progressBar);
        mRecyclerView = root.findViewById(R.id.recyclerViewCards);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new CardListAdapter();
        mAdapter.setCardSelectedListener(new CardSelectedListener() {
            @Override
            public void onCardSelected(QCard card) {

            }
        });
        mAdapter.setOnCardEditSelectedListener(new CardSelectedListener() {
            @Override
            public void onCardSelected(QCard card) {
                Intent intent = new Intent(getActivity(), CardAddActivity.class);
                intent.putExtra("card", card);
                startActivityForResult(intent, REQUEST_EDIT_CARD);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(mAdapter);

        loadCards();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_EDIT_CARD) {
            if (resultCode == Activity.RESULT_OK) {
                // QCard card = (QCard) data.getSerializableExtra("card");
                mAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadCards() {
        mAdapter.clear();
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("userId", GlobalData.getCurrentUser(requireContext()).getUserId())
                .orderBy("term", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                QCard card = document.toObject(QCard.class);
                                card.setId(document.getId());

                                mAdapter.add(card);
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}