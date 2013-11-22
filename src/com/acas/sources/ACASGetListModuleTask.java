package com.acas.sources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.acas.sources.exception.ACASInvalidKeyException;
import com.acas.sources.provider.ACASContract.ModuleTable;

public class ACASGetListModuleTask extends AsyncTask<ACASSecurity, Void, Integer> {

	static final String TAG = ACASGetListModuleTask.class.getSimpleName();

	static final int STATUS_OK = 0x1;
	static final int STATUS_KO = 0x2;
	static final int STATUS_CONNECTIVITY = 0x3;
	static final int STATUS_INVALID_KEY = 0x4;
	static final int STATUS_ALREADY_LAUNCH = 0x5;

	static final String WEBSERVICE_URL = "http://mantux.fr/applications/0.json";

	static final String POST_PACKAGE = "package";
	static final String POST_DATA_CODED = "data_coded";
	static final String JSON_ARRAY_CHILDS = "children";
	static final String ACAS_ERROR = "acas_error";

	static final String JSON_NODE_ID = "id";
	static final String JSON_NODE_NAME = "name";
	static final String JSON_NODE_PACKAGE = "package";
    static final String JSON_NODE_VERSION = "version";
    static final String JSON_MOTHER = "mother";
	static final String JSON_NODE_ENTRYPOINT = "entrypoint";

	private Boolean mIsLaunched = false;
	private Context mContext;

	public ACASGetListModuleTask(Context context) {
		mContext = context;
	}

