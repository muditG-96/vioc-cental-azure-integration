package com.valvoline.integration.services;

public interface AzureServices {
	
	public String fetchUserStory(String workItemId);
	public void processUserStory(String testCaseUrl);
	

}
