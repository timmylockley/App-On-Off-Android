package com.forest.apponandoff;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	
	private PackageManager pm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pm = getPackageManager();
		
		if (!hasUsageStatsPermission()) {
			startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
			Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show();
		}
		
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		
		ScrollView scrollView = new ScrollView(this);
		LinearLayout listLayout = new LinearLayout(this);
		listLayout.setOrientation(LinearLayout.VERTICAL);
		
		// Get all apps with launcher intent
		List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo appInfo : apps) {
			String pkg = appInfo.packageName;
			Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
			if (launchIntent != null && isUserApp(appInfo)) {
				String appName = pm.getApplicationLabel(appInfo).toString();
				
				Switch appSwitch = new Switch(this);
				appSwitch.setText(appName);
				appSwitch.setChecked(false); // default to off
				
				appSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
					if (isChecked) {
						launchApp(pkg);
						} else {
						forceStopApp(pkg);
					}
				});
				
				listLayout.addView(appSwitch);
			}
		}
		
		scrollView.addView(listLayout);
		mainLayout.addView(scrollView);
		
		setContentView(mainLayout);
	}
	
	private boolean hasUsageStatsPermission() {
		AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
		int mode = appOps.checkOpNoThrow("android:get_usage_stats",
		android.os.Process.myUid(), getPackageName());
		return mode == AppOpsManager.MODE_ALLOWED;
	}
	
	private void launchApp(String packageName) {
		Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		if (launchIntent != null) {
			startActivity(launchIntent);
			} else {
			Toast.makeText(this, "Unable to launch " + packageName, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void forceStopApp(String packageName) {
		try {
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(su.getOutputStream());
			os.writeBytes("am force-stop " + packageName + "\n");
			os.writeBytes("exit\n");
			os.flush();
			su.waitFor();
			Toast.makeText(this, "Force-stopped: " + packageName, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			Toast.makeText(this, "Root required to force stop " + packageName, Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean isUserApp(ApplicationInfo ai) {
		return (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
	}
}
