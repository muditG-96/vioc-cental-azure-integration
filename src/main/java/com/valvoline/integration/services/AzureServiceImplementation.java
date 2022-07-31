package com.valvoline.integration.services;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.valvoline.integration.constants.Constants;
import com.valvoline.integration.model.GherkinSteps;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AzureServiceImplementation implements AzureServices{

	
	@Value("${userStory.url}")
	private String userStoryUrl;
	
	@Value("${userStory.queryParam}")
	private String queryParam;
	
	@Value("${testCase.queryParam}")
	private String queryParamTestCase;
	
	private static String encodedPAT=null;
	
	private static String pat_token=null;
	
	private final static Logger LOGGER=LoggerFactory.getLogger(AzureServiceImplementation.class);
	
	@Override
	public String fetchUserStory(String workItemId) {
		
        Base64 base64 = new Base64();
        StringBuffer content = new StringBuffer();
        try {
        	if(null==encodedPAT && null==pat_token) {
        		pat_token=":"+System.getenv(Constants.PATTOKEN);
        		encodedPAT = new String(base64.encode(pat_token.getBytes()));
        	}
        URL url;
        url = new URL(userStoryUrl+workItemId+queryParam);
        HttpURLConnection con;
        con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty(Constants.AUTHORIZATION, "Basic "+encodedPAT);
        int status = con.getResponseCode();
        LOGGER.info("Connection STatus:"+status);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        
        while ((inputLine=in.readLine()) != null) {
        content.append(inputLine);
        } 
        
        }catch(IOException e){
        	e.printStackTrace();
        }
		return content.toString();
	}

	@Override
	public void processUserStory(String userStoryData) {
		
		JSONObject obj = new JSONObject (userStoryData);
		JSONArray relation = obj.getJSONArray (Constants.RELATIONS);
		for (int i = 0; i < relation.length(); i++) {

		String relationType = relation.getJSONObject(i).getJSONObject(Constants.ATTRIBUTES).getString(Constants.NAME);
			if (Constants.TESTS.equalsIgnoreCase(relationType) || Constants.TESTEDBY.equalsIgnoreCase(relationType)) {
				String testCaseData=fetchTestCase(relation.getJSONObject(i).getString(Constants.URL)+queryParamTestCase);
				createGherkinObject(testCaseData);
			}
		}
	}
	
	private String fetchTestCase(String testCaseUrl) {
		
		LOGGER.info("Test Case url to be called :"+testCaseUrl);
        StringBuffer content = new StringBuffer();
        try {
        URL url;
        url = new URL(testCaseUrl);
        HttpURLConnection con;
        con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty(Constants.AUTHORIZATION, "Basic "+encodedPAT);
        int status = con.getResponseCode();
        LOGGER.info("Connection Status:"+status);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        
        while ((inputLine=in.readLine()) != null) {
        content.append(inputLine);
        } 
        
        }catch(IOException e){
        	e.printStackTrace();
        }
        return content.toString();
	}
	private void createGherkinObject(String testCaseData) {
		
		//Converts the XML steps present in response to an Object
		
		JSONObject obj = new JSONObject(testCaseData);
		if(Constants.TESTCASE.equalsIgnoreCase(obj.getJSONObject (Constants.FIELDS).getString(Constants.WORKITEM_TYPE))) {
		String gherkinSteps= obj.getJSONObject (Constants.FIELDS).getString(Constants.STEPS);
		String testCaseTitle= obj.getJSONObject (Constants.FIELDS).getString(Constants.TITLE);
		int testCaseId= obj.getInt(Constants.ID);
		LOGGER.info("Test Case ID {}",testCaseId);
		LOGGER.info("Test Case Title {}",testCaseTitle);
		String newGherkin=gherkinSteps.replaceAll("\\\\", "")
				.replaceAll("&lt;DIV&gt;", "")
				.replaceAll("&lt;DIV&gt;&lt;P&gt;","")
				.replaceAll("&lt;DIV&gt;&lt;DIV&gt;&lt;P&gt;","")
				.replaceAll("&lt;P&gt;","")
				.replaceAll("&lt;/P&gt;","")
				.replaceAll("&lt;/DIV&gt;", "")
				.replaceAll("&lt;DIV&gt;&lt;P&gt;&lt;BR/&gt;&lt;/P&gt;&lt;/DIV&gt;", "")
				.replaceAll("&lt;/P&gt;&lt;/DIV&gt;", "")
				.replaceAll("&lt;BR/&gt;", "")
				.replaceAll("&lt;BR/&gt;&lt;/P&gt;","")
				.replaceAll("&lt;BR/&gt;&lt;/P&gt;&lt;/DIV&gt;","")
				.replaceAll("&lt;BR/&gt;&lt;/P&gt;&lt;/DIV&gt;&lt;/DIV&gt;","");

				//LOGGER.info("Gherkin Steps converted {}",newGherkin);
				StringReader sr = new StringReader(newGherkin);
				XmlMapper mapper = new XmlMapper();
				GherkinSteps gherkinObject=new GherkinSteps();
				try {
					gherkinObject = mapper.readValue(sr,GherkinSteps.class);
				} catch (StreamReadException e) {
					
					e.printStackTrace();
				} catch (DatabindException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				createFeatureFile(gherkinObject,testCaseTitle,testCaseId);
		}
	}
	
	private void createFeatureFile(GherkinSteps gherkinObject, String testCaseTitle,int testCaseId) {
		
		//TO DO This logic needs to be replaced by AWS S3 Bucket
		try {
		      File myObj = new File("C:\\Mudit\\ReachPartner_Data\\UserStory\\TestCase\\"+testCaseId+".txt");
		      if (myObj.createNewFile()) {
		        System.out.println("File created: " + myObj.getName());
		      } else {
		        System.out.println("File already exists.");
		      }
		      FileWriter myWriter = new FileWriter(myObj);
		      myWriter.write(Constants.HEADER1+testCaseId+"\n");
		      myWriter.write(Constants.HEADER2+testCaseId+Constants.UNDERSCORE+testCaseTitle+"\n");
		      gherkinObject.step.forEach(
		    		  (step)-> {
						try {
							myWriter.write(step.parameterizedString.get(0)+"\n");
							if(Constants.VALIDATESTEP.equalsIgnoreCase(step.type)) {
								myWriter.write(step.parameterizedString.get(1)+"\n");
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
				  
		      myWriter.close();
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
	}
}
