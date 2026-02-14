package com.v2v.eduguard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Default Fragment
        loadFragment(new AdminHomeFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if(item.getItemId() == R.id.nav_home){
                selectedFragment = new AdminHomeFragment();
            }
            else if(item.getItemId() == R.id.nav_students_list){
                selectedFragment = new AdminStudentsFragment();
            }
            else if(item.getItemId() == R.id.nav_faculty){
                selectedFragment = new FacultyFragment();
            }
            else if(item.getItemId() == R.id.nav_settings){
                selectedFragment = new AdminSettingsFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment){

        if(fragment != null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        }

        return false;
    }
}
