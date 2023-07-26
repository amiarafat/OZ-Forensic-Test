package com.muv.oztest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ozforensics.liveness.sdk.analysis.AnalysisRequest;
import com.ozforensics.liveness.sdk.analysis.entity.Analysis;
import com.ozforensics.liveness.sdk.analysis.entity.RequestResult;
import com.ozforensics.liveness.sdk.core.OzLivenessSDK;
import com.ozforensics.liveness.sdk.core.model.OzAbstractMedia;
import com.ozforensics.liveness.sdk.core.model.OzAction;
import com.ozforensics.liveness.sdk.exceptions.OzException;
import com.ozforensics.liveness.sdk.security.LicenseSource;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int OZ_LIVENESS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OzLivenessSDK.INSTANCE.init(
                this,
                Collections.singletonList(new LicenseSource.LicenseAssetId(R.raw.niyog_forensics)),
                null
        );

        Intent intent = OzLivenessSDK.INSTANCE.createStartIntent(Collections.singletonList(OzAction.Blank));
        activityFileResultLauncherResume.launch(intent);
    }


    ActivityResultLauncher<Intent> activityFileResultLauncherResume = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Log.d("TAG", "onActivityResult: "+result.getData().describeContents());
                List<OzAbstractMedia> sdkMediaResult = OzLivenessSDK.INSTANCE.getResultFromIntent(result.getData());
                String sdkErrorString = OzLivenessSDK.INSTANCE.getErrorFromIntent(result.getData());
                if (sdkMediaResult != null && !sdkMediaResult.isEmpty()) {
                    analyzeMedia(sdkMediaResult);
                } else {
                    Log.d("TAG", "onActivityResult: "+sdkErrorString);
                }
            }
        }
    });

    private void analyzeMedia(List<OzAbstractMedia> mediaList) {
        new AnalysisRequest.Builder()
                .addAnalysis(new Analysis(Analysis.Type.BIOMETRY, Analysis.Mode.ON_DEVICE, mediaList, Collections.emptyMap()))
                .build()
                .run(new AnalysisRequest.AnalysisListener() {

                    @Override
                    public void onSuccess(@NonNull RequestResult requestResult) {

                        Log.d("TAG", "onSuccess: "+requestResult.getAnalysisResults());
                    }

                    @Override public void onStatusChange(@NonNull AnalysisRequest.AnalysisStatus analysisStatus) {

                    }
                    @Override
                    public void onError(@NonNull OzException e) { e.printStackTrace(); }
                });
    }
}