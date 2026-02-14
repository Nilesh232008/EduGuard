package com.v2v.eduguard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class AdminSettingsFragment extends Fragment {

    CardView cardProfile;
    Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_admin_settings,
                container,
                false);

        cardProfile = view.findViewById(R.id.cardProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 🔥 OPEN PROFILE
        cardProfile.setOnClickListener(v -> {

            startActivity(new Intent(
                    getContext(),
                    AdminProfileActivity.class));

        });

        // 🔥 LOGOUT
        btnLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes",(d,w)-> logoutAdmin())
                    .setNegativeButton("Cancel",null)
                    .show();

        });

        return view;
    }

    private void logoutAdmin(){

        // Clear admin session
        SharedPreferences sp = getActivity()
                .getSharedPreferences("admin", 0);

        sp.edit().clear().apply();

        // Firebase signout (safe even if admin is hardcoded)
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(
                getContext(),
                LoginActivity.class);

        // 🚨 VERY IMPORTANT
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}
