package com.valvoline.integration.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.valvoline.integration.constants.Constants;
import com.valvoline.integration.services.AzureServices;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AzureController {
	
	@Autowired
	AzureServices azureServices;
	
	private final static Logger LOGGER=LoggerFactory.getLogger(AzureController.class);
	
	@PostMapping(value="/qaautomation/azurecard")
	public ResponseEntity<String> fetchAzureCard(@RequestBody String webHookRequest){
		
		LOGGER.info("Request received from webHook is {}",webHookRequest);
		JSONObject obj = new JSONObject(webHookRequest);
		String workItemId= obj.getJSONObject(Constants.RESOURCE).getString(Constants.WORKITEMID);
		fetchUserStory(workItemId);
		return ResponseEntity.ok("received");
		
	}
	private void fetchUserStory(String workItemId) {
		
		String userStoryData=azureServices.fetchUserStory(workItemId);
		LOGGER.info("User Story Response received is :"+userStoryData);
		JSONObject obj = new JSONObject(userStoryData);
		String boardColumn= obj.getJSONObject (Constants.FIELDS).getString(Constants.BOARDCOLUMN_FIELDS);
		
		if (Constants.UAT_BOARCOLUMN.equalsIgnoreCase(boardColumn)) {
			LOGGER.info("User Story "+workItemId+" board columns is :"+boardColumn);
			azureServices.processUserStory(userStoryData);
		}
	}

}
