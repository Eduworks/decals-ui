package com.eduworks.decals.ui.client.api;

import java.util.ArrayList;

import com.eduworks.gwt.client.net.CommunicationHub;
import com.eduworks.gwt.client.net.MultipartPost;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;

/**
 * DECALS specific API for levr scripts/ESB.
 * 
 * @author Tom Buskirk
 *
 */
public class DsESBApi extends ESBApi {
   
   //Turns an array list of strings into a levr recognized array of strings
   private static String buildQueryTerms(ArrayList<String> queryTerms) {
      StringBuffer sb = new StringBuffer();
      sb.append("[");
      for (int i=0;i<queryTerms.size();i++) {
         sb.append(queryTerms.get(i));
         if ((i + 1) !=queryTerms.size()) sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
   }
   
   /**
    * Perform a basic search
    * @param searchTerm The search term
    * @param rows The number of rows to return
    * @param page The page to start retrieval
    * @return Returns the query result JSON string
    */
   public static String decalsBasicSearch(String searchTerm, int rows, int page, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("searchTerm", searchTerm);
      jo.put("itemsPerPage", rows);
      jo.put("page", page);
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsBasicSearch"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Perform a SOLR search.
    * 
    * @param solrUrl The URL of the SOLR instance
    * @param query The SOLR query
    * @param rows The number of rows to return
    * @param returnFields The fields to retrieve
    * @param mustMatchAll The must match all flag (if set to yes, then all terms in the query must be matched 100%)
    * @param start The record to start retrieval
    * @param callback The event callback
    * @return Returns the query result JSON string
    */
   public static String decalsSolrSearch(String solrUrl, String query, String rows, String returnFields, boolean mustMatchAll, int start, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("solrUrl", solrUrl);
      jo.put("query", query);
      jo.put("rows", rows);
      jo.put("returnFields", returnFields);
      jo.put("idSort", "false");
      jo.put("useCursor", "false");
      jo.put("useMustMatchAll", String.valueOf(mustMatchAll));
      jo.put("start", start);
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsSolrQuery"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Performs a query count for all queries in the queryTerms list
    * 
    * @param solrUrl The URL of the SOLR instance
    * @param queryTerms The list of SOLR queries
    * @param mustMatchAll The must match all flag (if set to yes, then all terms in the query must be matched 100%)
    * @param callback The event callback
    * @return Returns the query result count JSON string
    */
   public static String decalsSolrQueryCounts(String solrUrl, ArrayList<String> queryTerms, boolean mustMatchAll, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("solrUrl", solrUrl);
      jo.put("terms", buildQueryTerms(queryTerms));
      jo.put("useMustMatchAll", String.valueOf(mustMatchAll));
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsSolrQueryCounts"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Returns all nouns for all text in the text list
    * 
    * @param textList The list of texts for which to retrieve the nouns
    * @param callback The event callback
    * @return Returns the nouns result JSON string
    */
   public static String decalsGetTextNouns(ArrayList<String> textList, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("text",buildQueryTerms(textList));
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsGetTextNouns"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Returns the nouns and keywords for the given text.
    * 
    * @param text The text for which to find the nouns and keywords.
    * @param callback The event callback
    * @return Returns the nouns and keywords for the given text as a JSON string
    */
   public static String decalsGetTextHighlights(String text, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("text",text);
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsGetTextHighlights"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Returns the word ontology/definitions/relationships for the given word.
    * 
    * @param word The word to define
    * @param levels The number of relationship levels to search
    * @param callback The even callback
    * @return Returns the word ontology/definitions/relationships for the given word as a JSON string
    */
   public static String decalsDefineWord(String word, int levels, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("word",word);
      jo.put("levels",String.valueOf(levels));
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsDefineWord"),
                                 mp, 
                                 false, 
                                 callback);
   }

   /**
    * Returns a set of Wikipedia related information for the page with the given title.
    * 
    * @param title The (approximate) title of the wikipedia page to search 
    * @param callback The event callback
    * @return Returns a set of Wikipedia related information for the page with the given title as a JSON string.
    */
   public static String decalsWikiInfo(String title, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("title",title);
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsWikiInfo"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   /**
    * Adds a tracking statement to the default levr database with the given information.
    * 
    * @param assignmentId The user/session assignment ID
    * @param message The message to track
    * @param callback The event callback
    * @return Tracking update confirmation as a JSON string.
    */
   public static String decalsAddTracking(String assignmentId, String message, ESBCallback<ESBPacket> callback) {
      MultipartPost mp = new MultipartPost();
      ESBPacket jo = new ESBPacket();
      jo.put("assignmentId",assignmentId);
      jo.put("message",message);
      mp.appendMultipartFormData("decalsData", jo);
      return CommunicationHub.sendMultipartPost(getESBActionURL("decalsAddTracking"),
                                 mp, 
                                 false, 
                                 callback);
   }
   
   
}
