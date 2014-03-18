package edu.berkeley.cs.amplab.carat.android.protocol;

import java.util.Map;

public class Action {

	private String name;
	private String username;
	private Map<String,String> options;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Map<String,String> getOptions() {
		return options;
	}

	public void setOptions(Map<String,String> options) {
		this.options = options;
	}

	
	
}

