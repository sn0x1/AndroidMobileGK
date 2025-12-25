package com.example.lab4_spacex;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String mission = getIntent().getStringExtra("mission_name");
        String details = getIntent().getStringExtra("details");
        String url = getIntent().getStringExtra("patch_url");

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        ImageView img = findViewById(R.id.imgDetail);
        Button btnBack = findViewById(R.id.btnBack); // Знаходимо кнопку

        tvTitle.setText(mission);
        tvDesc.setText(details != null ? details : "Опис відсутній");

        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).into(img);
        }

        // Обробка натискання на кнопку "Назад"
        btnBack.setOnClickListener(v -> finish());
    }
}