package com.qcard.ui.set;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.qcard.BuildConfig;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.data.model.QSet;
import com.qcard.data.model.SharableQSet;
import com.qcard.data.model.SharableWrapper;
import com.qcard.listener.CardSelectedListener;
import com.qcard.listener.OnChangeListener;
import com.qcard.ui.card.CardAddActivity;
import com.qcard.ui.card.CardListAdapter;
import com.qcard.util.ExportUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SetDetailActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_CARD = 943;
    private static final int REQUEST_EDIT_SET = 541;
    private static final int REQUEST_EDIT_CARD = 751;

    private QSet mSet;
    private CardListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBarLoading;
    private ProgressBar mProgressBarStatus;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSet = (QSet) getIntent().getSerializableExtra("set");

        mProgressBarLoading = findViewById(R.id.progressBar);
        mProgressBarStatus = findViewById(R.id.progressBarStatus);
        mRecyclerView = findViewById(R.id.recyclerViewCards);

        mAdapter = new CardListAdapter();
        mAdapter.setCardSelectedListener(new CardSelectedListener() {
            @Override
            public void onCardSelected(QCard card) {

            }
        });
        mAdapter.setOnChangeListener(new OnChangeListener() {
            @Override
            public void onChanged() {
                loadProgressStatus();
                setResult(RESULT_OK);
            }
        });
        mAdapter.setOnCardEditSelectedListener(new CardSelectedListener() {
            @Override
            public void onCardSelected(QCard card) {
                Intent intent = new Intent(SetDetailActivity.this, CardAddActivity.class);
                intent.putExtra("card", card);
                startActivityForResult(intent, REQUEST_EDIT_CARD);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.fabAddCards).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetDetailActivity.this, CardAddActivity.class);
                intent.putExtra("set", mSet);
                startActivityForResult(intent, REQUEST_ADD_CARD);
            }
        });

        loadCards();
    }

    private void loadCards() {
        mAdapter.clear();
        setTitle(mSet.getName());
        mProgressBarLoading.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("userId", GlobalData.getCurrentUser(this).getUserId())
                .whereEqualTo("setId", mSet.getId())
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBarLoading.setVisibility(View.GONE);
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

    @Override
    public void onStart() {
        super.onStart();
        loadProgressStatus();
    }

    private void loadProgressStatus() {
        mProgressBarStatus.setIndeterminate(true);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("setId", mSet.getId())
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ADD_CARD || requestCode == REQUEST_EDIT_CARD) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                loadCards();

                loadProgressStatus();
            }
        } else if (requestCode == REQUEST_EDIT_SET) {
            if (resultCode == RESULT_OK) {
                mSet = (QSet) data.getSerializableExtra("set");

                Intent result = new Intent();
                result.putExtra("set", mSet);
                setResult(RESULT_OK, result);

                loadCards();

                loadProgressStatus();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.set_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareSet();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            deleteSet();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            editSet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editSet() {
        Intent intent = new Intent(this, SetAddActivity.class);
        intent.putExtra("set", mSet);
        startActivityForResult(intent, REQUEST_EDIT_SET);
    }

    private void deleteSet() {
        mProgressDialog = ProgressDialog.show(this, "Deleting Set", "Please wait");

        DocumentReference setDocument = FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_SET)
                .document(mSet.getId());
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        batch.delete(setDocument);

        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("setId", mSet.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot ds :
                                queryDocumentSnapshots) {
                            DocumentReference cardDocument = FirebaseFirestore.getInstance()
                                    .collection(GlobalData.COL_CARD)
                                    .document(ds.getId());
                            batch.delete(cardDocument);
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent result = new Intent();
                                        result.putExtra("setDeleted", true);
                                        setResult(RESULT_OK, result);
                                        finish();
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mProgressDialog.dismiss();
                                    }
                                });
                    }
                });
    }

    private void shareSet() {
        mProgressDialog = ProgressDialog.show(this, "Preparing File", "Please wait");
        SharableQSet sharableQSet = ExportUtil.createSharableQSet(mSet, mAdapter.getItems());

        SharableWrapper sharableWrapper = new SharableWrapper();
        sharableWrapper.setAuthorId(GlobalData.getCurrentUser(this).getUserId());
        sharableWrapper.setVersion(BuildConfig.VERSION_NAME);
        sharableWrapper.addSharableQSet(sharableQSet);

        String fileContent = new Gson().toJson(sharableWrapper);

        String fileName = mSet.getName() + "_" + System.currentTimeMillis() + ".qcard";

        File sharable = new File(getFilesDir(), fileName);

        FileWriter writer;
        try {
            writer = new FileWriter(sharable);
            writer.append(fileContent);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mProgressDialog.dismiss();

        Uri path = FileProvider.getUriForFile(this, getString(R.string.file_provider), new File(sharable.getPath()));
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, "Hey! I am sharing my QCard.");
        i.putExtra(Intent.EXTRA_STREAM, path);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setType("plain/*");
        startActivity(i);
    }
}
