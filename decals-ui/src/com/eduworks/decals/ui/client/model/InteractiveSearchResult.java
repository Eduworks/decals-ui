package com.eduworks.decals.ui.client.model;

import java.util.ArrayList;

/**
 * Represents a result from the interactive search
 * 
 * @author Tom Buskirk
 *
 */
public class InteractiveSearchResult implements SearchResult {
   
   private static final int SHORT_DESC_LENGTH = 215;
   
   private String solrId;
   private String createdDateStr;
   private String lastModifiedDateStr;
   private String title;
   private ArrayList<String> keywords;
   private String source;
   private String description;
   private String contentType;
   private String author;
   private String publisher;
   private String resourceUrl;
   private String score;
   private boolean hasScreenshot = false;
   private String thumbnailImageUrl;
   private String urlStatus;
   
   /**
    * InteractiveSearchResult constructor.
    */
   public InteractiveSearchResult() {}
   
   /**
    * {@link InteractiveSearchResult#solrId}
    */
   public String getSolrId() {return solrId;}
   public void setSolrId(String solrId) {this.solrId = solrId;}
   
   /**
    * {@link InteractiveSearchResult#createdDateStr}
    */
   public String getCreatedDateStr() {return createdDateStr;}
   public void setCreatedDateStr(String createdDateStr) {this.createdDateStr = createdDateStr;}
   
   /**
    * {@link InteractiveSearchResult#lastModifiedDateStr}
    */
   public String getLastModifiedDateStr() {return lastModifiedDateStr;}
   public void setLastModifiedDateStr(String lastModifiedDateStr) {this.lastModifiedDateStr = lastModifiedDateStr;}
   
   /**
    * {@link InteractiveSearchResult#title}
    */
   @Override
   public String getTitle() {return title;}
   public void setTitle(String title) {this.title = title;}
   
   /**
    * {@link InteractiveSearchResult#keywords}
    */
   public ArrayList<String> getKeywords() {return keywords;}
   public void setKeywords(ArrayList<String> keywords) {this.keywords = keywords;}
   
   /**
    * {@link InteractiveSearchResult#source}
    */
   public String getSource() {return source;}
   public void setSource(String source) {this.source = source;}
   
   /**
    * {@link InteractiveSearchResult#description}
    */
   @Override
   public String getDescription() {return description;}
   public void setDescription(String description) {this.description = description;}
   
   /**
    * {@link InteractiveSearchResult#contentType}
    */
   public String getContentType() {return contentType;}
   public void setContentType(String contentType) {this.contentType = contentType;}
   
   /**
    * {@link InteractiveSearchResult#author}
    */
   @Override
   public String getAuthor() {return author;}
   public void setAuthor(String author) {this.author = author;}
   
   /**
    * {@link InteractiveSearchResult#publisher}
    */
   @Override
   public String getPublisher() {return publisher;}
   public void setPublisher(String publisher) {this.publisher = publisher;}
   
   /**
    * {@link InteractiveSearchResult#resourceUrl}
    */
   @Override
   public String getResourceUrl() {return resourceUrl;}
   public void setResourceUrl(String resourceUrl) {this.resourceUrl = resourceUrl;}
   
   /**
    * {@link InteractiveSearchResult#urlStatus}
    */
   public String getUrlStatus() {return urlStatus;}
   public void setUrlStatus(String urlStatus) {this.urlStatus = urlStatus;}
   
   /**
    * {@link InteractiveSearchResult#score}
    */
   public String getScore() {return score;}
   public void setScore(String score) {this.score = score;}
   
   /**
    * {@link InteractiveSearchResult#hasScreenshot}
    */
   @Override   
   public boolean getHasScreenshot() {return hasScreenshot;}
   public void setHasScreenshot(boolean hasScreenshot) {this.hasScreenshot = hasScreenshot;}
   
   /**
    * {@link InteractiveSearchResult#thumbnailImageUrl}
    */
   @Override
   public String getThumbnailImageUrl() {return thumbnailImageUrl;}   
   public void setThumbnailImageUrl(String thumbnailImageUrl) {this.thumbnailImageUrl = thumbnailImageUrl;}

   /**
    * Returns a truncated string of the resource URL.
    * 
    * @return Returns a truncated string of the resource URL. 
    */
   @Override
   public String getTruncatedResourceUrl() {
      if (getResourceUrl() == null || getResourceUrl().trim().isEmpty()) return "";
      if (getResourceUrl().length() <= MAX_URL_DISPLAY_LENGTH) return getResourceUrl();
      return getResourceUrl().substring(0,MAX_URL_DISPLAY_LENGTH) + "...";
   }
   
   /**
    * Returns a short description of the result.
    * 
    * @return  Returns a short description of the result. 
    */
   @Override
   public String toString() {
      return getTitle() + " : " + getDescription() + " : " +  getCreatedDateStr();
   }
   
   /**
    * Returns a truncated version of the description.
    * 
    * @return Returns a truncated version of the description.
    */
   @Override
   public String getShortDescription() {
      if (description.length() > SHORT_DESC_LENGTH) return description.substring(0, SHORT_DESC_LENGTH) + "...";
      else return description;
   }
   
   /**
    * Returns true if the result has a long description.  Returns false otherwise.
    * 
    * @return  Returns true if the result has a long description.  Returns false otherwise.
    */
   @Override
   public boolean hasLongDescription() {return (description.length() > SHORT_DESC_LENGTH)?true:false;}
   
}
