package com.eduworks.decals.ui.client;

/**
 * Class for helping manage and retrieve DECALS session information. 
 * 
 * @author Tom Buskirk
 *
 */
public class DsSession {
   
   public enum SearchType{INTERACTIVE,BASIC,DUAL}
   
   private String firstName;
   private String lastName;
   private String assignmentId = null;
   private String solrUrl = null;
   private String interactiveSearchThumbnailRootUrl = null;
   
   private SearchType sessionSearchType = SearchType.DUAL;
   
   private static final DsSession INSTANCE = new DsSession();
   
   private DsSession() {}

   public static DsSession getInstance() {return INSTANCE;}

   /**
    * {@link DsSession#firstName}
    */
   public String getFirstName() {return firstName;}
   public void setFirstName(String firstName) {this.firstName = firstName;}

   /**
    * {@link DsSession#lastName}
    */
   public String getLastName() {return lastName;}
   public void setLastName(String lastName) {this.lastName = lastName;}

   /**
    * {@link DsSession#assignmentId}
    */
   public String getAssignmentId() {return assignmentId;}
   public void setAssignmentId(String assignmentId) {this.assignmentId = assignmentId;}
   
   /**
    * {@link DsSession#solrUrl}
    */
   public String getSolrUrl() {return solrUrl;}
   public void setSolrUrl(String solrUrl) {this.solrUrl = solrUrl;}

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
    * Returns first name and last name separated by a space.
    * 
    * @return  Returns first name and last name separated by a space.
    */
   public String getFullName() {return firstName + " " + lastName;}
   
   
}
