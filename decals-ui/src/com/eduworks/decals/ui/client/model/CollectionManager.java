package com.eduworks.decals.ui.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.eduworks.decals.ui.client.util.DsUtil;
import com.google.gwt.json.client.JSONObject;

/**
 * Manages a group of collections.
 * 
 * @author Eduworks Corporation
 *
 */
public class CollectionManager {
   
   private ArrayList<Collection> collectionList = new ArrayList<Collection>();
   
   private static final int NEW_COLLECTION_ITEM_INDEX = 1;
   
   /**
    * Initializes the collection list
    * 
    * @param collections The JSON collection list
    */
   public void initCollectionList(JSONObject collections) {parseUserCollections(collections);}
   
   //parse collection list
   private void parseUserCollections(JSONObject collections) {
      collectionList.clear();
      for (JSONObject jo : DsUtil.parseJsonReturnList(collections)) collectionList.add(new Collection(jo));
      Collections.sort(collectionList);
   }
   
   /**
    * {@link CollectionManager#collectionList}
    */
   public ArrayList<Collection> getCollectionList() {return collectionList;}
   public void setCollectionList(ArrayList<Collection> collectionList) {this.collectionList = collectionList;}
   
   /**
    * Returns the number of collections being managed.
    * 
    * @return Returns the number of collections being managed.
    */
   public long getNumberOfCollections() {return collectionList.size();}
   
   /**
    * Returns the number of modifiable collections for the given user.
    * 
    * @param The user ID
    * @return Returns the number of modifiable collections for the given user.
    */
   public long getNumberOfModifiableCollections(String userId) {return getListOfModifiableCollections(userId).size();}
   
   /**
    * Returns a list of modifiable collections for the user.
    * 
    * @param userId The user ID
    * @return  Returns a list of modifiable collections for the user.
    */
   public ArrayList<Collection> getListOfModifiableCollections(String userId) {
      ArrayList<Collection> ret = new ArrayList<Collection>();
      for (Collection c :collectionList) {
         if (c.userCanModifyCollection(userId)) ret.add(c);
      }
      return ret;
   }
   
   /**
    * Adds the given collection to the collection list as the first element.
    * 
    * @param col The collection to add;
    */
   public void addCollection(Collection col) {collectionList.add(0,col);}
   
   /**
    * Removes the collection with the given ID from the collection list.
    * 
    * @param colId The ID of the collection to remove from the collection list.
    */
   public void removeCollection(String colId) {
      if(colId == null || colId.trim().isEmpty()) return;
      for (Collection c: collectionList) {
         if (colId.equalsIgnoreCase(c.getCollectionId())) {
            collectionList.remove(c);
            break;
         }
      }
   }
   
   /**
    * Replaces the collection list collection with the given collection based on matching IDs 
    * 
    * @param col The collection to place on the list
    */
   public void replaceCollection(Collection col) {
      removeCollection(col.getCollectionId());
      addCollection(col);
      Collections.sort(collectionList);
   }   
   
   //return the collection with the given ID
   private Collection getCollection(String colId) {
      for (Collection c: collectionList) {
         if (colId.equalsIgnoreCase(c.getCollectionId())) return c;         
      }
      return null;
   }
   
   //creates a new collection item with the given information
   private CollectionItem createCollectionItem(String collectionId, String title, String url, String description) {
      CollectionItem ci = new CollectionItem();
      ci.setCollectionId(collectionId);
      ci.setItemIndex(NEW_COLLECTION_ITEM_INDEX);
      ci.setLocatorKey(DsUtil.generateId(Collection.ITEM_LOCATOR_KEY_PREFIX));
      ci.setResourceDescription(description);
      ci.setResourceUrl(url);
      ci.setResourceTitle(title); 
      return ci;
   }
   
   //creates a new collection user with the given information
   private CollectionUser createCollectionUser(String collectionId, String userId, String firstName, String lastName, String access) {
      CollectionUser cu = new CollectionUser();
      cu.setCollectionId(collectionId);
      cu.setLocatorKey(DsUtil.generateId(Collection.USER_LOCATOR_KEY_PREFIX));
      cu.setUserId(userId);
      cu.setEmailAddress(userId);
      cu.setFirstName(firstName);
      cu.setLastName(lastName);
      cu.setAccess(access);
      return cu;
   }
   
   /**
    * Adds a new collection item with the given information to a collection with the given ID.
    * 
    * Returns true if the item is added.  Returns false if the item already existed or the collection does not exist.
    * 
    * @param collectionId The ID of the collection to add the item
    * @param title The item title
    * @param url The item URL
    * @param description The item description
    * @return  Returns true if the item is added.  Returns false if the item already existed or the collection does not exist.
    */
   public boolean addCollectionItem(String collectionId, String title, String url, String description) {
      Collection c = getCollection(collectionId);
      if (c == null) return false;
      if (c.itemExists(url)) return false;          
      c.addCollectionItemToTop(createCollectionItem(collectionId,title,url,description));
      return true;
   }
   
