package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.repository.AuthRepository;

public class LoginFragment extends Fragment {

    private boolean passwordVisible = false;

    // UI 컴포넌트 변수
    private EditText editEmail, editPassword;
    private Button buttonLogin;
    private TextView textSignUp;
    private ImageButton buttonTogglePassword;

    // 포지토리
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        authRepository = new AuthRepository();
    }

    // findViewById 세팅 함수
    private void initViews(View view) {
        editEmail = view.findViewById(R.id.edit_email);
        editPassword = view.findViewById(R.id.edit_password);
        buttonLogin = view.findViewById(R.id.button_login);
        textSignUp = view.findViewById(R.id.text_signup);
        buttonTogglePassword = view.findViewById(R.id.button_toggle_password);
    }

    // 클릭 리스너 설정 함수
    private void setupListeners() {
        // 회원 가입 이동
        textSignUp.setOnClickListener(v -> ((MainActivity) requireActivity()).showSignUp());

        // 비밀번호 표시/숨기기 토글 기능
        buttonTogglePassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            editPassword.setInputType(passwordVisible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editPassword.setSelection(editPassword.length());
        });

        // 로그인 버튼
        buttonLogin.setOnClickListener(v -> attemptLogin());
    }

    // 로그인 실행 로직
    private void attemptLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // 간단한 빈칸 검사
        if (email.isEmpty() || password.isEmpty()) {
            showToast("이메일과 비밀번호를 모두 입력해주세요.");
            return;
        }

        // 로그인 처리 중 버튼 막기 (중복 클릭 방지)
        buttonLogin.setEnabled(false);
        buttonLogin.setText("로그인 중...");

        // 파이어베이스에 로그인 요청
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                showToast("로그인 성공!");
                ((MainActivity) requireActivity()).showMap();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("로그인 실패: 이메일이나 비밀번호를 확인해주세요.");
                buttonLogin.setEnabled(true);
                buttonLogin.setText("로그인");
            }
        });
    }

    // 토스트 메세지ㅁㄴ
    private void showToast(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}