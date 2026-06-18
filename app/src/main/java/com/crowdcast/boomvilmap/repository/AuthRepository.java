package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // 결과를 Fragment로 돌려주기 위한 콜백 인터페이스
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Firebase Auth에 이메일/비번으로 계정 생성 요청
    public void signUp(String email, String password, String nickname, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 성공 : UID를 가져옴
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), email, nickname, callback);
                        }
                    } else {
                        // 실패 (ex: 이미 가입된 이메일, 네트워크 오류 등)
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "회원가입 실패";
                        callback.onFailure(errorMsg);
                    }
                });
    }

    // 계정의 UID를 문서 ID로 사용하여 Firestore DB에 유저 정보 저장
    private void saveUserToFirestore(String uid, String email, String nickname, AuthCallback callback) {
        User newUser = new User(email, nickname, System.currentTimeMillis());

        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("DB 저장 실패: " + e.getMessage());
                });
    }
}