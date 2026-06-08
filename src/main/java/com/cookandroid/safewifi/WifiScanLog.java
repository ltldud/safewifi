package com.cookandroid.safewifi;

public class WifiScanLog {

    private int    id;
    private String scanType;
    private String ssid;
    private String securityType;
    private int    riskScore;
    private String resultText;
    private String createdAt;

    public WifiScanLog() {}

    public WifiScanLog(String scanType, String ssid, String securityType,
                       int riskScore, String resultText, String createdAt) {
        this.scanType     = scanType;
        this.ssid         = ssid;
        this.securityType = securityType;
        this.riskScore    = riskScore;
        this.resultText   = resultText;
        this.createdAt    = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }

    public String getSecurityType() { return securityType; }
    public void setSecurityType(String securityType) { this.securityType = securityType; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getResultText() { return resultText; }
    public void setResultText(String resultText) { this.resultText = resultText; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}