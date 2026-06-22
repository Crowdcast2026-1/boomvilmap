package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.model.User;
import com.crowdcast.boomvilmap.repository.AuthRepository;
import com.crowdcast.boomvilmap.repository.FavoriteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyPageFragment extends Fragment {
    private AuthRepository authRepository;
    private FavoriteRepository favoriteRepository;
    private TextView profileAvatar;
    private TextView userName;
    private TextView userEmail;
    private TextView userStats;
    private User currentUser;
    private int favoriteCount = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authRepository = new AuthRepository();
        favoriteRepository = new FavoriteRepository();
        bindViews(view);
        loadUserProfile();
        loadFavoriteCount();
        view.findViewById(R.id.button_edit_profile).setOnClickListener(v -> showNicknameEditDialog());
        view.findViewById(R.id.button_logout).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setMessage("정말 로그아웃 하시겠어요?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("로그아웃", (dialog, which) -> {
                            new AuthRepository().logout();
                            ((MainActivity) requireActivity()).showLogin();
                        })
                        .show()
        );
    }

    private void bindViews(View view) {
        profileAvatar = view.findViewById(R.id.text_profile_avatar);
        userName = view.findViewById(R.id.text_user_name);
        userEmail = view.findViewById(R.id.text_user_email);
        userStats = view.findViewById(R.id.text_user_stats);
    }

    private void loadUserProfile() {
        userName.setText("사용자 정보 불러오는 중...");
        userEmail.setText("");

        authRepository.loadCurrentUser(new AuthRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                currentUser = user;
                renderUser(user);
            }

            @Override
            public void onLoggedOut() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).showLogin();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                userName.setText("사용자 정보 로드 실패");
                userEmail.setText(errorMessage != null ? errorMessage : "");
            }
        });
    }

    private void renderUser(User user) {
        String nickname = valueOrDefault(user.getNickname(), nicknameFromEmail(user.getEmail()));
        String email = valueOrDefault(user.getEmail(), "이메일 없음");

        userName.setText(nickname);
        userEmail.setText(email);
        profileAvatar.setText(avatarText(nickname, email));
        updateStatsText(favoriteCount, user.getCreatedAt());
    }

    private void loadFavoriteCount() {
        favoriteRepository.loadFavoriteCount(new FavoriteRepository.FavoriteCountCallback() {
            @Override
            public void onSuccess(int count) {
                if (!isAdded()) return;
                favoriteCount = count;
                long createdAt = currentUser != null ? currentUser.getCreatedAt() : 0L;
                updateStatsText(favoriteCount, createdAt);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                favoriteCount = -1;
                long createdAt = currentUser != null ? currentUser.getCreatedAt() : 0L;
                updateStatsText(favoriteCount, createdAt);
            }
        });
    }

    private void updateStatsText(int favoriteCount, long createdAt) {
        String favoriteText = favoriteCount >= 0 ? "즐겨찾기 " + favoriteCount + "곳" : "즐겨찾기 -곳";
        if (createdAt > 0L) {
            String createdAtText = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(createdAt));
            userStats.setText(favoriteText + " · 가입일 " + createdAtText);
        } else {
            userStats.setText(favoriteText);
        }
    }

    private void showNicknameEditDialog() {
        EditText editText = new EditText(requireContext());
        editText.setSingleLine(true);
        editText.setHint("닉네임");
        if (currentUser != null) {
            editText.setText(currentUser.getNickname());
            editText.setSelection(editText.length());
        }
        int padding = getResources().getDimensionPixelSize(R.dimen.screen_padding);
        editText.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(requireContext())
                .setTitle("닉네임 수정")
                .setView(editText)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", (dialog, which) -> {
                    String nickname = editText.getText().toString().trim();
                    updateNickname(nickname);
                })
                .show();
    }

    private void updateNickname(String nickname) {
        authRepository.updateNickname(nickname, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "닉네임을 수정했습니다.", Toast.LENGTH_SHORT).show();
                loadUserProfile();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "닉네임 수정 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String avatarText(String nickname, String email) {
        String source = valueOrDefault(nickname, email);
        if (source == null || source.trim().isEmpty()) return "U";
        return source.trim().substring(0, 1).toUpperCase(Locale.KOREA);
    }

    private String nicknameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "사용자";
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }
}
