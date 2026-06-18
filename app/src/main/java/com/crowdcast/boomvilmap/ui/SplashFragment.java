package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crowdcast.boomvilmap.R;

public class SplashFragment extends Fragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int progress = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProgressBar progressBar = view.findViewById(R.id.progress_loading);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                progress += 2;
                progressBar.setProgress(progress);
                if (progress >= 100) {
                    handler.postDelayed(() -> ((MainActivity) requireActivity()).showLogin(), 300);
                } else {
                    handler.postDelayed(this, 40);
                }
            }
        };
        handler.post(runnable);
    }
}
