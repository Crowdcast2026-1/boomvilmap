package com.crowdcast.boomvilmap;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {
    private boolean passwordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText password = view.findViewById(R.id.edit_password);
        ImageButton toggle = view.findViewById(R.id.button_toggle_password);
        TextView signUp = view.findViewById(R.id.text_signup);

        view.findViewById(R.id.button_login).setOnClickListener(v -> ((MainActivity) requireActivity()).showMap());
        signUp.setOnClickListener(v -> ((MainActivity) requireActivity()).showSignUp());

        toggle.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            password.setInputType(passwordVisible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            password.setSelection(password.length());
        });
    }
}