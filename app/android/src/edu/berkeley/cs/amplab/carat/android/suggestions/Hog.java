package edu.berkeley.cs.amplab.carat.android.suggestions;

import edu.berkeley.cs.amplab.carat.android.R;

public class Hog {
	private String name = "";
	private double confidence = 0.0;
	private int iconResource = R.drawable.ic_launcher;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getConfidence() {
		return confidence;
	}
	
	public void setIconResource(int icon) {
		this.iconResource = icon;
	}

	public int getIconResource() {
		return iconResource;
	}
	
}
