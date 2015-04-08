package com.eduworks.decals.ui.client;

import com.eduworks.decals.ui.client.pagebuilder.DsHtmlTemplates;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsGuestScreen;
import com.eduworks.gwt.client.component.AppEntry;
import com.eduworks.gwt.client.net.CommunicationHub;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Decals_ui extends AppEntry implements EntryPoint, ValueChangeHandler<String> {
   
   private static final String IS_TN_ROOT_PROP = "int.search.thumbnail.root";
   
   
   @Override
	public void onModuleLoad() {
      dispatcher = new DsScreenDispatch();
		defaultScreen = new DsGuestScreen();
		templates = GWT.create(DsHtmlTemplates.class);
		//parse DECALS properties
		CommunicationHub.sendHTTP(CommunicationHub.GET, DEFAULT_INSTALLATION_SETTINGS_LOC, null, false, new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket ESBPacket) {parseDecalsProperties(ESBPacket.getString(CONTENT_STREAM).split("\r\n|\r|\n"));}
         @Override
         public void onFailure(Throwable caught) {
            Window.alert("Couldn't find network settings");
         }
      });
	}
   
   //Parse DECALS specific properties
   private void parseDecalsProperties(String[] rawProperties) {      
      if ((rawProperties[0].indexOf(SITE_NAME_PROP) != -1)) {
         for (String prop:rawProperties) {            
            if (prop.indexOf(IS_TN_ROOT_PROP) != -1) DsSession.getInstance().setInteractiveSearchThumbnailRootUrl(parseProperty(prop));            
         }
      }            
   }
}
