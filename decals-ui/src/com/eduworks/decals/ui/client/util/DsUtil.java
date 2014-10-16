package com.eduworks.decals.ui.client.util;

import com.eduworks.decals.ui.client.DsScreenDispatch;
import com.eduworks.gwt.client.net.api.ESBApi;
import com.eduworks.gwt.client.net.callback.ESBCallback;
import com.eduworks.gwt.client.net.packet.ESBPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * General DECALS utility package for handling various tasks.
 * 
 * @author Tom B.
 *
 */
public class DsUtil {
   
   private static final String ERROR_BOX_STYLE = "alert-box error";
   
   /**
    * Common English stop words.
    */
   public static String[] stopWords = { 
      "a","able","about","across","after","all","almost","also","am","among","an","and","any","are","as","at","be",
      "because","been","but","by","can","cannot","could","dear","did","do","does","either","else","ever","every",
      "for","from","get","got","had","has","have","he","her","hers","him","his","how","however","i","if","in","into",
      "is","it","its","just","least","let","like","likely","may","me","might","most","must","my","neither","no",
      "nor","not","of","off","often","on","only","or","other","our","own","rather","said","say","says","she",
      "should","since","so","some","than","that","the","their","them","then","there","these","they","this","tis",
      "to","too","twas","us","wants","was","we","were","what","when","where","which","while","who","whom","why",
      "will","with","would","yet","you","your"
   };
   
   /**
    * Returns true if the given string is a stop word.  Returns false otherwise.
    * 
    * @param s The string to check.
    * @return Returns true if the given string is a stop word.  Returns false otherwise.
    */
   public static boolean isStopWord(String s) {      
      if (s == null || s.trim().isEmpty()) return false;
      for (String sw:stopWords) {
         if (sw.equalsIgnoreCase(s)) return true;
      }
      return false;
   }
   
   /**
    * Removes all stop words from the given string.
    * 
    * @param s The string in which to remove all stop words.
    * @return The given string with all stop words removed. 
    */
   public static String removeStopWords(String s) {
      StringBuffer sb = new StringBuffer();
      String[] sa = s.split(" ");
      for (int i=0;i<sa.length;i++) {
         if (!isStopWord(sa[i])) {
            sb.append(sa[i]);
            if (i != (sa.length - 1)) sb.append(" ");
         } 
      }
      return sb.toString().trim();
   }
   
   /**
    * Replaces all non-alpha characters with an empty string.
    * 
    * @param s The string in which to remove all non-alpha characters.
    * @return The given string with all non-alpha characters removed.
    */
   public static String removeNonAlphaChars(String s) {
      return s.replaceAll("[^a-zA-Z]", "");
   }
   
   /**
    * Returns true if the given string is a number.  Returns false otherwise.
    * 
    * @param s The string to check.
    * @return Returns true if the given string is a number.  Returns false otherwise.
    */
   public static boolean isValueNumber(String s) {
      try {
         Double.parseDouble(s);
         return true;
      }
      catch (Exception e) {
         return false;
      }      
   }
   
   /**
    * Basic window.alert call that is used to handle failed API calls.
    * 
    * @param caught The throwable that was caught on the failed call.
    */
   public static void handleFailedApiCall(Throwable caught) {
      //TODO handle this more elegantly
      Window.alert("failure->caught:" + caught.getMessage());
   }
   
   /**
    * Sends the given message to the DECALS tracking service.
    * 
    * @param message The message to send.
    */
   public static void sendTrackingMessage(String message) {
//      if (DsSession.getInstance().getAssignmentId() == null || DsSession.getInstance().getAssignmentId().trim().isEmpty()) return;
//      DsESBApi.decalsAddTracking(DsSession.getInstance().getAssignmentId(),message, new ESBCallback<ESBPacket>() {
//         @Override
//         public void onSuccess(ESBPacket result) {}
//         @Override
//         public void onFailure(Throwable caught) {handleFailedApiCall(caught);}
//         });
   }
   
