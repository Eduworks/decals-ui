package com.eduworks.decals.ui.client.pagebuilder.screen;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.json.client.JSONObject;
import com.eduworks.decals.ui.client.DsSession;
import com.eduworks.decals.ui.client.handler.BasicSearchHandler;
import com.eduworks.decals.ui.client.handler.InteractiveSearchHandler;
import com.eduworks.decals.ui.client.pagebuilder.DecalsScreen;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Screen handling for the main DECALS guest screen.
 * 
 * @author Tom Buskirk
 *
 */
public class DsGuestScreen extends DecalsScreen {
   
   private enum Tabs{INTERACTIVE_TAB, BASIC_TAB}
   
   private Tabs currentTab = Tabs.INTERACTIVE_TAB;
   private String username = "";
   private String password = "";
   private BasicSearchHandler basicSearchHandler = new BasicSearchHandler();
   private InteractiveSearchHandler interactiveSearchHandler = new InteractiveSearchHandler();
   
   private static final String ASSIGNMENT_ID = "assignmentId";
   
   private static final String EMAIL_IN_USE = "The given email has already been registered.";
   private static final String INVALID_LOGIN = "Invalid username/password";
   private static final String INFO_RETRIEVE_FAILED = "Could not obtain user information.";
   private static final String BAD_SESSION_ID = "A valid session could not be obtained.";
   
   private static final String REGISTRATION_ERROR_CONTAINER = "registrationErrorContainer";
   private static final String LOGIN_ERROR_CONTAINER = "loginErrorContainer";
   
   private static final String REGISTER_MODAL = "modalRegister";
   
   private static final String REGISTRATION_FIRST_NAME = "firstName";
   private static final String REGISTRATION_LAST_NAME = "lastName";
   private static final String REGISTRATION_EMAIL = "newEmail";
   private static final String REGISTRATION_PASSWORD = "newPassword";
   private static final String LOGIN_USERNAME = "loginEmail";
   private static final String LOGIN_PASSWORD = "loginPassword";
   
   private static final String REGISTER_FORM = "register";
   private static final String LOGIN_FORM = "login";
   
   private static final String BS_FIELD = "basicSearchInput";   
   private static final String BS_RESULTS = "basicSearchResults";
   private static final String BS_WHATIS = "whatIsThisBasicSearch";
   private static final String BS_COUNTER_CONTAINER = "numberOfBasicSearchResults";
   private static final String BS_COUNTER = "basicSearchResultsCounter";
   private static final String BS_BUSY = "basicSearchBusy";
   
   private static final String IS_FIELD = "interactiveSearchInput";
   private static final String IS_HEADER_FIELD = "interactiveSearchHeaderInput";
   private static final String IS_RESULTS = "interactiveSearchResults";
   private static final String IS_COUNTER_CONTAINER = "numberOfInteractiveSearchResults";
   private static final String IS_COUNTER = "interactiveSearchResultsCounter";
   private static final String IS_BUSY = "interactiveSearchBusy";
   private static final String IS_FILTER_TOOLS = "interactiveSearchResultsFilterTools";
   private static final String IS_RESULTS_SCREEN = "interactiveSearchResultsScreen";   
   private static final String IS_BREADCRUMB_HELP = "interactiveSearchBreadCrumbsHelp";
   private static final String IS_BREADCRUMB_CONT = "interactiveSearchBreadCrumbs";
   private static final String IS_QUESTION = "interactiveSearchQuestion";
   
   private static final String IS_WORD_DEF_CONTAINER = "interactiveSearchResultsWordDefinitionContainer";
   
