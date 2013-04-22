package com.mobiric.stackflairwidget.utils;


/**
 * Utility class for StackFlair specific functions.
 */
public class FlairUtils
{

	public static String getFlairDownloadUrl(String website, String user, String theme)
	{
		return "http://" + website + "/users/flair/" + user + ".png?theme=" + theme;
	}
}