   /**
    * Parses a given string into a JSONObject
    * 
    * I'm putting this here just in case I need to change how this is done.
    * 
    * @param jsonStr The JSON string to parse.
    * @return Returns the JSON object.
    */
   public static JSONObject parseJson(String jsonStr) {
     return (JSONObject) JSONParser.parseStrict(jsonStr);
   }
   
   /**
    * Dispatches to the teacher home screen if the user is a teacher.  Dispatches to student home page otherwise.
    * 
    * @param screenDispatch
    */
   public static void setUpAppropriateHomePage(final DsScreenDispatch screenDispatch) {
      ESBApi.userHasPermission("isTeacher", new ESBCallback<ESBPacket>() {
         @Override
         public void onSuccess(ESBPacket result) {
            if ("true".equalsIgnoreCase(result.getPayloadString())) screenDispatch.loadTeacherHomeScreen();
            else screenDispatch.loadStudentHomeScreen();
         }
         @Override
         public void onFailure(Throwable caught) {handleFailedApiCall(caught);}
         });
   }
   
   /**
    * Removes all widgets from the given root panel.
    * 
    * @param rp
    */
   public static final void removeAllWidgetsFromRootPanel(RootPanel rp) {      
      if (rp == null) return;
      for (int i=rp.getWidgetCount() - 1;i>=0;i--) {
         if (rp.getWidget(i) != null) rp.remove(i);
      }
   }
   
   /**
    * Removes an element with the given ID from the given root panel.
    * 
    * @param rp
    * @param elementIdToRemove
    */
   public static final void removeElementFromRootPanel(RootPanel rp, String elementIdToRemove) {      
      for (int i=rp.getWidgetCount() - 1;i>=0;i--) {
         if (rp.getWidget(i).getElement().getId().equals(elementIdToRemove)) {
            rp.remove(i);
            break;
         }
      }
   }
   
   /**
    * Clears all widgets from the widget container and displays the given error message in a simple error widget placed in the given error widget container.
    * (Say that fast!)
    * 
    * @param errorWidgetContainer The container in which to place the widget.
    * @param errorMessage The message to display.
    * @param removeOtherWidgets The remove other widgets indicator
    */
   public static final void showSimpleErrorMessage(String errorWidgetContainer, String errorMessage, boolean removeOtherWidgets) {
      ((Label)PageAssembler.elementToWidget(errorWidgetContainer, PageAssembler.LABEL)).getElement().setAttribute("style","display:block");
      StringBuffer sb = new StringBuffer();
      sb.append("<div class=\"" + ERROR_BOX_STYLE + "\">");
      sb.append("<span>" + errorMessage + "</span>");
      sb.append("</div>");
      HTML errorDialog = new HTML(sb.toString());
      if(removeOtherWidgets) removeAllWidgetsFromRootPanel(RootPanel.get(errorWidgetContainer));
      RootPanel.get(errorWidgetContainer).add(errorDialog);            
   }
      
   /**
    * Cleans the given string of all special characters and double spaces
    * 
    * @param s The string to clean.
    * @return Returns the clean string.
    */
   public static String cleanString(String s) {
      s = s.replace(":", " ");
      s = s.replace("-", " ");
      String s2 = s.replaceAll("[^\\w\\s]","").replaceAll("  ", " ").trim();
      String s3 = s2.replaceAll("  ", " ").trim();
      while (!s3.equals(s2)) {
         s3 = s2.replaceAll("  ", " ");
         if (!s3.equals(s2)) s2 = s3.replaceAll("  ", " ");
      }
      return s3;
   }
   
   /**
    * Returns true if the CheckBox with the given ID is checked.  Returns false otherwise.
    * 
    * @param checkBoxFieldId The ID of the CheckBox.
    * @return Returns true if the CheckBox with the given ID is checked.  Returns false otherwise.
    */
   public static boolean isCheckBoxChecked(String checkBoxFieldId) {
      if (checkBoxFieldId == null || checkBoxFieldId.trim().isEmpty()) return false;      
      return ((SimpleCheckBox)PageAssembler.elementToWidget(checkBoxFieldId, PageAssembler.CHECK_BOX)).getValue();
   }
   
