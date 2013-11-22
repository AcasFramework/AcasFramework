package com.acasframework;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

class ACASSecurity {
	
	static final String TAG = ACASSecurity.class.getSimpleName();
	
	static final String SHA1 = "SHA-1";
	
	private String mSecurityKey;
	String mPackage;
	boolean mIsValid = false;
	
	ACASSecurity(Context context, String securityKey) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Security set key="+ securityKey);
		}
		mSecurityKey = securityKey;
		mPackage = context.getPackageName();
	}
	
	String getEncodedData() {
		final String data = mPackage + mSecurityKey;
		final String str_encode = sha1(data);
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "data_encode : "+ str_encode + " + data_decode : " + data);
		}
		return str_encode;
	}
	
	void setKeyValidation(boolean valid) {
		if (ACAS.DEBUG_MODE) {
			Log.d(TAG, "Set key validation valid="+ valid);
		}
        mIsValid = valid;
	}
	
	@SuppressLint("DefaultLocale")
	private String sha1(String s) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(SHA1);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		digest.reset();
		byte[] data = digest.digest(s.getBytes());
		return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data)).toLowerCase();
	}
	
}
