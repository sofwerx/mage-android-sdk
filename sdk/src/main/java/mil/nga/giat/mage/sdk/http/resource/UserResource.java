package mil.nga.giat.mage.sdk.http.resource;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.Event;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.HttpClientManager;
import mil.nga.giat.mage.sdk.http.converter.UserConverterFactory;
import mil.nga.giat.mage.sdk.http.converter.UsersConverterFactory;
import mil.nga.giat.mage.sdk.utils.MediaUtility;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.PartMap;
import retrofit.http.Path;

/***
 * RESTful communication for users
 *
 * @author newmanw
 */

public class UserResource {

    public interface UserService {
        @POST("/api/login")
        Call<JsonObject> login(@Body JsonObject body);

        @POST("/auth/{strategy}/authorize")
        Call<JsonObject> authorize(@Path("strategy") String strategy, @Body JsonObject body);

        @POST("/api/logout")
        Call<ResponseBody> logout();

        @GET("/api/users")
        Call<Collection<User>> getUsers();

        @POST("/api/users")
        Call<JsonObject> createUser(@Body JsonObject body);

        @GET("/api/users/{userId}")
        Call<User> getUser(@Path("userId") String userId);

        @GET("/api/users/{userId}/icon")
        Call<ResponseBody> getIcon(@Path("userId") String userId);

        @GET("/api/users/{userId}/avatar")
        Call<ResponseBody> getAvatar(@Path("userId") String userId);

        @POST("/api/users/{userId}/events/{eventId}/recent")
        Call<User> addRecentEvent(@Path("userId") String userId, @Path("eventId") String eventId);

        @Multipart
        @PUT("/api/users/myself")
        Call<User> createAvatar(@PartMap Map<String, RequestBody> parts);

        @PUT("/api/users/myself/password")
        Call<JsonObject> changePassword(@Body JsonObject body);
    }

    private static final String LOG_NAME = UserResource.class.getName();

    private Context context;

    public UserResource(Context context) {
        this.context = context;
    }

    public JsonObject login(String username, String uid, String password) {
        JsonObject loginJson = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            UserService service = retrofit.create(UserService.class);

            JsonObject json = new JsonObject();
            json.addProperty("username", username);
            json.addProperty("uid", uid);
            json.addProperty("password", password);

            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                json.addProperty("appVersion", String.format("%s-%s", packageInfo.versionName, packageInfo.versionCode));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_NAME , "Problem retrieving package info.", e);
            }

            Response<JsonObject> response = service.login(json).execute();
            if (response.isSuccess()) {
                loginJson = response.body();
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Bad request.", e);
        }

        return loginJson;
    }

    public JsonObject authorize(String strategy, String uid, String accessToken) {
        JsonObject body = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            UserService service = retrofit.create(UserService.class);

            JsonObject json = new JsonObject();
            json.addProperty("uid", uid);
            json.addProperty("access_token", accessToken);

            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                json.addProperty("appVersion", String.format("%s-%s", packageInfo.versionName, packageInfo.versionCode));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_NAME , "Problem retrieving package info.", e);
            }

            Response<JsonObject> response = service.authorize(strategy, json).execute();
            if (response.isSuccess()) {
                body = response.body();
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Bad request.", e);
        }

        return body;
    }

    public boolean logout() {
        boolean status = false;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            UserService service = retrofit.create(UserService.class);

            Response<ResponseBody> response = service.logout().execute();
            if (response.isSuccess()) {
                status = true;
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Bad request.", e);
        }

        return status;
    }

    public Collection<User> getUsers() throws IOException {
        Collection<User> users = new ArrayList<>();

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(UsersConverterFactory.create(context))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        UserService service = retrofit.create(UserService.class);
        Response<Collection<User>> response = service.getUsers().execute();

        if (response.isSuccess()) {
            users = response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return users;
    }

    public JsonObject createUser(String username, String displayname, String email, String phone, String uid, String password) throws Exception {
        JsonObject user = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("displayName", displayname);
        json.addProperty("email", email);
        json.addProperty("phone", phone);
        json.addProperty("uid", uid);
        json.addProperty("password", password);
        json.addProperty("passwordconfirm", password);

        UserService service = retrofit.create(UserService.class);
        Response<JsonObject> response = service.createUser(json).execute();

        if (response.isSuccess()) {
            user = response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }

            throw new RuntimeException(response.errorBody().string());
        }

        return user;
    }

    public User getUser(String userId) throws IOException {
        User user = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(UserConverterFactory.create(context))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        UserService service = retrofit.create(UserService.class);
        Response<User> response = service.getUser(userId).execute();

        if (response.isSuccess()) {
            user = response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return user;
    }

    public InputStream getIcon(User user) throws IOException {
        InputStream inputStream = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        UserService service = retrofit.create(UserService.class);
        Response<ResponseBody> response = service.getIcon(user.getRemoteId()).execute();

        if (response.isSuccess()) {
            inputStream = response.body().byteStream();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return inputStream;
    }

    public InputStream getAvatar(User user) throws IOException {
        InputStream inputStream = null;

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        UserService service = retrofit.create(UserService.class);
        Response<ResponseBody> response = service.getAvatar(user.getRemoteId()).execute();

        if (response.isSuccess()) {
            inputStream = response.body().byteStream();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return inputStream;
    }

    public User addRecentEvent(User user, Event event) throws IOException {
        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(UserConverterFactory.create(context))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        UserService service = retrofit.create(UserService.class);
        Response<User> response = service.addRecentEvent(user.getRemoteId(), event.getRemoteId()).execute();

        if (response.isSuccess()) {
            return response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }

            return null;
        }
    }

    public User createAvatar(String avatarPath) {
        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(UserConverterFactory.create(context))
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            UserService service = retrofit.create(UserService.class);

            Map<String, RequestBody> parts = new HashMap<>();
            File avatar = new File(avatarPath);
            String mimeType = MediaUtility.getMimeType(avatarPath);
            RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), avatar);
            parts.put("avatar\"; filename=\"" + avatar.getName() + "\"", fileBody);

            Response<User> response = service.createAvatar(parts).execute();

            if (response.isSuccess()) {
                User user = response.body();

                UserHelper userHelper = UserHelper.getInstance(context);
                User currentUser = userHelper.readCurrentUser();
                currentUser.setAvatarUrl(user.getAvatarUrl());
                UserHelper.getInstance(context).update(currentUser);

                userHelper.setAvatarPath(currentUser, avatarPath);

                Log.d(LOG_NAME, "Updated user with remote_id " + user.getRemoteId());

                return user;
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Failure saving observation.", e);
        }

        return null;
    }

    public void changePassword(String username, String password, String newPassword, String newPasswordConfirm, Callback<JsonObject> callback) {
        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));

        OkHttpClient httpClient = HttpClientManager.getInstance(context).httpClient().clone();
        httpClient.interceptors().clear();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        UserService service = retrofit.create(UserService.class);

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        json.addProperty("newPassword", newPassword);
        json.addProperty("newPasswordConfirm", newPasswordConfirm);

        service.changePassword(json).enqueue(callback);
    }
}