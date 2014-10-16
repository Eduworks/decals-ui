package com.eduworks.decals.ui.client.util;

import java.util.ArrayList;

import com.eduworks.decals.ui.client.model.InteractiveSearchResult;
import com.eduworks.decals.ui.client.model.InteractiveSearchResultSetReturn;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * Parses return data from the decalsSolrSearch web service.
 * 
 * @author Tom Buskirk
 *
 */
public class SolrCruncherResponseParser {
   
   private static final String SC_NUM_FOUND_KEY = "total";   
   private static final String SC_DOCS_KEY = "items";
   
   private static final String URL_KEY = "url";
   private static final String TITLE_KEY = "title";
   private static final String ID_KEY = "id";
   private static final String DESC_KEY = "description";
   private static final String AUTHOR_KEY = "author";
   private static final String LAST_MODIFIED_KEY = "last_modified";
   private static final String PUBLISHER_KEY = "publisher";
   private static final String SOURCE_KEY = "source";
   private static final String CREATED_DATE_KEY = "create_date";
   private static final String KEYWORDS_KEY = "keywords";  
   private static final String CONTENT_TYPE_KEY = "content_type";
   private static final String SCORE_KEY = "score";
   private static final String THUMBNAIL_KEY = "thumbnail";
   private static final String URL_STATUS_KEY = "url_status";
   
   //Transforms a JSON array of strings into an array list of strings.
   private static ArrayList<String> parseJSONStringArray(JSONArray jsa) {
      ArrayList<String> retList = new ArrayList<String>();
      for (int i=0;i<jsa.size();i++) {
         retList.add(jsa.get(i).isString().stringValue());         
      }
      return retList;
   }   
   
   //Try to remove JSON formatted data from the given title string.  Need to add some additions.
   private static String checkTitleStringForJsonContent(String title) {
      if (title != null && title.toLowerCase().startsWith("{\"content\"")) {
         try {
            JSONObject jo = JSONParser.parseStrict(title).isObject();            
            if (jo != null) return jo.get("content").toString();
            else return title;
         }
         catch (Exception e) {return title;}
      }
      else return title;
   }
   
   //Builds an InteractiveSearchResult object from the given JSON document
   private static InteractiveSearchResult parseSolrCruncherDoc(JSONObject doc, String thumbnailRootUrl) throws Exception {
      InteractiveSearchResult sr = new InteractiveSearchResult();      
      if (doc.containsKey(ID_KEY)) sr.setSolrId(doc.get(ID_KEY).isString().stringValue());
      if (doc.containsKey(TITLE_KEY)) sr.setTitle(checkTitleStringForJsonContent(doc.get(TITLE_KEY).isArray().get(0).isString().stringValue()));
      if (doc.containsKey(SOURCE_KEY)) sr.setSource(doc.get(SOURCE_KEY).isString().stringValue());
      if (doc.containsKey(DESC_KEY)) sr.setDescription(doc.get(DESC_KEY).isString().stringValue().replace("<br>", "; "));
      if (doc.containsKey(CONTENT_TYPE_KEY)) sr.setContentType(doc.get(CONTENT_TYPE_KEY).isString().stringValue());
      if (doc.containsKey(AUTHOR_KEY)) sr.setAuthor(doc.get(AUTHOR_KEY).isString().stringValue());
      if (doc.containsKey(PUBLISHER_KEY)) sr.setPublisher(doc.get(PUBLISHER_KEY).isString().stringValue());
      if (doc.containsKey(URL_KEY)) sr.setResourceUrl(doc.get(URL_KEY).isString().stringValue());
      if (doc.containsKey(KEYWORDS_KEY)) sr.setKeywords(parseJSONStringArray(doc.get(KEYWORDS_KEY).isArray()));
      if (doc.containsKey(CREATED_DATE_KEY)) sr.setCreatedDateStr(doc.get(CREATED_DATE_KEY).isString().stringValue());
      if (doc.containsKey(LAST_MODIFIED_KEY)) sr.setLastModifiedDateStr(doc.get(LAST_MODIFIED_KEY).isString().stringValue());   
      if (doc.containsKey(SCORE_KEY)) sr.setScore(String.valueOf(doc.get(SCORE_KEY).isNumber().doubleValue()));
      if (doc.containsKey(URL_STATUS_KEY)) sr.setUrlStatus(doc.get(URL_STATUS_KEY).isString().stringValue());
      if (doc.containsKey(THUMBNAIL_KEY)) {
         sr.setHasScreenshot(true);
         sr.setThumbnailImageUrl(thumbnailRootUrl + doc.get(THUMBNAIL_KEY).isString().stringValue());
      }
      else sr.setHasScreenshot(false);
      return sr;
   }
   
   
   /**
    * Builds and populates an InteractiveSearchResultSetReturn based on the given JSON data and thumbnailRootUrl.
    * 
    * @param responseObject  The response from the decalsSolrSearch web service.
    * @param thumbnailRootUrl  The root URL to use for thumbnail images.
    * @return Returns an InteractiveSearchResultSetReturn built from the given JSON data
    */
   public static InteractiveSearchResultSetReturn parseSolrCruncherResponse(JSONObject responseObject, String thumbnailRootUrl) {      
      InteractiveSearchResultSetReturn ret =  new InteractiveSearchResultSetReturn();
      parseSolrCruncherResponse(responseObject,ret,thumbnailRootUrl);
      return ret;
   }
   
   /**
    * Parses the given JSON data and appends the results to the given InteractiveSearchResultSetReturn
    * 
    * @param responseObject The response from the decalsSolrSearch web service.
    * @param resultSet The result set in which to append new results
    * @param thumbnailRootUrl The root URL to use for thumbnail images.
    */
   public static void parseSolrCruncherResponse(JSONObject responseObject, InteractiveSearchResultSetReturn resultSet, String thumbnailRootUrl) {      
      resultSet.setNumTotalResultsFound((long)responseObject.get(SC_NUM_FOUND_KEY).isNumber().doubleValue());      
      JSONArray ra = responseObject.get(SC_DOCS_KEY).isArray();
      for (int i=0;i<ra.size();i++) {
         try {
            resultSet.getSearchResults().add(parseSolrCruncherDoc(ra.get(i).isObject(),thumbnailRootUrl));            
         }
         catch (Exception e) {
            //Window.alert("Error parsing item: " + i + " : " + e.toString());
         }  
      }      
   }


}
