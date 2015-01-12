package edu.berkeley.cs.amplab.carat.android.model_classes;

public class MyDeviceData {
	private long lastReportsTimeMillis;
	private long freshnessHours;
	private long freshnessMinutes;
	private String caratId;
	private String batteryLife;

	public MyDeviceData() {
	}

	public void setAllFields(long lastReportsTimeMillis, long freshnessHours, long freshnessMinutes, String caratId, String batteryLife) {
		this.lastReportsTimeMillis = lastReportsTimeMillis;
		this.freshnessHours = freshnessHours;
		this.freshnessMinutes = freshnessMinutes;
		this.caratId = caratId;
		this.batteryLife = batteryLife;
	}
	
	public long getLastReportsTimeMillis() {
		return lastReportsTimeMillis;
	}

	public void setLastReportsTimeMillis(long lastReportsTimeMillis) {
		this.lastReportsTimeMillis = lastReportsTimeMillis;
	}

	public long getFreshnessHours() {
		return freshnessHours;
	}

	public void setFreshnessHours(long freshnessHours) {
		this.freshnessHours = freshnessHours;
	}

	public long getFreshnessMinutes() {
		return freshnessMinutes;
	}

	public void setFreshnessMinutes(long freshnessMinutes) {
		this.freshnessMinutes = freshnessMinutes;
	}

	public String getCaratId() {
		return caratId;
	}

	public void setCaratId(String caratId) {
		this.caratId = caratId;
	}

	public String getBatteryLife() {
		return batteryLife;
	}

	public void setBatteryLife(String batteryLife) {
		this.batteryLife = batteryLife;
	}
}