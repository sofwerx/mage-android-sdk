package mil.nga.giat.mage.sdk.http.resource;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.Event;
import mil.nga.giat.mage.sdk.datastore.user.Team;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.gson.deserializer.EventsDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.TeamsDeserializer;
import mil.nga.giat.mage.sdk.http.HttpClientManager;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;

/***
 * RESTful communication for events
 *
 * @author newmanw
 */

public class EventResource {

    public interface EventService {
        @GET("/api/events")
        Call<Map<Event, Collection<Team>>> getEvents();

        @GET("/api/events/{eventId}/teams?populate=users")
        Call<Map<Team, Collection<User>>> getTeams(@Path("eventId") String eventId);
    }

    private static final String LOG_NAME = EventResource.class.getName();

    private Context context;

    public EventResource(Context context) {
        this.context = context;
    }

    public Map<Event, Collection<Team>> getEvents() throws IOException {
        Map<Event, Collection<Team>> events = new HashMap<>();

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(EventsDeserializer.getGsonBuilder(context)))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        EventService service = retrofit.create(EventService.class);
        Response<Map<Event, Collection<Team>>> response = service.getEvents().execute();

        if (response.isSuccess()) {
            events = response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return events;
    }

    public Map<Team, Collection<User>> getTeams(String eventId) throws IOException {
        Map<Team, Collection<User>> teams = new HashMap<>();

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(TeamsDeserializer.getGsonBuilder(context)))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        EventService service = retrofit.create(EventService.class);
        Response<Map<Team, Collection<User>>> response = service.getTeams(eventId).execute();

        if (response.isSuccess()) {
            teams = response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return teams;
    }
}