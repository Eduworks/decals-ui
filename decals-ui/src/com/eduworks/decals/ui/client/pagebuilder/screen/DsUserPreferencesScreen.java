package com.eduworks.decals.ui.client.pagebuilder.screen;

import java.util.Vector;

import org.json.JSONException;

import com.google.gwt.user.client.Element;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.eduworks.lang.json.impl.EwJsonObject;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML; 
import com.google.gwt.user.client.ui.ListBox;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.eduworks.decals.ui.client.Decals_ui;
import com.eduworks.decals.ui.client.DsSession;
import com.eduworks.decals.ui.client.api.DsESBApi;
import com.eduworks.decals.ui.client.pagebuilder.screen.enums.GRADE_LEVEL;
import com.eduworks.decals.ui.client.pagebuilder.screen.enums.LANGUAGE;
import com.eduworks.decals.ui.client.pagebuilder.screen.enums.RESOURCE_TYPE;

public class DsUserPreferencesScreen extends DecalsWithGroupMgmtScreen {

	public static String RESOURCE_TYPES_KEY = "resourceTypes";
	public static String GRADE_LEVELS_KEY = "gradeLevel";
	public static String LEARNING_OBJECTIVES_KEY = "learningObjectives";
	public static String LANGUAGE_KEY = "language";
	
	private static final String RESOURCE_TYPE_SELECT_ID = "prefResourceType";
	private static final String LANGUAGE_SELECT_ID = "prefLang";
	private static final String GRADE_LEVEL_SELECT_ID = "prefGrade";
	
	private static final String OBJECTIVE_LIST_ID = "userObjectivesList";
	private static final String NO_OBJECTIVE_ITEM_ID = "noObjectivesItem";
	
	private static final String ADD_OBJECTIVE_INPUT_ID = "prefAddObjectiveInput";
	private static final String ADD_OBJECTIVE_BTN_ID = "addObjectiveBtn";
	
	private static final String COMPETENCY_LINK_ID = "prefCompetencyLink";
	
	private static final String HELD_COMPETENCY_LIST_ID = "heldCompetenciesList";
	private static final String NO_HELD_COMPETENCY_ITEM_ID = "noHeldCompetenciesItem";
	
	private static final String DESIRED_COMPETENCY_LIST_ID = "desiredCompetenciesList";
	private static final String NO_DESIRED_COMPETENCY_ITEM_ID = "noDesiredCompetenciesItem";
	
	private static final String COMPETENCY_ID_INPUT = "prefAddCompetencyId";
	private static final String ADD_COMPETENCY_INPUT_ID = "prefAddCompetencyInput";
	private static final String ADD_COMPETENCY_BTN_ID = "addCompetencyBtn";
	
	private static final String SEARCH_COMPETENCY_INPUT_ID = "searchCompetencyInput";
	private static final String SEARCH_COMPETENCY_BTN_ID = "searchCompetencyBtn";
	
	private static final String SEARCH_COMPETENCY_RESULTS_ID = "competencyResultsList";
	
	private static final String SAVE_PREFERENCES_BTN_ID = "savePreferencesBtn";
	private static final String PREFS_CHANGED_ALERT_ID = "prefChangedAlert";
	
	private static final String CONFIRM_SAVE_PREFERENCES_BTN_ID = "confirmSavePreferences";
	private static final String CANCEL_SAVE_PREFERENCES_BTN_ID = "confirmCancelPreferences";
	
	private static final String CONFIRM_CHANGES_MODAL = "modalConfirmCancel";
	private static final String COMPETENCY_SEARCH_MODAL = "modalCompetencySearch";
	
	public static Vector<RESOURCE_TYPE> resourceTypes = new Vector<RESOURCE_TYPE>();
	public static Vector<GRADE_LEVEL> gradeLevels = new Vector<GRADE_LEVEL>();
	public static Vector<LANGUAGE> languages = new Vector<LANGUAGE>();

	public static Vector<String> learningObjectives = new Vector<String>();
	
	public static Vector<String> newDesiredCompetencyIds = new Vector<String>();
	
