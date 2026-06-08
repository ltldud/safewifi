package com.cookandroid.safewifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentResultActivity extends AppCompatActivity {

    private TextView     tvGradeLabel;
    private TextView     tvSsid;
    private TextView     tvSecurityType;
    private TextView     tvSignalStrength;
    private LinearLayout layoutGradeBackground;
    private LinearLayout layoutRiskItems;
    private Button       btnBack;
    private Button       btnSave;

    private WifiManager    wifiManager;
    private DatabaseHelper dbHelper;

    private String currentSsid;
    private String currentSecType;
    private String currentGrade;
    private int    currentRiskScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_result);

        tvGradeLabel          = (TextView)     findViewById(R.id.tvGradeLabel);
        tvSsid                = (TextView)     findViewById(R.id.tvSsid);
        tvSecurityType        = (TextView)     findViewById(R.id.tvSecurityType);
        tvSignalStrength      = (TextView)     findViewById(R.id.tvSignalStrength);
        layoutGradeBackground = (LinearLayout) findViewById(R.id.layoutGradeBackground);
        layoutRiskItems       = (LinearLayout) findViewById(R.id.layoutRiskItems);
        btnBack               = (Button)       findViewById(R.id.btnBack);
        btnSave               = (Button)       findViewById(R.id.btnSave);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        dbHelper    = new DatabaseHelper(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
            }
        });

        analyzeCurrentWifi();
    }

    private void analyzeCurrentWifi() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info == null) {
            tvGradeLabel.setText("Wi-Fi 미연결");
            return;
        }

        currentSsid      = info.getSSID().replace("\"", "");
        int rssi         = info.getRssi();
        currentSecType   = "WPA2"; // TODO: ScanResult 매칭으로 교체
        currentRiskScore = SecurityAnalyzer.calcRiskScore(currentSecType, rssi);
        currentGrade     = SecurityAnalyzer.getGrade(currentRiskScore);

        tvSsid.setText(currentSsid);
        tvSecurityType.setText(currentSecType);
        tvSignalStrength.setText(SecurityAnalyzer.getSignalDescription(rssi));

        updateGradeUI(currentGrade);
        addRiskItem(SecurityAnalyzer.getRiskDescription(currentSecType));
    }

    private void updateGradeUI(String grade) {
        int color;
        if (grade.equals(SecurityAnalyzer.GRADE_SAFE)) {
            color = Color.parseColor("#2E7D32");
        } else if (grade.equals(SecurityAnalyzer.GRADE_WARNING)) {
            color = Color.parseColor("#F57F17");
        } else {
            color = Color.parseColor("#C62828");
        }
        layoutGradeBackground.setBackgroundColor(color);
        tvGradeLabel.setText(grade);
    }

    private void addRiskItem(String description) {
        TextView tv = new TextView(this);
        tv.setText(description);
        tv.setTextSize(14);
        tv.setPadding(0, 4, 0, 4);
        layoutRiskItems.addView(tv);
    }

    private void saveResult() {
        if (currentSsid == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(new Date());

        WifiScanLog log = new WifiScanLog(
                "current", currentSsid, currentSecType,
                currentRiskScore, currentGrade, now);
        dbHelper.insert(log);
        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            analyzeCurrentWifi();
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }
    }
}