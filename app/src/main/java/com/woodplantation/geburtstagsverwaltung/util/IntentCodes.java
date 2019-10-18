package com.woodplantation.geburtstagsverwaltung.util;

import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;

/**
 * Created by Sebu on 18.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class IntentCodes {

	public final String DATASET = MainActivity.PACKAGE_NAME + "." + "DATASET";
	public final String INDEX = MainActivity.PACKAGE_NAME + "." + "INDEX";
	public final String NEW_ID = MainActivity.PACKAGE_NAME + "." + "NEW_ID";
	public final String OLD_PREFERENCES = MainActivity.PACKAGE_NAME + "." + "OLD_PREFERENCES";
	public final String ID = MainActivity.PACKAGE_NAME + "." + "ID";
	public final String WHICH = MainActivity.PACKAGE_NAME + "." + "WHICH";
	public final String WHEN = MainActivity.PACKAGE_NAME + "." + "WHEN";


	private static final IntentCodes ourInstance = new IntentCodes();

	public static IntentCodes getInstance() {
		return ourInstance;
	}

	private IntentCodes() {
	}
}
