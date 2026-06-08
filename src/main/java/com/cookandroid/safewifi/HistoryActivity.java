package com.cookandroid.safewifi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private View           layoutEmpty;
    private RecyclerView   recyclerView;
    private Button         btnBack;
    private Button         btnDeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper     = new DatabaseHelper(this);
        layoutEmpty  = findViewById(R.id.layoutEmpty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewHistory);
        btnBack      = (Button) findViewById(R.id.btnBack);
        btnDeleteAll = (Button) findViewById(R.id.btnDeleteAll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("전체 삭제")
                        .setMessage("모든 기록을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dbHelper.deleteAll();
                                load();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        List<WifiScanLog> logs = dbHelper.getAll();
        if (logs.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        HistoryAdapter adapter = new HistoryAdapter(logs);
        recyclerView.setAdapter(adapter);
    }

    // ── RecyclerView Adapter ──────────────────────────────
    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private List<WifiScanLog> items;

        public HistoryAdapter(List<WifiScanLog> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final WifiScanLog log = items.get(position);

            holder.tvSsid.setText(log.getSsid());
            holder.tvSecurityType.setText(log.getSecurityType());
            holder.tvCreatedAt.setText(log.getCreatedAt());
            holder.tvResultText.setText(log.getResultText());

            if (log.getScanType().equals("current")) {
                holder.tvScanType.setText("현재 분석");
            } else {
                holder.tvScanType.setText("주변 스캔");
            }

            int color;
            if (log.getResultText().equals(SecurityAnalyzer.GRADE_SAFE)) {
                color = Color.parseColor("#2E7D32");
            } else if (log.getResultText().equals(SecurityAnalyzer.GRADE_WARNING)) {
                color = Color.parseColor("#F57F17");
            } else {
                color = Color.parseColor("#C62828");
            }
            holder.tvResultText.setTextColor(color);
            holder.viewGradeBar.setBackgroundColor(color);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
                    intent.putExtra("log_id", log.getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSsid;
            TextView tvSecurityType;
            TextView tvCreatedAt;
            TextView tvScanType;
            TextView tvResultText;
            View     viewGradeBar;

            public ViewHolder(View itemView) {
                super(itemView);
                tvSsid         = (TextView) itemView.findViewById(R.id.tvSsid);
                tvSecurityType = (TextView) itemView.findViewById(R.id.tvSecurityType);
                tvCreatedAt    = (TextView) itemView.findViewById(R.id.tvCreatedAt);
                tvScanType     = (TextView) itemView.findViewById(R.id.tvScanType);
                tvResultText   = (TextView) itemView.findViewById(R.id.tvResultText);
                viewGradeBar   = itemView.findViewById(R.id.viewGradeBar);
            }
        }
    }
}