   private static final String IS_FILTER_NAVIGATION = "intSrchFilterNavigation";   
   private static final String IS_FILTER_NAV_QUESTION = "intSrchFilterNavQuestionLabel"; 
   private static final String IS_FILTER_NAV_ANSWER_1_LI = "intSrchFilterNavAnswer1Li";
   private static final String IS_FILTER_NAV_ANSWER_2_LI = "intSrchFilterNavAnswer2Li";
   private static final String IS_FILTER_NAV_ANSWER_3_LI = "intSrchFilterNavAnswer3Li";
   private static final String IS_FILTER_NAV_LINKS = "intSrchFilterNavLinks";  
   private static final String IS_FILTER_NAV_MORE_ANSWERS = "intSrchFilterNavMoreAnswers"; 
   private static final String IS_FILTER_NAV_MORE_OPTIONS = "intSrchFilterNavMoreOptions";  
   private static final String IS_FILTER_NAV_GL_KDG_LI = "intSrchFilterNavAnswerGlKgLi";
   private static final String IS_FILTER_NAV_GL_ES_LI = "intSrchFilterNavAnswerGlEsLi";
   private static final String IS_FILTER_NAV_GL_MS_LI = "intSrchFilterNavAnswerGlMsLi";
   private static final String IS_FILTER_NAV_GL_HS_LI = "intSrchFilterNavAnswerGlHsLi";
   private static final String IS_FILTER_NAV_GL_CU_LI = "intSrchFilterNavAnswerGlCuLi";
   private static final String IS_FILTER_NAV_GL_VTP_LI = "intSrchFilterNavAnswerGlVtpLi";
   private static final String IS_FILTER_NAV_GL_APPLY_LI = "intSrchFilterNavAnswerApplyGlLi";
   private static final String IS_FILTER_NAV_GL_APPLY = "intSrchFilterNavAnswerApplyGl";   
   private static final String IS_FILTER_NAV_GL_TOGGLE_LI = "intSrchFilterNavAnswerToggleAllLi";
   private static final String IS_FILTER_NAV_GL_TOGGLE = "intSrchFilterNavAnswerToggleAll";   
   private static final String IS_FILTER_NAV_GL_KDG_CB = "intSrchGlKgCb";
   private static final String IS_FILTER_NAV_GL_ES_CB = "intSrchGlEsCb";
   private static final String IS_FILTER_NAV_GL_MS_CB = "intSrchGlMsCb";
   private static final String IS_FILTER_NAV_GL_HS_CB = "intSrchGlHsCb";
   private static final String IS_FILTER_NAV_GL_CU_CB = "intSrchGlCuCb";
   private static final String IS_FILTER_NAV_GL_VTP_CB = "intSrchGlVtpCb";
   
   private static final String IS_APPLIED_GL_CONTAINER = "interactiveSearchAppliedGradeLevelsContainer";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_LINK_TXT = "intSrchFilterNavMoreOptionsTxt";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_CONTAINER = "intSrchFilterNavigationMopContainer";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_DISAMBIG_LI = "intSrchFilterNavigationMopDisambigLi";   
   private static final String IS_FILTER_NAV_MORE_OPTIONS_CAT_LI = "intSrchFilterNavigationMopRelCatLi";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_TOPIC_LI = "intSrchFilterNavigationMopRelTopicLi";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_GL_LI = "intSrchFilterNavigationMopGradeLevLi";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_DISAMBIG = "intSrchFilterNavigationMopDisambig";   
   private static final String IS_FILTER_NAV_MORE_OPTIONS_CAT = "intSrchFilterNavigationMopRelCat";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_TOPIC = "intSrchFilterNavigationMopRelTopic";
   private static final String IS_FILTER_NAV_MORE_OPTIONS_GL = "intSrchFilterNavigationMopGradeLev";
   
   
   private static final String IS_DIV = "interactiveSearch";
   private static final String BS_DIV = "basicSearch";
   private static final String IS_TYPING = "interactiveSearchTyping";
   
   private static final String IS_TAB_LINK = "interactiveSearchTabLink";
   private static final String IS_TAB = "interactiveTab";   
   private static final String BS_TAB_LINK = "basicSearchTabLink";
   private static final String BS_TAB = "basicTab";
   
   private static final String DEFAULT_HEADER = "defaultHeader";
   private static final String SEARCH_HEADER = "searchHeader";
   
   private static final String VALID_EVENT = "valid";
   
   @SuppressWarnings("unused")
   private boolean haveBasicSearchResults = false;
   private boolean haveInteractiveSearchResults = false;
   
   private static final String FIRST_NAME_KEY = "firstName";
   private static final String LAST_NAME_KEY = "lastName";
   
   //Parses the first name and last name from the user info return.
   private void parseUserInfo(JSONObject userInfo) {
      try {
         DsSession.getInstance().setFirstName(userInfo.get(FIRST_NAME_KEY).isString().stringValue());
         DsSession.getInstance().setLastName(userInfo.get(LAST_NAME_KEY).isString().stringValue());
      }
      catch (Exception e) {
         DsSession.getInstance().setFirstName(ESBApi.username);
      }
   }
   
