package com.booyue.karaoke.utils;

import android.util.Log;

public class LoggerUtils {
	
	private static boolean isOpen = false;

	private static final String TAG = "LoggerUtils";
	
	public static void v(String tag, String msg){
		if(isOpen && msg!= null && tag != null){
			Log.v(tag, msg);
		}
	}
	public static void v(String msg){
		if(isOpen && msg!= null){
			Log.v(TAG,msg);
		}
	}

	public static void i(String tag, String msg){
		if(isOpen && msg!= null && tag != null){
			Log.i(tag, msg);
		}

	}

	public static void i(String msg){
		if(isOpen && msg!= null){
			Log.i(TAG,msg);
		}
	}


	public static void d(String tag, String msg){

		if(isOpen&& msg!= null && tag != null){
			Log.d(tag, msg);

		}
	}
	public static void d(String msg){

		if(isOpen&& msg!= null){
			Log.d(TAG, msg);

		}
	}


	public static void e(String tag, String msg){
		if(isOpen&& msg!= null && tag != null){
			Log.e(tag, msg);
		}
	}

	public static void e(String msg){

		if(isOpen&& msg!= null){
			Log.e(TAG, msg);
		}
	}
}

