package com.eduworks.decals.ui.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.eduworks.decals.ui.client.model.BasicSearchResult;
import com.eduworks.decals.ui.client.model.InteractiveSearchResult;
import com.eduworks.decals.ui.client.model.SearchResult;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * Generates search results widgets.
 * 
 * @author Tom Buskirk
 *
 */
public class SearchResultWidgetGenerator {
   
   private static final String DEFAULT_WIDGET_ID_TOKEN = "x";
   
   private static final String THUMBNAIL_ANCHOR_SUFFIX = "-srThumbnailAnchor";
   private static final String THUMBNAIL_IMAGE_SUFFIX = "-srThumbnailImage";
   private static final String DETAILS_ANCHOR_SUFFIX = "-srDetailsAnchor";
   private static final String DETAILS_TITLE_SUFFIX = "-srDetailsTitle";
   private static final String DETAILS_LINK_SUFFIX = "-srDetailsLink";
   private static final String DETAILS_DESC_SUFFIX = "-srDetailsDesc";
   private static final String DETAILS_SOURCE_SUFFIX = "-srDetailsSource";
   private static final String DETAILS_REDIRECT_WARNING_SUFFIX = "-srRedirectWarning";
   private static final String DETAILS_DESC_TOGGLE_SUFFIX = "-srDetailsToggle";
   private static final String DETAILS_DESC_TOGGLE_STATUS = "-srDetailsToggleStatus";
   
   private static final String DETAILS_SCORE_SUFFIX = "-srDetailsScore";
   private static final String DETAILS_CREATE_DATE_SUFFIX = "-srDetailsCreateDate";   
   
   private static final String DESC_FULL_STATUS = "full";
   private static final String DESC_SHORT_STATUS = "short";
   
   private static final String SHOW_MORE_DESC = "More...";
   private static final String SHOW_LESS_DESC = "Less...";
   
   private static final String DEFAULT_THUMBNAIL_URL = "images/thumbnail-search-result.jpg";
   private static final String DEFAULT_BOOKSHARE_THUMBNAIL_URL = "images/bookShare-default-tn.png";
   
   private static final String BOOKSHARE_URL = "www.bookshare.org";
   
   private HashMap<String,String> shortDescMap = new HashMap<String,String>();
   private HashMap<String,String> fullDescMap = new HashMap<String,String>();
   
   //Retrieve the current token from the element ID
   private String getTokenFromElementId(String elementId, String suffix) {
      return(elementId.substring(0,elementId.indexOf(suffix)));
   }
   
   //Determines if the given URL is from bookshare.org   
   private boolean isBookshareResource(String url) {
      if (url == null ||  url.trim().isEmpty()) return false;
      else return url.indexOf(BOOKSHARE_URL) > -1;
   }
   