   /**
    * Adds a new collection item with the given information to a collection with the given ID.
    * 
    * Overwrites an item with the same URL if it exists.
    * 
    * @param collectionId The ID of the collection to add the item
    * @param title The item title
    * @param url The item URL
    * @param description The item description
    */
   public void overwriteCollectionItem(String collectionId, String title, String url, String description) {
      Collection c = getCollection(collectionId);
      c.removeItem(url);
      c.addCollectionItemToTop(createCollectionItem(collectionId,title,url,description));
   }
   
   /**
    * Adds a new user with the given information to a collection with the given ID.
    * 
    * Overwrites an user with the same user ID if it exists.
    * 
    * @param collectionId The ID of the collection to add the user
    * @param userId The ID of the user
    * @param firstName The first name of the user
    * @param lastName The last name of the user
    * @param access  The collection access of the user
    */
   public void overwriteCollectionUser(String collectionId, String userId, String firstName, String lastName, String access) {
      Collection c = getCollection(collectionId);
      c.removeUser(userId);
      c.addCollectionUser(createCollectionUser(collectionId,userId,firstName,lastName,access));
   }
   
   /**
    * Returns a list of users that is given user list - collection users.
    * 
    * @param collectionId The ID of the collection to use.
    * @param userList The super set user list
    * 
    * @return Returns a list of users that is given user list - collection users.
    */
   public ArrayList<AppUser> removeCollectionUsersFromUserList(String collectionId, ArrayList<AppUser> userList) {
      Collection c = getCollection(collectionId);     
      ArrayList<AppUser> tempList = new ArrayList<AppUser>();
      for (AppUser u: userList) {
         if (!c.getAccessMap().containsKey(u.getUserId())) tempList.add(u);
      }
      return tempList;
   }
   
   /**
    * Returns true if any collection is in a changed state.  False if they are not.
    * 
    * @return Returns true if any collection is in a changed state.  False if they are not.
    */
   public boolean anyCollectionInChangedState() {
      for (Collection c: collectionList) {
         if (c.getHasChanged()) return true;
      }
      return false;         
   }
   
   /**
    * Removes the given user from the given collection.
    * 
    * @param collectionId The collection ID
    * @param userId The user ID
    */
   public void removeCollectionUser(String collectionId, String userId) {
      getCollection(collectionId).removeUser(userId);
   }
   
   /**
    * Removes the given item from the given collection.
    * 
    * @param collectionId The collection ID
    * @param resourceUrl The item URL
    */
   public void removeCollectionItem(String collectionId, String resourceUrl) {
      getCollection(collectionId).removeItem(resourceUrl);
   }
   
   /**
    * Removes the given item from all collections
    * 
    * @param resourceUrl The item URL
    */
   public void removeItemFromAllCollections(String resourceUrl) {for (Collection c:collectionList) c.removeItem(resourceUrl);}
   
   /**
    * Removes the given user from all collections
    * 
    * @param userId The user ID
    */
   public void removeUserFromAllCollections(String userId) {for (Collection c:collectionList) c.removeUser(userId);}

   
   /**
    * Updates the collection with the given ID with the given information
    * 
    * @param collectionId  The ID of the collection to update
    * @param description The collection description
    * @param itemOrderMap The map containing collection item ordering
    * @param userAccessMap The map containing user access
    */
   public void updateCollection(String collectionId, String description, HashMap<String,Integer> itemOrderMap, HashMap<String,String> userAccessMap) {
      Collection c = getCollection(collectionId);
      c.setDescription(description);
      c.updateItemIndices(itemOrderMap);
      c.updateUserAccesses(userAccessMap);      
   }
   
   /**
    * Returns a list of collections the given user can modify.
    * 
    * @param userId The user ID
    * @return  Returns a list of collections the given user can modify.
    */
   public ArrayList<Collection> getCanModifyCollectionList(String userId) {
      ArrayList<Collection> modList = new ArrayList<Collection>();
      for (Collection c: collectionList) {
         if (c.userCanModifyCollection(userId)) modList.add(c);
      }
      Collections.sort(modList);
      return modList;
   }
   
   /**
    * Generates and returns a unique collection name if the given name is already in use.
    * 
    * ex: If 'Math Collection' exists, 'Math Collection (2)' is returned if it doesn't exist.
    * If 'Math Collection (2)' exists, 'Math Collection (3)' is returned if it doesn't exist.
    * etc.
    *
    * Recommended by client...
    * 
    * @param name The name to "uniqify"
    * @return Returns a unique collection name if the given name is already in use.
    */
   public String generateUniqueCollectionName(String name) {
      boolean found = false;
      for (Collection c:collectionList) {
         if (c.getName().equals(name)) {
            found = true;
            break;
         }
      }
      if (found) return generateUniqueCollectionName(name,0);
      else return name;
   }
   
   // Generates and returns a unique collection name if the given name is already in use.
   private String generateUniqueCollectionName(String name, int idx) {
      int idx2 = idx + 1;      
      boolean found = false;
      for (Collection c:collectionList) {
         if (c.getName().equals(name + " (" + idx2  + ")")) {
            found = true;
            break;
         }
      }
      if (found) return generateUniqueCollectionName(name,idx2);
      else return name + " (" + idx2  + ")";
   }
   
}
