package com.qcard.ui.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCategory;
import com.qcard.data.model.QSet;
import com.qcard.listener.CategorySelectedListener;
import com.qcard.listener.SetSelectedListener;

public class SetsFragment extends Fragment {

    private static final int REQUEST_ADD_SET = 960;
    private static final int REQUEST_SET_DETAIL = 61;

    private SetListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBarLoading;
    private ProgressBar mProgressBarStatus;

    private TextInputEditText mEditTextFilter;
    private QCategory mFilterCategory = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sets, container, false);
        root.findViewById(R.id.fabAddSet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddSet();
            }
        });

        mProgressBarStatus = root.findViewById(R.id.progressBarStatus);
        mProgressBarLoading = root.findViewById(R.id.progressBar);
        mRecyclerView = root.findViewById(R.id.recyclerViewSets);
        mEditTextFilter = root.findViewById(R.id.etCategoryFilter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SetListAdapter();
        mAdapter.setSetSelectedListener(new SetSelectedListener() {
            @Override
            public void onSetSelected(QSet set) {
                Intent intent = new Intent(getActivity(), SetDetailActivity.class);
                intent.putExtra("set", set);
                startActivityForResult(intent, REQUEST_SET_DETAIL);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        mEditTextFilter.setText("All");
        mEditTextFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryFilterDialog();
            }
        });

        loadSets();
    }

    private void loadSets() {
        mAdapter.clear();
        mProgressBarLoading.setVisibility(View.VISIBLE);
        Query query = FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_SET);
        query = query.whereEqualTo("userId", GlobalData.getCurrentUser(requireContext()).getUserId());
        if (mFilterCategory != null) {
            query = query.whereEqualTo("categoryId", mFilterCategory.getId());
        }
        query.orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBarLoading.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                QSet set = document.toObject(QSet.class);
                                set.setId(document.getId());

                                mAdapter.add(set);
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProgressStatus();
    }

    private void loadProgressStatus() {
        mProgressBarStatus.setIndeterminate(true);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("userId", GlobalData.getCurrentUser(requireContext()).getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBarStatus.setIndeterminate(false);
                        if (task.isSuccessful()) {
                            mProgressBarStatus.setProgress(0);
                            mProgressBarStatus.setMax(task.getResult().getDocuments().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getBoolean("remembered")) {
                                    mProgressBarStatus.setProgress(mProgressBarStatus.getProgress() + 1);
                                }
                            }
                        } else {
                            Log.w("TAG", "Error getting status.", task.getException());
                        }
                    }
                });
    }

    private void onClickAddSet() {
        Intent intent = new Intent(getActivity(), SetAddActivity.class);
        startActivityForResult(intent, REQUEST_ADD_SET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ADD_SET) {
            if (resultCode == Activity.RESULT_OK) {
                loadSets();
                loadProgressStatus();
            }
        } else if (requestCode == REQUEST_SET_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                loadSets();
                loadProgressStatus();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openCategoryFilterDialog() {
        SetCategoryListDialog dialog = SetCategoryListDialog.newInstance(true);
        dialog.setOnCategorySelectedListener(new CategorySelectedListener() {
            @Override
            public void onCategorySelected(QCategory category) {
                mFilterCategory = category;
                if (mFilterCategory == null) {
                    mEditTextFilter.setText("All");
                } else {
                    mEditTextFilter.setText(category.getName());
                }
                loadSets();
            }
        });
        dialog.show(getChildFragmentManager(), "SetCategoryListDialog");
    }
}