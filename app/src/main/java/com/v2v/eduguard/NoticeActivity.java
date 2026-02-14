package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NoticeActivity extends AppCompatActivity {

    EditText etTitle, etMessage;
    Button btnPublish;

    DatabaseReference noticeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        btnPublish = findViewById(R.id.btnPublish);

        noticeRef = FirebaseDatabase.getInstance().getReference("Notices");

        btnPublish.setOnClickListener(v -> publishNotice());
    }

    private void publishNotice(){

        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if(title.isEmpty() || message.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        String noticeId = noticeRef.push().getKey();

        HashMap<String,Object> noticeMap = new HashMap<>();
        noticeMap.put("title",title);
        noticeMap.put("message",message);
        noticeMap.put("createdAt",System.currentTimeMillis());
        noticeMap.put("createdBy","admin");

        noticeRef.child(noticeId)
                .setValue(noticeMap)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Notice Published!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                });
    }
}
