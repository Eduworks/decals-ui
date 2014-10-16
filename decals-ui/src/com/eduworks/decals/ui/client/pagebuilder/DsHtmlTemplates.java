package com.eduworks.decals.ui.client.pagebuilder;

import com.eduworks.gwt.client.component.HtmlTemplates;
import com.google.gwt.resources.client.TextResource;

public interface DsHtmlTemplates extends HtmlTemplates {
	/**
	 * getLoginWidget Retrieves the login widget source code
	 * @return TextResource
	 */
	@Source("template/DecalsGuestHeader.html")
	public TextResource getGuestHeader();
	
	@Source("template/DecalsHeader.html")
   public TextResource getHeader();
	
	@Source("template/DecalsFooter.html")
	public TextResource getFooter();
	
	@Source("template/DecalsStudentHomePanel.html")
	public TextResource getStudentHomePanel();
	
	@Source("template/DecalsTeacherHomePanel.html")
   public TextResource getTeacherHomePanel();
	
	@Source("template/DecalsGuestPanel.html")
   public TextResource getGuestPanel();
	
	@Source("template/DecalsSearchPanel.html")
	public TextResource getSearchPanel();
	
	@Source("template/BasicSearchResultWidget.html")
	public TextResource getBasicSearchResultWidget();
	
	@Source("template/InteractiveSearchResultWidget.html")
   public TextResource getInteractiveSearchResultWidget();
	
}
