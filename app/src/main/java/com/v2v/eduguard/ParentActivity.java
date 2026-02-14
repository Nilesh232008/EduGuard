package com.v2v.eduguard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        bottomNav = findViewById(R.id.parent_bottom_nav);

        // ✅ Default Screen
        loadFragment(new ParentHomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            if(item.getItemId() == R.id.nav_home){
                fragment = new ParentHomeFragment();
            }
            else if(item.getItemId() == R.id.nav_attendance_mark){
                fragment = new ParentAttendanceFragment();
            }
            else if(item.getItemId() == R.id.nav_homework){
                fragment = new ParentHomeworkFragment();
            }
            else if(item.getItemId() == R.id.nav_profile){
                fragment = new ParentProfileFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment){

        if(fragment != null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.parent_fragment_container, fragment)
                    .commit();

            return true;
        }

        return false;
    }
}

