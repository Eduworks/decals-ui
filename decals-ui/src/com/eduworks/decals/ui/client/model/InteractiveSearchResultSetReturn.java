package com.eduworks.decals.ui.client.model;

import java.util.ArrayList;

/**
 * Helper class for managing a list of {@link InteractiveSearchResult}
 * 
 * @author Tom Buskirk
 *
 */
public class InteractiveSearchResultSetReturn {
   
   private long numTotalResultsFound;
   private ArrayList<InteractiveSearchResult> searchResults = new ArrayList<InteractiveSearchResult>();
   
   /**
    * {@link InteractiveSearchResultSetReturn#numTotalResultsFound}
    */
   public long getNumTotalResultsFound() {return numTotalResultsFound;}
   public void setNumTotalResultsFound(long numTotalResultsFound) {this.numTotalResultsFound = numTotalResultsFound;}
   
   /**
    * {@link InteractiveSearchResultSetReturn#searchResults}
    */
   public ArrayList<InteractiveSearchResult> getSearchResults() {return searchResults;}
   public void setSearchResults(ArrayList<InteractiveSearchResult> searchResults) {this.searchResults = searchResults;}
   
   /**
    * Returns a subset of {@link InteractiveSearchResultSetReturn#searchResults} based on the given start and count.
    * 
    * @param start The index of the {@link InteractiveSearchResultSetReturn#searchResults} to start the subset. 
    * @param count The number of items to return.
    * @return  Returns a subset of {@link InteractiveSearchResultSetReturn#searchResults} based on the given start and count.
    */
   public ArrayList<InteractiveSearchResult> getSearchResults(int start, int count) {
      try {
         ArrayList<InteractiveSearchResult> retList = new ArrayList<InteractiveSearchResult>();
         int currentIndex = start;
         for (int i=0;i<count;i++){
            currentIndex = start + i;
            if ((searchResults.size() - 1) >= currentIndex) {
               retList.add(searchResults.get(currentIndex));
            }
            else break;
         } 
         return retList;
      }
      catch (Exception e) {
         return new ArrayList<InteractiveSearchResult>();
      }
   }
   
   /**
    * Clears the list of search results.
    */
   public void clearSearchResults() {if (searchResults != null) searchResults.clear();}
   
//   public void removeSomeResults(int numberToKeep) {
//      ArrayList<InteractiveSearchResult> subList = new ArrayList<InteractiveSearchResult>();
//      for (int i=0;i<numberToKeep;i++){
//         if ((searchResults.size() - 1) >= i) subList.add(searchResults.get(i));
//         else break;
//      } 
//      searchResults = subList;
//   }
   
  
   
}
