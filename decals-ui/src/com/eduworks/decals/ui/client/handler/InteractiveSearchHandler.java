package com.eduworks.decals.ui.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.eduworks.decals.ui.client.DsSession;
import com.eduworks.decals.ui.client.api.DsESBApi;
import com.eduworks.decals.ui.client.model.InteractiveSearchResultSetReturn;
import com.eduworks.decals.ui.client.model.WikiInfoItem;
import com.eduworks.decals.ui.client.model.WordOntologyDefinition;
import com.eduworks.decals.ui.client.model.WordOntologyItem;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.decals.ui.client.util.OntologyHelper;
import com.eduworks.decals.ui.client.util.WikiInfoHelper;
import com.eduworks.decals.ui.client.util.SearchResultWidgetGenerator;
import com.eduworks.decals.ui.client.util.SolrCruncherResponseParser;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Class for dealing with interactive search.
 * 
 * @author Tom Buskirk
 * 
 * All of this keyword and definition search term building is just 'try an see'.  I don't know if there is a good way to search for something based on it's definition.
 * 
 */
public class InteractiveSearchHandler extends SearchHandler {
   
   private static final String WORD_DEF_CLASS = "wordDef";
   private static final String APPLIED_GRADE_LEVELS_CLASS = "appliedGradeLevels"; 
   
   private static final int MAX_NUM_DEF_TERMS = 200;
   
   private static final String WORD_BOOST = "^4";
   
   private static final String TH_KEYWORDS_KEY = "keywords";
   private static final String TH_NOUNS_KEY = "nouns";
   //private static final String TH_TEXT_KEY = "text";
   
   private static final String MORE_OPTS_MORE_TEXT = "More Options";
   private static final String MORE_OPTS_LESS_TEXT = "Less Options";
   
   //private static final String TEMP_NAV_QUESTION = "Initializing helper...";
   private static final String TEMP_NAV_QUESTION = "Searching...";
   
   private static final String MULTI_DEFINITION_NAV_QUESTION_PREFIX = "Did you mean ";
   private static final String MULTI_DEFINITION_NAV_QUESTION_SUFFIX = " defined as:";
   private static final String NYM_NAV_QUESTION_PREFIX = "Are you looking for:";   
   private static final String RELATED_TOPICS_QUESTION_PREFIX = "Related topics:";
   private static final String RELATED_CATEGORIES_QUESTION_PREFIX = "Related categories:";
   private static final String GRADE_LEVELS_QUESTION_PREFIX = "Grade Levels:";
   
   private static final String SOLR_KEYWORDS_FIELD = "keywords";
   private static final String SOLR_TITLE_FIELD = "title";
   private static final String SOLR_DESC_FIELD = "description";   
   private static final String SOLR_GRADE_LEVELS_FIELD = "grade_levels";
   private static final String SOLR_QUERY_FIELDS = "score " + SOLR_KEYWORDS_FIELD + " " + SOLR_TITLE_FIELD + " " + SOLR_DESC_FIELD + " author create_date url publisher thumbnail url_status";
   
   private static final String GRADE_LEVEL_QUERY_STRING_PREFIX = " AND ( ";
   private static final String GRADE_LEVEL_QUERY_STRING_SUFFIX = " )";
   
   private static final String GRADE_LEVEL_KG = "KG";   
   private static final String GRADE_LEVEL_ES = "ES";
   private static final String GRADE_LEVEL_MS = "MS";
   private static final String GRADE_LEVEL_HS = "HS";
   private static final String GRADE_LEVEL_CU = "CU";
   private static final String GRADE_LEVEL_VTP = "VTP";
   
   private static final String APPLIED_GRADE_LEVELS_DESC = "Applied Grade Levels:";
   private static final String GRADE_LEVEL_ALL_DESC = "All";
   private static final String GRADE_LEVEL_NONE_DESC = "None";
   private static final String GRADE_LEVEL_KG_DESC = "Kindergarten";   
   private static final String GRADE_LEVEL_ES_DESC = "Elementary School";
   private static final String GRADE_LEVEL_MS_DESC = "Middle School";
   private static final String GRADE_LEVEL_HS_DESC = "High School";
   private static final String GRADE_LEVEL_CU_DESC = "College/University";
   private static final String GRADE_LEVEL_VTP_DESC = "Vocational/Technical/Professional";
   
   private static final int DEFINE_WORD_LEVELS = 3;
   
   private static final int RESULT_COUNT_UPPER_LIMIT = 10000;
   
   private static final int RESULTS_PER_PAGE = 25;
   //private static final int NUMBER_OF_NAV_ANSWERS = 3;
   
   private static final String DEFINITION_MODE_ANSWER_PREFIX = "defNavAnswer_";
   private static final String NYM_MODE_ANSWER_PREFIX = "nymNavAnswer_";
   private static final String CATEGORY_MODE_ANSWER_PREFIX = "catNavAnswer_";
   private static final String TOPIC_MODE_ANSWER_PREFIX = "topNavAnswer_"; 
   
   private static final String NAV_ANSWER_SUFFIX = "<i class=\"fa fa-chevron-circle-right\"></i>";
   
   private static final String SHOW_MORE_RESULTS_ID_SUFFIX = "-nsh";
   
   private enum NavMode{DEFINITION,NYM,CATEGORY,TOPIC,GRADE}
   private enum MoreOptsMode{MORE,LESS}
   private enum GradeLevelToggleMode{ALL,NONE}
   
   private InteractiveSearchResultSetReturn intSearchResultSet; 
   private int lastResultStart;
   private int lastNavAnswerIndex;
   private String lastWordForOntology;
   
   private NavMode currentNavMode;
   private MoreOptsMode currentMoreOptsMode = MoreOptsMode.LESS;
   private GradeLevelToggleMode currentGradeLevelToggleMode = GradeLevelToggleMode.ALL;
   
   private OntologyHelper ontologyHelper;
   private WikiInfoHelper wikiInfoHelper;
      
   private String counterStatementDisplayTerm;
   
   @SuppressWarnings("unused")
   private String breadCrumbElementId;
   @SuppressWarnings("unused")
   private String filterNavMoreOptGrdLvlLiId;
   
   private String wordDefContainerId;   
   private String appliedGradeLevelsContainerId;
   private String filterNavContaierId;
   private String filterNavQuestionId;
   private String filterNavAnswer1LiId;
   private String filterNavAnswer2LiId;
   private String filterNavAnswer3LiId;
   private String filterNavLinksId;
   private String filterNavMoreAnswersLinkId;
   private String filterNavMoreOptionsLinkId;
   private String filterNavGrdLvlKgLiId;
   private String filterNavGrdLvlEsLiId;
   private String filterNavGrdLvlMsLiId;
   private String filterNavGrdLvlHsLiId;
   private String filterNavGrdLvlCuLiId;
   private String filterNavGrdLvlVtpLiId;
   private String filterNavGrdLvlApplyLiId;
   private String filterNavGrdLvlApplyLinkId;
   private String filterNavGrdLvlToggleLiId;
   private String filterNavGrdLvlToggleLinkId;
   private String filterNavGrdLvlKgCbId;
   private String filterNavGrdLvlEsCbId;
   private String filterNavGrdLvlMsCbId;
   private String filterNavGrdLvlHsCbId;
   private String filterNavGrdLvlCuCbId;
   private String filterNavGrdLvlVtpCbId;
   private String filterNavMoreOptLinkTxtId;
   private String filterNavMoreOptContainerId;
   private String filterNavMoreOptDisambigLiId;
   private String filterNavMoreOptCatLiId;
   private String filterNavMoreOptTopicLiId;
   private String filterNavMoreOptDisambigId;
   private String filterNavMoreOptCatId;
   private String filterNavMoreOptTopicId;
   private String filterNavMoreOptGrdLvlId;
   private String searchTermBoxId;
   
   private boolean kgGlApplied = true;
   private boolean esGlApplied = true;
   private boolean msGlApplied = true;
   private boolean hsGlApplied = true;
   private boolean cuGlApplied = true;
   private boolean vtpGlApplied = true;
   
