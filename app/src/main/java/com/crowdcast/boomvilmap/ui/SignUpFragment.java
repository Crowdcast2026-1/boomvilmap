package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.repository.AuthRepository;
import com.crowdcast.boomvilmap.util.CaptchaGenerator;

public class SignUpFragment extends Fragment {

    // UI 컴포넌트 변수
    private ImageButton buttonBack;
    private EditText editEmail, editPassword, editConfirm, editNickname, editCaptcha;
    private TextView textPasswordError;
    private ImageView imageCaptcha;
    private Button buttonRefreshCaptcha, buttonSubmit;
    private CheckBox checkTerms;

    // 캡챠 정답 임시 저장소
    private String currentCaptchaAnswer;

    // 리포지토리
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        authRepository = new AuthRepository();

        // 화면이 생성될 때 캡챠 이미지 최초 로드
        refreshCaptcha();
    }

    // findViewById 세팅 함수
    private void initViews(View view) {
        buttonBack = view.findViewById(R.id.button_back);
        editEmail = view.findViewById(R.id.edit_signup_email);
        editPassword = view.findViewById(R.id.edit_signup_password);
        editConfirm = view.findViewById(R.id.edit_signup_confirm);
        textPasswordError = view.findViewById(R.id.text_password_error);
        editNickname = view.findViewById(R.id.edit_nickname);
        editCaptcha = view.findViewById(R.id.edit_captcha);
        imageCaptcha = view.findViewById(R.id.text_captcha);
        buttonRefreshCaptcha = view.findViewById(R.id.button_refresh_captcha);
        checkTerms = view.findViewById(R.id.check_terms);
        buttonSubmit = view.findViewById(R.id.button_signup_submit);
    }

    // 클릭 리스너 설정 함수
    private void setupListeners() {
        // 뒤로가기 버튼
        buttonBack.setOnClickListener(v -> ((MainActivity) requireActivity()).showLogin());

        // 캡챠 새로고침 버튼
        buttonRefreshCaptcha.setOnClickListener(v -> refreshCaptcha());

        // 가입하기 버튼
        buttonSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    // 캡챠 이미지 생성 및 UI 적용
    private void refreshCaptcha() {
        CaptchaGenerator.Captcha captcha = CaptchaGenerator.generate();
        imageCaptcha.setImageBitmap(captcha.image);
        currentCaptchaAnswer = captcha.answer;
        editCaptcha.setText(""); // 입력칸 비우기
    }

    // 데이터 유효성 검사 및 가입 처리
    private void validateAndSubmit() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirm = editConfirm.getText().toString().trim();
        String nickname = editNickname.getText().toString().trim();
        String captchaInput = editCaptcha.getText().toString().trim();

        // 1. 이메일 유효성 검사
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("올바른 이메일 형식을 입력해주세요.");
            return;
        }

        // 2. 비밀번호 길이 검사
        if (password.length() < 8) {
            showToast("비밀번호는 8자 이상이어야 합니다.");
            return;
        }

        // 3. 비밀번호 확인 검사
        if (!password.equals(confirm)) {
            textPasswordError.setVisibility(View.VISIBLE);
            return;
        } else {
            textPasswordError.setVisibility(View.GONE);
        }

        // 4. 닉네임 유무 검사
        if (nickname.isEmpty()) {
            showToast("사용할 닉네임을 입력해주세요.");
            return;
        }

        // 5. 캡챠 검사 (대소문자 무시)
        if (!captchaInput.equalsIgnoreCase(currentCaptchaAnswer)) {
            showToast("캡챠 문자가 일치하지 않습니다.");
            refreshCaptcha(); // 틀리면 새 이미지로 교체
            return;
        }

        // 6. 약관 동의 검사
        if (!checkTerms.isChecked()) {
            showToast("필수 약관에 동의해주세요.");
            return;
        }

        showToast("유효성 검사 통과! 가입 진행 중...");

        // 가입 진행중 <= 클릭 막기
        buttonSubmit.setEnabled(false);
        buttonSubmit.setText("가입 처리 중...");

        // 저장소에 가입 요청 던지기
        authRepository.signUp(email, password, nickname, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                showToast("회원가입이 완료되었습니다!");
                ((MainActivity) requireActivity()).showMap();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("가입 실패: " + errorMessage);
                buttonSubmit.setEnabled(true);
                buttonSubmit.setText("가입하기");
            }
        });
    }

    // 안내 메시지용 토스트
    private void showToast(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}