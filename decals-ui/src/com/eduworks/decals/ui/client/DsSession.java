package com.eduworks.decals.ui.client;

import java.util.ArrayList;

import com.eduworks.decals.ui.client.api.DsESBApi;
import com.eduworks.decals.ui.client.handler.InteractiveSearchHandler;
import com.eduworks.decals.ui.client.model.AppUser;
import com.eduworks.decals.ui.client.model.Collection;
import com.eduworks.decals.ui.client.model.CollectionManager;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.google.gwt.json.client.JSONObject;

/**
 * Class for helping manage and retrieve DECALS session information. 
 * 
 * @author Eduworks Corporation
 *
 */
public class DsSession {
   
   public enum SearchType{INTERACTIVE,BASIC,DUAL}
   
   private String assignmentId = null;
   private String interactiveSearchThumbnailRootUrl = null;
   private String downloadFileUrlPrefix;
   private String lrPublishNode;
   private String lrPublishSubmitter;
   private String lrPublishCurator;
   private String lrPublishFromNode;
   private String lrPublishParadataActor;
   private AppUser sessionUser;
      
   private SearchType sessionSearchType = SearchType.DUAL;
   
   private CollectionManager collectionManager;
   
   public enum SessionState{LOGGED_IN, NONE, LOGGING_OUT}
   
   private SessionState sessionState;
   
   private InteractiveSearchHandler cachedLrSearchHandler;
   
   private static final DsSession INSTANCE = new DsSession();
   
   private DsSession() {
      collectionManager = new CollectionManager();
      cachedLrSearchHandler = null;
      setSessionState(SessionState.NONE);
   }

   public static DsSession getInstance() {return INSTANCE;}
   
   /**
    * {@link DsSession#sessionUser}
    */
   public AppUser getSessionUser() {return sessionUser;}
   public void setSessionUser(AppUser sessionUser) {this.sessionUser = sessionUser;}

   /**
    * {@link DsSession#assignmentId}
    */
   public String getAssignmentId() {return assignmentId;}
   public void setAssignmentId(String assignmentId) {this.assignmentId = assignmentId;}
      
   /**
    * {@link DsSession#interactiveSearchThumbnailRootUrl}
    */
   public String getInteractiveSearchThumbnailRootUrl() {return interactiveSearchThumbnailRootUrl;}
   public void setInteractiveSearchThumbnailRootUrl(String interactiveSearchThumbnailRootUrl) {this.interactiveSearchThumbnailRootUrl = interactiveSearchThumbnailRootUrl;}
   
   /**
    * {@link DsSession#sessionSearchType}
    */
   public SearchType getSessionSearchType() {return sessionSearchType;}
   public void setSessionSearchType(SearchType sessionSearchType) {this.sessionSearchType = sessionSearchType;}

   /**
    * {@link DsSession#downloadFileUrlPrefix}
    */
   public String getDownloadFileUrlPrefix() {return downloadFileUrlPrefix;}
   public void setDownloadFileUrlPrefix(String downloadFileUrlPrefix) {this.downloadFileUrlPrefix = downloadFileUrlPrefix;}

   /**
    * {@link DsSession#lrPublishNode}
    */
   public String getLrPublishNode() {return lrPublishNode;}
   public void setLrPublishNode(String lrPublishNode) {this.lrPublishNode = lrPublishNode;}
   
   /**
    * {@link DsSession#lrPublishSubmitter}
    */
   public String getLrPublishSubmitter() {return lrPublishSubmitter;}
   public void setLrPublishSubmitter(String lrPublishSubmitter) {this.lrPublishSubmitter = lrPublishSubmitter;}

   /**
    * {@link DsSession#lrPublishCurator}
    */
   public String getLrPublishCurator() {return lrPublishCurator;}
   public void setLrPublishCurator(String lrPublishCurator) {this.lrPublishCurator = lrPublishCurator;}

   /**
    * {@link DsSession#lrPublishFromNode}
    */
   public String getLrPublishFromNode() {return lrPublishFromNode;}
   public void setLrPublishFromNode(String lrPublishFromNode) {this.lrPublishFromNode = lrPublishFromNode;}

   /**
    * {@link DsSession#lrPublishParadataActor}
    */
   public String getLrPublishParadataActor() {return lrPublishParadataActor;}
   public void setLrPublishParadataActor(String lrPublishParadataActor) {this.lrPublishParadataActor = lrPublishParadataActor;}
   
   /**
    * {@link DsSession#sessionState}
    */
   public SessionState getSessionState() {return sessionState;}
   public void setSessionState(SessionState sessionState) {this.sessionState = sessionState;}
   
