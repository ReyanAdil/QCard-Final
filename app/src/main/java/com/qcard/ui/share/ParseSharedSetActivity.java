package com.qcard.ui.share;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.data.model.QSet;
import com.qcard.data.model.SharableQCard;
import com.qcard.data.model.SharableQSet;
import com.qcard.data.model.SharableWrapper;
import com.qcard.ui.card.CardAddActivity;
import com.qcard.ui.login.LoginActivity;
import com.qcard.ui.set.SetAddActivity;
import com.qcard.util.AlertUtil;
import com.qcard.util.ExportUtil;
import com.qcard.util.RealPathUtil;

import java.util.List;

public class ParseSharedSetActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE_PERMISSION = 285;
    private static final int REQUEST_LOGIN = 598;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_set_parser);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.progressBar);

        checkLogin();
    }

    private void checkLogin() {
        if (GlobalData.getCurrentUser(this) == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("requestedLogin", true);
            startActivityForResult(intent, REQUEST_LOGIN);
        } else {
            checkImportOption();
        }
    }

    private void checkImportOption() {
        if (isPermissionGranted()) {
            startImport();
        } else {
            requestPermission();
        }
    }

    private void startImport() {
        mProgressBar.setVisibility(View.VISIBLE);

        Intent data = getIntent();
        Uri fileUri = data.getData();
        String realPath = RealPathUtil.getRealPath(this, fileUri);

        try {
            String content = ExportUtil.getStringFromFile(realPath);
            SharableWrapper sharableWrapper = new Gson().fromJson(content, SharableWrapper.class);

            addQSetToFirebase(sharableWrapper.getSharableQSets().get(0));

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.alert(ParseSharedSetActivity.this, "QCard import failed", "Please check if it is a valid file", true);
        }
    }

    private void addQSetToFirebase(SharableQSet sharableQSet) {
        QSet set = new QSet();
        set.setUserId(GlobalData.getCurrentUser(this).getUserId());
        set.setTime(System.currentTimeMillis());
        set.setName(sharableQSet.getName());
        set.setDescription(sharableQSet.getDescription());

        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_SET)
                .add(set)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        set.setId(documentReference.getId());
                        addQCardsToFirebase(documentReference.getId(), sharableQSet.getCards());
                    }
                });
    }

    private void addQCardsToFirebase(String id, List<SharableQCard> cards) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (SharableQCard qCard : cards) {
            QCard card = new QCard();
            card.setUserId(GlobalData.getCurrentUser(this).getUserId());
            card.setTerm(qCard.getTerm());
            card.setSetId(id);
            card.setDefinition(qCard.getDefinition());
            card.setTermImage(qCard.getTermImage());
            card.setDefinitionImage(qCard.getDefinitionImage());
            card.setTime(System.currentTimeMillis());

            DocumentReference add = FirebaseFirestore.getInstance()
                    .collection(GlobalData.COL_CARD).document();
            batch.set(add, card);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertUtil.alert(ParseSharedSetActivity.this, "QCard set added", "QCard set added in your account", true);
                } else {
                    AlertUtil.alert(ParseSharedSetActivity.this, "QCard import failed", "Please check if it is a valid file", true);
                }
            }
        });
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_STORAGE_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_STORAGE_PERMISSION) {
            if (isPermissionGranted()) {
                startImport();
            } else {
                // Permission denied
                Toast.makeText(this, "Please allow storage access", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                checkImportOption();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
