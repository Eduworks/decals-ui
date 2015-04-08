package com.eduworks.decals.ui.client;

import com.eduworks.decals.ui.client.pagebuilder.screen.DsApplicationAdminScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsGuestScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsStudentHomeScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsTeacherHomeScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsUserHomeScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsUserLrRSearchScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsUserManagementScreen;
import com.eduworks.gwt.client.pagebuilder.ScreenDispatch;

public class DsScreenDispatch extends ScreenDispatch
{
   
	/**
	 * Loads DsGuestScreen.
	 */
	public void loadGuestScreen() {loadScreen(new DsGuestScreen(), true);}	
	
	/**
	 * Loads DsStudentHomeScreen.
	 */
	public void loadStudentHomeScreen() {loadScreen(new DsStudentHomeScreen(), true);}	
	
	/**
	 * Loads DsTeacherHomeScreen.
	 */
	public void loadTeacherHomeScreen() {loadScreen(new DsTeacherHomeScreen(), true);}
	
	/**
    * Loads DsUserHomeScreen.
    */
   public void loadUserHomeScreen() {loadScreen(new DsUserHomeScreen(), true);}
   
   /**
    * Loads DsUserLRSearchScreen.
    */
   public void loadUserLRSearchScreen() {loadScreen(new DsUserLrRSearchScreen(), true);}
   
   /**
    * Loads DsApplicationAdminScreen.
    */
   public void loadApplicationAdminScreen() {loadScreen(new DsApplicationAdminScreen(), true);}
   
   /**
    * Loads DsUserManagementScreen.
    */
   public void loadUserManagementScreen() {loadScreen(new DsUserManagementScreen(), true);}
}
