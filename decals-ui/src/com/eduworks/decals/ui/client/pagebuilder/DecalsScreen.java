package com.eduworks.decals.ui.client.pagebuilder;

import com.eduworks.decals.ui.client.DsScreenDispatch;
import com.eduworks.decals.ui.client.pagebuilder.DsHtmlTemplates;
import com.eduworks.gwt.client.component.AppSettings;
import com.eduworks.gwt.client.pagebuilder.ScreenTemplate;

/**
 * Base screen class.
 * 
 * @author Eduworks Corporation
 *
 */
public abstract class DecalsScreen extends ScreenTemplate {
   
   public static final String CONTENT_PANE = "contentPane";
   public static final String VALID_EVENT = "valid";
   
	public DsScreenDispatch getDispatcher() {return (DsScreenDispatch) AppSettings.dispatcher;}
	public DsHtmlTemplates getTemplates() {return (DsHtmlTemplates) AppSettings.templates;}
	
}