	public DsUserPreferencesScreen(ESBPacket packet) {
		resourceTypes = new Vector<RESOURCE_TYPE>();
		gradeLevels = new Vector<GRADE_LEVEL>();
		languages = new Vector<LANGUAGE>();
		learningObjectives = new Vector<String>();
		newDesiredCompetencyIds = new Vector<String>();
		
		prefsChanged = false;
		
		JSONObject thing = null;
		
		if (packet.containsKey("contentStream")) 
			thing = JSONParser.parseStrict(packet.getContentString()).isObject();
		else
			thing = packet.isObject();
		
		if(thing == null){
			return;
		}else{
			if(thing.containsKey(RESOURCE_TYPES_KEY)){
				JSONArray types = thing.get(RESOURCE_TYPES_KEY).isArray();
				
				if(types != null){
					for(int i = 0; i < types.size(); i++){
						String type = types.get(i).isString().stringValue();
						resourceTypes.add(RESOURCE_TYPE.findSymbolType(type));
					}
				}
			}
			
			if(thing.containsKey(GRADE_LEVELS_KEY)){
				JSONArray grades = thing.get(GRADE_LEVELS_KEY).isArray();
				
				if(grades != null){
					for(int i = 0; i < grades.size(); i++){
						String grade = grades.get(i).isString().stringValue();
						gradeLevels.add(GRADE_LEVEL.findSymbolGrade(grade));
					}
				}
			}
			
			if(thing.containsKey(LEARNING_OBJECTIVES_KEY)){
				JSONArray objectives = thing.get(LEARNING_OBJECTIVES_KEY).isArray();
			
				if(objectives != null){
					for(int i = 0; i < objectives.size(); i++){
						learningObjectives.add(objectives.get(i).isString().stringValue());
					}
				}
			}
			
			if(thing.containsKey(LANGUAGE_KEY)){
				JSONArray langs = thing.get(LANGUAGE_KEY).isArray();
				
				if(langs != null){
					for(int i = 0; i < langs.size(); i++){
						languages.add(LANGUAGE.findSymbolLanguage(langs.get(i).isString().stringValue()));
					}
				}
			}
		}
		
		DsESBApi.decalsUserCompetencies(setupCompetenciesCallback);
		DsESBApi.decalsLearningObjectives(setupLearningObjectivesCallback);
	}
	
	@Override
	public void display() {
		
		PageAssembler.ready(new HTML(getTemplates().getUserPreferencesPanel().getText()));
		PageAssembler.buildContents();
		
		UI.setupPreferences();
		setupPageHandlers(); 
		DOM.getElementById(COMPETENCY_LINK_ID).setAttribute("href", DsSession.getInstance().getCompetencyManagerUrl() + "/home?competencySessionId="+DsSession.getUser().getCompetencySessionId());
		
	}

