package com.example.travelapp.Activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.travelapp.Fragment.WishlistFragment;
import com.example.travelapp.Fragment.ExplorerFragment;
import com.example.travelapp.Fragment.HomeFragment;
import com.example.travelapp.Fragment.LoginFragment;
import com.example.travelapp.Fragment.ProfileFragment;
import com.example.travelapp.R;
import com.example.travelapp.SharedPrefsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;


public class MainActivity extends BaseActivity {
    private SharedPrefsHelper prefsHelper;
    private FirebaseAuth mAuth;
    private ChipNavigationBar bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefsHelper = new SharedPrefsHelper(this);
        mAuth = FirebaseAuth.getInstance();
        // Khởi tạo ChipNavigationBar
        bottomNav = findViewById(R.id.bottom_nav);
        updateNavigationMenu();
        // Đặt sự kiện cho ChipNavigationBar
        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                // Gọi hàm loadFragment với các fragment tương ứng khi chọn item
                if (id == R.id.homebtn) {
                    loadFragment(new HomeFragment());
                } else if (id == R.id.explorer) {
                    loadFragment(new ExplorerFragment());
                } else if (id == R.id.bookmark) {
                    loadFragment(new WishlistFragment());
                } else if (id == R.id.profile) {
                    loadFragment(new ProfileFragment());
                } else if (id == R.id.login) {
                    loadFragment(new LoginFragment());
                }
            }
        });

        // Thiết lập mặc định cho fragment khi ứng dụng khởi động
        if (savedInstanceState == null) {
            bottomNav.setItemSelected(R.id.homebtn, true); // Chọn Home mặc định
            loadFragment(new HomeFragment()); // Hiển thị HomeFragment khi ứng dụng bắt đầu
        }
    }

    private void updateNavigationMenu() {
        if (prefsHelper.isLoggedIn()) {
            bottomNav.setMenuResource(R.menu.menu_bottom); // Menu với Profile
        } else {
            bottomNav.setMenuResource(R.menu.menu_bottom_logged_out); // Menu với Login
        }
    }

    // Hàm loadFragment dùng để thay đổi fragment
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Thay thế nội dung trong fragment_container
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật menu khi quay lại activity
        updateNavigationMenu();
    }
}