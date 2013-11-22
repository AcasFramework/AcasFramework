package com.acas.sources;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class ACASModule {

	static final String TAG = ACASModule.class.getSimpleName();
	
	static final String EXTRA_FROM = "from";
	
	public String mPackage;
    public String mName;
    public String mVersion;
	public String mEntryPoint;
	public String mImgUrl;
	
	public ACASModule(String modulePackage,  String moduleName, String moduleVersion, String moduleEntryPoint, String moduleImgUrl) {
		mPackage = modulePackage;
		mName = moduleName;
		mVersion = moduleVersion;
		mEntryPoint = moduleEntryPoint;
		mImgUrl = moduleImgUrl;
	}
	
	/**
	 * <p>Returns a string containing a concise, human-readable description of this object.</p>
	 * 
	 * @return a printable representation of this object. 
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("package=").append(mPackage).append("\n");
		sb.append("name=").append(mName).append("\n");
		sb.append("version=").append(mVersion).append("\n");
		sb.append("entryPoint=").append(mEntryPoint).append("\n");
		sb.append("imageUrl=").append(mImgUrl).append("\n");
		return sb.toString();
	}

    /**
     *  <p>Verify if a module is installed on the device.</p>
     *
     * @param ctx
     *          The current context of the application.
     * @return
     *          <strong>True</strong> if it is installed.
     *          <strong>False</strong> if it is not installed.
     */
	public boolean isInstalled(Context ctx) {
		try{
		    ctx.getPackageManager().getApplicationInfo(mPackage, 0);
		    return true;
		} catch (PackageManager.NameNotFoundException e) {
			if (ACAS.DEBUG_MODE) {
				Log.d(TAG, "Package name not found !"+ mPackage);
			}
		}
		return false;
	}

    /**
     * Method called to launch a module
     *
     * @param ctx
     *        the current context of the application.
     *
     * @return
     *          <strong>True</strong> if it is launched.
     *          <strong>False</strong> if it is not installed.
     */
	public boolean enter(Context ctx) {
		if (!isInstalled(ctx)) {
			return false;
		}
		Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(mPackage);
        intent.putExtra(EXTRA_FROM, ctx.getPackageName());
		ctx.startActivity(intent);
		return true;
	}

    /**
     * Method called to open the Play Store in order to see the app description of the current
     * module. Should be use to allow the user to download it.
     * @param ctx
     *        The current context of the application.
     * @return
     *          <strong>True</strong> if it is launched.
     *          <strong>False</strong> if the Store is not available.
     */
    public boolean goToPlayStore(Context ctx) {
        boolean result = false;
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + this.mPackage)));
            result = true;
        } catch (ActivityNotFoundException e) {
            if (ACAS.DEBUG_MODE) {
                Log.e("ERROR", "no market access");
            }
            Toast.makeText(ctx, "No market access", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

}