	/**
	 * <p>
	 * Use to download the JSON which contain the sub-modules information
	 * </p>
	 * 
	 * @param params
	 *            <p>
	 *            The first string is the application package
	 *            </p>
	 *            <p>
	 *            The second is the encoded data
	 *            </p>
	 * @return
	 */
	protected Integer doInBackground(ACASSecurity... params) {
		// Protect from multiple task
		synchronized (mIsLaunched) {
			if (mIsLaunched) {
				if (ACAS.DEBUG_MODE) {
					Log.w(TAG, "Task is already started");
				}
				return STATUS_ALREADY_LAUNCH;
			}
			mIsLaunched = true;
		}

		// Extract parameter
		if (params.length == 0) {
			return STATUS_INVALID_KEY;
		}
		final ACASSecurity security = params[0];
		if (security == null) {
			return STATUS_INVALID_KEY;
		}
		final String sPackage = security.mPackage;
        final String sSecurityData = security.getEncodedData();

		// Download module list
		final String json = downloadModuleList(sPackage, sSecurityData);
		if (json == null) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Download process fail");
			}
			return STATUS_KO;
		}

		// Parsing result
		final int status = parseModuleList(json);
		switch (status) {
		case STATUS_KO:
			return getLocalModuleList();
		}
		
		return status;
	}

	private String downloadModuleList(String sPackage, String sDataEncoded) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Get the module list of " + sPackage);
			Log.i(TAG, "Parsing " + WEBSERVICE_URL);
		}

		final HttpPost httppost = new HttpPost(WEBSERVICE_URL);
		final HttpClient httpclient = new DefaultHttpClient();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(POST_PACKAGE, sPackage));
		nameValuePairs.add(new BasicNameValuePair(POST_DATA_CODED, sDataEncoded));

		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "HTTP REQUEST: " + httppost.toString());
			Log.i(TAG, "Sha1: " + sDataEncoded);
		}

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);

			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity e = response.getEntity();
				return EntityUtils.toString(e);
			}
		} catch (UnsupportedEncodingException e) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Error an call webservice", e);
			}
		} catch (ClientProtocolException e) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Error an call webservice", e);
			}
		} catch (IOException e) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Error an call webservice", e);
			}
		}
		return null;
	}

	private int parseModuleList(String json) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Try to parse module list");
		}
		try {
			// Parse JSON
			final JSONObject jObj = new JSONObject(json);
            if (ACAS.DEBUG_MODE) {
                Log.d(TAG, "JSON RECEIVED:"+ jObj.toString());

            }
			// Check error
			if (jObj.has(ACAS_ERROR)) {
				if (ACAS.DEBUG_MODE) {
					try {
						Log.e(TAG, "ERROR: " + jObj.getString(ACAS_ERROR));
					} catch (JSONException e) {
						Log.e(TAG, "Unknown error");
					}
				}
				return STATUS_INVALID_KEY;
			}

			// Get content resolver
			final ContentResolver cr = mContext.getContentResolver();

			// Delete previous data
			cr.delete(ModuleTable.CONTENT_URI, null, null);

			// Extract data
			if (!jObj.has(JSON_ARRAY_CHILDS)) {
				if (ACAS.DEBUG_MODE) {
					Log.e(TAG, "Error: no sub module");
				}
				return STATUS_KO;
			}

            // extract parent' module
            if (jObj.has(JSON_MOTHER) && !jObj.optString(JSON_MOTHER).equals("none")) {
                // Extract node
                final JSONObject jNode = jObj.getJSONObject(JSON_MOTHER);

                // Extract data
                final String moduleName = jNode.getString(JSON_NODE_NAME);
                final String modulePackage = jNode.getString(JSON_NODE_PACKAGE);
                final String moduleVersion = jNode.getString(JSON_NODE_VERSION);
                ACAS.setMother(new ACASModule(modulePackage, null, moduleName, moduleVersion, null, null));
            }

			// Get static module list
			ArrayList<ACASModule> moduleList = ACAS.getModuleList();

			// Get child node
			final JSONArray jArray = jObj.getJSONArray(JSON_ARRAY_CHILDS);
			final int length = jArray.length();
			for (int i = 0; i < length; i++) {

				// Extract node
				final JSONObject jNode = jArray.getJSONObject(i);

				// Extract data
				final String moduleName = jNode.getString(JSON_NODE_NAME);
				final String modulePackage = jNode.getString(JSON_NODE_PACKAGE);
				final String moduleVersion = jNode.getString(JSON_NODE_VERSION);

				// Put data into database container
				final ContentValues cv = new ContentValues();
				cv.put(ModuleTable.MODULE_NAME, moduleName);
				cv.put(ModuleTable.MODULE_PACKAGE, modulePackage);
				cv.put(ModuleTable.MODULE_VERSION, moduleVersion);

				// Put data into static container
				final ACASModule module = new ACASModule(modulePackage, null, moduleName, moduleVersion, null, null);

				// Debug
				if (ACAS.DEBUG_MODE) {
					Log.d(TAG, "New entrie in ContentValues:");
					Log.d(TAG, cv.toString());
				}

				// Insert into database
				cr.insert(ModuleTable.CONTENT_URI, cv);

				// Insert into static
				moduleList.add(module);

			}

			return STATUS_OK;
		} catch (JSONException e) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Error an add to module list", e);
			}
		}
		return STATUS_KO;
	}

	private int getLocalModuleList() {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Try to get local module list");
		}

		// Get content resolver
		final ContentResolver cr = mContext.getContentResolver();

		// Get static module list
		ArrayList<ACASModule> moduleList = ACAS.getModuleList();

		// Query local database
		final Cursor cursor = cr.query(ModuleTable.CONTENT_URI, null, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			if (ACAS.DEBUG_MODE) {
				Log.w(TAG, "Cursor is null or empty");
			}
			return STATUS_KO;
		}

		// Get column index
		final int indexName = cursor.getColumnIndex(ModuleTable.MODULE_NAME);
		final int indexPackage = cursor.getColumnIndex(ModuleTable.MODULE_PACKAGE);
		final int indexVersion = cursor.getColumnIndex(ModuleTable.MODULE_VERSION);

		// Extract result
		do {
			// Extract data
			final String moduleName = cursor.getString(indexName);
			final String modulePackage = cursor.getString(indexPackage);
			final String moduleVersion = cursor.getString(indexVersion);

			// Create container
            if (ACAS.DEBUG_MODE) {
                Log.i("INSTALL MODULE", moduleName + "#"+ modulePackage+ "#"+ moduleVersion);
            }
			final ACASModule module = new ACASModule(modulePackage, null, moduleName, moduleVersion, null, null);

			// Add to static list
			moduleList.add(module);

		} while (cursor.moveToNext());

		return STATUS_OK;
	}

	@Override
	protected void onPostExecute(Integer status) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "End of the task with status=" + status);
		}

		synchronized (mIsLaunched) {
			mIsLaunched = false;
		}

		switch (status) {

		case STATUS_INVALID_KEY:
			ACAS.mSecurity.setKeyValidation(false);
			throw new ACASInvalidKeyException("The keyApi is invalid");

		case STATUS_ALREADY_LAUNCH:
			// do nothing
			break;

		case STATUS_CONNECTIVITY:
		case STATUS_KO:
		case STATUS_OK:
			ACAS.mSecurity.setKeyValidation(true);
			ACAS.mListModuleReceiver.deliveryModules();
			break;
		}
	}

	public synchronized boolean isLaunched() {
		return mIsLaunched;
	}
}