   /**
    * Applies the given value to the given to the CheckBox with the given ID (true for checked, false for unchecked).
    * 
    * @param checkBoxFieldId The ID of the CheckBox.
    * @param value The value to apply to the CheckBox (true for checked, false for unchecked).
    */
   public static void applyCheckBoxValue(String checkBoxFieldId, boolean value) {
      if (checkBoxFieldId != null && !checkBoxFieldId.trim().isEmpty()) {
         ((SimpleCheckBox)PageAssembler.elementToWidget(checkBoxFieldId, PageAssembler.CHECK_BOX)).setValue(value);
      }
   }
   
   /**
    * Returns the text of the Anchor with the given ID.
    * 
    * @param anchorFieldId The ID of the Anchor field to get the text.
    * @return Returns the text of the Anchor with the given ID.
    */
   public static final String getAnchorText(String anchorFieldId) {
      if (anchorFieldId != null && !anchorFieldId.trim().isEmpty()) return ((Anchor)PageAssembler.elementToWidget(anchorFieldId, PageAssembler.A)).getText();
      else return null;            
   }
   
   /**
    * Returns the text of the TextBox with the given ID.  Returns null if the given ID is blank or null.
    * 
    * @param textBoxFieldId The ID of the TextBox field to retrieve the text.
    * @return Returns the text of the TextBox with the given ID. Returns null if the given ID is blank or null.
    */
   public static final String getTextBoxText(String textBoxFieldId) {
      if (textBoxFieldId != null && !textBoxFieldId.trim().isEmpty()) return ((TextBox)PageAssembler.elementToWidget(textBoxFieldId, PageAssembler.TEXT)).getText();
      else return null;
   }
   
   /**
    * Sets the text of the TextBox with the given ID to the given text.
    * 
    * @param textBoxFieldId The ID of the TextBox field to set the text.
    * @param text The text to set.
    */
   public static final void setTextBoxText(String textBoxFieldId, String text) {
      if (textBoxFieldId != null && !textBoxFieldId.trim().isEmpty()) 
           ((TextBox)PageAssembler.elementToWidget(textBoxFieldId, PageAssembler.TEXT)).setText(text);      
   }
   
   /**
    * Sets the text of the Anchor with the given ID to the given text.
    * 
    * @param anchorFieldId The ID of the Anchor field to set the text.
    * @param text The text to set.
    */
   public static final void setAnchorText(String anchorFieldId, String text) {
      if (anchorFieldId != null && !anchorFieldId.trim().isEmpty()) 
         ((Anchor)PageAssembler.elementToWidget(anchorFieldId,PageAssembler.A)).setText(text);      
   }
   
   /**
    * Sets the text of the Label with the given ID to the given text.
    * 
    * @param labelFieldId The ID of the Label field to set the text.
    * @param text The text to set.
    */
   public static final void setLabelText(String labelFieldId, String text) {
      if (labelFieldId != null && !labelFieldId.trim().isEmpty()) 
         ((Label)PageAssembler.elementToWidget(labelFieldId,PageAssembler.LABEL)).setText(text);      
   }
   
   /**
    * Returns the text of the Label with the given ID.  Returns null if the given ID is blank or null.
    * 
    * @param labelFieldId The ID of the Label field to retrieve the text.
    * @return Returns the text of the Label with the given ID. Returns null if the given ID is blank or null.
    */
   public static final String getLabelText(String labelFieldId) {
      if (labelFieldId != null && !labelFieldId.trim().isEmpty()) return ((Label)PageAssembler.elementToWidget(labelFieldId,PageAssembler.LABEL)).getText();
      else return null;
   }
   
