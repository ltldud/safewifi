package com.cookandroid.safewifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NearbyWifiActivity extends AppCompatActivity {

    private WifiManager         wifiManager;
    private RecyclerView        recyclerView;
    private NearbyWifiAdapter   adapter;
    private View                layoutEmpty;
    private View                layoutScanStatus;
    private View                layoutScanSummary;
    private TextView            tvTotalCount;
    private TextView            tvSafeCount;
    private TextView            tvWarningCount;
    private TextView            tvDangerCount;
    private Button              btnBack;
    private Button              btnScan;

    private List<ScanResult> scanResults = new ArrayList<ScanResult>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onScanComplete();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_wifi);

        wifiManager       = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        recyclerView      = (RecyclerView) findViewById(R.id.recyclerViewNearby);
        layoutEmpty       = findViewById(R.id.layoutEmpty);
        layoutScanStatus  = findViewById(R.id.layoutScanStatus);
        layoutScanSummary = findViewById(R.id.layoutScanSummary);
        tvTotalCount      = (TextView) findViewById(R.id.tvTotalCount);
        tvSafeCount       = (TextView) findViewById(R.id.tvSafeCount);
        tvWarningCount    = (TextView) findViewById(R.id.tvWarningCount);
        tvDangerCount     = (TextView) findViewById(R.id.tvDangerCount);
        btnBack           = (Button) findViewById(R.id.btnBack);
        btnScan           = (Button) findViewById(R.id.btnScan);

        adapter = new NearbyWifiAdapter(scanResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    private void startScan() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        layoutScanStatus.setVisibility(View.VISIBLE);
        layoutScanSummary.setVisibility(View.GONE);
        wifiManager.startScan();
    }

    @SuppressWarnings("MissingPermission")
    private void onScanComplete() {
        layoutScanStatus.setVisibility(View.GONE);
        List<ScanResult> results = wifiManager.getScanResults();

        scanResults.clear();
        scanResults.addAll(results);
        adapter.notifyDataSetChanged();

        int safe   = 0;
        int warn   = 0;
        int danger = 0;

        for (int i = 0; i < results.size(); i++) {
            ScanResult sr  = results.get(i);
            String sec     = SecurityAnalyzer.parseSecurityType(sr.capabilities);
            int score      = SecurityAnalyzer.calcRiskScore(sec, sr.level);
            String grade   = SecurityAnalyzer.getGrade(score);

            if (grade.equals(SecurityAnalyzer.GRADE_SAFE)) {
                safe++;
            } else if (grade.equals(SecurityAnalyzer.GRADE_WARNING)) {
                warn++;
            } else {
                danger++;
            }
        }

        tvTotalCount.setText("총 " + results.size() + "개");
        tvSafeCount.setText("안전 " + safe);
        tvWarningCount.setText("주의 " + warn);
        tvDangerCount.setText("위험 " + danger);

        if (results.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        layoutScanSummary.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }
    }

    // ── RecyclerView Adapter ──────────────────────────────
    static class NearbyWifiAdapter extends RecyclerView.Adapter<NearbyWifiAdapter.ViewHolder> {

        private List<ScanResult> items;

        public NearbyWifiAdapter(List<ScanResult> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nearby_wifi, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScanResult sr = items.get(position);
            String sec    = SecurityAnalyzer.parseSecurityType(sr.capabilities);
            int score     = SecurityAnalyzer.calcRiskScore(sec, sr.level);
            String grade  = SecurityAnalyzer.getGrade(score);

            if (sr.SSID.isEmpty()) {
                holder.tvSsid.setText("(숨겨진 네트워크)");
            } else {
                holder.tvSsid.setText(sr.SSID);
            }
            holder.tvSecurityType.setText(sec + " · " + sr.level + " dBm");

            if (sec.equals(SecurityAnalyzer.SEC_OPEN)) {
                holder.tvOpenBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvOpenBadge.setVisibility(View.GONE);
            }

            int color;
            if (grade.equals(SecurityAnalyzer.GRADE_SAFE)) {
                color = Color.parseColor("#2E7D32");
            } else if (grade.equals(SecurityAnalyzer.GRADE_WARNING)) {
                color = Color.parseColor("#F57F17");
            } else {
                color = Color.parseColor("#C62828");
            }
            holder.tvGradeText.setText(grade);
            holder.tvGradeText.setTextColor(color);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSsid;
            TextView tvSecurityType;
            TextView tvGradeText;
            TextView tvOpenBadge;

            public ViewHolder(View itemView) {
                super(itemView);
                tvSsid         = (TextView) itemView.findViewById(R.id.tvSsid);
                tvSecurityType = (TextView) itemView.findViewById(R.id.tvSecurityType);
                tvGradeText    = (TextView) itemView.findViewById(R.id.tvGradeText);
                tvOpenBadge    = (TextView) itemView.findViewById(R.id.tvOpenBadge);
            }
        }
    }
}