   //Listener for description toggle
   protected EventCallback descToggleListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         Element e = Element.as(event.getEventTarget());
         String token = getTokenFromElementId(e.getId(),DETAILS_DESC_TOGGLE_SUFFIX);
         if (DESC_SHORT_STATUS.equals(DsUtil.getLabelText(token + DETAILS_DESC_TOGGLE_STATUS))) {
            DsUtil.setLabelText(token + DETAILS_DESC_SUFFIX,fullDescMap.get(token + DETAILS_DESC_SUFFIX));
            DsUtil.setLabelText(token + DETAILS_DESC_TOGGLE_STATUS,fullDescMap.get(DESC_FULL_STATUS));
            DsUtil.setAnchorText(token + DETAILS_DESC_TOGGLE_SUFFIX, SHOW_LESS_DESC);            
         }
         else {
            DsUtil.setLabelText(token + DETAILS_DESC_SUFFIX, shortDescMap.get(token + DETAILS_DESC_SUFFIX));
            DsUtil.setLabelText(token + DETAILS_DESC_TOGGLE_STATUS, DESC_SHORT_STATUS);
            DsUtil.setAnchorText(token + DETAILS_DESC_TOGGLE_SUFFIX, SHOW_MORE_DESC);            
         }
      }
   };
   
   //Listens for errors retrieving thumbnail images
   protected EventCallback thumbnailErrorListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         Element e = Element.as(event.getEventTarget());
         ((Image)PageAssembler.elementToWidget(e.getId(),PageAssembler.IMAGE)).setUrl(DEFAULT_THUMBNAIL_URL);
      }
   };
   
   //Tries to replace apostrophe special characters with ASCII apostrophes
   private String cleanApostropheString(String str) {
      String temp = str;
      temp = temp.replaceAll("&#39;", "'");
      temp = temp.replaceAll("&#039;", "'");
      temp = temp.replaceAll("&#0039;", "'");
      temp = temp.replaceAll("&#34;", "\"");
      temp = temp.replaceAll("&#034;", "\"");
      temp = temp.replaceAll("&#0034;", "\"");
      return temp;
   }
   
   //Populates a widget element having the given widgetElementId with the appropriate values from the given search result.
    private void assignSearchResultWidgetValues(String widgetElementId, SearchResult sr) {
      if (widgetElementId.endsWith(THUMBNAIL_ANCHOR_SUFFIX) || widgetElementId.endsWith(DETAILS_ANCHOR_SUFFIX)) {
         ((Anchor)PageAssembler.elementToWidget(widgetElementId,PageAssembler.A)).setTarget(sr.getTitle());
         ((Anchor)PageAssembler.elementToWidget(widgetElementId,PageAssembler.A)).setHref(sr.getResourceUrl());
      }
      else if (widgetElementId.endsWith(THUMBNAIL_IMAGE_SUFFIX)) {
         String thumbnailUrl = null;
         if (sr.getHasScreenshot()) thumbnailUrl = sr.getThumbnailImageUrl();
         if (thumbnailUrl == null)  {
            if (isBookshareResource(sr.getResourceUrl())) thumbnailUrl = DEFAULT_BOOKSHARE_THUMBNAIL_URL;
            else thumbnailUrl = DEFAULT_THUMBNAIL_URL; 
         }
         PageAssembler.attachHandler(widgetElementId,Event.ONERROR,thumbnailErrorListener);
         ((Image)PageAssembler.elementToWidget(widgetElementId,PageAssembler.IMAGE)).setUrl(thumbnailUrl);         
      }
      else if (widgetElementId.endsWith(DETAILS_TITLE_SUFFIX)) DsUtil.setLabelText(widgetElementId, cleanApostropheString(sr.getTitle()));
      //else if (widgetElementId.endsWith(DETAILS_LINK_SUFFIX)) DsUtil.setLabelText(widgetElementId, sr.getResourceUrl());
      else if (widgetElementId.endsWith(DETAILS_LINK_SUFFIX)) DsUtil.setLabelText(widgetElementId, sr.getTruncatedResourceUrl());
      else if (widgetElementId.endsWith(DETAILS_DESC_SUFFIX)) {
         shortDescMap.put(widgetElementId, cleanApostropheString(sr.getShortDescription()));
         fullDescMap.put(widgetElementId, cleanApostropheString(sr.getDescription()));
         DsUtil.setLabelText(widgetElementId, cleanApostropheString(sr.getShortDescription()));                  
      }
      else if (widgetElementId.endsWith(DETAILS_SOURCE_SUFFIX)) {
         if (sr.getPublisher() != null && !sr.getPublisher().trim().isEmpty()) DsUtil.setLabelText(widgetElementId, sr.getPublisher());
         else if (sr.getAuthor() != null && !sr.getAuthor().trim().isEmpty()) DsUtil.setLabelText(widgetElementId, sr.getAuthor());            
         else DsUtil.setLabelText(widgetElementId, "N/A");
      }
      else if (widgetElementId.endsWith(DETAILS_DESC_TOGGLE_SUFFIX)) {
         if (sr.hasLongDescription()) {
            ((Anchor)PageAssembler.elementToWidget(widgetElementId,PageAssembler.A)).getElement().setAttribute("style","display:block;font-size:12px;font-weight:bold");
            PageAssembler.attachHandler(widgetElementId,Event.ONCLICK,descToggleListener);
            DsUtil.setAnchorText(widgetElementId, SHOW_MORE_DESC);
         }
      }
      else if (widgetElementId.endsWith(DETAILS_DESC_TOGGLE_STATUS)) DsUtil.setLabelText(widgetElementId, DESC_SHORT_STATUS);
   }
   
   //Assigns interactive specific search result values
   private void assignInteractiveSearchResultWidgetValues(String widgetElementId, InteractiveSearchResult sr) {
      if (widgetElementId.endsWith(DETAILS_SCORE_SUFFIX)) DsUtil.setLabelText(widgetElementId, sr.getScore());
      else if (widgetElementId.endsWith(DETAILS_CREATE_DATE_SUFFIX)) DsUtil.setLabelText(widgetElementId, sr.getCreatedDateStr());
      else if (widgetElementId.endsWith(DETAILS_REDIRECT_WARNING_SUFFIX)) {
         if (sr.getUrlStatus().startsWith("3")) DsUtil.showLabel(widgetElementId);
         else DsUtil.hideLabel(widgetElementId);
      }
   }
   
   /**
    * Builds a set of Basic Search result widgets and adds them to the given parent element 
    * 
    * @param resultList The result list
    * @param parentElementId The id of the parent element
    * @param widgetText The widget text
    */
   public void addBasicSearchResultWidgets(ArrayList<BasicSearchResult> resultList, String parentElementId, String widgetText) {
      Vector<String> widgetIds;
      for (BasicSearchResult bsr:resultList) {
         widgetIds = PageAssembler.inject(parentElementId, DEFAULT_WIDGET_ID_TOKEN, new HTML(widgetText), false);
         for (String id:widgetIds) assignSearchResultWidgetValues(id,bsr);
      }
   }
   
   /**
    * Builds a set of Interactive Search result widgets and adds them to the given parent element 
    * 
    * @param resultList The result list
    * @param parentElementId The id of the parent element
    * @param widgetText The widget text
    */
   public void addInteractiveSearchResultWidgets(ArrayList<InteractiveSearchResult> resultList, String parentElementId, String widgetText) {
      Vector<String> widgetIds;
      for (InteractiveSearchResult isr:resultList) {
         widgetIds = PageAssembler.inject(parentElementId, DEFAULT_WIDGET_ID_TOKEN, new HTML(widgetText), false);
         for (String id:widgetIds) {
            assignSearchResultWidgetValues(id,isr);
            assignInteractiveSearchResultWidgetValues(id,isr);            
         }
      }      
   }

}
