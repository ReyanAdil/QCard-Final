package com.qcard.ui.set;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCategory;
import com.qcard.data.model.QSet;
import com.qcard.listener.CategorySelectedListener;
import com.qcard.ui.card.CardAddActivity;

import java.util.Locale;

public class SetAddActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    private TextInputLayout tilSetName;
    private TextInputLayout tilCategory;
    private TextInputLayout tilDescription;
    private TextInputEditText etSetName;
    private TextInputEditText etCategory;
    private TextInputEditText etDescription;
    private ExtendedFloatingActionButton mFabAddCards;

    private QCategory mCategory;

    private QSet mSet;

    private boolean isEditMode() {
        return mSet != null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSet = (QSet) getIntent().getSerializableExtra("set");

        tilSetName = findViewById(R.id.tilSetName);
        tilCategory = findViewById(R.id.tilCategory);
        tilDescription = findViewById(R.id.tilDescription);

        etSetName = findViewById(R.id.etSetName);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);

        mFabAddCards = findViewById(R.id.fabAddCards);

        if (isEditMode()) {
            setTitle(R.string.edit_set);
            mFabAddCards.setText(getString(R.string.done));
            mFabAddCards.setIconResource(R.drawable.ic_baseline_done_24);

            fillData();
        } else {
            setTitle(R.string.add_set);
            mFabAddCards.setText(getString(R.string.add_cards));
            mFabAddCards.setIconResource(R.drawable.ic_baseline_add_24);
        }

        mFabAddCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddSet();
            }
        });

        etCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryDialog();
            }
        });
    }

    private void openCategoryDialog() {
        SetCategoryListDialog dialog = SetCategoryListDialog.newInstance(false);
        dialog.setOnCategorySelectedListener(new CategorySelectedListener() {
            @Override
            public void onCategorySelected(QCategory category) {
                mCategory = category;
                etCategory.setText(category.getName());
            }
        });
        dialog.show(getSupportFragmentManager(), "SetCategoryListDialog");
    }

    private void fillData() {
        etSetName.setText(mSet.getName());
        etDescription.setText(mSet.getDescription());

        if (mSet.getCategoryId() != null && !mSet.getCategoryId().isEmpty()) {
            fetchCategory(mSet.getCategoryId());
        }
    }

    private void fetchCategory(String categoryId) {
        mProgressDialog = ProgressDialog.show(this, "Loading data", "Please wait");
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CATEGORY)
                .document(categoryId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mCategory = documentSnapshot.toObject(QCategory.class);
                        etCategory.setText(mCategory.getName());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void onClickAddSet() {
        String name = etSetName.getText().toString();
        String desc = etDescription.getText().toString();

        if (name.isEmpty()) {
            tilSetName.setError(getString(R.string.required));
        } else {
            if (isEditMode()) {
                mSet.setName(name);
                mSet.setDescription(desc);
                if (mCategory != null) {
                    mSet.setCategoryId(mCategory.getId());
                }

                FirebaseFirestore.getInstance()
                        .collection(GlobalData.COL_SET)
                        .document(mSet.getId())
                        .update("name", mSet.getName(), "description", mSet.getDescription(), "categoryId", mSet.getCategoryId())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent result = new Intent();
                                result.putExtra("set", mSet);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                        });
            } else {
                QSet set = new QSet();
                set.setUserId(GlobalData.getCurrentUser(this).getUserId());
                set.setTime(System.currentTimeMillis());
                set.setName(name);
                set.setDescription(desc);
                if (mCategory != null) {
                    set.setCategoryId(mCategory.getId());
                }
                FirebaseFirestore.getInstance()
                        .collection(GlobalData.COL_SET)
                        .add(set)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                set.setId(documentReference.getId());
                                Intent intent = new Intent(SetAddActivity.this, CardAddActivity.class);
                                intent.putExtra("set", set);
                                startActivity(intent);
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