   /**
    * Listener for show more results click.
    */
   protected EventCallback showMoreResultsListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         int newStartResult = lastResultStart + RESULTS_PER_PAGE;      
         hideShowMoreResultsButton();
         showShowMoreResultsBusyImage();
         performSolrSearch(searchTerm,RESULTS_PER_PAGE,newStartResult,syncId);                           
      }
   };
   
   /**
    * Listener for show something else click.
    */
   protected EventCallback showMoreAnswersListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
         if (currentNavMode.equals(NavMode.DEFINITION)) addNextMultiDefinitionNavigation(lastWordForOntology);
         else if (currentNavMode.equals(NavMode.NYM)) addNextNymNavigation();
         else if (currentNavMode.equals(NavMode.TOPIC)) addNextTopicNavigation();
         else if (currentNavMode.equals(NavMode.CATEGORY)) addNextCategoryNavigation();
         else if (currentNavMode.equals(NavMode.GRADE)) findAndHandleWordOntology(searchTerm, syncId);
      }
   };
   
   /**
    * Listener for show more options click. 
    */
   protected EventCallback showMoreOptionsListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         handleShowMoreOptionsSelect(syncId);
      }
   };
   
   /**
    * Listener for disambiguate advanced options click.
    */
   protected EventCallback aoDisambigSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            hideAllNavigationLineItems();
            DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
            findAndHandleWordOntology(lastWordForOntology, syncId);
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for related categories advanced options click.
    */
   protected EventCallback aoCatSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            hideAllNavigationLineItems();
            DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
            findAndHandleWikiInfo(lastWordForOntology,syncId,false);
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for related topics advanced options click.
    */
   protected EventCallback aoTopicSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            hideAllNavigationLineItems();
            DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
            findAndHandleWikiInfo(lastWordForOntology,syncId,true);
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for grade level advanced options click.
    */
   protected EventCallback aoGrdLvlSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            showGradeLevelNavigation();
         }
         catch (Exception e) {}
      }
   };   
   
   //Returns the index value for the given element ID
   private int getIndexFromElementId(String elementId, String prefix) {
      return Integer.parseInt(elementId.substring(prefix.length()));
   }
   
   /**
    * Listener for apply grade levels click.
    */
   protected EventCallback applyGradeLevelSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            handleApplyGradeLevel(syncId);
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for toggle selected grade levels click.
    */
   protected EventCallback toggleGradeLevelsSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            handleToggleSelectedGradeLevels();
         }
         catch (Exception e) {}
      }
   };
   