   /**
    * Sets the focus to the TextBox with the given ID.
    * 
    * @param textBoxFieldId The ID of the TextBox field to set focus.
    */
   public static final void setFocus(String textBoxFieldId) {
      if (textBoxFieldId != null && !textBoxFieldId.trim().isEmpty())
         ((TextBox)PageAssembler.elementToWidget(textBoxFieldId, PageAssembler.TEXT)).setFocus(true);
   }
   
   /**
    * Sets the style of the label with the given ID to 'display:block'.
    * 
    * @param labelFieldId The ID of the label field to show.
    */
   public static final void showLabel(String labelFieldId) {
      if (labelFieldId != null && !labelFieldId.trim().isEmpty())
         ((Label)PageAssembler.elementToWidget(labelFieldId, PageAssembler.LABEL)).getElement().setAttribute("style","display:block");
   }
   
   /**
    * Sets the style of the label with the given ID to 'display:none'.
    * 
    * @param labelFieldId The ID of the label field to hide.
    */
   public static final void hideLabel(String labelFieldId) {
      if (labelFieldId != null && !labelFieldId.trim().isEmpty())
         ((Label)PageAssembler.elementToWidget(labelFieldId, PageAssembler.LABEL)).getElement().setAttribute("style","display:none");
   }
   
   /**
    * Sets the style of the image with the given ID to the given style.
    * 
    * @param imageFieldId The ID of the image field to hide.
    * @param style The style to set for the image
    */
   public static final void setImageStyle(String imageFieldId, String style) {
      if (imageFieldId != null && !imageFieldId.trim().isEmpty())
         ((Image)PageAssembler.elementToWidget(imageFieldId,PageAssembler.IMAGE)).getElement().setAttribute("style",style);
   }
   
   /**
    * Sets the style of the image with the given ID to 'display:none'.
    * 
    * @param imageFieldId The ID of the image field to hide.
    */
   public static final void hideImage(String imageFieldId) {
      setImageStyle(imageFieldId,"display:none");
   }
   
   /**
    * Sets the style of the button with the given ID to the given style.
    * 
    * @param buttonFieldId The ID of the button field to show.
    */
   public static final void setButtonStyle(String buttonFieldId, String style) {
      if (buttonFieldId != null && !buttonFieldId.trim().isEmpty())
         ((Button)PageAssembler.elementToWidget(buttonFieldId, PageAssembler.BUTTON)).getElement().setAttribute("style",style);
   }
   
   
   /**
    * Sets the style of the button with the given ID to 'display:block'.
    * 
    * @param buttonFieldId The ID of the button field to show.
    */
   public static final void showButton(String buttonFieldId) {
      setButtonStyle(buttonFieldId,"display:block");
   }
   
   /**
    * Sets the style of the button with the given ID to 'display:none'.
    * 
    * @param buttonFieldId The ID of the button field to hide.
    */
   public static final void hideButton(String buttonFieldId) {
      setButtonStyle(buttonFieldId,"display:none");
   }
   
   /**
    * Clears all widgets from the widget container and displays the given error message in a simple error widget placed in the given error widget container.
    * (Say that fast!)
    * 
    * This call removes all other widgets on the display container.
    * 
    * @param errorWidgetContainer The container in which to place the widget.
    * @param errorMessage The message to display.
    */
   public static final void showSimpleErrorMessage(String errorWidgetContainer, String errorMessage) {
      showSimpleErrorMessage(errorWidgetContainer, errorMessage, true);
//      ((Label)PageAssembler.elementToWidget(errorWidgetContainer, PageAssembler.LABEL)).getElement().setAttribute("style","display:block");
//      StringBuffer sb = new StringBuffer();
//      sb.append("<div class=\"" + ERROR_BOX_STYLE + "\">");
//      sb.append("<span>" + errorMessage + "</span>");
//      sb.append("</div>");
//      HTML errorDialog = new HTML(sb.toString());
//      removeAllWidgetsFromRootPanel(RootPanel.get(errorWidgetContainer));
//      RootPanel.get(errorWidgetContainer).add(errorDialog);            
   }

}
