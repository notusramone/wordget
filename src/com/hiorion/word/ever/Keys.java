package com.hiorion.word.ever;

import com.dropbox.client2.session.Session.AccessType;

import android.os.Environment;

public class Keys {
	public static int WordAddFlag=1;
	
	//public static int SyncTag_DeleteSynced=-1;
	public static int DataTag_Deleted=0;
	public static int DataTag_Regular=1;
	//public static int SyncTag_New=2;
	//public static int SyncTag_Updated=3;
	
	public static String Lib_uuid_global="";
	
	//Dropbox data
	public final static  String APP_KEY = "4k7y212qbbo5epi";
	public final static  String APP_SECRET = "wdx1yahm9u7dhi8";
	public final static  AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	public final static  String ACCOUNT_PREFS_NAME = "com.hiorion.wordeget_dropbox_prefs";
	public final static  String ACCESS_KEY_NAME = "ACCESS_KEY";
	public final static  String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	public static final String LogTag="wordget";
	
	public static final String IntentExtraKey_WordID="com.hiorion.wordget.wordid";
	
	public static final String IntentExtraKey_WordID_e="com.hiorion.wordget.wordide";
	
	public static final String DBName="wordget.db";
	
	public static final String FolderLocalApp=Environment.getExternalStorageDirectory().getPath()+"/wordget";
	
	public static final String FolderLocalAppData=FolderLocalApp+"/data";
	
	public static final String FolderLocalAppSpeech=FolderLocalApp+"/speech";
	
	public static final String FolderDropboxLock="/data/lock";
	
	public static final String DBPath_DropBox="/data/dont_move_or_delete_or_edit/wordget.db";
	
	public static final String DBPath_Local=FolderLocalAppData+"/wordget.db";
	
	public static final String DBPath_Sys=Environment.getDataDirectory().getPath()+"/data/com.hiorion.word.ever/databases/wordget.db";

	public static boolean MainListRefresh = false;

	
	public static String GetPartOfSpeech(int part){
		String partos="";
		switch(part){
		case 1:
			partos= "n";
			break;
		case 2:
			partos=  "adj";
			break;
		case 3:
			partos=  "v";
			break;
		case 4:
			partos=  "adv";
			break;
		case 5:
			partos=  "prep";
			break;
		case 6:
			partos=  "conj";
			break;
		case 7:
			partos=  "pron";
			break;
		case 8:
			partos=  "other";
			break;
		}
		return partos;
	}
}