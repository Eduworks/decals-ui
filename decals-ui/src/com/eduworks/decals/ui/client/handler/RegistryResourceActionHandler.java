package com.eduworks.decals.ui.client.handler;

import java.util.ArrayList;
import java.util.HashMap;

import com.eduworks.decals.ui.client.DsSession;
import com.eduworks.decals.ui.client.api.DsESBApi;
import com.eduworks.decals.ui.client.handler.InteractiveSearchHandler.CommentHandlerType;
import com.eduworks.decals.ui.client.model.Collection;
import com.eduworks.decals.ui.client.model.InteractiveSearchResult;
import com.eduworks.decals.ui.client.model.ResourceComment;
import com.eduworks.decals.ui.client.model.ResourceCommentSummaryInfo;
import com.eduworks.decals.ui.client.model.SearchHandlerParamPacket;
import com.eduworks.decals.ui.client.pagebuilder.DecalsScreen;
import com.eduworks.decals.ui.client.util.CollectionsViewBuilder;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.decals.ui.client.util.RegistrySearchResultWidgetGenerator;
import com.eduworks.decals.ui.client.util.ResourceCommentParser;
import com.eduworks.decals.ui.client.util.ResourceRatingParser;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Handles actions (ratings and comments) that happen to the resources returned from the registry/interactive search. 
 * 
 * @author Eduworks Corporation
 *
 */
public class RegistryResourceActionHandler {   
   
   private static final String NO_COMMENTS_MESSAGE = "No comments have been submitted for this resource";
   private static final String NO_COMMENTS_COLOR = "#A4A4A4";
   
   private static final String COMMENT_CREATOR_CLASS = "commentCreator";
   private static final String COMMENT_CONTENTS_CLASS = "commentContents";
   private static final String COMMENT_CREATOR_CONT_CLASS = "commentCreatorContainer";
   private static final String COMMENT_CONTENTS_CONT_CLASS = "commentContentsContainer";
   
   //all of these should be passed through the param packet...this is kind of inconsistent and cumbersome at the moment
   private static final String ARTC_PICKLIST = "addToCollectionPickList";
   private static final String ARTC_INNER_CONTAINER = "addToCollectionListContainer";
   private static final String ARTC_COL_LIST_CONTAINER = "addToCollectionList";
   private static final String ARTC_COL_NAME = "artcColName";
   private static final String ARTC_MODAL = "modalAddToCollection";
   private static final String ARTC_RESOURCE_TITLE = "addToCollectionResourceTitle";
   private static final String ARTC_CANCEL_BTN= "addToCollectionCancelButton";
   private static final String ARTC_CONFIRM = "addToCollectionConfirm";
   private static final String ARTC_FORM = "addToCollectionForm";
   private static final String ARTC_CONFIRM_RESOURCE_TITLE = "addToCollectionConfirmResourceTitle";
   private static final String ARTC_CONFIRM_COL_NAME = "addToCollectionConfirmCollectionName";
   private static final String ARTC_CONFIRM_COL_ID = "addToCollectionCollectionId";
   private static final String ARTC_CONFIRM_RESOURCE_URL = "addToCollectionResourceUrl";   
   private static final String ARTC_CONFIRM_RESOURCE_TITLE_HIDDEN = "addToCollectionResourceTitleHidden";
   private static final String ARTC_CONFIRM_RESOURCE_URL_HIDDEN = "addToCollectionResourceUrlHidden";
   private static final String ARTC_CONFIRM_RESOURCE_DESC = "addToCollectionResourceDescriptionHidden";
   private static final String ARTC_CONFIRM_BTNS = "addToCollectionConfirmButtons";
   private static final String ARTC_EXISTS= "addToCollectionExists";
   private static final String ARTC_REPLACE = "addToCollectionReplace";
   private static final String ARTC_SUCCESS = "addToCollectionSuccess";
   private static final String ARTC_BUSY = "addToCollectionBusy";  
      
   private static final int LIST_ITEMS_PER_PAGE = 5;
   
   private SearchHandlerParamPacket paramPacket;
   private InteractiveSearchHandler searchHandler;
   private String commentWidgetId;
   
   private HashMap<String,Collection> addResourceCollectionWidgets = new HashMap<String,Collection>();
   
