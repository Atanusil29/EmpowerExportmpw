package com.empower.app.EmpowerRestTemplate2.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class EmpoerController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/GetOTDSToken")
	public String getOTDSToken(){
		
		System.out.println("Request access token");
		String otdsUrl = "https://win-v8tbqs6gv8i:8443/otdsws/login";
		HttpHeaders headers = new HttpHeaders();
		/*String plainCreds = "empadmin:goal@2020";
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		headers.add("Authorization", "Basic " + base64Creds);*/
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("grant_type", "password");
		map.add("username", "empadmin");
		map.add("password", "goal@2020");
		map.add("client_id", "Empower");
		map.add("client_secret", "vQ1n9bik6ZLJ4GcpjZc2Jlv25DjJRQom");
		map.add("scope", "resource:EmpowerRESTAPI");
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		//HttpEntity<String> entity = new HttpEntity<String>("requestJson", headers);
		ResponseEntity<String> response = restTemplate.exchange(otdsUrl, HttpMethod.POST, request, String.class);
		//System.out.println(response.getBody());
		JSONObject jsonObject = new JSONObject(response.getBody());
		System.out.println("Access_Token: "+jsonObject.getString("access_token"));
		
		System.out.println("Request XSRF-TOKEN");
		String getTokenUrl = "https://win-v8tbqs6gv8i:8090/mpw/resource/GetToken";
		HttpHeaders xsrfHeaders = new HttpHeaders();
		xsrfHeaders.setContentType(MediaType.APPLICATION_JSON);
		xsrfHeaders.set("Authorization", "Bearer "+jsonObject.getString("access_token"));
		HttpEntity<MultiValueMap<String, String>> xsrfEntity = new HttpEntity<MultiValueMap<String, String>>(map, xsrfHeaders);
		ResponseEntity<String> result = restTemplate.exchange(getTokenUrl, HttpMethod.GET, xsrfEntity, String.class);
		JSONObject jsonObjectxsrf = new JSONObject(result.getBody());
		String temp = result.getHeaders().get("Set-Cookie").get(0);
		System.out.println("All jsession: "+temp);
		int endIndex = temp.toString().indexOf(";");
		String jsessionId = temp.toString().substring(11, endIndex);
		System.out.println("JSESSIONID is: " + temp.toString().substring(11, endIndex) + "\n");
		//System.out.println("Access_Token: "+jsonObjectxsrf.getJSONObject("body").get("csrfToken"));
		String csrfToken = jsonObjectxsrf.getJSONObject("body").getString("csrfToken");
		System.out.println("csrfToken : "+csrfToken);
		
		System.out.println("Get the document ID");
		String getDocUrl = "https://win-v8tbqs6gv8i:8090/mpw/resource/documents?userId=strstenantadmin&sortField=importDate&limit=2&offset=0&deleted=false";
		HttpHeaders docHeaders = new HttpHeaders();
		docHeaders.setContentType(MediaType.APPLICATION_JSON);
		docHeaders.set("Authorization", "Bearer "+jsonObject.getString("access_token"));
		HttpEntity<MultiValueMap<String, String>> docEntity = new HttpEntity<MultiValueMap<String, String>>(map, docHeaders);
		ResponseEntity<String> resultDoc = restTemplate.exchange(getDocUrl, HttpMethod.GET, docEntity, String.class);
		JSONObject jsonObjectdoc = new JSONObject(resultDoc.getBody());
		System.out.println("Doc token: "+jsonObjectdoc.getJSONObject("body").get("documents"));
		//String docDetails = jsonObjectdoc.getJSONObject("body").getJSONArray("documents").toString();
		//docIDTemp.toList();
		
		//ParseJSON jsonParsing = new ParseJSON();
		//ParseJSON.parseJSONForDocumentParameters(docDetails, "docId");
		ArrayList<String> documentId = ParseJSON.parseJSONForDocumentParameters(resultDoc.getBody(), "docId");
		System.out.println("docIDs are: "+ParseJSON.parseJSONForDocumentParameters(resultDoc.getBody(), "docId"));
		
		for(int i=0;i<=documentId.size()-1;i++){
			//Export the document
			String exportURL = "https://win-v8tbqs6gv8i:8090/mpw/resource/documents/"+documentId.iterator().next()+"/export";
			System.out.println("Export URL is: "+exportURL);
			HttpHeaders exportHeader = new HttpHeaders();
			exportHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			//exportHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			exportHeader.add("Cookie", "JSESSIONID="+jsessionId);
			exportHeader.add("X-CSRF-TOKEN", csrfToken);
			exportHeader.setContentDispositionFormData("Content-Disposition", "attachment; filename=CustomerLetter.mpw");
			MultiValueMap<String, String> exportMap = new LinkedMultiValueMap<String, String>();
			exportMap.set("preserveDoc", "true");
			HttpEntity<MultiValueMap<String, String>> exportDocRequest = new HttpEntity<MultiValueMap<String, String>>(exportMap, exportHeader);
			//HttpEntity<String> entity = new HttpEntity<String>("requestJson", headers);
			//ResponseEntity<String> exportDocumentresponse = restTemplate.exchange(exportURL, HttpMethod.POST, exportDocRequest, String.class);
			ResponseEntity<byte[]> exportDocumentresponse = restTemplate.exchange(exportURL, HttpMethod.POST, exportDocRequest, byte[].class);
			try {
				Files.write(Paths.get("C:\\MySpace\\Empower\\"+ documentId.get(i) +".mpw"), exportDocumentresponse.getBody(),StandardOpenOption.CREATE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	return "Exported Documents: "+documentId;	
//		return exportDocumentresponse.toString();	
	}
	
}