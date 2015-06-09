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
	
	private static final String DESIRED_COMPETENCY_LIST_ID = "desiredCompetenciesList";
	private static final String NO_DESIRED_COMPETENCY_ITEM_ID = "noDesiredCompetenciesItem";
	
	private static final String COMPETENCY_ID_INPUT = "prefAddCompetencyId";
	private static final String ADD_COMPETENCY_INPUT_ID = "prefAddCompetencyInput";
	private static final String ADD_COMPETENCY_BTN_ID = "addCompetencyBtn";
	
	private static final String SAVE_PREFERENCES_BTN_ID = "savePreferencesBtn";
	private static final String PREFS_CHANGED_ALERT_ID = "prefChangedAlert";
	
	private static final String CONFIRM_SAVE_PREFERENCES_BTN_ID = "confirmSavePreferences";
	private static final String CANCEL_SAVE_PREFERENCES_BTN_ID = "confirmCancelPreferences";
	
	private static final String CONFIRM_CHANGES_MODAL = "modalConfirmCancel";
	private static final String COMPETENCY_SEARCH_MODAL = "modalCompetencySearch";
	
	public Vector<RESOURCE_TYPE> resourceTypes = new Vector<RESOURCE_TYPE>();
	public Vector<GRADE_LEVEL> gradeLevels = new Vector<GRADE_LEVEL>();
	public Vector<LANGUAGE> languages = new Vector<LANGUAGE>();

	public Vector<String> learningObjectives = new Vector<String>();
	
	public Vector<String> newDesiredCompetencyIds = new Vector<String>();
	
	public DsUserPreferencesScreen(ESBPacket packet) {
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
		DsESBApi.decalsLearningObjectives(learningObjectivesCallback);
	}
	
	@Override
	public void display() {
		
		PageAssembler.ready(new HTML(getTemplates().getUserPreferencesPanel().getText()));
		PageAssembler.buildContents();
		
		setupPreferences();
		setupPageHandlers(); 

	}

	private void setupPageHandlers(){
		PageAssembler.attachHandler(DOM.getElementById(RESOURCE_TYPE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		PageAssembler.attachHandler(DOM.getElementById(LANGUAGE_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		PageAssembler.attachHandler(DOM.getElementById(GRADE_LEVEL_SELECT_ID), Event.ONCHANGE, enableSaveCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID), Event.ONKEYPRESS, typingObjectiveCallback);
		PageAssembler.attachHandler(DOM.getElementById(ADD_OBJECTIVE_BTN_ID), Event.ONCLICK, addLearningObjectiveCallback);
		
		PageAssembler.attachHandler(DOM.getElementById(ADD_COMPETENCY_INPUT_ID), Event.ONCLICK | Event.ONFOCUS, openCompetencyCallback);
		PageAssembler.attachHandler(DOM.getElementById(ADD_COMPETENCY_BTN_ID), Event.ONCLICK, addDesiredCompetencyCallback);
	}
	
	private void setupPreferences(){
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
	
	
	private void setupBasicPreferences(){
		
		
		setupBasicResourceTypeSelect();
		setupBasicGradeLevelSelect();
		
		String objectives = "";
		for(String obj : learningObjectives){
			objectives+="<li>"+obj+" <i class='fa fa-times'/></li>";
		}
		if(!objectives.isEmpty())
			DOM.getElementById("userObjectives").setInnerHTML(objectives);
	}
	
	private void setupBasicResourceTypeSelect(){
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
	
	private void setupBasicGradeLevelSelect(){
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
	
	private void setupBasicLanguageSelect(){
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
	
	private void setupOnlinePreferences(ESBPacket packet){
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
	
	private void addLearningObjectiveToList(String objective){
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
	
	private void removeLearningObjectiveFromList(String objective){
		if(learningObjectives.size() == 0)
			DOM.getElementById(NO_OBJECTIVE_ITEM_ID).removeClassName("hidden");	
	}
	
	private void addCompetencyToDesiredList(String competencyTitle){
		Element listItem = DOM.createElement("li");
		listItem.setInnerText(competencyTitle);
		
		DOM.getElementById(DESIRED_COMPETENCY_LIST_ID).appendChild(listItem);
		
		DOM.getElementById(NO_DESIRED_COMPETENCY_ITEM_ID).setClassName("hidden");
	}
	
	private void setupCompetencyLists(JSONObject competencyObj){
		if(competencyObj != null){
			for(String id : competencyObj.keySet()){
				JSONObject competency = competencyObj.get(id).isObject();
				
				if(competency != null){
					
				}
			}
		}
	}
	
	
	private boolean prefsChanged = false;
	
	private void informChangesMade(){
		prefsChanged = true;
		DOM.getElementById(SAVE_PREFERENCES_BTN_ID).removeClassName("hidden");
		DOM.getElementById(PREFS_CHANGED_ALERT_ID).removeClassName("hidden");
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
	
	private EventCallback savePreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			DsESBApi.decalsUpdateUserPreferences(resourceTypes, languages, gradeLevels, learningObjectives, preferencesSavedCallback);
			DsESBApi.decalsAddDesiredCompetencies(newDesiredCompetencyIds, competenciesSavedCallback);
			
			prefsChanged = false;
			DOM.getElementById(SAVE_PREFERENCES_BTN_ID).addClassName("hidden");
			DOM.getElementById(PREFS_CHANGED_ALERT_ID).addClassName("hidden");
		}
	};
	
	private ESBCallback<ESBPacket> preferencesSavedCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess(ESBPacket esbPacket) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private ESBCallback<ESBPacket> competenciesSavedCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess(ESBPacket esbPacket) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private EventCallback confirmSavePreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			DsESBApi.decalsUpdateUserPreferences(resourceTypes, languages, gradeLevels, learningObjectives, preferencesSavedCallback);
			DsESBApi.decalsAddDesiredCompetencies(newDesiredCompetencyIds, competenciesSavedCallback);
			
			PageAssembler.closePopup(CONFIRM_CHANGES_MODAL);
		}
	};
	
	private EventCallback confirmCancelPreferencesCallback = new EventCallback() {
		@Override
		public void onEvent(com.google.gwt.user.client.Event event) {
			PageAssembler.closePopup(CONFIRM_CHANGES_MODAL);
		}
	};
	
	private EventCallback addLearningObjectiveCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			informChangesMade();
			
			InputElement input = InputElement.as(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID));
			String learningObjective = input.getValue();
			
			learningObjectives.add(learningObjective);
			
			addLearningObjectiveToList(learningObjective);
			
			input.setValue("");
		}
	};
	
	private EventCallback removeLearningObjectiveCallback = new EventCallback() {
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
			
			removeLearningObjectiveFromList(oldObjective);
			e.removeFromParent();
			
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
			
			addCompetencyToDesiredList(competencyTitle);
			titleInput.setValue("");
		}
	};
	
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
				if( thing.containsKey("competencies") ){
					setupCompetencyLists(thing.get("competencies").isObject());
				}
			}
		}
	};
	
	private ESBCallback<ESBPacket> learningObjectivesCallback = new ESBCallback<ESBPacket>() {
		@Override
		public void onSuccess(ESBPacket esbPacket) {
			Element e = DOM.getElementById(ADD_OBJECTIVE_INPUT_ID);
			JSONArray objectives = esbPacket.getArray("obj");
			
			setupTypeahead(e, objectives);
		}
		
		@Override
		public void onFailure(Throwable caught) {}
	};
	
	private EventCallback typingObjectiveCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			if(event.getKeyCode() == 13){
				informChangesMade();
				
				String learningObjective = InputElement.as(DOM.getElementById(ADD_OBJECTIVE_INPUT_ID)).getValue();
				
				learningObjectives.add(learningObjective);
				
				addLearningObjectiveToList(learningObjective);
				
				clearTypeaheadValue();
			}else{
				scrollToBottom();
			}
		}
	};
	
	private EventCallback openCompetencyCallback = new EventCallback() {
		@Override
		public void onEvent(Event event) {
			PageAssembler.openPopup(COMPETENCY_SEARCH_MODAL);
			InputElement.as(DOM.getElementById(ADD_COMPETENCY_INPUT_ID)).blur();
		}
	};
	
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