	private void setupPageHandlers(){
		PageAssembler.attachHandler(DOM.getElementById(RESOURCE_TYPE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		PageAssembler.attachHandler(DOM.getElementById(LANGUAGE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		PageAssembler.attachHandler(DOM.getElementById(GRADE_LEVEL_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID), Event.ONKEYPRESS, typingObjectiveCallback);
		PageAssembler.attachHandler(DOM.getElementById(ADD_OBJECTIVE_BTN_ID), Event.ONCLICK, addLearningObjectiveCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(ADD_COMPETENCY_INPUT_ID), Event.ONCLICK | Event.ONFOCUS, openCompetencyModalCallback);
		PageAssembler.attachHandler(DOM.getElementById(ADD_COMPETENCY_BTN_ID), Event.ONCLICK, openCompetencyModalCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(SEARCH_COMPETENCY_INPUT_ID), Event.ONKEYPRESS, keypressSearchCompetenciesCallback);
		PageAssembler.attachHandler(DOM.getElementById(SEARCH_COMPETENCY_BTN_ID), Event.ONCLICK, searchCompetenciesCallback);
	}
	
	private ESBCallback<ESBPacket> setupCompetenciesCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onFailure(Throwable caught) {}

		@Override
		public void onSuccess(ESBPacket packet) {
			JSONObject thing = null;
			
			if (packet.containsKey("contentStream")) 
				thing = JSONParser.parseStrict(packet.getContentString()).isObject();
			else
				thing = packet.isObject();
			
			if(thing == null){
				return;
			}else{
				UI.setupCompetencyLists(thing);
			}
		}
	};
	
	private ESBCallback<ESBPacket> setupLearningObjectivesCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onSuccess(ESBPacket esbPacket) {
			Element e = DOM.getElementById(ADD_OBJECTIVE_INPUT_ID);
			JSONArray objectives = esbPacket.getArray("obj");
			
			setupTypeahead(e, objectives);
		}
		
		@Override
		public void onFailure(Throwable caught) {}
	};
	
	private static boolean prefsChanged = false;
	
	private static void informChangesMade(){
		prefsChanged = true;
		
		UI.showSavePreferencesButton();
		
		PageAssembler.attachHandler(DOM.getElementById(SAVE_PREFERENCES_BTN_ID), Event.ONCLICK, savePreferencesCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(CONFIRM_SAVE_PREFERENCES_BTN_ID), Event.ONCLICK, confirmSavePreferencesCallback);
		PageAssembler.attachHandler(DOM.getElementById(CANCEL_SAVE_PREFERENCES_BTN_ID), Event.ONCLICK, confirmCancelPreferencesCallback);
	}
	
	@Override
	public void lostFocus() {
		if(prefsChanged){
			PageAssembler.openPopup(CONFIRM_CHANGES_MODAL);
		}
	}
	
	private static class UI{
		
		/** Setup Preferences on Page Load **/
		
		public static void setupPreferences(){
			DsESBApi.decalsPreferenceTypes(new ESBCallback<ESBPacket>(){

				@Override
				public void onFailure(Throwable caught) {
					setupBasicPreferences();
				}
		
				@Override
				public void onSuccess(ESBPacket esbPacket) {
					setupOnlinePreferences(esbPacket);
				}
			   
			});
			
			for(String objective : learningObjectives){
				addLearningObjectiveToList(objective);
			}
		}
		
		public static void setupBasicPreferences(){
			
			
			setupBasicResourceTypeSelect();
			setupBasicGradeLevelSelect();
			
			String objectives = "";
			for(String obj : learningObjectives){
				objectives+="<li>"+obj+" <i class='fa fa-times'/></li>";
			}
			if(!objectives.isEmpty())
				DOM.getElementById("userObjectives").setInnerHTML(objectives);
		}
		
		public static void setupBasicResourceTypeSelect(){
			String resourceOptions = "";
			for(RESOURCE_TYPE type : RESOURCE_TYPE.values()){		
				if(resourceTypes.contains(type)){
					resourceOptions+="<option value='"+type.toSymbol()+"' selected='selected'>"+type.toString()+"</option>";
				}else{
					resourceOptions+="<option value='"+type.toSymbol()+"'>"+type.toString()+"</option>";
				}
			}
			DOM.getElementById("prefResourceType").setInnerHTML(resourceOptions);
		}
		
		public static void setupBasicGradeLevelSelect(){
			String gradeOptions = "";
			for(GRADE_LEVEL grade : GRADE_LEVEL.values()){
				if(gradeLevels.contains(grade)){
					gradeOptions+="<option value='"+grade.toSymbol()+"' selected='selected'>"+grade.toString()+"</option>";
				}else{
					gradeOptions+="<option value='"+grade.toSymbol()+"'>"+grade.toString()+"</option>";
				}
			}
			DOM.getElementById("prefGrade").setInnerHTML(gradeOptions);
		}
		
		public static void setupBasicLanguageSelect(){
			String languageOptions = "";
			for(LANGUAGE language : LANGUAGE.values()){
				if(language.equals(LANGUAGE.OTHER)){
					if(languages.contains(language)){
						languageOptions+="<option value='"+language.toSymbol()+"' selected='selected'>"+language.toString()+"</option>";
					}else{
						languageOptions+="<option value='"+language.toSymbol()+"'>"+language.toString()+"</option>";
					}
				}else{
					if(languages.contains(language)){
						languageOptions+="<option value='"+language.toSymbol()+"' selected='selected'>"+language.toString()+" ("+language.toSymbol()+")</option>";
					}else{
						languageOptions+="<option value='"+language.toSymbol()+"'>"+language.toString()+" ("+language.toSymbol()+")</option>";
					}
				}
			}
			DOM.getElementById("prefLang").setInnerHTML(languageOptions);
		}
		
		public static void setupOnlinePreferences(ESBPacket packet){
			String str = packet.getContentString();
			JSONObject thing = null;
			
			if (packet.containsKey("contentStream")) 
				thing = JSONParser.parseStrict(packet.getContentString()).isObject();
			else
				thing = packet.isObject();
			
			if(thing == null){
				setupPreferences();
			}else{
				if(thing.containsKey(RESOURCE_TYPES_KEY)){
					JSONObject types = thing.get(RESOURCE_TYPES_KEY).isObject();
					if(types != null){
						String resourceOptions = "";
						for(String key : types.keySet()){
							if(resourceTypes.contains(RESOURCE_TYPE.findSymbolType(key))){
								resourceOptions+="<option value='"+key+"' selected='selected'>"+types.get(key).isString().stringValue()+"</option>";
							}else{
								resourceOptions+="<option value='"+key+"'>"+types.get(key).isString().stringValue()+"</option>";
							}
							
							DOM.getElementById("prefResourceType").setInnerHTML(resourceOptions);
						}
					}else{
						setupBasicResourceTypeSelect();
					}
				}
				
				if(thing.containsKey(GRADE_LEVELS_KEY)){
					JSONObject grades = thing.get(GRADE_LEVELS_KEY).isObject();
					
					if(grades != null){
						String gradeOptions = "";
						for(String key : grades.keySet()){
							if(gradeLevels.contains(GRADE_LEVEL.findSymbolGrade(key))){
								gradeOptions+="<option value='"+key+"' selected='selected'>"+grades.get(key).isString().stringValue()+"</option>";
							}else{
								gradeOptions+="<option value='"+key+"'>"+grades.get(key).isString().stringValue()+"</option>";
							}
						}
						
						DOM.getElementById("prefGrade").setInnerHTML(gradeOptions);
					}else{
						setupBasicGradeLevelSelect();
					}
				}
				
				if(thing.containsKey(LANGUAGE_KEY)){
					JSONArray possibleLanguages = thing.get(LANGUAGE_KEY).isArray();
					
					if(possibleLanguages != null){
						boolean hasOther = false;
						
						String languageOptions = "";
						for(int i = 0; i < possibleLanguages.size(); i++){
							String key = possibleLanguages.get(i).isString().stringValue();
							LANGUAGE lang = LANGUAGE.findSymbolLanguage(key);
							
							if(lang != null){
								if(languages.contains(lang)){
									languageOptions+="<option value='"+key+"' selected='selected'>"+lang.toString()+" ("+lang.toSymbol()+")</option>";
								}else{
									languageOptions+="<option value='"+key+"'>"+lang.toString()+" ("+lang.toSymbol()+")</option>";
								}
							}
							
							if(lang.equals(LANGUAGE.OTHER))
								hasOther = true;
						}
						
						if(!hasOther){
							if(languages.contains(LANGUAGE.OTHER.toSymbol())){
								languageOptions += "<option value='"+LANGUAGE.OTHER.toSymbol()+"' selected='selected'>"+LANGUAGE.OTHER.toString()+"</option>";
							}else{
								languageOptions += "<option value='"+LANGUAGE.OTHER.toSymbol()+"' >"+LANGUAGE.OTHER.toString()+"</option>";
							}
						}
						
						DOM.getElementById("prefLang").setInnerHTML(languageOptions);
					}else{
						setupBasicLanguageSelect();
					}
				}
			}
		}
		
		public static void setupCompetencyLists(JSONObject competencyObj){
			if(competencyObj != null){
				for(String id : competencyObj.keySet()){
					JSONObject competency = competencyObj.get(id).isObject();
					
					if(competency != null){
						String levelId = competency.get(":recordLevel").isArray().get(0).isString().stringValue();
						
						JSONObject levels = competency.get("modelLevels").isObject();
						if(levels != null){
							int maxRank = -1;
							String maxId = "";
							
							for(String key : levels.keySet()){
								try{
									int levelRank = Integer.parseInt(levels.get(key).isObject().get(":competencyLevelRank").isArray().get(0).isString().stringValue());
									if(levelRank > maxRank){
										maxRank = levelRank;
										maxId = key;
									}
								}catch(NumberFormatException e){	}
							}
	
							if(levelId.equals(maxId)){
								addCompetencyToHeldList(competency);
							}else{
								addCompetencyToDesiredList(competency);
							}
						}
					}
				}
			}
		}
		
		/** Learning Objective Methods **/
		
		public static void addLearningObjectiveToList(String objective){
			Element listItem = DOM.createElement("li");
			Element span = DOM.createElement("span");
			span.setInnerText(objective);
			
			listItem.appendChild(span);
			
			Element removeBtn = DOM.createElement("i");
			removeBtn.addClassName("fa");
			removeBtn.addClassName("fa-times");
			listItem.appendChild(removeBtn);
			
			PageAssembler.attachHandler(removeBtn, Event.ONCLICK, removeLearningObjectiveCallback);
			
			DOM.getElementById(OBJECTIVE_LIST_ID).appendChild(listItem);
			
			DOM.getElementById(NO_OBJECTIVE_ITEM_ID).addClassName("hidden");	
		}
		
		public static void removeLearningObjectiveFromList(Element e){
			e.removeFromParent();
			
			if(learningObjectives.size() == 0)
				DOM.getElementById(NO_OBJECTIVE_ITEM_ID).removeClassName("hidden");	
		}
		
		/** Competency Methods **/
		
		public static void addCompetencyToHeldList(JSONObject competency){
			String competencyTitle = competency.get("competencyDetails").isObject().get(":competencyTitle").isArray().get(0).isString().stringValue();
			
			Element listItem = DOM.createElement("li");
			listItem.setInnerText(competencyTitle);
			
			DOM.getElementById(HELD_COMPETENCY_LIST_ID).appendChild(listItem);
			
			DOM.getElementById(NO_HELD_COMPETENCY_ITEM_ID).setClassName("hidden");
		}
		
		public static void addCompetencyToDesiredList(JSONObject competency){
			String competencyTitle = competency.get("competencyDetails").isObject().get(":competencyTitle").isArray().get(0).isString().stringValue();
			
			Element listItem = DOM.createElement("li");
			listItem.setInnerText(competencyTitle);
			
			DOM.getElementById(DESIRED_COMPETENCY_LIST_ID).appendChild(listItem);
			
			DOM.getElementById(NO_DESIRED_COMPETENCY_ITEM_ID).setClassName("hidden");
		}
		
		public static void showSavePreferencesButton(){
			DOM.getElementById(SAVE_PREFERENCES_BTN_ID).removeClassName("hidden");
			DOM.getElementById(PREFS_CHANGED_ALERT_ID).removeClassName("hidden");
		}

		public static void hideSavePreferencesButton(){
			DOM.getElementById(SAVE_PREFERENCES_BTN_ID).addClassName("hidden");
			DOM.getElementById(PREFS_CHANGED_ALERT_ID).addClassName("hidden");
		}
		
		public static void closeSavePreferencesModal(){
			PageAssembler.closePopup(CONFIRM_CHANGES_MODAL);
		}
		
		
		
		public static void openCompetencyModal() {
			PageAssembler.openPopup(COMPETENCY_SEARCH_MODAL);
			InputElement.as(DOM.getElementById(ADD_COMPETENCY_INPUT_ID)).blur();
		}
		
		public static void displayCompetencyResults(JSONObject thing) {
			for(String modelId : thing.keySet()){
				JSONObject competencies = thing.get(modelId).isObject();
				for(String competencyId : competencies.keySet()){
					JSONObject competency = competencies.get(competencyId).isObject();
					addCompetencySearchResult(competency);
				}
			}
			
		}
		
		public static void addCompetencySearchResult(JSONObject competency){
			String competencyTitle = competency.get(":competencyTitle").isArray().get(0).isString().stringValue();
		
			Element result = DOM.createElement("div");	
			result.addClassName("columns");
			result.addClassName("large-12");
			
			result.setInnerText(competencyTitle);
			
			Element resultsContainer = DOM.getElementById(SEARCH_COMPETENCY_RESULTS_ID);
			
			resultsContainer.appendChild(result);
		}
	}
	

	/** Save Preference Callbacks **/
	
	private EventCallback enableSaveCallback = new EventCallback(){
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			informChangesMade();
			
			ListBox resourceTypeSelect = ListBox.wrap(DOM.getElementById(RESOURCE_TYPE_SELECT_ID));
			ListBox gradeSelect = ListBox.wrap(DOM.getElementById(GRADE_LEVEL_SELECT_ID));
			ListBox langSelect = ListBox.wrap(DOM.getElementById(LANGUAGE_SELECT_ID));
			
			
			resourceTypes = new Vector<RESOURCE_TYPE>();
			for(int i = 0; i < resourceTypeSelect.getItemCount(); i++){
				if(resourceTypeSelect.isItemSelected(i)){
					resourceTypes.add(RESOURCE_TYPE.findSymbolType(resourceTypeSelect.getValue(i)));
				}
			}
			
			languages = new Vector<LANGUAGE>();
			for(int i = 0; i < langSelect.getItemCount(); i++){
				if(langSelect.isItemSelected(i)){
					languages.add(LANGUAGE.findSymbolLanguage(langSelect.getValue(i)));
				}
			}
			
			gradeLevels = new Vector<GRADE_LEVEL>();
			for(int i = 0; i < gradeSelect.getItemCount(); i++){
				if(gradeSelect.isItemSelected(i)){
					gradeLevels.add(GRADE_LEVEL.findSymbolGrade(gradeSelect.getValue(i)));
				}
			}
			
			PageAssembler.attachHandler(DOM.getElementById(RESOURCE_TYPE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
			PageAssembler.attachHandler(DOM.getElementById(LANGUAGE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
			PageAssembler.attachHandler(DOM.getElementById(GRADE_LEVEL_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
			PageAssembler.attachHandler(DOM.getElementById(ADD_OBJECTIVE_BTN_ID), Event.ONCLICK, enableSaveCallback);
			PageAssembler.attachHandler(DOM.getElementById(ADD_COMPETENCY_BTN_ID), Event.ONCLICK, enableSaveCallback);
		}
	};
	
	private static EventCallback savePreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			DsESBApi.decalsUpdateUserPreferences(resourceTypes, languages, gradeLevels, learningObjectives, preferencesSavedCallback);
			DsESBApi.decalsAddDesiredCompetencies(newDesiredCompetencyIds, competenciesSavedCallback);
			
			prefsChanged = false;
			
			UI.hideSavePreferencesButton();
		}
	};
	
	public static boolean prefsSaved = false;
	public static boolean competenciesSaved = false;
	
	private static ESBCallback<ESBPacket> preferencesSavedCallback = new ESBCallback<ESBPacket>() {

		
		@Override
		public void onFailure(Throwable caught) {
			
		}

		@Override
		public void onSuccess(ESBPacket esbPacket) {
			if(competenciesSaved){
				UI.hideSavePreferencesButton();
				competenciesSaved = false;
			}else{
				prefsSaved = true;
			}
		}
	};
	
	private static ESBCallback<ESBPacket> competenciesSavedCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onFailure(Throwable caught) {

		}

		@Override
		public void onSuccess(ESBPacket esbPacket) {
			if(competenciesSaved){
				UI.hideSavePreferencesButton();
				competenciesSaved = false;
			}else{
				prefsSaved = true;
			}
		}
	};
	
	
	/** Save Preferences Modal Callbacks **/
	
	private static EventCallback confirmSavePreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			DsESBApi.decalsUpdateUserPreferences(resourceTypes, languages, gradeLevels, learningObjectives, preferencesSavedCallback);
			DsESBApi.decalsAddDesiredCompetencies(newDesiredCompetencyIds, competenciesSavedCallback);
			
			UI.closeSavePreferencesModal();
		}
	};
	
	private static EventCallback confirmCancelPreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			UI.closeSavePreferencesModal();
		}
	};
	
