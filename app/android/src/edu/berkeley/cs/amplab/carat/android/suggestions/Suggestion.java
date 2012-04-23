package edu.berkeley.cs.amplab.carat.android.suggestions;

import android.graphics.drawable.Drawable;

public class Suggestion {
	private String name = "";
	private String action = "";
	private String benefit = "";
	private String type = "";
	private Drawable icon = null;

	public Suggestion(String action, String name, String type) {
		this.action = action;
		this.name = name;
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public String getAppName() {
		return name;
	}

	public void setBenefit(String benefit) {
		this.benefit = benefit;
	}

	public String getBenefit() {
		return benefit;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public Drawable getIcon() {
		return this.icon;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