//   private int getIndexFromMarkupElementId(String elementId, String prefix) {
//      int start = elementId.indexOf(prefix) + prefix.length();      
//      int end = elementId.indexOf("_",start);
//      if (end != -1) return Integer.parseInt(elementId.substring(start,end));
//      else return Integer.parseInt(elementId.substring(start));
//   }
   
   /**
    * Listener for extract hyperlink selection.
    */
   protected EventCallback extractMarkupSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {            
            Element e = Element.as(event.getEventTarget());
            String id = e.getId();
            if (id.indexOf(WikiInfoHelper.CATEGORY_MARKUP_TYPE) >= 0)  handleCategorySelection(DsUtil.getAnchorText(id),syncId);
            else if (id.indexOf(WikiInfoHelper.TOPIC_MARKUP_TYPE) >= 0) handleTopicSelection(DsUtil.getAnchorText(id),syncId);
            else return;            
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for definition selected.
    */
   protected EventCallback definitionSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            String id;
            Element e = Element.as(event.getEventTarget());
            if (e.getId() == null || e.getId().trim().isEmpty()) id = e.getParentElement().getId();
            else id = e.getId();            
            int itemIdx = getIndexFromElementId(id,DEFINITION_MODE_ANSWER_PREFIX);
            handleDefinitionSelection(itemIdx,syncId);
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for related word selection.
    */
   protected EventCallback relatedWordSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            String id;
            Element e = Element.as(event.getEventTarget());
            if (e.getId() == null || e.getId().trim().isEmpty()) id = e.getParentElement().getId();
            else id = e.getId();            
            int itemIdx = getIndexFromElementId(id,NYM_MODE_ANSWER_PREFIX);
            handleRelatedWordSelection(itemIdx,syncId);        
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for related category selection.
    */
   protected EventCallback categorySelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            String id;
            Element e = Element.as(event.getEventTarget());
            if (e.getId() == null || e.getId().trim().isEmpty()) id = e.getParentElement().getId();
            else id = e.getId();            
            int itemIdx = getIndexFromElementId(id,CATEGORY_MODE_ANSWER_PREFIX);
            handleCategorySelection(itemIdx,syncId);        
         }
         catch (Exception e) {}
      }
   };
   
   /**
    * Listener for related topic selection.
    */
   protected EventCallback topicSelectedListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         syncId++;
         try {
            String id;
            Element e = Element.as(event.getEventTarget());
            if (e.getId() == null || e.getId().trim().isEmpty()) id = e.getParentElement().getId();
            else id = e.getId();            
            int itemIdx = getIndexFromElementId(id,TOPIC_MODE_ANSWER_PREFIX);
            handleTopicSelection(itemIdx,syncId);        
         }
         catch (Exception e) {}
      }
   };
   
   //Handle apply grade level selection
   private void handleApplyGradeLevel(final long currentSyncId) {  
      updateAppliedGradeLevels();      
      addAppliedGradeLevelDisplay();
      performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);            
   }
   
   //Update grade level flags based on check box values.
   private void updateAppliedGradeLevels() {
      kgGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlKgCbId);      
      esGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlEsCbId);
      msGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlMsCbId);
      hsGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlHsCbId);
      cuGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlCuCbId);
      vtpGlApplied = DsUtil.isCheckBoxChecked(filterNavGrdLvlVtpCbId);            
   }
   
   //Handle show more options selected
   private void handleShowMoreOptionsSelect(final long currentSyncId) {
      if (currentMoreOptsMode.equals(MoreOptsMode.LESS)) {
         DsUtil.showLabel(filterNavMoreOptContainerId);
         currentMoreOptsMode = MoreOptsMode.MORE;
         DsUtil.setLabelText(filterNavMoreOptLinkTxtId,MORE_OPTS_LESS_TEXT);
         PageAssembler.attachHandler(filterNavMoreOptDisambigId,Event.ONCLICK,aoDisambigSelectedListener);
         PageAssembler.attachHandler(filterNavMoreOptCatId,Event.ONCLICK,aoCatSelectedListener);
         PageAssembler.attachHandler(filterNavMoreOptTopicId,Event.ONCLICK,aoTopicSelectedListener);
         PageAssembler.attachHandler(filterNavMoreOptGrdLvlId,Event.ONCLICK,aoGrdLvlSelectedListener);
      }
      else {
         DsUtil.hideLabel(filterNavMoreOptContainerId);
         currentMoreOptsMode = MoreOptsMode.LESS;
         DsUtil.setLabelText(filterNavMoreOptLinkTxtId,MORE_OPTS_MORE_TEXT);
      }
   }
   
   //Check grade level check boxes for check status 
   private void checkCheckBoxStatus() {
      if (DsUtil.isCheckBoxChecked(filterNavGrdLvlKgCbId) &&      
          DsUtil.isCheckBoxChecked(filterNavGrdLvlEsCbId) &&
          DsUtil.isCheckBoxChecked(filterNavGrdLvlMsCbId) &&          
          DsUtil.isCheckBoxChecked(filterNavGrdLvlHsCbId) &&          
          DsUtil.isCheckBoxChecked(filterNavGrdLvlCuCbId) &&
          DsUtil.isCheckBoxChecked(filterNavGrdLvlVtpCbId)) currentGradeLevelToggleMode = GradeLevelToggleMode.ALL;
      else if (!DsUtil.isCheckBoxChecked(filterNavGrdLvlKgCbId) &&      
               !DsUtil.isCheckBoxChecked(filterNavGrdLvlEsCbId) &&
               !DsUtil.isCheckBoxChecked(filterNavGrdLvlMsCbId) &&          
               !DsUtil.isCheckBoxChecked(filterNavGrdLvlHsCbId) &&          
               !DsUtil.isCheckBoxChecked(filterNavGrdLvlCuCbId) &&
               !DsUtil.isCheckBoxChecked(filterNavGrdLvlVtpCbId)) currentGradeLevelToggleMode = GradeLevelToggleMode.NONE;
   }
   
   //Handle show more options selected
   private void handleToggleSelectedGradeLevels() {
      checkCheckBoxStatus();
      if (currentGradeLevelToggleMode.equals(GradeLevelToggleMode.ALL)) {
         currentGradeLevelToggleMode = GradeLevelToggleMode.NONE;
         applyAllGradeLevelCheckBoxValues(false);
      }
      else {
         currentGradeLevelToggleMode = GradeLevelToggleMode.ALL;
         applyAllGradeLevelCheckBoxValues(true);
      }
   }
   
   //Apply the value to all grade level check boxes
   private void applyAllGradeLevelCheckBoxValues(boolean value) {
      DsUtil.applyCheckBoxValue(filterNavGrdLvlKgCbId,value);      
      DsUtil.applyCheckBoxValue(filterNavGrdLvlEsCbId,value);
      DsUtil.applyCheckBoxValue(filterNavGrdLvlMsCbId,value);
      DsUtil.applyCheckBoxValue(filterNavGrdLvlHsCbId,value);
      DsUtil.applyCheckBoxValue(filterNavGrdLvlCuCbId,value);
      DsUtil.applyCheckBoxValue(filterNavGrdLvlVtpCbId,value);
   }
   
   //Handle topic selections when passed the topic string
   private void handleTopicSelection(String topic, final long currentSyncId) {
      int topicIdx = wikiInfoHelper.getRelatedTopicIndex(topic);
      if (topicIdx != -1) handleTopicSelection(topicIdx,currentSyncId);      
   }
   
   //Handle category selections when passed the category string
   private void handleCategorySelection(String category, final long currentSyncId) {
      int categoryIdx = wikiInfoHelper.getRelatedCategoryIndex(category);
      if (categoryIdx != -1) handleCategorySelection(categoryIdx,currentSyncId);
   }
   
   //Handle topic selection when passed the topic index   
   private void handleTopicSelection(int idx, final long currentSyncId) {
      WikiInfoItem wii = wikiInfoHelper.getRelatedTopics().get(idx);
      wii.setWikiLookupValue(wii.getName());
      wii.setSearchTerm(wii.getName());
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
      setCounterContainerDisplay(false);
      setSearchBusyDisplay(true);
      DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
      hideAllNavigationLineItems();
      counterStatementDisplayTerm = wii.getName();
      searchTerm = wii.getSearchTerm();
      clearWordDefinition();
      //performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);      
      findAndHandleWordOntology(wii.getName(), currentSyncId);
      setUpAdvancedOptions(wii.getName());
      setSearchTermBoxText(wii.getName());
   }
   
   //Handle category selection when passed the category index
   private void handleCategorySelection(int idx, final long currentSyncId) {
      WikiInfoItem wii = wikiInfoHelper.getRelatedCategories().get(idx);
      wii.setWikiLookupValue(wii.getName());
      wii.setSearchTerm(wii.getName());
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
      setCounterContainerDisplay(false);
      setSearchBusyDisplay(true);
      DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
      hideAllNavigationLineItems();
      counterStatementDisplayTerm = wii.getName();
      searchTerm = wii.getSearchTerm();
      clearWordDefinition();
      //performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);      
      findAndHandleWordOntology(wii.getName(), currentSyncId);
      setUpAdvancedOptions(wii.getName());
      setSearchTermBoxText(wii.getName());
   }
   
   //Handle related words selection
   private void handleRelatedWordSelection(int idx, final long currentSyncId) {
      WordOntologyItem woi = ontologyHelper.getCurrentDefinition().getRelatedWords().get(idx);
      woi.setWikiLookupValue(woi.getWord());
      if (woi.getWord().toLowerCase().indexOf(ontologyHelper.getCurrentDefinition().getWord().toLowerCase()) >= 0) {
         woi.setSearchValue(woi.getWord());
      }
      else {
         woi.setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + woi.getWord());
      }
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
      setCounterContainerDisplay(false);
      setSearchBusyDisplay(true);
      DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
      hideAllNavigationLineItems();
      counterStatementDisplayTerm = woi.getWord();
      searchTerm = woi.getSearchValue();
      clearWordDefinition();
      //performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);      
      findAndHandleWordOntology(woi.getWord(), currentSyncId);
      setUpAdvancedOptions(woi.getWord());
      setSearchTermBoxText(woi.getWord());
   }
   
   //Handle definition selection
   //There are a lot of adhoc things going on here trying to find the best combination of words from the definition
   // to use to perform the search query.
   private void handleDefinitionSelection(int idx, final long currentSyncId) {
      DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
      hideAllNavigationLineItems();
      ontologyHelper.applyAsCurrentDefintion(idx);      
      if (ontologyHelper.getCurrentDefinition().hasDomain()) {
         if (ontologyHelper.getCurrentDefinition().getDomain().equalsIgnoreCase(ontologyHelper.getCurrentDefinition().getWord())) {
            ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord());
            ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord());
         }
         else {
            ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + ontologyHelper.getCurrentDefinition().getDomain());
            ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + ontologyHelper.getCurrentDefinition().getDomain());
         }
         finishDefinitionSelectedActions(currentSyncId);
      }
      else buildSearchValuesForNonDomainDefintion(currentSyncId);
   }
   
   //Build the search counter display for the current definition
   private String buildFancyCounterStatementDisplayTermForCurrentDefinition() {
      String detail = ontologyHelper.getCurrentDefinition().getWikiLookupValue().toLowerCase().replace(ontologyHelper.getCurrentDefinition().getWord().toLowerCase(),"").trim();
      StringBuffer sb = new StringBuffer();
      sb.append(ontologyHelper.getCurrentDefinition().getWord());
      if (detail != null && !detail.trim().isEmpty()) sb.append(" (" + detail + ")");
      return sb.toString();      
   }
   
   //After deciding how to perform the definition search, perform it and update the appropriate page elements
   private void finishDefinitionSelectedActions(final long currentSyncId) {
      addWordDefinition(ontologyHelper.getCurrentDefinition());
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
      setCounterContainerDisplay(false);
      setSearchBusyDisplay(true);
      //counterStatementDisplayTerm = ontologyHelper.getCurrentDefinition().getWikiLookupValue();
      counterStatementDisplayTerm = buildFancyCounterStatementDisplayTermForCurrentDefinition();
      ArrayList<String >nymList = ontologyHelper.getRelatedSingleWordsForCurrentDefinition(MAX_NUM_DEF_TERMS);
      if (nymList.size() > 0) searchTerm = ontologyHelper.getCurrentDefinition().getWord() + " " + getStringFromList(nymList);
      else searchTerm = ontologyHelper.getCurrentDefinition().getSearchValue();
      performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
      lastNavAnswerIndex = -1;
      lastWordForOntology = ontologyHelper.getCurrentDefinition().getWikiLookupValue();
      buildCurrentDefRelatedResultCountsAndPopulateNymNavigation(currentSyncId);
      setUpAdvancedOptions(ontologyHelper.getCurrentDefinition().getWikiLookupValue());
      setSearchTermBoxText(ontologyHelper.getCurrentDefinition().getWord());
      
   }
   
   //Returns a list of strings from a JSON array of strings
   private ArrayList<String> getListFromJsonArray(JSONArray ja) {
      ArrayList<String> strList = new ArrayList<String>();
      for (int i=0;i<ja.size();i++) {
         strList.add(ja.get(i).isString().stringValue());
      }
      return strList;
   }
   
   //Handle definitions that have a one noun definition
   private void handleWithOneWord(String word) {
      if (ontologyHelper.getCurrentDefinition().getWord().equalsIgnoreCase(word)) {
         ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord());
         ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord());
      }
      else {
         ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + word);
         ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + word);
      }
   }
   
   //Handle definitions where keywords occur multiple times
   private ArrayList<String> getMultiOccuringWords(ArrayList<String> kwList) {
      ArrayList<String> multiOccuringList = new ArrayList<String>();
      HashMap<String,Long> wordMap = new HashMap<String,Long>();
      String[] kwParts;
      for (String kw:kwList) {
         kwParts = kw.split(" ");
         for (String kwp:kwParts) {
            if (wordMap.containsKey(kwp.toLowerCase())) wordMap.put(kwp.toLowerCase(),wordMap.get(kwp.toLowerCase()) + 1);
            else wordMap.put(kwp.toLowerCase(),new Long(1));
         }
      }      
      Iterator<String> it = wordMap.keySet().iterator();
      String key;
      while (it.hasNext()) {
         key = it.next();
         if (wordMap.get(key) > 1) multiOccuringList.add(key);
      }
      return multiOccuringList;
   }
   
   //Builds a string from all the values in the given array list of strings
   private String getStringFromList(ArrayList<String> sl) {
      StringBuffer sb = new StringBuffer();
      for (String s:sl) sb.append(s + " ");
      return sb.toString().trim();
   }
   
   //