   private final native String initAddToCollectionListFiltering(String listContainer, String searchField1, int itemsPerPage) /*-{
      var addToCollectionOptions = {
         valueNames: [searchField1],
            //page:itemsPerPage,
            plugins: [
                //$wnd.ListPagination({outerWindow: 5})
            ]
    }; 
    var addToCollecdtionList = new $wnd.List(listContainer, addToCollectionOptions);      
   }-*/;
   
   public RegistryResourceActionHandler(InteractiveSearchHandler searchHandler, SearchHandlerParamPacket paramPacket) {
      this.paramPacket = paramPacket;
      this.searchHandler = searchHandler;
      if (CommentHandlerType.MODIFY.equals(searchHandler.getCommentHandlerType())) { 
         PageAssembler.attachHandler(paramPacket.getCommentFormId(),DecalsScreen.VALID_EVENT,addCommentSubmitListener);
      }
   }
   
   //handle rate resource
   private void handleRateResource(final String widgetId, String resourceUrl, int rating) {
      //Window.alert("rating: " + rating  +  "  URL: " + resourceUrl);
      DsESBApi.decalsAddRating(resourceUrl, rating, new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            RegistrySearchResultWidgetGenerator.updateRatingInfo(widgetId,ResourceRatingParser.parseRatingSummary(result.getObject(ESBApi.ESBAPI_RETURN_OBJ)),
                  searchHandler.getRatingHandlerType());            
         }
         @Override
         public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
      });
   }
   
   //handle the add comment response
   private void handleAddCommentResponse(ResourceCommentSummaryInfo commentInfo) {
      DsUtil.hideLabel(paramPacket.getCommentBusyId());
      RegistrySearchResultWidgetGenerator.updateCommentInfo(commentWidgetId, commentInfo, searchHandler.getCommentHandlerType());
      PageAssembler.closePopup(paramPacket.getCommentModalId());
   }
   
   //publish content submit listener
   protected EventCallback addCommentSubmitListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         String resourceUrl = DsUtil.getLabelText(paramPacket.getCommentResourceUrlId());
         String comment = DsUtil.getTextAreaText(paramPacket.getCommentInputId()).trim();
         if (comment == null || comment.isEmpty()) return;         
         DsUtil.showLabel(paramPacket.getCommentBusyId());
         DsESBApi.decalsAddComment(resourceUrl, comment, new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {            
               handleAddCommentResponse(ResourceCommentParser.parseCommentSummary(result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject()));            
            }
            @Override
            public void onFailure(Throwable caught) {
               DsUtil.hideLabel(paramPacket.getCommentBusyId());
               DsUtil.handleFailedApiCall(caught);
            }
         });
      }
   }; 
   
   //add the no comments available statement
   private void addNoCommentsAvailableHtml() {
      RootPanel.get(paramPacket.getCommentContainerId()).add(new HTML("<p><i style=\"color:" + NO_COMMENTS_COLOR + "\">" + NO_COMMENTS_MESSAGE +"</i></p>"));
   }
   
   private HTML generateCommentHtml(ResourceComment comment) {
      StringBuffer sb = new StringBuffer();
      sb.append("<div class=\"row\">");
      sb.append("<div class=\"row\">");
      sb.append("<div class=\"small-12 columns " + COMMENT_CREATOR_CONT_CLASS + "\">");
      sb.append("<span class=\"" + COMMENT_CREATOR_CLASS + "\">");
      sb.append("<i class=\"fa fa-comment\"></i> <b>" + comment.getUserId() + "</b>");
      sb.append(" (" + comment.getCreateDateStr() + ")");
      sb.append("</span>");
      sb.append("</div>");
      sb.append("</div>");
      sb.append("<div class=\"row\">");
      sb.append("<div class=\"small-12 columns " + COMMENT_CONTENTS_CONT_CLASS + "\">");
      sb.append("<span class=\"" + COMMENT_CONTENTS_CLASS + "\">");
      sb.append(comment.getComment());
      sb.append("</span>");
      sb.append("</div>");
      sb.append("</div>");      
      sb.append("</div>");   
      return new HTML(sb.toString());
   }
   
   //populate the modal comments
   private void populateCommentsModal(ArrayList<ResourceComment> commentList) {
      DsUtil.removeAllChildrenFromElement(paramPacket.getCommentContainerId());
      if (commentList == null || commentList.size() <= 0) addNoCommentsAvailableHtml();
      else {
         for (ResourceComment comment: commentList) RootPanel.get(paramPacket.getCommentContainerId()).add(generateCommentHtml(comment));
      }
      PageAssembler.openPopup(paramPacket.getCommentModalId());
   }
   
   //handle comment on resource
   private void handleCommentOnResource(final String widgetId, String resourceUrl) {
      InteractiveSearchResult resource = searchHandler.getRegistryResource(resourceUrl);
      if (CommentHandlerType.MODIFY.equals(searchHandler.getCommentHandlerType())) {
         DsUtil.setLabelText(paramPacket.getCommentResourceUrlId(),resourceUrl);
         DsUtil.setTextAreaText(paramPacket.getCommentInputId(),"");
      }
      DsUtil.setLabelText(paramPacket.getCommentTitleId(),resource.getTitle());      
      DsESBApi.decalsGetCommentsForUrl(resourceUrl, new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {       
            commentWidgetId = widgetId;
            if (result == null || !result.containsKey(ESBApi.ESBAPI_RETURN_OBJ)) populateCommentsModal(null);
            else populateCommentsModal(ResourceCommentParser.parseCommentList(result.get(ESBApi.ESBAPI_RETURN_OBJ).isArray()));            
         }
         @Override
         public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
      });
   }
   
   //rating click event listener
   private class RatingClickListener extends EventCallback {      
      private String widgetId;    
      private int rating;
      private String resourceUrl;
      
      public RatingClickListener(String widgetId, String resourceUrl, int rating) {
         this.widgetId = widgetId;
         this.resourceUrl = resourceUrl;
         this.rating = rating;
      }      
      @Override
      public void onEvent(Event event) {handleRateResource(widgetId,resourceUrl,rating);}
   }
   
   //comment click event listener
   private class CommentClickListener extends EventCallback {      
      private String widgetId;    
      private String resourceUrl;
      
      public CommentClickListener(String widgetId, String resourceUrl) {
         this.widgetId = widgetId;
         this.resourceUrl = resourceUrl;         
      }      
      @Override
      public void onEvent(Event event) {handleCommentOnResource(widgetId,resourceUrl);}
   }
   
   /**
    * Adds a rating click listener for the given widget ID
    * 
    * @param widgetId The widget ID 
    * @param resourceUrl The resource URL 
    * @param rating The rating of click
    */
   public void addRatingClickListener(String widgetId, String resourceUrl, int rating) {
      if (widgetId != null && resourceUrl != null) PageAssembler.attachHandler(widgetId,Event.ONCLICK, new RatingClickListener(widgetId,resourceUrl,rating));
   }
   
   /**
    * Adds a comment click listener for the given widget ID
    * 
    * @param widgetId The widget ID 
    * @param resourceUrl The resource URL
    */
   public void addCommentClickListener(String widgetId, String resourceUrl) {
      if (widgetId != null && resourceUrl != null) PageAssembler.attachHandler(widgetId,Event.ONCLICK, new CommentClickListener(widgetId,resourceUrl));
   }
   
   //handle add to collection response 
   private void handleAddToCollectionResponse(JSONObject colRes) {
      Collection col = new Collection(colRes);
      DsSession.getUserCollectionManager().replaceCollection(col);
      DsUtil.hideLabel(ARTC_BUSY);
      DsUtil.showLabel(ARTC_SUCCESS);
   }
   
   //handle add to collection submit
   protected EventCallback addToCollectionSubmitHandler = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         DsUtil.hideLabel(ARTC_CONFIRM_BTNS);
         DsUtil.showLabel(ARTC_BUSY);
         String collectionId = DsUtil.getLabelText(ARTC_CONFIRM_COL_ID);
         String resourceTitle = DsUtil.getLabelText(ARTC_CONFIRM_RESOURCE_TITLE_HIDDEN);
         String resourceUrl = DsUtil.getLabelText(ARTC_CONFIRM_RESOURCE_URL_HIDDEN);
         String resourceDescription = DsUtil.getLabelText(ARTC_CONFIRM_RESOURCE_DESC);
         DsESBApi.decalsAddCollectionItem(collectionId,resourceUrl,resourceTitle,resourceDescription, result.toJson(), "0",new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {       
               handleAddToCollectionResponse(result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject());            
            }
            @Override
            public void onFailure(Throwable caught) {
               DsUtil.hideLabel(ARTC_BUSY);
               DsUtil.handleFailedApiCall(caught);
            }
         });         
      }
   };
   
   private static InteractiveSearchResult result;
   
   //TODO
   //handle add resource collection chosen
   private void handleAddResourceCollectionChosen(Collection col, InteractiveSearchResult sr) {
      result = sr;
	   
	  DsUtil.setLabelText(ARTC_CONFIRM_RESOURCE_TITLE,sr.getTitle());
      DsUtil.setLabelText(ARTC_CONFIRM_COL_NAME,col.getName());
      DsUtil.setLabelText(ARTC_CONFIRM_RESOURCE_URL,sr.getResourceUrl());
      DsUtil.setLabelText(ARTC_CONFIRM_COL_ID,col.getCollectionId());
      DsUtil.setLabelText(ARTC_CONFIRM_RESOURCE_TITLE_HIDDEN,sr.getTitle());
      DsUtil.setLabelText(ARTC_CONFIRM_RESOURCE_URL_HIDDEN,sr.getResourceUrl());
      DsUtil.setLabelText(ARTC_CONFIRM_RESOURCE_DESC,sr.getDescription());
      DsUtil.hideLabel(ARTC_PICKLIST);      
      DsUtil.hideLabel(ARTC_BUSY);
      DsUtil.showLabel(ARTC_CONFIRM); 
      DsUtil.showLabelInline(ARTC_CONFIRM_BTNS);     
      PageAssembler.attachHandler(ARTC_FORM,DecalsScreen.VALID_EVENT,addToCollectionSubmitHandler);     
   }
   
   //add resource collection click event listener
   private class AddResourceCollectionClickListener extends EventCallback {      
      private Collection col;    
      private InteractiveSearchResult sr;
      public AddResourceCollectionClickListener(Collection col, InteractiveSearchResult sr) {
         this.col = col;
         this.sr = sr;
      }      
      @Override
      public void onEvent(Event event) {handleAddResourceCollectionChosen(col,sr);}
   }
   
   //register add resource collection widget even handlers
   private void registerAddResourceCollectionWidgets(InteractiveSearchResult sr) {
      for (String key:addResourceCollectionWidgets.keySet()) {
         PageAssembler.attachHandler(key,Event.ONCLICK,new AddResourceCollectionClickListener(addResourceCollectionWidgets.get(key),sr));
      }
   }
   
   //handle add to collection
   private void handleAddToCollection(String widgetId, InteractiveSearchResult sr) {
      DsUtil.hideLabel(ARTC_CONFIRM);
      DsUtil.hideLabel(ARTC_EXISTS);
      DsUtil.hideLabel(ARTC_SUCCESS);
      DsUtil.showLabel(ARTC_PICKLIST);         
      DsUtil.showLabel(ARTC_CANCEL_BTN);
      DsUtil.setLabelText(ARTC_RESOURCE_TITLE,sr.getTitle());
      addResourceCollectionWidgets.clear();
      CollectionsViewBuilder.buildAddResourceCollectionList(ARTC_INNER_CONTAINER,DsSession.getUserModifiableCollections(),addResourceCollectionWidgets);
      registerAddResourceCollectionWidgets(sr);
      initAddToCollectionListFiltering(ARTC_COL_LIST_CONTAINER,ARTC_COL_NAME,LIST_ITEMS_PER_PAGE);
      PageAssembler.openPopup(ARTC_MODAL);
   }
   
   //add to collection click event listener
   private class AddToCollectionClickListener extends EventCallback {      
      private String widgetId;    
      private InteractiveSearchResult sr;
      
      public AddToCollectionClickListener(String widgetId, InteractiveSearchResult sr) {
         this.widgetId = widgetId;
         this.sr = sr;         
      }      
      @Override
      public void onEvent(Event event) {handleAddToCollection(widgetId,sr);}
   }
   
   /**
    * Adds a add to collection click listener for the given widget ID
    * 
    * @param widgetId The widget ID 
    * @param sr The search result
    */
   public void addAddToCollectionClickListener(String widgetId, InteractiveSearchResult sr) {
      if (widgetId != null && sr != null) PageAssembler.attachHandler(widgetId,Event.ONCLICK, new AddToCollectionClickListener(widgetId,sr));
   }
   
}
