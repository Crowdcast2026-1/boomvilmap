package com.crowdcast.boomvilmap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class MyPageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.button_logout).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setMessage("정말 로그아웃 하시겠어요?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("로그아웃", (dialog, which) -> ((MainActivity) requireActivity()).showLogin())
                        .show()
        );
    }
}
