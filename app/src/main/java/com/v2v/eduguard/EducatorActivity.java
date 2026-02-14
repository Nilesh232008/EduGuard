package com.v2v.eduguard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EducatorActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educator);

        bottomNav = findViewById(R.id.educator_bottom_nav);

        // Default Fragment
        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if(item.getItemId() == R.id.nav_dashboard){
                selectedFragment = new DashboardFragment();
            }
            else if(item.getItemId() == R.id.nav_students){
                selectedFragment = new StudentsFragment();
            }
            else if(item.getItemId() == R.id.nav_attendance){
                selectedFragment = new AttendanceFragment();
            }
            else if(item.getItemId() == R.id.nav_insights){
                selectedFragment = new InsightsFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment){

        if(fragment != null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.educator_fragment_container, fragment)
                    .commit();

            return true;
        }

        return false;
    }
}

