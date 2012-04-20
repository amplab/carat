package edu.berkeley.cs.amplab.carat.android.suggestions;

public class Suggestion {
	private String name = "";
	private String benefit = "";

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return "Kill " + name;
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
}
