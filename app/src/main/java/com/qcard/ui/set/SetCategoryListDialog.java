package com.qcard.ui.set;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qcard.listener.CategorySelectedListener;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCategory;

public class SetCategoryListDialog extends DialogFragment {

    private CategorySelectedListener mOnCategorySelectedListener;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private SetCategoryListAdapter mAdapter;

    private boolean filterMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
        filterMode = getArguments().getBoolean("filterMode");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_set_category_list, container, false);
        mRecyclerView = v.findViewById(R.id.recyclerViewCategory);
        mProgressBar = v.findViewById(R.id.progressBar);
        if (filterMode) {
            v.findViewById(R.id.fabAddCategory).setVisibility(View.GONE);
        }
        v.findViewById(R.id.fabAddCategory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddCategory();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SetCategoryListAdapter();
        mAdapter.setOnItemClickListener(new CategorySelectedListener() {
            @Override
            public void onCategorySelected(QCategory category) {
                mOnCategorySelectedListener.onCategorySelected(category);
                dismiss();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        loadCategories();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void loadCategories() {
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CATEGORY)
                .whereEqualTo("userId", GlobalData.getCurrentUser(requireContext()).getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            mAdapter.add(null);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                QCategory cat = document.toObject(QCategory.class);
                                cat.setId(document.getId());

                                mAdapter.add(cat);
                            }
                        } else {
                            Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void onClickAddCategory() {
        SetCategoryAddDialog dialog = SetCategoryAddDialog.newInstance();
        dialog.setOnCategoryAddedListener(new CategorySelectedListener() {
            @Override
            public void onCategorySelected(QCategory category) {
                mAdapter.add(category);
            }
        });
        dialog.show(getChildFragmentManager(), "SetCategoryAddDialog");
    }

    void setOnCategorySelectedListener(CategorySelectedListener listener) {
        mOnCategorySelectedListener = listener;
    }

    public static SetCategoryListDialog newInstance(boolean filterMode) {
        Bundle args = new Bundle();
        args.putBoolean("filterMode", filterMode);
        SetCategoryListDialog dialog = new SetCategoryListDialog();
        dialog.setArguments(args);
        return dialog;
    }
}