   //Handles a login request
   private void handleLogin(ESBPacket result) {
      String sessionId = result.getPayloadString();
      if (sessionId == null) DsUtil.showSimpleErrorMessage(LOGIN_ERROR_CONTAINER,BAD_SESSION_ID);
      else {
         ESBApi.sessionId = sessionId;
         ESBApi.username = username;
         //TODO update this for new getUser version
//         ESBApi.getUser(new ESBCallback<ESBPacket>() {
//            @Override
//            public void onSuccess(ESBPacket result) {
//               parseUserInfo(result.get(ESBApi.ESBAPI_RETURN_OBJ).isObject());
//               PageAssembler.closePopup(REGISTER_MODAL);
//               PageAssembler.setTemplate(getTemplates().getHeader().getText(), getTemplates().getFooter().getText(), CONTENT_PANE);
//               DsUtil.setUpAppropriateHomePage(getDispatcher());
//            }
//            @Override
//            public void onFailure(Throwable caught) {DsUtil.showSimpleErrorMessage(LOGIN_ERROR_CONTAINER,INFO_RETRIEVE_FAILED);}
//            });                
      }
   }
   
   //Initiates a ESB login
   private void doLogin() {
      ESBApi.login(username, password, new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {handleLogin(result);}
         @Override
         public void onFailure(Throwable caught) {DsUtil.showSimpleErrorMessage(LOGIN_ERROR_CONTAINER,INVALID_LOGIN);}
         });
   }
   
   //Handles account create request
   private void handleUserCreate(ESBPacket result) {      
      if (result.getPayloadString().equalsIgnoreCase("true")) {
         String firstName = DsUtil.getTextBoxText(REGISTRATION_FIRST_NAME);
         String lastName = DsUtil.getTextBoxText(REGISTRATION_LAST_NAME);
         ESBApi.updateUserAtCreate(firstName, lastName, username, new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {doLogin();}
            @Override
            public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
            });         
      }
      else {         
         DsUtil.showSimpleErrorMessage(REGISTRATION_ERROR_CONTAINER,EMAIL_IN_USE);
      }      
   }
   
   //Account create request listener
   protected EventCallback createAccountListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         username = DsUtil.getTextBoxText(REGISTRATION_EMAIL);
         password = DsUtil.getTextBoxText(REGISTRATION_PASSWORD);
         ESBApi.createUser(username, password, new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {handleUserCreate(result);}
            @Override
            public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
            });
      }
   };
   
   //Login listener
   protected EventCallback loginListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         username = DsUtil.getTextBoxText(LOGIN_USERNAME);
         password = DsUtil.getTextBoxText(LOGIN_PASSWORD);
         doLogin();
      }
   };
   
   //Resets the basic search fields
   private void resetBasicSearch() {
      DsUtil.hideLabel(BS_RESULTS);
      DsUtil.hideLabel(BS_COUNTER_CONTAINER);
      DsUtil.showLabel(BS_WHATIS);
      DsUtil.setTextBoxText(BS_FIELD,"");
      DsUtil.setFocus(BS_FIELD);
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(BS_RESULTS));
      haveBasicSearchResults = false;
   }
   
   //Sets up and calls basic search handler
   private void performBasicSearch(String searchTerm) {
      haveBasicSearchResults = true;
      DsUtil.showLabel(BS_RESULTS);
      DsUtil.hideLabel(BS_WHATIS);
      basicSearchHandler.performBasicSearch(0, searchTerm, BS_RESULTS, getTemplates().getBasicSearchResultWidget().getText(), BS_COUNTER, BS_COUNTER_CONTAINER, BS_BUSY);
   }
   
   //Basic search request listener
   protected EventCallback basicSearchListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
            String basicSearchTerm = DsUtil.getTextBoxText(BS_FIELD).trim();            
            if (basicSearchTerm == null || basicSearchTerm.trim().isEmpty()) resetBasicSearch();
            else performBasicSearch(basicSearchTerm);        
         }
      }
   };
   
   //Resets the interactive search fields
   private void resetInteractiveSearch() {      
      interactiveSearchHandler.clearSearch();
      DsUtil.hideLabel(IS_FILTER_TOOLS);
      DsUtil.hideLabel(IS_FILTER_NAVIGATION);      
      DsUtil.hideLabel(IS_RESULTS_SCREEN);
      DsUtil.hideLabel(IS_TYPING);
      DsUtil.hideLabel(SEARCH_HEADER);
      DsUtil.showLabel(DEFAULT_HEADER);
      DsUtil.showLabel(IS_DIV);
      DsUtil.setFocus(IS_FIELD);
      DsUtil.setTextBoxText(IS_FIELD,"");
      DsUtil.setTextBoxText(IS_HEADER_FIELD,"");
      DsUtil.removeAllWidgetsFromRootPanel(RootPanel.get(IS_RESULTS));
      haveInteractiveSearchResults = false;
   }
   
   //Sets up and calls interactive search handler
   private void performInteractiveSearch(String searchTerm) {
      haveInteractiveSearchResults = true;      
      DsUtil.hideLabel(IS_TYPING);
      DsUtil.hideLabel(IS_BREADCRUMB_CONT);
      DsUtil.showLabel(IS_FILTER_TOOLS);
      DsUtil.showLabel(IS_FILTER_NAVIGATION);     
      DsUtil.showLabel(IS_RESULTS_SCREEN);
      DsUtil.showLabel(IS_RESULTS);
      DsUtil.showLabel(IS_QUESTION);
      DsUtil.showLabel(IS_BREADCRUMB_HELP);
      //This parameter list is silly...need to do something about it at some point.
      interactiveSearchHandler.performInteractiveSearch(searchTerm, IS_RESULTS, getTemplates().getInteractiveSearchResultWidget().getText(), IS_COUNTER, 
            IS_COUNTER_CONTAINER, IS_BUSY, IS_BREADCRUMB_CONT,IS_WORD_DEF_CONTAINER,IS_FILTER_NAVIGATION,IS_FILTER_NAV_QUESTION, IS_FILTER_NAV_ANSWER_1_LI,
            IS_FILTER_NAV_ANSWER_2_LI,IS_FILTER_NAV_ANSWER_3_LI,IS_FILTER_NAV_LINKS, IS_FILTER_NAV_MORE_ANSWERS, IS_FILTER_NAV_MORE_OPTIONS, 
            IS_FILTER_NAV_GL_KDG_LI,IS_FILTER_NAV_GL_ES_LI, IS_FILTER_NAV_GL_MS_LI,IS_FILTER_NAV_GL_HS_LI,IS_FILTER_NAV_GL_CU_LI,IS_FILTER_NAV_GL_VTP_LI,
            IS_FILTER_NAV_GL_APPLY_LI,IS_FILTER_NAV_GL_APPLY,IS_FILTER_NAV_GL_KDG_CB,IS_FILTER_NAV_GL_ES_CB, IS_FILTER_NAV_GL_MS_CB,IS_FILTER_NAV_GL_HS_CB,
            IS_FILTER_NAV_GL_CU_CB,IS_FILTER_NAV_GL_VTP_CB,IS_APPLIED_GL_CONTAINER,IS_FILTER_NAV_MORE_OPTIONS_CONTAINER,IS_FILTER_NAV_MORE_OPTIONS_DISAMBIG_LI,
            IS_FILTER_NAV_MORE_OPTIONS_CAT_LI,IS_FILTER_NAV_MORE_OPTIONS_TOPIC_LI,IS_FILTER_NAV_MORE_OPTIONS_GL_LI,IS_FILTER_NAV_MORE_OPTIONS_DISAMBIG,
            IS_FILTER_NAV_MORE_OPTIONS_CAT,IS_FILTER_NAV_MORE_OPTIONS_TOPIC,IS_FILTER_NAV_MORE_OPTIONS_GL,IS_FILTER_NAV_MORE_OPTIONS_LINK_TXT,IS_HEADER_FIELD,
            IS_FILTER_NAV_GL_TOGGLE_LI,IS_FILTER_NAV_GL_TOGGLE);      
   }
   
   //Interactive search listener for header bar
   protected EventCallback interactiveSearchHeaderListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
            String interactiveSearchTerm = DsUtil.getTextBoxText(IS_HEADER_FIELD).trim();            
            if (interactiveSearchTerm == null || interactiveSearchTerm.isEmpty()) resetInteractiveSearch();
            else performInteractiveSearch(interactiveSearchTerm);
         }
      }
   };
   
   //Interactive search listener for middle page search box
   protected EventCallback interactiveSearchListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         String value = DsUtil.getTextBoxText(IS_FIELD);
         DsUtil.setTextBoxText(IS_HEADER_FIELD, value);
         DsUtil.hideLabel(DEFAULT_HEADER);
         DsUtil.showLabel(SEARCH_HEADER);
         DsUtil.setFocus(IS_HEADER_FIELD);
         DsUtil.hideLabel(IS_DIV);
         DsUtil.showLabel(IS_TYPING);
         DsUtil.setTextBoxText(IS_FIELD,"");
      }
   };
   
   //Interactive search tab listener and handler
   protected EventCallback interactiveTabListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         if (Tabs.INTERACTIVE_TAB.equals(currentTab)) return;
         currentTab = Tabs.INTERACTIVE_TAB;
         DsUtil.hideLabel(BS_DIV);
         String isSearchText = DsUtil.getTextBoxText(IS_HEADER_FIELD);
         if (isSearchText == null || isSearchText.trim().isEmpty()) {
            DsUtil.hideLabel(SEARCH_HEADER);
            DsUtil.showLabel(DEFAULT_HEADER);
            DsUtil.showLabel(IS_DIV);
            DsUtil.setFocus(IS_FIELD);            
         }
         else{
            DsUtil.hideLabel(DEFAULT_HEADER);
            DsUtil.showLabel(SEARCH_HEADER);            
            DsUtil.setFocus(IS_HEADER_FIELD);
            if (haveInteractiveSearchResults) {
               DsUtil.showLabel(IS_FILTER_TOOLS);
               DsUtil.showLabel(IS_FILTER_NAVIGATION);               
               DsUtil.showLabel(IS_RESULTS_SCREEN);
            }
            else DsUtil.showLabel(IS_TYPING);
         }
      }
   };
   
   //Basic search tab listener and handler
   protected EventCallback basicTabListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         if (Tabs.BASIC_TAB.equals(currentTab)) return;
         currentTab = Tabs.BASIC_TAB;
         DsUtil.hideLabel(SEARCH_HEADER);
         DsUtil.showLabel(DEFAULT_HEADER);
         DsUtil.hideLabel(IS_DIV);
         DsUtil.hideLabel(IS_TYPING);
         DsUtil.hideLabel(IS_FILTER_TOOLS);
         DsUtil.hideLabel(IS_FILTER_NAVIGATION);         
         DsUtil.hideLabel(IS_RESULTS_SCREEN);
         DsUtil.showLabel(BS_DIV);
         DsUtil.setFocus(BS_FIELD);
      }
   };
   
   //Attempts to retrieve the assignment ID from the query string
   private void getAssignmentIdFromQueryString() {
      String queryString = Window.Location.getQueryString();      
      if (queryString != null && !queryString.trim().isEmpty()) {
         queryString = queryString.substring(1); 
         String[] qps = queryString.split("&");
         String[] aip;
         for (String qp:qps) {
            if (qp.startsWith(ASSIGNMENT_ID + "=")) {
               aip = qp.split("=");
               DsSession.getInstance().setAssignmentId(aip[1]);
            }
         }
      }
   }

   //Display handler for guest screen display
   @Override
   public void display() {
      //TODO make magnifying glass work for search button
      PageAssembler.setTemplate(getTemplates().getGuestHeader().getText(), getTemplates().getFooter().getText(), CONTENT_PANE);
      PageAssembler.ready(new HTML(getTemplates().getGuestPanel().getText()));
      PageAssembler.buildContents();      
      currentTab = Tabs.INTERACTIVE_TAB;
      PageAssembler.attachHandler(IS_TAB_LINK,Event.ONCLICK,interactiveTabListener);
      PageAssembler.attachHandler(BS_TAB_LINK,Event.ONCLICK,basicTabListener);
      PageAssembler.attachHandler(IS_TAB,Event.ONCLICK,interactiveTabListener);
      PageAssembler.attachHandler(BS_TAB,Event.ONCLICK,basicTabListener);
      PageAssembler.attachHandler(REGISTER_FORM,VALID_EVENT,createAccountListener);
      PageAssembler.attachHandler(LOGIN_FORM,VALID_EVENT,loginListener);
      PageAssembler.attachHandler(BS_FIELD,Event.ONKEYDOWN,basicSearchListener);
      PageAssembler.attachHandler(IS_HEADER_FIELD,Event.ONKEYDOWN,interactiveSearchHeaderListener);
      PageAssembler.attachHandler(IS_FIELD,Event.ONKEYDOWN,interactiveSearchListener);
      getAssignmentIdFromQueryString();
      DsUtil.sendTrackingMessage("Entered guest screen");
      
      //TODO make magnifying glass active
   }
   
   @Override
   public void lostFocus() {}
}
