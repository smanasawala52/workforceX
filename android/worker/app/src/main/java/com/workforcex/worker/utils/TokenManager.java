package com.workforcex.worker.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the JWT and user info in SharedPreferences so the user stays
 * logged in across app restarts. Call clear() on logout.
 */
public class TokenManager {

    private static final String PREFS_NAME = "workforcex_prefs";
    private static final String KEY_TOKEN  = "token";
    private static final String KEY_ROLE   = "role";
    private static final String KEY_MOBILE = "mobile";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(String token, String role, String mobileNumber) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .putString(KEY_MOBILE, mobileNumber)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }

    public String getRole()  { return prefs.getString(KEY_ROLE, null); }

    public String getMobile() { return prefs.getString(KEY_MOBILE, null); }

    /** Returns "Bearer <token>" ready to pass as the Authorization header */
    public String getBearerToken() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    public boolean isLoggedIn() { return getToken() != null; }

    public void clear() { prefs.edit().clear().apply(); }
}