//   private String getHighestValueKeyword(JSONObject kwo) {
//      double highScore = 0;
//      String bestKeyword = null;
//      String key;
//      Iterator<String> it = kwo.keySet().iterator();
//      double value;
//      while (it.hasNext()) {
//         key = it.next();
//         value = kwo.get(key).isNumber().doubleValue();
//         if (kwo.get(key).isNumber().doubleValue() > highScore) {
//            bestKeyword = key;
//            highScore = value;
//         }
//      }
//      return bestKeyword;
//   }
   
   //Handle definitions with multiple keywords
   private void handleWithMultipleKeywords(ArrayList<String> kwList, ArrayList<String> nounList) {
      ArrayList<String> mowl = getMultiOccuringWords(kwList);
      if (mowl.size() > 0) {
         String mowlString = getStringFromList(mowl);
         ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + mowlString);
         ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + mowlString);
      }
      else {
         ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + kwList.get(0));
         if (nounList != null && nounList.size() > 0) {
            ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + getStringFromList(nounList));
         }
         else {
            ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + 
                                                                 DsUtil.removeStopWords(ontologyHelper.getCurrentDefinition().getDefinition()));
         }
      }
   }
   
   //Handle definition with noun list
   private void handleWithNouns(ArrayList<String> nounList) {
      ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + getStringFromList(nounList));
      ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + getStringFromList(nounList));      
   }
   
   //Handle definition with definition text 
   private void handleWithDefinitionText() {
      ontologyHelper.getCurrentDefinition().setWikiLookupValue(ontologyHelper.getCurrentDefinition().getWord() + " " + DsUtil.removeStopWords(ontologyHelper.getCurrentDefinition().getDefinition()));
      ontologyHelper.getCurrentDefinition().setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + DsUtil.removeStopWords(ontologyHelper.getCurrentDefinition().getDefinition()));      
   }
   
   //Decide how to handle definition
   private void parseAndHandleNonDomainDefintionSearchValueResults(JSONObject result, final long currentSyncId) {
      ArrayList<String> kwList = null;
      ArrayList<String> nounList = null;
      if (result.containsKey(TH_KEYWORDS_KEY)) kwList = getListFromJsonArray(result.get(TH_KEYWORDS_KEY).isArray());
      if (result.containsKey(TH_NOUNS_KEY)) nounList = getListFromJsonArray(result.get(TH_NOUNS_KEY).isArray());
      if (nounList != null && nounList.size() == 1) handleWithOneWord(nounList.get(0));
      else if (kwList != null && kwList.size() == 1) handleWithOneWord(kwList.get(0));
      else if (kwList != null && kwList.size() > 1) handleWithMultipleKeywords(kwList,nounList);
      else if (nounList != null && nounList.size() > 0) handleWithNouns(nounList);
      else handleWithDefinitionText();
      finishDefinitionSelectedActions(currentSyncId);      
   }
   
   //Get definition information
   private void buildSearchValuesForNonDomainDefintion(final long currentSyncId) {
      DsESBApi.decalsGetTextHighlights(ontologyHelper.getCurrentDefinition().getDefinition().replaceAll("[^A-Za-z0-9 ;,.]", ""),new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            try {        
               if (currentSyncId == syncId) parseAndHandleNonDomainDefintionSearchValueResults(result.getObject(ESBApi.ESBAPI_RETURN_OBJ),currentSyncId);
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (buildSearchValuesForNonDomainDefintion): " + e2.getMessage());}
         }
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server(buildSearchValuesForNonDomainDefintion)");}
         });   
      
   }
  
   //Add the show more results listener
   @Override
   protected void addShowMoreResultsHandler() {
      PageAssembler.attachHandler(showMoreResultsButtonId,Event.ONCLICK,showMoreResultsListener);
   }

   @Override
   protected String getShowMoreResultsIdSuffix() {return SHOW_MORE_RESULTS_ID_SUFFIX;}
   
   //Build the search counter statement
   private String getSearchCounterStatement() {
      long numShown = 0;
      if ((lastResultStart + RESULTS_PER_PAGE) > intSearchResultSet.getNumTotalResultsFound()) numShown = intSearchResultSet.getNumTotalResultsFound();
      else numShown = lastResultStart + RESULTS_PER_PAGE;
      return "Showing " + counterFormat.format(numShown) + " of " +  counterFormat.format(intSearchResultSet.getNumTotalResultsFound()) + " results for \"" + counterStatementDisplayTerm + "\"";      
   }
   
   //Populate the page with the interactive search results
   private void populateInteractiveResults(int start, final long currentSyncId) {
      if (currentSyncId == syncId) {
         if (start == 0) DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
         lastResultStart = start;      
         hideShowMoreResultsDiv();                 
         setCounterContainerDisplay(true);
         setSearchBusyDisplay(false);  
         DsUtil.setLabelText(counterElementId, getSearchCounterStatement());      
         SearchResultWidgetGenerator srwg = new SearchResultWidgetGenerator();
         srwg.addInteractiveSearchResultWidgets(intSearchResultSet.getSearchResults(start,RESULTS_PER_PAGE),parentElementId,widgetText);
         if (intSearchResultSet.getNumTotalResultsFound() > (lastResultStart + RESULTS_PER_PAGE)) addShowMoreResultsButton();               
      }
   }
   
   //Build the grade level query string
   private String getAppliedGradeLevelsQueryString() {
      if (kgGlApplied && esGlApplied && msGlApplied && hsGlApplied && cuGlApplied && vtpGlApplied) return "";
      if (!kgGlApplied && !esGlApplied && !msGlApplied && !hsGlApplied && !cuGlApplied && !vtpGlApplied) return "";
      StringBuffer sb = new StringBuffer();
      sb.append(GRADE_LEVEL_QUERY_STRING_PREFIX);
      if (kgGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_KG + " OR ");      
      if (esGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_ES + " OR ");
      if (msGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_MS + " OR ");
      if (hsGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_HS + " OR ");
      if (cuGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_CU + " OR ");
      if (vtpGlApplied) sb.append(SOLR_GRADE_LEVELS_FIELD + ":" + GRADE_LEVEL_VTP + " OR ");
      sb.setLength(sb.length() - 4);
      sb.append(GRADE_LEVEL_QUERY_STRING_SUFFIX);
      return sb.toString();      
   }
   
   //Performs a solr search
   private void performSolrSearch(String query, int numberOfRecords, final int start, final long currentSyncId) {
      DsUtil.setLabelText("realSearchQuery", "Search Query: " + query + getAppliedGradeLevelsQueryString());
      DsESBApi.decalsSolrSearch(DsSession.getInstance().getSolrUrl(),query + getAppliedGradeLevelsQueryString(),String.valueOf(numberOfRecords),SOLR_QUERY_FIELDS,false,start,new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            try {        
               if (currentSyncId == syncId) {  
                  if (intSearchResultSet == null) intSearchResultSet = new InteractiveSearchResultSetReturn();
                  if (start == 0) intSearchResultSet.clearSearchResults(); 
                  SolrCruncherResponseParser.parseSolrCruncherResponse(result.getObject(ESBApi.ESBAPI_RETURN_OBJ), intSearchResultSet, DsSession.getInstance().getInteractiveSearchThumbnailRootUrl());                                    
                  populateInteractiveResults(start,currentSyncId);
               }
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results: " + e2.getMessage());}
         }
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server");}
         });         
   }
   
   //Show the applied grade level on the page
   private void addAppliedGradeLevelDisplay() {
      DsUtil.showLabel(appliedGradeLevelsContainerId);
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(appliedGradeLevelsContainerId));
      StringBuffer sb = new StringBuffer();
      sb.append("<p class=\"" + APPLIED_GRADE_LEVELS_CLASS + "\">");
      sb.append("<b>" + APPLIED_GRADE_LEVELS_DESC + " </b>");
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
   
   //Add a navigation answer
   private void addNavAnswer(String answerLiId, String answerId, String answerText) {
      DsUtil.showLabel(answerLiId);         
      StringBuffer sb = new StringBuffer();
      sb.append("<a id=\"" + answerId + "\">");
      sb.append(NAV_ANSWER_SUFFIX);
      sb.append(" " + answerText);
      sb.append("</a>");
      HTML navAnswer = new HTML(sb.toString());
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(answerLiId));
      RootPanel.get(answerLiId).add(navAnswer);
   }
   
   //Generate an answer ID
   private String genNavAnswerId(String prefix) {return prefix + lastNavAnswerIndex;}
   
   //Set the related word navigation 'question'
   private void buildNymNavigationQuestion() {DsUtil.setLabelText(filterNavQuestionId,NYM_NAV_QUESTION_PREFIX);}
   
   //Add related word answer
   private void addNymNavigationAnswer(String navAnswerLiId, int navAnswerIndex) {
      WordOntologyItem item = ontologyHelper.getCurrentDefinition().getRelatedWords().get(navAnswerIndex);
      String answerId = genNavAnswerId(NYM_MODE_ANSWER_PREFIX);
      addNavAnswer(navAnswerLiId,answerId,"<b>" + item.getWord() + "</b>" + ": " + item.getDefinition());
      PageAssembler.attachHandler(answerId,Event.ONCLICK,relatedWordSelectedListener);
   }
      
   //Add the next set of related word navigation questions if any exist.  If not, add wiki info
   private void addNextNymNavigation() {      
      currentNavMode = NavMode.NYM;
      hideAllNavigationLineItems();
      buildNymNavigationQuestion();
      addWordDefinition(ontologyHelper.getCurrentDefinition());
      lastNavAnswerIndex++;
      if (ontologyHelper.getCurrentDefinition().getNumberOfRelatedWords() >= lastNavAnswerIndex + 1) {
         addNymNavigationAnswer(filterNavAnswer1LiId,lastNavAnswerIndex);
         lastNavAnswerIndex++;
         if (ontologyHelper.getCurrentDefinition().getNumberOfRelatedWords() >= lastNavAnswerIndex + 1) {
            addNymNavigationAnswer(filterNavAnswer2LiId,lastNavAnswerIndex);
            lastNavAnswerIndex++;
            if (ontologyHelper.getCurrentDefinition().getNumberOfRelatedWords() >= lastNavAnswerIndex + 1) {
               addNymNavigationAnswer(filterNavAnswer3LiId,lastNavAnswerIndex);
            }
         }
         DsUtil.showLabel(filterNavLinksId);
         PageAssembler.attachHandler(filterNavMoreAnswersLinkId,Event.ONCLICK,showMoreAnswersListener);
         PageAssembler.attachHandler(filterNavMoreOptionsLinkId,Event.ONCLICK,showMoreOptionsListener);         
      }
      else findAndHandleWikiInfo(ontologyHelper.getCurrentDefinition().getWikiLookupValue(),syncId,false);   
   }
         
   //Returns the current definition's related word list
   private ArrayList<String> getCurrentDefinitionRelatedWordsList() {
      ArrayList<String> retList = new ArrayList<String>();
      for (WordOntologyItem woi:ontologyHelper.getCurrentDefinition().getRelatedWords()) {
         woi.setSearchValue(ontologyHelper.getCurrentDefinition().getWord() + " " + woi.getWord());
         retList.add(woi.getSearchValue());
      }      
      return retList;    
   }
   
   //Parse the query result counts for the current definition's related words
   private void parseCurrentDefRelatedWordsResultCountsResults(JSONObject rcr) {
      for (WordOntologyItem woi: ontologyHelper.getCurrentDefinition().getRelatedWords()) {         
         if (rcr.containsKey(woi.getSearchValue())) {
            woi.setNumResults((long)rcr.get(woi.getSearchValue()).isNumber().doubleValue());            
         }         
      } 
   }
   
   //Build the result counts for the current definition's related words.
   private void buildCurrentDefRelatedResultCountsAndPopulateNymNavigation(final long currentSyncId) {
      if (!ontologyHelper.currentDefinitionHasRelations()) findAndHandleWikiInfo(ontologyHelper.getCurrentDefinition().getWikiLookupValue(),currentSyncId,false);
      else {
         DsESBApi.decalsSolrQueryCounts(DsSession.getInstance().getSolrUrl(),getCurrentDefinitionRelatedWordsList(),true,new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {
               try {                  
                  if (currentSyncId == syncId) {
                     parseCurrentDefRelatedWordsResultCountsResults(result.getObject(ESBApi.ESBAPI_RETURN_OBJ));
                     if (!ontologyHelper.currentDefinitionHasAnyRelationResults()) {
                        findAndHandleWikiInfo(ontologyHelper.getCurrentDefinition().getWikiLookupValue(),currentSyncId,false);
                     }
                     else {
                        ontologyHelper.applyCurrentDefinitionWordRelationsOrder();
                        addNextNymNavigation();
                     }                     
                  }                  
               }
               catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (buildCurrentDefRelatedResultCountsAndPopulateNymNavigation): " + e2.getMessage());}
            }
            @Override
            public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (buildCurrentDefRelatedResultCountsAndPopulateNymNavigation)");}
            });
      }
   }
   
   //Add definition navigation 'question'
   private void buildDefinitionNavigationQuestion(String word) {
      DsUtil.setLabelText(filterNavQuestionId,MULTI_DEFINITION_NAV_QUESTION_PREFIX + "\"" + word + "\"" + MULTI_DEFINITION_NAV_QUESTION_SUFFIX);
   }
     
   //Add definition navigation answer
   private void addDefinitionNavigationAnswer(String navAnswerLiId, int navAnswerIndex) {
      WordOntologyDefinition def = ontologyHelper.getDefinitions().get(navAnswerIndex);
      String answerId = genNavAnswerId(DEFINITION_MODE_ANSWER_PREFIX);
      addNavAnswer(navAnswerLiId,answerId,def.getDefinition());
      PageAssembler.attachHandler(answerId,Event.ONCLICK,definitionSelectedListener);
   }
   
   //Add the next set of definition questions if there are any.  If not, add wiki info questions
   private void addNextMultiDefinitionNavigation(String word) {      
      lastWordForOntology = word;
      currentNavMode = NavMode.DEFINITION;
      clearWordDefinition();
      hideAllNavigationLineItems();
      buildDefinitionNavigationQuestion(word);      
      lastNavAnswerIndex++;
      if (ontologyHelper.getNumberOfDefinitions() >= lastNavAnswerIndex + 1) {
         addDefinitionNavigationAnswer(filterNavAnswer1LiId,lastNavAnswerIndex);
         lastNavAnswerIndex++;
         if (ontologyHelper.getNumberOfDefinitions() >= lastNavAnswerIndex + 1) {
            addDefinitionNavigationAnswer(filterNavAnswer2LiId,lastNavAnswerIndex);
            lastNavAnswerIndex++;
            if (ontologyHelper.getNumberOfDefinitions() >= lastNavAnswerIndex + 1) {
               addDefinitionNavigationAnswer(filterNavAnswer3LiId,lastNavAnswerIndex);
            }
         }
         DsUtil.showLabel(filterNavLinksId);
         PageAssembler.attachHandler(filterNavMoreAnswersLinkId,Event.ONCLICK,showMoreAnswersListener);
         PageAssembler.attachHandler(filterNavMoreOptionsLinkId,Event.ONCLICK,showMoreOptionsListener);         
      }
      else findAndHandleWikiInfo(word,syncId,false);
   }
   
   //Handle the results from the word ontology lookup
   private void handleOntologyResults(String word, ESBPacket result) {      
      final long currentSyncId = syncId;
      ArrayList<String> nymList;
      if (result.containsKey(ESBApi.ESBAPI_RETURN_OBJ) && result.get(ESBApi.ESBAPI_RETURN_OBJ) instanceof JSONArray && result.getArray(ESBApi.ESBAPI_RETURN_OBJ).size() > 0) {         
         if (ontologyHelper == null) {
            ontologyHelper = new OntologyHelper();
            ontologyHelper.registerUsedWord(searchTerm.toLowerCase().trim());
         }
         ontologyHelper.initFromDefineWordReturn(result.getArray(ESBApi.ESBAPI_RETURN_OBJ));
         if (ontologyHelper.getNumberOfDefinitions() == 0) {
            performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
            findAndHandleWikiInfo(word,currentSyncId,false);                
         } 
         else if (ontologyHelper.getNumberOfDefinitions() == 1) {
            lastNavAnswerIndex = -1;
            nymList = ontologyHelper.getRelatedSingleWordsForCurrentDefinition(MAX_NUM_DEF_TERMS);
            if (nymList.size() > 0) searchTerm = word + " " + getStringFromList(nymList);       
            performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
            buildCurrentDefRelatedResultCountsAndPopulateNymNavigation(currentSyncId);
         } 
         else if (ontologyHelper.getNumberOfDefinitions() > 1) {
            lastNavAnswerIndex = -1;
            nymList = ontologyHelper.getRelatedSingleWordsForAllDefinitions(MAX_NUM_DEF_TERMS);
            if (nymList.size() > 0) searchTerm = word + WORD_BOOST + " " + getStringFromList(nymList);       
            performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
            addNextMultiDefinitionNavigation(word);
         }
      }
      else {
         performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
         findAndHandleWikiInfo(word,currentSyncId,false);
      }
   }
   
   //Find the ontology for the given word.
   private void findAndHandleWordOntology(final String word, final long currentSyncId) {
      lastWordForOntology = word;
      DsESBApi.decalsDefineWord(word,DEFINE_WORD_LEVELS,new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            try {        
               if (currentSyncId == syncId) handleOntologyResults(word, result);
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (define word): " + e2.getMessage());}
         }
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (define word)");}
         });         
   }
   
   //Build the topic navigation 'question'
   private void buildTopicNavigationQuestion() {DsUtil.setLabelText(filterNavQuestionId,RELATED_TOPICS_QUESTION_PREFIX);}
   
   //Add the topic navigation answer
   private void addTopicNavigationAnswer(String navAnswerLiId, int navAnswerIndex) {
      WikiInfoItem item = wikiInfoHelper.getRelatedTopics().get(navAnswerIndex);
      String answerId = genNavAnswerId(TOPIC_MODE_ANSWER_PREFIX);
      addNavAnswer(navAnswerLiId,answerId,item.getName());
      PageAssembler.attachHandler(answerId,Event.ONCLICK,topicSelectedListener);
   }
   
   //Add the next set of topic navigation if there are any.  If not, show grade level navigation
   private void addNextTopicNavigation() {
      currentNavMode = NavMode.TOPIC;
      hideAllNavigationLineItems();
      buildTopicNavigationQuestion();      
      lastNavAnswerIndex++;
      if (wikiInfoHelper.getNumberOfGoodTopics() >= lastNavAnswerIndex + 1) {
         addTopicNavigationAnswer(filterNavAnswer1LiId,lastNavAnswerIndex);
         lastNavAnswerIndex++;
         if (wikiInfoHelper.getNumberOfGoodTopics() >= lastNavAnswerIndex + 1) {
            addTopicNavigationAnswer(filterNavAnswer2LiId,lastNavAnswerIndex);
            lastNavAnswerIndex++;
            if (wikiInfoHelper.getNumberOfGoodTopics() >= lastNavAnswerIndex + 1) {
               addTopicNavigationAnswer(filterNavAnswer3LiId,lastNavAnswerIndex);
            }
         }
         DsUtil.showLabel(filterNavLinksId);
         PageAssembler.attachHandler(filterNavMoreAnswersLinkId,Event.ONCLICK,showMoreAnswersListener);
         PageAssembler.attachHandler(filterNavMoreOptionsLinkId,Event.ONCLICK,showMoreOptionsListener);         
      }
      else showGradeLevelNavigation();
   }
   
   //Build the related category navigation 'question'
   private void buildCategoryNavigationQuestion() {DsUtil.setLabelText(filterNavQuestionId,RELATED_CATEGORIES_QUESTION_PREFIX);}
   
   //Add category navigation answer
   private void addCategoryNavigationAnswer(String navAnswerLiId, int navAnswerIndex) {
      WikiInfoItem item = wikiInfoHelper.getRelatedCategories().get(navAnswerIndex);
      String answerId = genNavAnswerId(CATEGORY_MODE_ANSWER_PREFIX);
      addNavAnswer(navAnswerLiId,answerId,item.getName());
      PageAssembler.attachHandler(answerId,Event.ONCLICK,categorySelectedListener);
   }
   
   //Add the next set of category navigation if there are any.  If not add topic navigation
   private void addNextCategoryNavigation() {
      currentNavMode = NavMode.CATEGORY;
      hideAllNavigationLineItems();
      buildCategoryNavigationQuestion();      
      lastNavAnswerIndex++;
      if (wikiInfoHelper.getNumberOfGoodCategories() >= lastNavAnswerIndex + 1) {
         addCategoryNavigationAnswer(filterNavAnswer1LiId,lastNavAnswerIndex);
         lastNavAnswerIndex++;
         if (wikiInfoHelper.getNumberOfGoodCategories() >= lastNavAnswerIndex + 1) {
            addCategoryNavigationAnswer(filterNavAnswer2LiId,lastNavAnswerIndex);
            lastNavAnswerIndex++;
            if (wikiInfoHelper.getNumberOfGoodCategories() >= lastNavAnswerIndex + 1) {
               addCategoryNavigationAnswer(filterNavAnswer3LiId,lastNavAnswerIndex);
            }
         }
         DsUtil.showLabel(filterNavLinksId);
         PageAssembler.attachHandler(filterNavMoreAnswersLinkId,Event.ONCLICK,showMoreAnswersListener);
         PageAssembler.attachHandler(filterNavMoreOptionsLinkId,Event.ONCLICK,showMoreOptionsListener);         
      }
      else {
         lastNavAnswerIndex = -1;
         buildTopicResultCountsAndPopulateTopicNavigation(syncId);
      }
   }
   
   //Returns a list of related topics
   private ArrayList<String> getRelatedTopicsList() {
      ArrayList<String> retList = new ArrayList<String>();
      for (WikiInfoItem wii:wikiInfoHelper.getRelatedTopics()) {
         retList.add(wii.getName());
      }      
      return retList;    
   }
   
   //Parse the topic query result counts
   private void parseTopicResultCountsResults(JSONObject rcr) {
      long numResults;
      for (WikiInfoItem wii: wikiInfoHelper.getRelatedTopics()) {         
         if (rcr.containsKey(wii.getName())) {
            numResults = (long)rcr.get(wii.getName()).isNumber().doubleValue();            
            if (numResults >= RESULT_COUNT_UPPER_LIMIT) numResults = 0; //disregard a high number of results as a search failure
            wii.setNumResults(numResults);            
         }         
      } 
   }
   
   //Build the topic query result counts
   private void buildTopicResultCountsAndPopulateTopicNavigation(final long currentSyncId) {
         DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);
         DsESBApi.decalsSolrQueryCounts(DsSession.getInstance().getSolrUrl(),getRelatedTopicsList(),true,new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            try {                  
               if (currentSyncId == syncId) {
                  parseTopicResultCountsResults(result.getObject(ESBApi.ESBAPI_RETURN_OBJ));
                  wikiInfoHelper.applyTopicOrder();
                  addNextTopicNavigation();                                       
               }                  
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (buildTopicResultCountsAndPopulateTopicNavigation): " + e2.getMessage());}
         }         
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (buildTopicResultCountsAndPopulateTopicNavigation)");}
         });      
   }
   
   //Register the hyperlink listeners for the wiki info description extract
   private void registerExtractMarkupLinks() {
      Iterator<String> it = wikiInfoHelper.getExtractMarkupMap().keySet().iterator();      
      while (it.hasNext()) PageAssembler.attachHandler(it.next(),Event.ONCLICK,extractMarkupSelectedListener);
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
   
   //Step two of setting up the advanced options menu
   private void setUpAdvancedOptionsStep2(final String title, final long currentSyncId) {   
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
                  //DsUtil.showLabel(filterNavMoreOptGrdLvlLiId);
               }
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (setUpAdvancedOptionsStep2): " + e2.getMessage());}
         }
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (setUpAdvancedOptionsStep2)");}
         }); 
   }
   
   //Step one of setting up the advanced options menu
   private void setUpAdvancedOptionsStep1(final String word, final long currentSyncId) {
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
                  setUpAdvancedOptionsStep2(word,currentSyncId);
               }
            }
            catch (Exception e2) {if (currentSyncId == syncId) showSearchError("Error processing interactive search results (setUpAdvancedOptionsStep1): " + e2.getMessage());}
         }
         @Override
         public void onFailure(Throwable caught) {if (currentSyncId == syncId) showSearchError("Error contacting search server (setUpAdvancedOptionsStep1)");}
         });  
   }
   
   //Step 0 of setting up the advanced options menu
   private void setUpAdvancedOptions(String word) {
      DsUtil.hideLabel(filterNavMoreOptDisambigLiId);
      DsUtil.hideLabel(filterNavMoreOptCatLiId);
      DsUtil.hideLabel(filterNavMoreOptTopicLiId);      
      long currentSyncId = syncId;        
      setUpAdvancedOptionsStep1(word,currentSyncId);
   }
   
   //Build the grade level navigation 'question'
   private void buildGradeLevelNavigationQuestion() {DsUtil.setLabelText(filterNavQuestionId,GRADE_LEVELS_QUESTION_PREFIX);}
   
   //Display grade level navigation.
   private void showGradeLevelNavigation() {
      currentNavMode = NavMode.GRADE;
      hideAllNavigationLineItems();
      buildGradeLevelNavigationQuestion();    
      DsUtil.showLabel(filterNavGrdLvlKgLiId);
      DsUtil.showLabel(filterNavGrdLvlEsLiId);
      DsUtil.showLabel(filterNavGrdLvlMsLiId);
      DsUtil.showLabel(filterNavGrdLvlHsLiId);
      DsUtil.showLabel(filterNavGrdLvlCuLiId);
      DsUtil.showLabel(filterNavGrdLvlVtpLiId);
      DsUtil.showLabel(filterNavGrdLvlApplyLiId);
      DsUtil.showLabel(filterNavGrdLvlToggleLiId);
      DsUtil.showLabel(filterNavLinksId);
      PageAssembler.attachHandler(filterNavGrdLvlApplyLinkId,Event.ONCLICK,applyGradeLevelSelectedListener);
      PageAssembler.attachHandler(filterNavGrdLvlToggleLinkId,Event.ONCLICK,toggleGradeLevelsSelectedListener);
      synchGradeLevelCheckBoxes();
   }
   
   //Synchronize the grade level check boxes with the applied values
   private void synchGradeLevelCheckBoxes() {
      applyAllGradeLevelCheckBoxValues(false);
      if (kgGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlKgCbId,true);      
      if (esGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlEsCbId,true);
      if (msGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlMsCbId,true);
      if (hsGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlHsCbId,true);
      if (cuGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlCuCbId,true);
      if (vtpGlApplied) DsUtil.applyCheckBoxValue(filterNavGrdLvlVtpCbId,true);  
   }
   
   //Hide all navigation
   private void hideAllNavigationLineItems() {
      DsUtil.hideLabel(filterNavAnswer1LiId);
      DsUtil.hideLabel(filterNavAnswer2LiId);
      DsUtil.hideLabel(filterNavAnswer3LiId);      
      DsUtil.hideLabel(filterNavGrdLvlKgLiId);
      DsUtil.hideLabel(filterNavGrdLvlEsLiId);
      DsUtil.hideLabel(filterNavGrdLvlMsLiId);
      DsUtil.hideLabel(filterNavGrdLvlHsLiId);
      DsUtil.hideLabel(filterNavGrdLvlCuLiId);
      DsUtil.hideLabel(filterNavGrdLvlVtpLiId);
      DsUtil.hideLabel(filterNavGrdLvlApplyLiId);
      DsUtil.hideLabel(filterNavGrdLvlToggleLiId);
      DsUtil.hideLabel(filterNavLinksId);
   }
   
   //Display the word definition
   private void addWordDefinition(String word, String definition) {
      DsUtil.showLabel(wordDefContainerId);
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(wordDefContainerId));
      StringBuffer sb = new StringBuffer();
      sb.append("<p class=\"" + WORD_DEF_CLASS + "\">");
      sb.append("<b>" + word + ": </b>");
      sb.append(definition);      
      sb.append("</p>");
      RootPanel.get(wordDefContainerId).add(new HTML(sb.toString()));      
   }
   
   //Display the word ontology item definition
   private void addWordDefinition(WordOntologyItem woi) {addWordDefinition(woi.getWord(),woi.getDefinition());}
   
   //Clear the word definition
   private void clearWordDefinition() {
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(wordDefContainerId));
      DsUtil.hideLabel(wordDefContainerId);
   }
   
   //Change the search term box text
   private void setSearchTermBoxText(String text) {DsUtil.setTextBoxText(searchTermBoxId,text);}
   
   //Set up the initial interactive search navigation
   private void setUpInitialNavigation(final long currentSyncId) {
      DsUtil.showLabel(filterNavContaierId);
      DsUtil.setLabelText(filterNavQuestionId,TEMP_NAV_QUESTION);      
      hideAllNavigationLineItems();
      addAppliedGradeLevelDisplay();
      findAndHandleWordOntology(searchTerm, currentSyncId);
      setUpAdvancedOptions(searchTerm);
   }   
  
   //Execute a new interactive search
   private void executeNewSearch() {
      final long currentSyncId = syncId;
      try {
         DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(parentElementId));
         setCounterContainerDisplay(false);
         setSearchBusyDisplay(true);
         DsUtil.hideLabel(filterNavContaierId);         
         counterStatementDisplayTerm = searchTerm;
         //performSolrSearch(searchTerm,RESULTS_PER_PAGE,0,currentSyncId);
         setUpInitialNavigation(currentSyncId);
      }
      catch (Exception e) {if (currentSyncId == syncId) showSearchError("Interactive search failed: " + e.getMessage());}      
   }
   
   /**
    * Performs the interactive search with the small list of given parameters.
    * 
    * @param searchTerm The initial search term to use
    * @param parentElementId Search results parent element ID
    * @param widgetText The results widget text
    * @param counterElementId The results counter element ID
    * @param counterContainerElementId The results counter container element ID
    * @param searchBusyElementId The busy indicator element ID
    * @param breadCrumbElementId The bread crumb element ID
    * @param wordDefContainerId The word definition container element ID
    * @param filterNavContaierId The filter navigation container element ID
    * @param filterNavQuestionId The navigation 'question' element ID
    * @param filterNavAnswer1LiId The navigation answer 1 element ID
    * @param filterNavAnswer2LiId The navigation answer 2 element ID
    * @param filterNavAnswer3LiId The navigation answer 3 element ID
    * @param filterNavLinksId The navigation links container element ID
    * @param filterNavMoreAnswersLinkId The 'show more' hyperlink element ID
    * @param filterNavMoreOptionsLinkId The 'more options' hyperlink element ID
    * @param filterNavGrdLvlKgLiId The kindergarten line item element ID
    * @param filterNavGrdLvlEsLiId The elementary school line item element ID
    * @param filterNavGrdLvlMsLiId The middle school line item element ID
    * @param filterNavGrdLvlHsLiId The high school line item element ID
    * @param filterNavGrdLvlCuLiId The college/university line item element ID
    * @param filterNavGrdLvlVtpLiId The vocational/technical/professional line item element ID
    * @param filterNavGrdLvlApplyLiId The apply grade level line item element ID
    * @param filterNavGrdLvlApplyLinkId The apply grade level hyperlink element ID
    * @param filterNavGrdLvlKgCbId The kindergarten check box element ID
    * @param filterNavGrdLvlEsCbId The elementary school check box element ID
    * @param filterNavGrdLvlMsCbId The middle school check box element ID
    * @param filterNavGrdLvlHsCbId The high school check box element ID
    * @param filterNavGrdLvlCuCbId The college/university check box element ID
    * @param filterNavGrdLvlVtpCbId The vocational/technical/professional check box element ID
    * @param appliedGradeLevelsContainerId The applied grade levels container element ID
    * @param filterNavMoreOptContainerId The more options container element ID
    * @param filterNavMoreOptDisambigLiId The more options disambiguate line item element ID
    * @param filterNavMoreOptCatLiId The more options related categories line item element ID
    * @param filterNavMoreOptTopicLiId The more options related topic line item element ID
    * @param filterNavMoreOptGrdLvlLiId The more options grade level line item element ID
    * @param filterNavMoreOptDisambigId The more options disambiguate hyperlink element ID
    * @param filterNavMoreOptCatId The more options related categories hyperlink element ID
    * @param filterNavMoreOptTopicId The more options related topics hyperlink element ID
    * @param filterNavMoreOptGrdLvlId The more options grade levels hyperlink element ID
    * @param filterNavMoreOptLinkTxtId The more options link text element ID
    * @param searchTermBoxId The search term box element ID
    */
   public void performInteractiveSearch(String searchTerm, String parentElementId, String widgetText, String counterElementId, 
                                        String counterContainerElementId, String searchBusyElementId, String breadCrumbElementId, String wordDefContainerId,
                                        String filterNavContaierId, String filterNavQuestionId, String filterNavAnswer1LiId, String filterNavAnswer2LiId, 
                                        String filterNavAnswer3LiId, String filterNavLinksId, String filterNavMoreAnswersLinkId, String filterNavMoreOptionsLinkId,
                                        String filterNavGrdLvlKgLiId,String filterNavGrdLvlEsLiId, String filterNavGrdLvlMsLiId, String filterNavGrdLvlHsLiId, 
                                        String filterNavGrdLvlCuLiId, String filterNavGrdLvlVtpLiId, String filterNavGrdLvlApplyLiId, String filterNavGrdLvlApplyLinkId, 
                                        String filterNavGrdLvlKgCbId, String filterNavGrdLvlEsCbId, String filterNavGrdLvlMsCbId, String filterNavGrdLvlHsCbId,
                                        String filterNavGrdLvlCuCbId, String filterNavGrdLvlVtpCbId, String appliedGradeLevelsContainerId,
                                        String filterNavMoreOptContainerId, String filterNavMoreOptDisambigLiId, String filterNavMoreOptCatLiId,
                                        String filterNavMoreOptTopicLiId, String filterNavMoreOptGrdLvlLiId, String filterNavMoreOptDisambigId,
                                        String filterNavMoreOptCatId, String filterNavMoreOptTopicId, String filterNavMoreOptGrdLvlId, String filterNavMoreOptLinkTxtId,
                                        String searchTermBoxId, String filterNavGrdLvlToggleLiId, String filterNavGrdLvlToggleLinkId) {
      DsUtil.sendTrackingMessage("Initiated interactive search for \"" + searchTerm + "\"");
      this.parentElementId = parentElementId;
      this.widgetText = widgetText;   
      this.counterElementId = counterElementId;
      this.counterContainerElementId = counterContainerElementId;
      this.searchBusyElementId = searchBusyElementId;
      this.breadCrumbElementId = breadCrumbElementId;
      this.wordDefContainerId = wordDefContainerId;
      this.filterNavContaierId = filterNavContaierId;
      this.filterNavQuestionId = filterNavQuestionId;
      this.filterNavAnswer1LiId = filterNavAnswer1LiId;
      this.filterNavAnswer2LiId = filterNavAnswer2LiId;
      this.filterNavAnswer3LiId = filterNavAnswer3LiId;
      this.filterNavLinksId = filterNavLinksId;
      this.filterNavMoreAnswersLinkId = filterNavMoreAnswersLinkId;
      this.filterNavMoreOptionsLinkId = filterNavMoreOptionsLinkId;
      this.filterNavGrdLvlKgLiId = filterNavGrdLvlKgLiId;
      this.filterNavGrdLvlEsLiId = filterNavGrdLvlEsLiId;
      this.filterNavGrdLvlMsLiId = filterNavGrdLvlMsLiId;
      this.filterNavGrdLvlHsLiId = filterNavGrdLvlHsLiId;
      this.filterNavGrdLvlCuLiId = filterNavGrdLvlCuLiId;
      this.filterNavGrdLvlVtpLiId = filterNavGrdLvlVtpLiId;
      this.filterNavGrdLvlApplyLiId = filterNavGrdLvlApplyLiId;
      this.filterNavGrdLvlApplyLinkId = filterNavGrdLvlApplyLinkId;
      this.filterNavGrdLvlKgCbId = filterNavGrdLvlKgCbId;
      this.filterNavGrdLvlEsCbId = filterNavGrdLvlEsCbId;
      this.filterNavGrdLvlMsCbId = filterNavGrdLvlMsCbId;
      this.filterNavGrdLvlHsCbId = filterNavGrdLvlHsCbId;
      this.filterNavGrdLvlCuCbId = filterNavGrdLvlCuCbId;
      this.filterNavGrdLvlVtpCbId = filterNavGrdLvlVtpCbId; 
      this.appliedGradeLevelsContainerId = appliedGradeLevelsContainerId;      
      this.filterNavMoreOptContainerId = filterNavMoreOptContainerId;
      this.filterNavMoreOptDisambigLiId = filterNavMoreOptDisambigLiId;
      this.filterNavMoreOptCatLiId = filterNavMoreOptCatLiId;
      this.filterNavMoreOptTopicLiId = filterNavMoreOptTopicLiId;
      this.filterNavMoreOptGrdLvlLiId = filterNavMoreOptGrdLvlLiId;
      this.filterNavMoreOptDisambigId = filterNavMoreOptDisambigId;
      this.filterNavMoreOptCatId = filterNavMoreOptCatId;
      this.filterNavMoreOptTopicId = filterNavMoreOptTopicId;
      this.filterNavMoreOptGrdLvlId = filterNavMoreOptGrdLvlId;
      this.filterNavMoreOptLinkTxtId = filterNavMoreOptLinkTxtId;
      this.searchTermBoxId = searchTermBoxId;
      this.filterNavGrdLvlToggleLiId = filterNavGrdLvlToggleLiId;
      this.filterNavGrdLvlToggleLinkId = filterNavGrdLvlToggleLinkId; 
      showMoreResultsOuterDivId = null;
      showMoreResultsButtonId = null;  
      showMoreResultsBusyId = null;      
      syncId++;      
      this.searchTerm = DsUtil.cleanString(searchTerm.replace("+", " ")); 
      clearSearch();
      //breadCrumbList.clear();
      executeNewSearch();      
   }   
   
   /**
    * Clear the interactive search.
    */
   public void clearSearch() {      
      ontologyHelper = null;
      wikiInfoHelper = null;
      intSearchResultSet = null;
      clearWordDefinition();
//      bcIndex = -1;
   }

}
