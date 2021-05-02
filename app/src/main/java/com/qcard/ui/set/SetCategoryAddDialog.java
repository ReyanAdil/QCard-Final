package com.qcard.ui.set;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.FirestoreClient;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCategory;
import com.qcard.listener.CategorySelectedListener;

import java.util.Objects;

public class SetCategoryAddDialog extends DialogFragment {

    private CategorySelectedListener mCategorySelectedListener;

    private TextInputLayout tilCategoryName;
    private TextInputEditText etCategoryName;
    private FloatingActionButton fabRed, fabYellow, fabOrange, fabGreen, fabBlue;

    private int color = Color.TRANSPARENT;

    private View.OnClickListener colorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            fabRed.setImageResource(0);
            fabYellow.setImageResource(0);
            fabOrange.setImageResource(0);
            fabGreen.setImageResource(0);
            fabBlue.setImageResource(0);

            if (v == fabRed) {
                color = ContextCompat.getColor(requireContext(), R.color.red);
                fabRed.setImageResource(R.drawable.ic_baseline_done_24);
            } else if (v == fabYellow) {
                color = ContextCompat.getColor(requireContext(), R.color.yellow);
                fabYellow.setImageResource(R.drawable.ic_baseline_done_24);
            } else if (v == fabOrange) {
                color = ContextCompat.getColor(requireContext(), R.color.orange);
                fabOrange.setImageResource(R.drawable.ic_baseline_done_24);
            } else if (v == fabGreen) {
                color = ContextCompat.getColor(requireContext(), R.color.green);
                fabGreen.setImageResource(R.drawable.ic_baseline_done_24);
            } else if (v == fabBlue) {
                color = ContextCompat.getColor(requireContext(), R.color.blue);
                fabBlue.setImageResource(R.drawable.ic_baseline_done_24);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_set_category_add, container, false);
        tilCategoryName = v.findViewById(R.id.tilCategory);
        etCategoryName = v.findViewById(R.id.etCategory);
        fabRed = v.findViewById(R.id.fbRed);
        fabBlue = v.findViewById(R.id.fbBlue);
        fabOrange = v.findViewById(R.id.fbOrange);
        fabYellow = v.findViewById(R.id.fbYellow);
        fabGreen = v.findViewById(R.id.fbGreen);

        fabRed.setOnClickListener(colorClickListener);
        fabBlue.setOnClickListener(colorClickListener);
        fabOrange.setOnClickListener(colorClickListener);
        fabYellow.setOnClickListener(colorClickListener);
        fabGreen.setOnClickListener(colorClickListener);

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

    }

    private void onClickAddCategory() {
        String name = etCategoryName.getText().toString();
        if (name.isEmpty()) {
            tilCategoryName.setError(getString(R.string.required));
        } else {
            QCategory cat = new QCategory();
            cat.setUserId(GlobalData.getCurrentUser(requireContext()).getUserId());
            cat.setName(name);
            cat.setColor(color);
            FirebaseFirestore.getInstance()
                    .collection(GlobalData.COL_CATEGORY)
                    .add(cat)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            cat.setId(documentReference.getId());
                            mCategorySelectedListener.onCategorySelected(cat);
                            dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    void setOnCategoryAddedListener(CategorySelectedListener listener) {
        mCategorySelectedListener = listener;
    }

    public static SetCategoryAddDialog newInstance() {
        Bundle args = new Bundle();
        SetCategoryAddDialog dialog = new SetCategoryAddDialog();
        dialog.setArguments(args);
        return dialog;
    }
}
