package com.qcard.ui.card;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kroegerama.imgpicker.BottomSheetImagePicker;
import com.kroegerama.imgpicker.ButtonType;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.data.model.QCategory;
import com.qcard.data.model.QSet;
import com.qcard.listener.CardSelectedListener;
import com.qcard.listener.CategorySelectedListener;
import com.qcard.ui.set.SetCategoryListDialog;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class CardAddActivity extends AppCompatActivity implements BottomSheetImagePicker.OnImagesSelectedListener {

    private ProgressDialog mProgressDialog;

    private QSet mSet;
    private QCard mQCard;

    private TextInputLayout tilTerm;
    private TextInputEditText etTerm;
    private ImageButton imageButtonTerm;

    private TextInputLayout tilDefinition;
    private TextInputEditText etDefinition;
    private ImageButton imageButtonDefinition;

    private String termFileId;
    private String definitionFileId;

    private boolean isEditMode() {
        return mQCard != null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSet = (QSet) getIntent().getSerializableExtra("set");
        mQCard = (QCard) getIntent().getSerializableExtra("card");

        tilTerm = findViewById(R.id.tilTerm);
        etTerm = findViewById(R.id.etTerm);
        imageButtonTerm = findViewById(R.id.ivTermImage);

        tilDefinition = findViewById(R.id.tilDefinition);
        etDefinition = findViewById(R.id.etDefinition);
        imageButtonDefinition = findViewById(R.id.ivDefinitionImage);

        if (isEditMode()) {
            setTitle(R.string.edit_card);
            findViewById(R.id.fabAddNext).setVisibility(View.GONE);

            fillData();
        } else {
            setTitle(R.string.add_cards);
        }

        imageButtonTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser("term");
            }
        });

        imageButtonDefinition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser("definition");
            }
        });

        findViewById(R.id.fabAddNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddNext();
            }
        });

        findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDone();
            }
        });
    }

    private void fillData() {
        etTerm.setText(mQCard.getTerm());
        etDefinition.setText(mQCard.getDefinition());

        termFileId = mQCard.getTermImage();
        definitionFileId = mQCard.getDefinitionImage();

        if (termFileId != null && !termFileId.isEmpty()) {
            FirebaseStorage.getInstance().getReference(termFileId)
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri)
                                    .resizeDimen(R.dimen.dp54, R.dimen.dp54)
                                    .centerInside()
                                    .into(imageButtonTerm);
                        }
                    });
        }

        if (definitionFileId != null && !definitionFileId.isEmpty()) {
            FirebaseStorage.getInstance().getReference(definitionFileId)
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri)
                                    .resizeDimen(R.dimen.dp54, R.dimen.dp54)
                                    .centerInside()
                                    .into(imageButtonDefinition);
                        }
                    });
        }
    }

    private void openImageChooser(String tag) {
        new BottomSheetImagePicker.Builder(getString(R.string.file_provider))
                .cameraButton(ButtonType.Button)
                .galleryButton(ButtonType.Button)
                .singleSelectTitle(R.string.pick_image)
                .peekHeight(R.dimen.peekHeight)
                .requestTag(tag)
                .build()
                .show(getSupportFragmentManager(), null);
    }

    @Override
    public void onImagesSelected(@NonNull List<? extends Uri> uris, @Nullable String tag) {
        Uri uri = uris.get(0);
        uploadFile(uri, tag);
    }

    private void uploadFile(Uri uri, String type) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference fileRef = firebaseStorage.getReference().child("cards/" + fileName);

        UploadTask uploadTask = fileRef.putFile(uri);
        mProgressDialog = ProgressDialog.show(this, "Uploading", "Please wait");
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (mProgressDialog != null) mProgressDialog.dismiss();

                if (type.equals("term")) {
                    termFileId = fileRef.getPath();
                    Picasso.get().load(uri)
                            .resizeDimen(R.dimen.dp54, R.dimen.dp54)
                            .centerInside()
                            .into(imageButtonTerm);
                } else if (type.equals("definition")) {
                    definitionFileId = fileRef.getPath();
                    Picasso.get().load(uri)
                            .resizeDimen(R.dimen.dp54, R.dimen.dp54)
                            .centerInside()
                            .into(imageButtonDefinition);
                }
            }
        });
    }

    private void onClickDone() {
        String term = etTerm.getText().toString();
        String definition = etDefinition.getText().toString();
        if (term.isEmpty() && definition.isEmpty()) {
            setResult(RESULT_OK);
            finish();
        } else {
            saveCard(new CardSelectedListener() {
                @Override
                public void onCardSelected(QCard card) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }
    }

    private void onClickAddNext() {
        saveCard(new CardSelectedListener() {
            @Override
            public void onCardSelected(QCard card) {
                reset();
            }
        });
    }

    private void reset() {
        tilTerm.setError(null);
        tilDefinition.setError(null);
        etDefinition.setText("");
        etTerm.setText("");
        termFileId = null;
        definitionFileId = null;
    }

    private void saveCard(CardSelectedListener listener) {
        String term = etTerm.getText().toString();
        String definition = etDefinition.getText().toString();

        if (term.isEmpty()) {
            tilTerm.setError(getString(R.string.required));
        } else if (definition.isEmpty()) {
            tilDefinition.setError(getString(R.string.required));
        } else {
            if (isEditMode()) {
                mQCard.setTerm(term);
                mQCard.setDefinition(definition);
                mQCard.setTermImage(termFileId);
                mQCard.setDefinitionImage(definitionFileId);


                FirebaseFirestore.getInstance()
                        .collection(GlobalData.COL_CARD)
                        .document(mQCard.getId())
                        .update("term", mQCard.getTerm(),
                                "definition", mQCard.getDefinition(),
                                "termImage", mQCard.getTermImage(),
                                "definitionImage", mQCard.getDefinitionImage()
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent result = new Intent();
                                result.putExtra("card", mQCard);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                QCard card = new QCard();
                card.setUserId(GlobalData.getCurrentUser(this).getUserId());
                card.setTerm(term);
                card.setTime(System.currentTimeMillis());
                card.setSetId(mSet.getId());
                card.setDefinition(definition);
                card.setTermImage(termFileId);
                card.setDefinitionImage(definitionFileId);

                FirebaseFirestore.getInstance()
                        .collection(GlobalData.COL_CARD)
                        .add(card)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                card.setId(documentReference.getId());
                                listener.onCardSelected(card);
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
