package com.eduworks.decals.ui.client.pagebuilder.screen;

import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.user.client.ui.HTML;

public class DsUserPreferencesScreen extends DecalsWithGroupMgmtScreen {

	@Override
	public void display() {

		
		PageAssembler.ready(new HTML(getTemplates().getUserPreferencesPanel().getText()));
		PageAssembler.buildContents();
		
		setupPageHandlers(); 

	}

	private void setupPageHandlers(){
		
	}
	
	@Override
	public void lostFocus() {
		// TODO Auto-generated method stub

	}

}