   /**
    * {@link DsSession#cachedLrSearchHandler}
    */
   public InteractiveSearchHandler getCachedLrSearchHandler() {return cachedLrSearchHandler;}
   public void setCachedLrSearchHandler(InteractiveSearchHandler cachedLrSearchHandler) {this.cachedLrSearchHandler = cachedLrSearchHandler;}
   
   /**
    * {@link DsSession#collectionManager}
    */
   public CollectionManager getCollectionManager() {return collectionManager;}
   
   /**
    * Returns the session user.
    * 
    * @return  Returns the session user.
    */
   public static AppUser getUser() {return getInstance().getSessionUser();}
   
   /**
    * Returns the user's collections.
    * 
    * @return  Returns the user's collections.
    */
   public static ArrayList<Collection> getUserCollections() {return getUserCollectionManager().getCollectionList();}
   
   /**
    * Returns the user's modifiable collections.
    * 
    * @return  Returns the user's modifiable collections.
    */
   public static ArrayList<Collection> getUserModifiableCollections() {return getUserCollectionManager().getListOfModifiableCollections(getUser().getUserId());}
   
   /**
    * Returns true if the user has collections.  Returns false otherwise.
    * 
    * @return Returns true if the user has collections.  Returns false otherwise.
    */
   public static boolean userHasCollections() {
      if (getInstance().getCollectionManager() == null) return false;
      return getInstance().getCollectionManager().getNumberOfCollections() > 0;
   }
   
   /**
    * Returns true if the user has modifiable collections.  Returns false otherwise.
    * 
    * @return Returns true if the user has modifiable collections.  Returns false otherwise.
    */
   public static boolean userHasModifiableCollections() {
      if (getInstance().getCollectionManager() == null) return false;
      return getInstance().getCollectionManager().getNumberOfModifiableCollections(getUser().getUserId()) > 0;
   }
   
   /**
    * Removes the given item from all collections
    * 
    * @param resourceUrl The item URL
    */
   public static void removeItemFromAllCollections(String resourceUrl) {getInstance().getCollectionManager().removeItemFromAllCollections(resourceUrl);}
   
   /**
    * Removes the given user from all collections
    * 
    * @param userId The user ID
    */
   public static void removeUserFromAllCollections(String userId) {getInstance().getCollectionManager().removeUserFromAllCollections(userId);}
   
   /**
    * Returns a list of collections the given user can modify.
    * 
    * @return  Returns a list of collections the given user can modify.
    */
   public static ArrayList<Collection> getCanModifyCollectionList() {
      if (getInstance().getCollectionManager() == null) return null;
      return getInstance().getCollectionManager().getCanModifyCollectionList(getUser().getUserId());
   }
   
   /**
    * Returns the collection manager for the session user.
    * 
    * @return  Returns the collection manager for the session user.
    */
   public static CollectionManager getUserCollectionManager() {return getInstance().getCollectionManager();}
      
   
   /**
    * Parses application/session settings out of the given JSON Object
    * 
    * @param jo The object containing the settings.
    */
   public void parseAppSettings(JSONObject jo) {
      setDownloadFileUrlPrefix(jo.get(DsESBApi.DOWNLOAD_FILE_URL_PREFIX_KEY).isString().stringValue());
      setLrPublishNode(jo.get(DsESBApi.LR_PUBLISH_NODE_KEY).isString().stringValue());
      setLrPublishSubmitter(jo.get(DsESBApi.LR_PUBLISH_SUBMITTER_KEY).isString().stringValue());
      setLrPublishCurator(jo.get(DsESBApi.LR_PUBLISH_CURATOR_KEY).isString().stringValue());
      setLrPublishFromNode(jo.get(DsESBApi.LR_PUBLISH_FROM_NODE_KEY).isString().stringValue());
      setLrPublishParadataActor(jo.get(DsESBApi.LR_PUBLISH_PD_ACTOR_KEY).isString().stringValue());
   }
   
   /**
    * Builds the session user's collection list.
    */
   public void buildUserCollections() {
      DsESBApi.decalsGetUserCollections(new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            collectionManager.initCollectionList(result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject());            
         }
         @Override
         public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
      });   
   }
   
   /**
    * Reverts user collections to a baseline status if any of them are in a changed state.
    */
   public void verifyUserCollectionSync() {
      if (!SessionState.LOGGING_OUT.equals(sessionState)) {
         if (collectionManager.anyCollectionInChangedState()) buildUserCollections();
      }
   }
   
}
