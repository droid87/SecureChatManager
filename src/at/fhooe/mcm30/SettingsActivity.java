package at.fhooe.mcm30;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	private SharedPreferences sharedPrefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//		String value = sharedPrefs.getString(getString(R.string.pref_key_name),
//				"-");
		initSummery(getPreferenceScreen().getPreference(0));
	}
	
	private void initSummery(Preference p) {
		if (p instanceof EditTextPreference) {
	        EditTextPreference editTextPref = (EditTextPreference) p;
	        editTextPref.setSummary(editTextPref.getText());
		}
	}

}
