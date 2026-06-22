package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

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

    public interface UserCallback {
        void onSuccess(User user);
        void onLoggedOut();
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

    //로그인
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "로그인 실패";
                        callback.onFailure(errorMsg);
                    }
                });
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public void loadCurrentUser(UserCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onLoggedOut();
            return;
        }

        db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    String email = valueOrDefault(document.getString("email"), firebaseUser.getEmail());
                    String nickname = valueOrDefault(document.getString("nickname"), firebaseUser.getDisplayName());
                    if (nickname == null || nickname.trim().isEmpty()) {
                        nickname = nicknameFromEmail(email);
                    }
                    Long createdAt = document.getLong("createdAt");
                    callback.onSuccess(new User(email, nickname, createdAt != null ? createdAt : 0L));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateNickname(String nickname, AuthCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure("로그인이 필요합니다.");
            return;
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            callback.onFailure("닉네임을 입력해주세요.");
            return;
        }

        String trimmedNickname = nickname.trim();
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", trimmedNickname);
        data.put("email", firebaseUser.getEmail());

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(trimmedNickname)
                            .build();
                    firebaseUser.updateProfile(request)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    String message = task.getException() != null
                                            ? task.getException().getMessage()
                                            : "프로필 업데이트 실패";
                                    callback.onFailure(message);
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    private String nicknameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "사용자";
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}
