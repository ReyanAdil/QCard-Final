package com.qcard.ui.set;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.qcard.R;
import com.qcard.data.GlobalData;
import com.qcard.data.model.QCard;
import com.qcard.data.model.QSet;
import com.qcard.listener.CardSelectedListener;
import com.qcard.listener.ShakeDetector;
import com.qcard.ui.card.CardListAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PracticeActivity extends AppCompatActivity {

    private QSet mSet;

    private TextView mTextViewTerm, mTextViewDefinition;
    private ProgressBar mProgressBar;
    private ImageView mImageViewTerm, mImageViewDefinition;
    private ProgressBar mProgressTerm, mProgressDefinition;
    private FloatingActionButton mFabPrevious, mFabNext, mFabMemorize, mFabFlip;

    private View mFrontView;
    private View mBackView;

    private ArrayList<QCard> mCards = new ArrayList<>();
    private int mCurrentIndex = 0;

    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private boolean mIsBackVisible = false;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSet = (QSet) getIntent().getSerializableExtra("set");

        mProgressBar = findViewById(R.id.progressBar);

        mTextViewTerm = findViewById(R.id.tvTerm);
        mImageViewTerm = findViewById(R.id.ivTermImage);
        mProgressTerm = findViewById(R.id.progressBarTermImage);

        mTextViewDefinition = findViewById(R.id.tvDefinition);
        mImageViewDefinition = findViewById(R.id.ivDefinitionImage);
        mProgressDefinition = findViewById(R.id.progressBarDefinitionImage);

        mFrontView = findViewById(R.id.viewFront);
        mBackView = findViewById(R.id.viewBack);

        mFabFlip = findViewById(R.id.fabFlip);
        mFabPrevious = findViewById(R.id.fabPrevious);
        mFabNext = findViewById(R.id.fabNext);
        mFabMemorize = findViewById(R.id.fabRemember);

        mFabPrevious.setEnabled(false);
        mFabFlip.setEnabled(false);
        mFabNext.setEnabled(false);
        mFabMemorize.setEnabled(false);
        mProgressDefinition.setVisibility(View.GONE);
        mProgressTerm.setVisibility(View.GONE);

        mFabFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flip();
            }
        });

        mFabMemorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memorize();
            }
        });

        mFabPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex--;
                startTheShow();
            }
        });

        mFabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentIndex == mCards.size() - 1) {
                    finish();
                } else {
                    mCurrentIndex++;
                    startTheShow();
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                flip();
            }
        });

        loadAnimations();
        changeCameraDistance();
        loadCards();
    }

    private void changeCameraDistance() {
        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        mFrontView.setCameraDistance(scale);
        mBackView.setCameraDistance(scale);
    }

    private void loadAnimations() {
        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_out);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_in);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    private void loadCards() {
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .whereEqualTo("userId", GlobalData.getCurrentUser(this).getUserId())
                .whereEqualTo("setId", mSet.getId())
                .whereEqualTo("remembered", false)
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                QCard card = document.toObject(QCard.class);
                                card.setId(document.getId());

                                mCards.add(card);
                            }
                            mCurrentIndex = 0;
                            startTheShow();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error getting cards", Toast.LENGTH_SHORT).show();
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void memorize() {
        QCard currentCard = mCards.get(mCurrentIndex);
        if (currentCard == null || currentCard.isRemembered()) return;

        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(GlobalData.COL_CARD)
                .document(currentCard.getId())
                .update("remembered", true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void startTheShow() {
        findViewById(R.id.viewFront).setVisibility(View.VISIBLE);
        findViewById(R.id.viewBack).setVisibility(View.VISIBLE);

        QCard currentCard = mCards.get(mCurrentIndex);
        if (currentCard == null) {
            finish();
            return;
        }

        if (mIsBackVisible) {
            mIsBackVisible = false;

            mFrontView.setAlpha(1);
            mFrontView.setRotationY(0);

            mBackView.setAlpha(1);
            mBackView.setRotationY(0);
        }

        mFabFlip.setEnabled(true);
        mFabMemorize.setEnabled(true);
        mFabPrevious.setEnabled(true);
        mFabNext.setEnabled(true);
        mFabNext.setImageResource(R.drawable.ic_baseline_arrow_forward_24);

        if (mCurrentIndex == 0) {
            mFabPrevious.setEnabled(false);
        } else if (mCurrentIndex == mCards.size() - 1) {
            mFabNext.setImageResource(R.drawable.ic_baseline_done_24);
        }

        if (currentCard.getTermImage() != null && !currentCard.getTermImage().isEmpty()) {
            mImageViewTerm.setVisibility(View.VISIBLE);
            mProgressTerm.setVisibility(View.VISIBLE);
            FirebaseStorage.getInstance().getReference(currentCard.getTermImage())
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("TAG", "onComplete: " + uri);
                            Picasso.get().load(uri)
                                    .fit()
                                    .into(mImageViewTerm, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            mProgressTerm.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            mProgressTerm.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressTerm.setVisibility(View.GONE);
                        }
                    });
        } else {
            mProgressTerm.setVisibility(View.GONE);
            mImageViewTerm.setVisibility(View.GONE);
        }

        if (currentCard.getDefinitionImage() != null && !currentCard.getDefinitionImage().isEmpty()) {
            mProgressDefinition.setVisibility(View.VISIBLE);
            mImageViewDefinition.setVisibility(View.VISIBLE);
            FirebaseStorage.getInstance().getReference(currentCard.getDefinitionImage())
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("TAG", "onComplete: " + uri);
                            Picasso.get().load(uri)
                                    .fit()
                                    .into(mImageViewDefinition, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            mProgressDefinition.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            mProgressDefinition.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDefinition.setVisibility(View.GONE);
                        }
                    });
        } else {
            mProgressDefinition.setVisibility(View.GONE);
            mImageViewDefinition.setVisibility(View.GONE);
        }
        mTextViewTerm.setText(currentCard.getTerm());
        mTextViewDefinition.setText(currentCard.getDefinition());
    }

    public void flip() {
        if (!mIsBackVisible) {
            mSetRightOut.setTarget(mFrontView);
            mSetLeftIn.setTarget(mBackView);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = true;
        } else {
            mSetRightOut.setTarget(mBackView);
            mSetLeftIn.setTarget(mFrontView);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = false;
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
