package com.eduworks.decals.ui.client;

import com.eduworks.decals.ui.client.pagebuilder.screen.DsGuestScreen;
//import com.eduworks.decals.ui.client.pagebuilder.screen.DsSearchScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsStudentHomeScreen;
import com.eduworks.decals.ui.client.pagebuilder.screen.DsTeacherHomeScreen;
import com.eduworks.gwt.client.pagebuilder.ScreenDispatch;

public class DsScreenDispatch extends ScreenDispatch
{
//	@Override
//	public void loadHomeScreen() {
//		loadScreen(new DsStudentHomeScreen(), true);
//	}
	
//	@Override
//	public void loadLoginScreen() {
//		loadScreen(new DsStudentHomeScreen(), true);
//	}
	
//	public void loadSearchScreen() {
//		loadScreen(new DsSearchScreen(), true);
//	}
	
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
}
