package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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

        if (savedInstanceState == null) {
            showSplash();
        }
    }

    private void replace(Fragment fragment, boolean showBottomNav) {
        bottomNavigation.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    public void showSplash() { replace(new SplashFragment(), false); }
    public void showLogin() { replace(new LoginFragment(), false); }
    public void showSignUp() { replace(new SignUpFragment(), false); }
    public void showMap() { replace(new MapFragment(), true); }
    public void showSearch() { replace(new SearchFragment(), true); }
    public void showFavorites() { replace(new FavoritesFragment(), true); }
    public void showMyPage() { replace(new MyPageFragment(), true); }

    public void showDetail(int spotId) {
        replace(DetailFragment.newInstance(spotId), false);
    }
}
