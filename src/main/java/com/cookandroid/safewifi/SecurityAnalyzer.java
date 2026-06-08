package com.cookandroid.safewifi;

public class SecurityAnalyzer {

    public static final String GRADE_SAFE    = "안전";
    public static final String GRADE_WARNING = "주의";
    public static final String GRADE_DANGER  = "위험";

    public static final String SEC_OPEN = "OPEN";
    public static final String SEC_WEP  = "WEP";
    public static final String SEC_WPA  = "WPA";
    public static final String SEC_WPA2 = "WPA2";
    public static final String SEC_WPA3 = "WPA3";

    public static String parseSecurityType(String capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return SEC_OPEN;
        }
        if (capabilities.contains("WPA3")) {
            return SEC_WPA3;
        }
        if (capabilities.contains("WPA2")) {
            return SEC_WPA2;
        }
        if (capabilities.contains("WPA")) {
            return SEC_WPA;
        }
        if (capabilities.contains("WEP")) {
            return SEC_WEP;
        }
        return SEC_OPEN;
    }

    public static int calcRiskScore(String secType, int rssi) {
        int base = 50;
        if (secType.equals(SEC_OPEN)) {
            base = 90;
        } else if (secType.equals(SEC_WEP)) {
            base = 70;
        } else if (secType.equals(SEC_WPA)) {
            base = 40;
        } else if (secType.equals(SEC_WPA2)) {
            base = 15;
        } else if (secType.equals(SEC_WPA3)) {
            base = 5;
        }

        int penalty = 0;
        if (rssi < -80) {
            penalty = 10;
        } else if (rssi < -70) {
            penalty = 5;
        }

        int score = base + penalty;
        if (score > 100) {
            score = 100;
        }
        return score;
    }

    public static String getGrade(int score) {
        if (score <= 30) {
            return GRADE_SAFE;
        } else if (score <= 60) {
            return GRADE_WARNING;
        } else {
            return GRADE_DANGER;
        }
    }

    public static String getRiskDescription(String secType) {
        if (secType.equals(SEC_OPEN)) {
            return "⚠️ 암호화 없는 오픈 네트워크 — 데이터 도청에 매우 취약합니다.";
        } else if (secType.equals(SEC_WEP)) {
            return "⚠️ WEP 방식은 취약 — 수 분 내 해킹이 가능합니다.";
        } else if (secType.equals(SEC_WPA)) {
            return "🔶 WPA 방식은 WPA2보다 보안 수준이 낮습니다.";
        } else if (secType.equals(SEC_WPA2)) {
            return "✅ WPA2는 현재 표준 암호화 방식 — 비교적 안전합니다.";
        } else if (secType.equals(SEC_WPA3)) {
            return "✅ WPA3 최신 표준 — 가장 안전한 방식입니다.";
        } else {
            return "암호화 방식을 확인할 수 없습니다.";
        }
    }

    public static String getSignalDescription(int rssi) {
        if (rssi >= -55) {
            return "매우 강함 (" + rssi + " dBm)";
        } else if (rssi >= -65) {
            return "강함 (" + rssi + " dBm)";
        } else if (rssi >= -75) {
            return "보통 (" + rssi + " dBm)";
        } else if (rssi >= -85) {
            return "약함 (" + rssi + " dBm)";
        } else {
            return "매우 약함 (" + rssi + " dBm)";
        }
    }
}