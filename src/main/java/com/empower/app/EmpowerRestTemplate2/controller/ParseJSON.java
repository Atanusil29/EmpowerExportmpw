package com.empower.app.EmpowerRestTemplate2.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ParseJSON {
	public static ArrayList<String> parseJSONForDocumentParameters(String jsonInput, String key) {
		// Creating a JSONParser object
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject, jsonObjectHeader, jsonObjectBody;
//		String paramValue = null;
		ArrayList<String> paramValues = null;
		try {
			// Parsing the contents of the JSON from String/File
			if (jsonInput != null)
				jsonObject = (JSONObject) jsonParser.parse(jsonInput);
			else
				jsonObject = (JSONObject) jsonParser.parse(new FileReader("C:/temp/test3.json"));

//	         System.out.println("Contents of the JSON are: " + jsonObject.toJSONString());
			jsonObjectHeader = (JSONObject) jsonObject.get("header");
//	         System.out.println("header:" + jsonObjectHeader);
			jsonObjectBody = (JSONObject) jsonObject.get("body");
//	         System.out.println("body:" + jsonObjectBody);

			// Retrieving the array
			JSONArray jsonArrayDocuments = (JSONArray) jsonObjectBody.get("documents");
//	         System.out.println("documents:" + jsonArrayDocuments);
//			paramValue = getParameterValueFromDocuments(jsonArrayDocuments, key);
			paramValues = getParameterValueFromDocuments(jsonArrayDocuments, key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return paramValues;
	}

	public static ArrayList<String> getParameterValueFromDocuments(JSONArray jsonArrayDocuments, String key) {
		ArrayList<String> params = new ArrayList<String>();
		// Iterating the contents of the array
		Iterator<JSONObject> iterator = jsonArrayDocuments.iterator();
		while(iterator.hasNext()){
			params.add((String) iterator.next().get(key));
		}
		return params;
	}

}
