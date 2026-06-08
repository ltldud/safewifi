package com.cookandroid.safewifi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private WifiScanLog    log;

    private LinearLayout layoutResultBackground;
    private TextView     tvResultText;
    private TextView     tvSsid;
    private TextView     tvScanType;
    private TextView     tvSecurityType;
    private TextView     tvRiskScore;
    private TextView     tvCreatedAt;
    private Button       btnBack;
    private Button       btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper               = new DatabaseHelper(this);
        layoutResultBackground = (LinearLayout) findViewById(R.id.layoutResultBackground);
        tvResultText           = (TextView) findViewById(R.id.tvResultText);
        tvSsid                 = (TextView) findViewById(R.id.tvSsid);
        tvScanType             = (TextView) findViewById(R.id.tvScanType);
        tvSecurityType         = (TextView) findViewById(R.id.tvSecurityType);
        tvRiskScore            = (TextView) findViewById(R.id.tvRiskScore);
        tvCreatedAt            = (TextView) findViewById(R.id.tvCreatedAt);
        btnBack                = (Button) findViewById(R.id.btnBack);
        btnDelete              = (Button) findViewById(R.id.btnDelete);

        int id = getIntent().getIntExtra("log_id", -1);
        if (id == -1) {
            finish();
            return;
        }
        log = dbHelper.getById(id);
        if (log == null) {
            finish();
            return;
        }

        bind();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DetailActivity.this)
                        .setTitle("기록 삭제")
                        .setMessage("이 기록을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dbHelper.delete(log.getId());
                                Toast.makeText(DetailActivity.this,
                                        "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }

    private void bind() {
        tvSsid.setText(log.getSsid());
        tvSecurityType.setText(log.getSecurityType());
        tvRiskScore.setText(log.getRiskScore() + " / 100");
        tvCreatedAt.setText(log.getCreatedAt());
        tvResultText.setText(log.getResultText());

        if (log.getScanType().equals("current")) {
            tvScanType.setText("현재 연결 분석");
        } else {
            tvScanType.setText("주변 Wi-Fi 스캔");
        }

        int color;
        if (log.getResultText().equals(SecurityAnalyzer.GRADE_SAFE)) {
            color = Color.parseColor("#2E7D32");
        } else if (log.getResultText().equals(SecurityAnalyzer.GRADE_WARNING)) {
            color = Color.parseColor("#F57F17");
        } else {
            color = Color.parseColor("#C62828");
        }
        layoutResultBackground.setBackgroundColor(color);
    }
}