	/** Learning Objective Callbacks **/
	
	private EventCallback addLearningObjectiveCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			InputElement input = InputElement.as(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID));
			String learningObjective = input.getValue();
			
			if(!learningObjective.isEmpty()){
				informChangesMade();
				
				learningObjectives.add(learningObjective);
				
				UI.addLearningObjectiveToList(learningObjective);
			}
			
			input.setValue("");
		}
	};
	
	private static EventCallback removeLearningObjectiveCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			informChangesMade();
			
			Element removeBtn = (Element) event.getTarget();
			Element e = removeBtn;
			
			while(!e.getTagName().equalsIgnoreCase("li")){
				e = (Element) e.getParentElement();
			}
			
			String oldObjective = e.getInnerText();
			
			learningObjectives.remove(oldObjective);
			
			UI.removeLearningObjectiveFromList(e);
			
			
		}
	};
	
	private EventCallback typingObjectiveCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			if(event.getKeyCode() == 13){
				informChangesMade();
				
				String learningObjective = InputElement.as(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID)).getValue();
				
				learningObjectives.add(learningObjective);
				
				UI.addLearningObjectiveToList(learningObjective);
				
				clearTypeaheadValue();
			}else{
				scrollToBottom();
			}
		}
	};
	
	/** Competency Modal Callbacks **/
	
	private EventCallback openCompetencyModalCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			UI.openCompetencyModal();
		}
	};
	
	private EventCallback searchCompetenciesCallback = new EventCallback(){
		@Override
		public void onEvent(Event event) {
			searchCompetencies();
		}
		
	};
	
	private EventCallback keypressSearchCompetenciesCallback = new EventCallback(){

		@Override
		public void onEvent(Event event) {
			if(event.getKeyCode() == 13){
				searchCompetencies();
			}
		}
		
	}; 
	
	private void searchCompetencies(){
		InputElement input = InputElement.as(DOM.getElementById(SEARCH_COMPETENCY_INPUT_ID));
		
		String query = input.getValue();
		
		DsESBApi.decalsSearchCompetencies(query, competencyResultsCallback);
	}
	
	private ESBCallback<ESBPacket> competencyResultsCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess(ESBPacket packet) {
			JSONObject thing = null;
			
			if (packet.containsKey("contentStream")) 
				thing = JSONParser.parseStrict(packet.getContentString()).isObject();
			else
				thing = packet.isObject();
			
			if(thing == null){
				return;
			}else{
				UI.displayCompetencyResults(thing);
			}
		}
	};
	
	private EventCallback addDesiredCompetencyCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			informChangesMade();
			
			InputElement input = InputElement.as(DOM.getElementById(COMPETENCY_ID_INPUT));
			String competencyId = input.getValue();
			
			newDesiredCompetencyIds.add(competencyId);
			
			InputElement titleInput = InputElement.as(DOM.getElementById(ADD_COMPETENCY_INPUT_ID));
			String competencyTitle = titleInput.getValue();
			
			//UI.addCompetencyToDesiredList(competencyTitle);
			titleInput.setValue("");
		}
	};
	
	
	/** Native Javascript Functions **/
	
	public static final native void setupTypeahead(Element e, JSONArray objectives) /*-{
		var substringMatcher = function(strs) {
		  return function findMatches(q, cb) {
		    var matches, substringRegex;
		 
		    // an array that will be populated with substring matches
		    matches = [];
		 
		    // regex used to determine if a string contains the substring `q`
		    substrRegex = new RegExp(q, 'i');
		 
		    // iterate through the pool of strings and for any string that
		    // contains the substring `q`, add it to the `matches` array
		    for(var idx in strs["jsArray"]){
		      var str = strs["jsArray"][idx];
		      if (substrRegex.test(str)) {
		        matches.push(str);
		      }
		    }
		 
		    cb(matches);
		  };
		};
		
		$wnd.$(e).typeahead({
				hint: true,
				highlight: true,
				minLength: 1
		},
		{
			name: "Objectives",
			source: substringMatcher(objectives)
		});
	}-*/;
	
	public static final native void clearTypeaheadValue() /*-{
		$wnd.$("#prefAddObjectiveInput").typeahead("val", "");
	}-*/;
	
	public static final native void scrollToBottom() /*-{
		$wnd.scroll(0,$doc.body.scrollHeight+100);
	}-*/;

}
