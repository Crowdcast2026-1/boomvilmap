package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.crowdcast.boomvilmap.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private int bottomNavigationBaseHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigationBaseHeight = getResources().getDimensionPixelSize(R.dimen.bottom_nav_height);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, systemBars.top, 0, 0);

            bottomNavigation.setPadding(
                    bottomNavigation.getPaddingLeft(),
                    bottomNavigation.getPaddingTop(),
                    bottomNavigation.getPaddingRight(),
                    systemBars.bottom
            );
            bottomNavigation.getLayoutParams().height = bottomNavigationBaseHeight + systemBars.bottom;
            bottomNavigation.requestLayout();
            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) showMap();
            else if (id == R.id.nav_search) showSearch();
            else if (id == R.id.nav_favorites) showFavorites();
            else if (id == R.id.nav_mypage) showMyPage();
            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof DetailFragment) {
                bottomNavigation.setVisibility(View.GONE);
            } else {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            showSplash();
        }
    }

    private void replace(Fragment fragment, boolean showBottomNav, boolean addToBackStack) {
        bottomNavigation.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
    }

    private void replace(Fragment fragment, boolean showBottomNav) {
        replace(fragment, showBottomNav, false);
    }

    public void showSplash() { replace(new SplashFragment(), false); }
    public void showLogin() { replace(new LoginFragment(), false); }
    public void showSignUp() { replace(new SignUpFragment(), false); }
    public void showMap() { replace(new MapFragment(), true); }
    public void showSearch() { replace(new SearchFragment(), true); }
    public void showFavorites() { replace(new FavoritesFragment(), true); }
    public void showMyPage() { replace(new MyPageFragment(), true); }

    public void showDetail(int spotId) {
        replace(DetailFragment.newInstance(spotId), false, true);
    }
}