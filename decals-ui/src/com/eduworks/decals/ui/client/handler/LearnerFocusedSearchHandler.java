package com.eduworks.decals.ui.client.handler;

import java.util.ArrayList;

import com.eduworks.decals.ui.client.DsSession;
import com.eduworks.decals.ui.client.DsUserPreferences;
import com.eduworks.decals.ui.client.api.DsESBApi;
import com.eduworks.decals.ui.client.handler.InteractiveSearchHandler.CommentHandlerType;
import com.eduworks.decals.ui.client.handler.InteractiveSearchHandler.RatingHandlerType;
import com.eduworks.decals.ui.client.model.InteractiveSearchResultSetReturn;
import com.eduworks.decals.ui.client.model.SearchHandlerParamPacket;
import com.eduworks.decals.ui.client.pagebuilder.screen.enums.GRADE_LEVEL;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.decals.ui.client.util.OntologyHelper;
import com.eduworks.decals.ui.client.util.SolrResultsResponseParser;
import com.eduworks.decals.ui.client.util.WikiInfoHelper;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.google.gwt.core.client.Callback;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class LearnerFocusedSearchHandler extends InteractiveSearchHandler {
	
	protected boolean ignoreGradeLevelPrefs = false;
	protected boolean ignoreLanguagePrefs = false;
	protected boolean ignoreResourceTypePrefs = false;
	protected boolean ignoreLearningObjectivePrefs = false;
	protected boolean ignoreCompetencyPrefs = false;
	
	public void performInteractiveSearch(String searchTerm, String widgetText, SearchHandlerParamPacket paramPacket,
	         RatingHandlerType ratingHandlerType, CommentHandlerType commentHandlerType, boolean buildAddToCollectionWidgets){
		DsUtil.sendTrackingMessage("Initiated interactive search for \"" + searchTerm + "\"");
	    
		this.searchQuery = DsUtil.cleanString(searchTerm.replace("+", " "));
	    this.widgetText = widgetText;
	    
	    parseParamPacket(paramPacket);
	    showMoreResultsOuterDivId = null;
	    showMoreResultsButtonId = null;  
	    showMoreResultsBusyId = null;     
	    
	    this.ratingHandlerType = ratingHandlerType;
	    this.commentHandlerType = commentHandlerType;
	    this.buildAddToCollectionWidgets = buildAddToCollectionWidgets;
	    actionHandler = new RegistryResourceActionHandler(this,paramPacket);
	   
	    syncId++; 
	    clearSearch();
	    
	    executeLearnerFocusedSearch();
	}
	
	protected void executeLearnerFocusedSearch(){
		final long currentSyncId = syncId;
	    try {
	    	DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(resultsContainerId));
	        setCounterContainerDisplay(false);
	        setSearchBusyDisplay(true);
	        DsUtil.hideLabel(filterNavContaierId);         
	        displayedSearchTerm = searchQuery;
	        setupInitialLearnerFocusedNavigation(currentSyncId);
	    }
	    catch (Exception e) {
	    	if (currentSyncId == syncId) showSearchError("Interactive search failed: " + e.getMessage());
	    }  
	}
	
	protected void setupInitialLearnerFocusedNavigation(final long currentSyncId){
		DsUtil.showLabel(filterNavContaierId);
	    DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
	    hideAllNavigationLineItems();
	    addLearnerFocusedAppliedGradeLevelDisplay();      
	    findAndHandleLearnerFocusedWordOntology(searchQuery, currentSyncId);
	    setUpLearnerFocusedAdvancedOptions(searchQuery);
	}
	
	//Show the applied grade level on the page
	protected void addLearnerFocusedAppliedGradeLevelDisplay() {
		if(DsUserPreferences.getInstance().prefsAreLocal){
			displayLearnerFocusedGradeLevelDisplay();
		}else{
			DsUserPreferences.getInstance().pullDownPreferences(new Callback<ESBPacket, Throwable>(){
				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub	
				}

				@Override
				public void onSuccess(ESBPacket result) {
					displayLearnerFocusedGradeLevelDisplay();
				}
			});
		}
	}
	
	private void displayLearnerFocusedGradeLevelDisplay(){
		DsUtil.showLabel(appliedGradeLevelsContainerId);
		DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(appliedGradeLevelsContainerId));
		StringBuffer sb = new StringBuffer();
		sb.append("<p class=\"" + APPLIED_GRADE_LEVELS_CLASS + "\">");
		sb.append("<b>" + APPLIED_GRADE_LEVELS_DESC + " </b>");
		
		ArrayList<GRADE_LEVEL> prefGradeLevels = DsUserPreferences.getInstance().gradeLevels;
		
		if(prefGradeLevels.contains(GRADE_LEVEL.KINDERGARTEN)) kgGlApplied = true;
		else kgGlApplied = false;
		if(prefGradeLevels.contains(GRADE_LEVEL.ELEMENTARY)) esGlApplied = true;
		else esGlApplied = false;
		if(prefGradeLevels.contains(GRADE_LEVEL.MIDDLE_SCHOOL)) msGlApplied = true;
		else msGlApplied = false;
		if(prefGradeLevels.contains(GRADE_LEVEL.HIGH_SCHOOL)) hsGlApplied = true;
		else hsGlApplied = false;
		if(prefGradeLevels.contains(GRADE_LEVEL.HIGHER_ED)) cuGlApplied = true;
		else cuGlApplied = false;
		if(prefGradeLevels.contains(GRADE_LEVEL.VOCATIONAL)) vtpGlApplied = true;
		else vtpGlApplied = false;
		
		if (kgGlApplied && esGlApplied && msGlApplied && hsGlApplied && cuGlApplied && vtpGlApplied) sb.append(GRADE_LEVEL_ALL_DESC);
		else if (!kgGlApplied && !esGlApplied && !msGlApplied && !hsGlApplied && !cuGlApplied && !vtpGlApplied) sb.append(GRADE_LEVEL_NONE_DESC);
		else {
			if (kgGlApplied) sb.append(GRADE_LEVEL_KG_DESC + ", ");      
			if (esGlApplied) sb.append(GRADE_LEVEL_ES_DESC + ", ");
			if (msGlApplied) sb.append(GRADE_LEVEL_MS_DESC + ", ");
			if (hsGlApplied) sb.append(GRADE_LEVEL_HS_DESC + ", ");
			if (cuGlApplied) sb.append(GRADE_LEVEL_CU_DESC + ", ");
			if (vtpGlApplied) sb.append(GRADE_LEVEL_VTP_DESC + ", ");
			sb.setLength(sb.length() - 2);
		}
		sb.append("</p>");
		RootPanel.get(appliedGradeLevelsContainerId).add(new HTML(sb.toString()));
	}
	
	protected void findAndHandleLearnerFocusedWordOntology(final String word, final long currentSyncId){
		lastWordForOntology = word;
		DsESBApi.decalsDefineWord(word,DEFINE_WORD_LEVELS,new ESBCallback<ESBPacket>() {
			@Override
			public void onSuccess(ESBPacket result) {
				try {        
					if (currentSyncId == syncId) handleLearnerFocusedOntologyResults(word, result);
				}
				catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (define word): " + e2.getMessage());}
			}
			@Override
			public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (define word)");}
		});         
	}
	
	//Handle the results from the word ontology lookup
	private void handleLearnerFocusedOntologyResults(String word, ESBPacket result) {      
		final long currentSyncId = syncId;
		ArrayList<String> nymList;
		if (result.containsKey(ESBApi.ESBAPI_RETURN_OBJ) && result.get(ESBApi.ESBAPI_RETURN_OBJ) instanceof JSONArray && result.getArray(ESBApi.ESBAPI_RETURN_OBJ).size() > 0) {
			if (ontologyHelper == null) {
	            ontologyHelper = new OntologyHelper();
	            ontologyHelper.registerUsedWord(searchQuery.toLowerCase().trim());
			}
			ontologyHelper.initFromDefineWordReturn(result.getArray(ESBApi.ESBAPI_RETURN_OBJ));
			
			if (ontologyHelper.getNumberOfDefinitions() == 0) {
				performLearnerFocusedSolrSearch(searchQuery,RESULTS_PER_PAGE,0,currentSyncId);
				findAndHandleWikiInfo(word,currentSyncId,false);                
			} 
	        else if (ontologyHelper.getNumberOfDefinitions() == 1) {
	            lastNavAnswerIndex = -1;
	            nymList = ontologyHelper.getRelatedSingleWordsForCurrentDefinition(MAX_NUM_DEF_TERMS);
	            if (nymList.size() > 0) searchQuery = word + " " + getStringFromList(nymList);       
	            performLearnerFocusedSolrSearch(searchQuery,RESULTS_PER_PAGE,0,currentSyncId);
	            buildCurrentDefRelatedResultCountsAndPopulateNymNavigation(currentSyncId);
	        } 
	        else if (ontologyHelper.getNumberOfDefinitions() > 1) {
	            lastNavAnswerIndex = -1;
	            nymList = ontologyHelper.getRelatedSingleWordsForAllDefinitions(MAX_NUM_DEF_TERMS);
	            if (nymList.size() > 0) searchQuery = word + WORD_BOOST + " " + getStringFromList(nymList);       
	            performLearnerFocusedSolrSearch(searchQuery,RESULTS_PER_PAGE,0,currentSyncId);
	            addNextMultiDefinitionNavigation(word);
	        }
		}
		else {
			performLearnerFocusedSolrSearch(searchQuery,RESULTS_PER_PAGE,0,currentSyncId);
			findAndHandleWikiInfo(word,currentSyncId,false);
		}
	}
	
	//Performs a solr search with an update to the search history
	private void performLearnerFocusedSolrSearch(final String query, int numberOfRecords, final int start, final long currentSyncId) {
		performLearnerFocusedSolrSearch(query,numberOfRecords,start,currentSyncId,true);
	}
	   
	//Performs a solr search
	private void performLearnerFocusedSolrSearch(final String query, int numberOfRecords, final int start, final long currentSyncId, final boolean updateHistory) {
		DsUtil.setLabelText("realSearchQuery", "Search Query: " + query + getAppliedGradeLevelsQueryString());
		DsESBApi.decalsSolrLearnerFocusedSearch(query + getAppliedGradeLevelsQueryString(),String.valueOf(numberOfRecords),SOLR_QUERY_FIELDS,false,start,new ESBCallback<ESBPacket>() {
			@Override
			public void onSuccess(ESBPacket result) {
				try {        
					if (currentSyncId == syncId) {  
						if (intSearchResultSet == null) intSearchResultSet = new InteractiveSearchResultSetReturn();
						if (start == 0) intSearchResultSet.clearSearchResults(); 
						if (updateHistory) updateSearchHistory(displayedSearchTerm,query);
						SolrResultsResponseParser.parseSolrRegistryResponse(result.getObject(ESBApi.ESBAPI_RETURN_OBJ), intSearchResultSet, DsSession.getInstance().getInteractiveSearchThumbnailRootUrl());                                    
						populateInteractiveResults(start,currentSyncId);
					}
				}
				catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results: " + e2.getMessage());}
			}
			@Override
			public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server");}
		});         
	}
	
	//Build the wiki info lookup data
	private void findAndHandleWikiInfo(final String title, final long currentSyncId, final boolean goToTopics) {
		//DsESBApi.decalsWikiInfo(title,new ESBCallback<ESBPacket>() {
		//for some reason wiki api doesn't always like caps (ex. Small Hive Beetle vs. small hive beetle)
		DsESBApi.decalsWikiInfo(title.toLowerCase(),new ESBCallback<ESBPacket>() { 
			@Override
			public void onSuccess(ESBPacket result) {
				try {        
					if (currentSyncId == syncId) handleWikiInfoResults(title, result, goToTopics);
				}
				catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (wiki info): " + e2.getMessage());}
			}
			@Override
			public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (wiki info)");}
		});         
	}
	
	//Handle the results of the wiki info lookup
	private void handleWikiInfoResults(String title, ESBPacket result, boolean goToTopics) {
		DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
		final long currentSyncId = syncId;     
		if (result.containsKey(ESBApi.ESBAPI_RETURN_OBJ)) {         
			if (wikiInfoHelper == null) wikiInfoHelper = new WikiInfoHelper();
			
			wikiInfoHelper.initFromWikiInfoReturn(title,result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject(),true,true);
			
			if (wikiInfoHelper.getHasGoodInfo()) {
				addWordDefinition(title,wikiInfoHelper.getDescriptionExtract());
	            //addWordDefinition(title,wikiInfoHelper.getDescriptionExtract() + "<br><br>" + wikiInfoHelper.getNonMarkedDescriptionExtract());
	            registerExtractMarkupLinks();            
	            if (wikiInfoHelper.getNumberOfGoodCategories() > 0 && !goToTopics) {
	            	lastNavAnswerIndex = -1;
	            	addNextCategoryNavigation(); 
	            }
	            else if (wikiInfoHelper.getNumberOfGoodTopics() > 0) {
	            	lastNavAnswerIndex = -1;
	            	buildTopicResultCountsAndPopulateTopicNavigation(currentSyncId);
	            }
	            else showGradeLevelNavigation();
			}
			else showGradeLevelNavigation();
		}
		else showGradeLevelNavigation();
	}
	   
	
	/* Advanced Options */
	
	protected void setUpLearnerFocusedAdvancedOptions(String word) {
		DsUtil.hideLabel(filterNavMoreOptDisambigLiId);
		DsUtil.hideLabel(filterNavMoreOptCatLiId);
		DsUtil.hideLabel(filterNavMoreOptTopicLiId);      
		long currentSyncId = syncId;        
		setUpLearnerFocusedAdvancedOptionsStep1(word,currentSyncId);
	}
	
	private void setUpLearnerFocusedAdvancedOptionsStep1(final String word, final long currentSyncId) {
		DsESBApi.decalsDefineWord(word,DEFINE_WORD_LEVELS,new ESBCallback<ESBPacket>() {
	         @Override
	         public void onSuccess(ESBPacket result) {
	            try {        
	               if (currentSyncId == syncId) {
	                  if (result.containsKey(ESBApi.ESBAPI_RETURN_OBJ) && result.get(ESBApi.ESBAPI_RETURN_OBJ) instanceof JSONArray && result.getArray(ESBApi.ESBAPI_RETURN_OBJ).size() > 0) {         
	                     OntologyHelper aooh = new OntologyHelper();
	                     aooh.initFromDefineWordReturn(result.getArray(ESBApi.ESBAPI_RETURN_OBJ));
	                     if (aooh.getNumberOfDefinitions() > 0) DsUtil.showLabel(filterNavMoreOptDisambigLiId); 
	                  }
	                  setUpLearnerFocusedAdvancedOptionsStep2(word,currentSyncId);
	               }
	            }
	            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (setUpAdvancedOptionsStep1): " + e2.getMessage());}
	         }
	         @Override
	         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (setUpAdvancedOptionsStep1)");}
	      });  
	}
	
	private void setUpLearnerFocusedAdvancedOptionsStep2(final String title, final long currentSyncId) {   
		DsESBApi.decalsWikiInfo(title.toLowerCase(),new ESBCallback<ESBPacket>() { 
			@Override
			public void onSuccess(ESBPacket result) {
				try {        
					if (currentSyncId == syncId) {
						if (result.containsKey(ESBApi.ESBAPI_RETURN_OBJ)) {         
							WikiInfoHelper aowih = new WikiInfoHelper();
							aowih.initFromWikiInfoReturn(title,result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject(),false,false);
							if (aowih.getNumberOfGoodCategories() > 0) DsUtil.showLabel(filterNavMoreOptCatLiId);
							if (aowih.getNumberOfGoodTopics() > 0) DsUtil.showLabel(filterNavMoreOptTopicLiId); 
						}
						//	DsUtil.showLabel(filterNavMoreOptGrdLvlLiId);
					}
				}
				catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (setUpAdvancedOptionsStep2): " + e2.getMessage());}
			}
			@Override
			public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (setUpAdvancedOptionsStep2)");}
		}); 
	}
	
}
