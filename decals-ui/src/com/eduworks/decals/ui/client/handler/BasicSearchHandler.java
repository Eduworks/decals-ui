package com.eduworks.decals.ui.client.handler;

import java.util.ArrayList;

import com.eduworks.decals.ui.client.model.BasicSearchResult;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.decals.ui.client.util.SearchResultWidgetGenerator;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Performs basic search against free.ed.gov.
 * 
 * @author Tom Buskirk
 *
 */
public class BasicSearchHandler extends SearchHandler{
   
   public static final String BASIC_SEARCH_HOST = "72.243.185.28";   
   public static final String THUMBNAIL_HOST = "free.ed.gov";
   //private static final String DEFAULT_SEARCH_ERROR = "Could not obtain search results for: ";     
   private static final int VALID_RESPONSE_CODE = 200;
   private static final String TOTAL_RESULTS_KEY = "count";
   private static final String RESULTS_KEY = "data";
   private static final int SHOW_MORE_RESULTS_TRIGGER = 25;
   
   private static final String SHOW_MORE_RESULTS_ID_SUFFIX = "-bsh";
   
   protected int currentPage;
   
   @Override
   protected String getShowMoreResultsIdSuffix() {return SHOW_MORE_RESULTS_ID_SUFFIX;}
      
   protected EventCallback showMoreResultsListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         currentPage++;         
         hideShowMoreResultsButton();
         showShowMoreResultsBusyImage();
         fetchBasicSearchResults(buildSearchUrl(),false);         
      }
   };
   
   @Override
   protected void addShowMoreResultsHandler() {
      PageAssembler.attachHandler(showMoreResultsButtonId,Event.ONCLICK,showMoreResultsListener);
   }
   
   //build the search counter statement
   private String getSearchCounterStatement(JSONObject ro) {
      long totalNumberOfResults = Long.parseLong(ro.get(TOTAL_RESULTS_KEY).toString());
      return "\"" + searchTerm +  "\" - " + counterFormat.format(totalNumberOfResults) + " results found";
   }
   
   
   //parses the results array into a list of basic search results
   private ArrayList<BasicSearchResult> parseResultsArray(JSONArray resultsArray) {
      ArrayList<BasicSearchResult> resultsList = new ArrayList<BasicSearchResult>();
      for (int i=0;i<resultsArray.size();i++) resultsList.add(new BasicSearchResult((JSONObject)resultsArray.get(i)));
      return resultsList;
   }
   
   //populate the search results
   private void populateBasicResults(JSONObject ro, boolean isNewSearch) {      
      hideShowMoreResultsDiv();
      JSONArray resultsArray = (JSONArray)ro.get(RESULTS_KEY);
      if (isNewSearch) DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));      
      setCounterContainerDisplay(true);
      setSearchBusyDisplay(false);      
      DsUtil.setLabelText(counterElementId, getSearchCounterStatement(ro));
      SearchResultWidgetGenerator srwg = new SearchResultWidgetGenerator();
      srwg.addBasicSearchResultWidgets(parseResultsArray(resultsArray),parentElementId,widgetText);      
      if (resultsArray.size() >= SHOW_MORE_RESULTS_TRIGGER) addShowMoreResultsButton();
   }
   
   //attempt to fetch results from free.ed.gov
   private void fetchBasicSearchResults(String urlString, final boolean isNewSearch) {      
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,urlString);
      final long currentSyncId = syncId;
      try {         
         if (isNewSearch) {
            DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
            setCounterContainerDisplay(false);
            setSearchBusyDisplay(true);       
         } 
         builder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
               if (currentSyncId == syncId) showSearchError("The search request has timed out.");
            }
            @Override
            public void onResponseReceived(Request request, Response response) {
               //make sure there is no race condition...if so, ignore the results.
               if (currentSyncId == syncId) {
                  if (VALID_RESPONSE_CODE == response.getStatusCode()) populateBasicResults(DsUtil.parseJson(response.getText()),isNewSearch);
                  else if (response.getStatusCode() == 0) showSearchError("The search request has timed out.");
                  else showSearchError("Received an invalid response code from the search server  (" + response.getStatusCode() + "): " + response.getStatusText());
               }               
            }       
         });
      } 
      catch (RequestException e) {
         if (currentSyncId == syncId) showSearchError("Could not connect to the search server.");
      }         
   } 
   
   //builds the free.ed.gov search URL
   private String buildSearchUrl() {
      return "http://" + BASIC_SEARCH_HOST + "/search?page=" + currentPage + "&terms=" + URL.encode(searchTerm);      
   }
   
   /**
    * Performs a basic search against free.ed.gov and populates the given element with the results.
    * 
    * @param page The starting search page.
    * @param searchTerm The non URL encoded search term. (URL encoding is done internally).
    * @param parentElementId The element ID to attach the results.
    * @param widgetText The widget text to use for the results.
    * @param counterElementId The element ID of the search counter.
    * @param counterContainerElementId The element ID of the search counter container.
    * @param searchBusyElementId The element ID of the busy indicator.
    */
   public void performBasicSearch(int page, String searchTerm, String parentElementId, String widgetText, String counterElementId, 
                                  String counterContainerElementId, String searchBusyElementId) {
      DsUtil.sendTrackingMessage("Performed basic search for \"" + searchTerm + "\"");
      this.parentElementId = parentElementId;
      this.widgetText = widgetText;   
      this.counterElementId = counterElementId;
      this.searchTerm = searchTerm;
      this.counterContainerElementId = counterContainerElementId;
      this.searchBusyElementId = searchBusyElementId;
      this.currentPage = page;
      showMoreResultsOuterDivId = null;
      showMoreResultsButtonId = null;  
      showMoreResultsBusyId = null;
      syncId++;
      fetchBasicSearchResults(buildSearchUrl(),true);     
   }

}
