package com.sajoldev.habittracker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.utils.ExportManager;

public class SettingsFragment extends Fragment {

    private static final String APP_PACKAGE = "com.sajoldev.habittracker";
    private static final String PRIVACY_POLICY_URL = "https://example.com/privacy-policy";
    private static final int REQUEST_CODE_IMPORT = 1001;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri fileUri = result.getData().getData();
                            if (fileUri != null) {
                                ExportManager.importFromJSON(requireContext(), fileUri);
                            }
                        }
                    }
                }
        );

        LinearLayout btnExportPDF = view.findViewById(R.id.btnExportPDF);
        LinearLayout btnExportJSON = view.findViewById(R.id.btnExportJSON);
        LinearLayout btnImportJSON = view.findViewById(R.id.btnImportJSON);
        LinearLayout btnShareApp = view.findViewById(R.id.btnShareApp);
        LinearLayout btnRateUs = view.findViewById(R.id.btnRateUs);
        LinearLayout btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        TextView tvVersion = view.findViewById(R.id.tvVersion);

        btnExportPDF.setOnClickListener(v -> {
            ExportManager.exportToPDF(requireContext());
        });

        btnExportJSON.setOnClickListener(v -> {
            ExportManager.exportToJSON(requireContext());
        });

        btnImportJSON.setOnClickListener(v -> openFilePicker());

        btnShareApp.setOnClickListener(v -> shareApp());

        btnRateUs.setOnClickListener(v -> rateApp());

        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());

        tvVersion.setText("Version 1.0");

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        filePickerLauncher.launch(intent);
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Habit Tracker App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing Habit Tracker app!\n\nhttps://play.google.com/store/apps/details?id=" + APP_PACKAGE);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE));
            startActivity(rateIntent);
        } catch (Exception e) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + APP_PACKAGE));
            startActivity(rateIntent);
        }
    }

    private void openPrivacyPolicy() {
        Intent privacyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL));
        startActivity(privacyIntent);
    }
}
