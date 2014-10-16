package com.eduworks.decals.ui.client.handler;

import com.eduworks.decals.ui.client.DsScreenDispatch;
import com.eduworks.decals.ui.client.util.DsUtil;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.callback.EventCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.user.client.Event;

/**
 * Attach handlers for the elements on the DECALS Header
 * 
 * Handlers are can be attached as needed or all together using {@link #setUpHeader(String)}.
 * 
 * @author Tom Buskirk
 *
 */
public class DsHeaderHandler {
   
   private DsScreenDispatch screenDispatch = null;
   
   private static final String HEADER_DASHBOARD = "headerDashboard";
   private static final String HEADER_LOGOUT = "headerLogout";
   private static final String HEADER_USERNAME = "headerUsername";
   
   /**
    * DsHeaderHandler constructor
    * 
    * @param screenDispatch The screenDispatch to associate with this handler
    */
   public DsHeaderHandler(DsScreenDispatch screenDispatch) {
      this.screenDispatch = screenDispatch;
   }
   
   /**
    * Listener for dashboard clicks
    */
   protected EventCallback dashboardListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         DsUtil.setUpAppropriateHomePage(screenDispatch);
      }
   };
  
   /**
    * Listener for logout clicks
    */
   protected EventCallback logoutListener = new EventCallback() {
      @Override
      public void onEvent(Event event) {
         ESBApi.logout(new ESBCallback<ESBPacket>() {
            @Override
            public void onSuccess(ESBPacket result) {screenDispatch.loadGuestScreen();}
            @Override
            public void onFailure(Throwable caught) {DsUtil.handleFailedApiCall(caught);}
            });
      }
   };
   
   /**
    * Attach the dashboard listeners
    */
   public void attachDashboardHandler() {      
      PageAssembler.attachHandler(HEADER_DASHBOARD, Event.ONCLICK, dashboardListener);
   }
   
   /**
    * Attach the logout listener
    */
   public void attachLogoutHandler() {
      PageAssembler.attachHandler(HEADER_LOGOUT, Event.ONCLICK, logoutListener);
   }
   
   /**
    * Attach the user name to the page header
    * 
    * @param headerUsernameText The user name to attach
    */
   public void attachUsernameToHeader(String headerUsernameText) {
      DsUtil.setLabelText(HEADER_USERNAME, headerUsernameText);      
   }
   
   /**
    * Set up all header options
    * 
    * @param headerUsernameText The user name to attach to the header
    */
   public void setUpHeader(String headerUsernameText) {
      attachDashboardHandler();
      attachLogoutHandler();
      attachUsernameToHeader(headerUsernameText);
   }

}
