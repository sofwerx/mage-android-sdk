package mil.nga.giat.mage.sdk.http.resource;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.DaoStore;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationError;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationFavorite;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationImportant;
import mil.nga.giat.mage.sdk.datastore.user.Event;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.http.HttpClientManager;
import mil.nga.giat.mage.sdk.http.converter.AttachmentConverterFactory;
import mil.nga.giat.mage.sdk.http.converter.ObservationConverterFactory;
import mil.nga.giat.mage.sdk.http.converter.ObservationImportantConverterFactory;
import mil.nga.giat.mage.sdk.http.converter.ObservationsConverterFactory;
import mil.nga.giat.mage.sdk.utils.ISO8601DateFormatFactory;
import mil.nga.giat.mage.sdk.utils.MediaUtility;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;

/***
 * RESTful communication for observations
 *
 * @author newmanw
 */
public class ObservationResource {

    public interface ObservationService {
        @GET("/api/events/{eventId}/observations")
        Call<Collection<Observation>> getObservations(@Path("eventId") String eventId, @Query("startDate") String startDate);

        @POST("/api/events/{eventId}/observations/id")
        Call<Observation> createObservationId(@Path("eventId") String eventId);

        @PUT("/api/events/{eventId}/observations/{observationId}")
        Call<Observation> updateObservation(@Path("eventId") String eventId, @Path("observationId") String observationId, @Body Observation observation);

        @POST("/api/events/{eventId}/observations/{observationId}/states")
        Call<JsonObject> archiveObservation(@Path("eventId") String eventId, @Path("observationId") String observationId, @Body JsonObject state);

        @GET("/api/events/{eventId}/form/icons.zip")
        Call<ResponseBody> getObservationIcons(@Path("eventId") String eventId);

        @Streaming
        @GET("/api/events/{eventId}/observations/{observationId}/attachments/{attachmentId}")
        Call<ResponseBody> getAttachment(@Path("eventId") String eventId, @Path("observationId") String observationId, @Path("attachmentId") String attachmentId);

        @Multipart
        @POST("/api/events/{eventId}/observations/{observationId}/attachments")
        Call<Attachment> createAttachment(@Path("eventId") String eventId, @Path("observationId") String observationId, @PartMap Map<String, RequestBody> parts);

        @PUT("/api/events/{eventId}/observations/{observationId}/favorite")
        Call<Observation> favoriteObservation(@Path("eventId") String eventId, @Path("observationId") String observationId);

        @DELETE("/api/events/{eventId}/observations/{observationId}/favorite")
        Call<Observation> unfavoriteObservation(@Path("eventId") String eventId, @Path("observationId") String observationId);

        @PUT("/api/events/{eventId}/observations/{observationId}/important")
        Call<Observation> addImportant(@Path("eventId") String eventId, @Path("observationId") String observationId, @Body JsonObject important);

        @DELETE("/api/events/{eventId}/observations/{observationId}/important")
        Call<Observation> removeImportant(@Path("eventId") String eventId, @Path("observationId") String observationId);
    }

    private static final String LOG_NAME = ObservationResource.class.getName();

    private Context context;

    public ObservationResource(Context context) {
        this.context = context;
    }

    public Collection<Observation> getObservations(Event event) {
        Collection<Observation> observations = new ArrayList<>();

        if (event == null || event.getRemoteId() == null) {
            return observations;
        }

        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ObservationsConverterFactory.create(event))
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        DateFormat iso8601Format = ISO8601DateFormatFactory.ISO8601();
        ObservationHelper observationHelper = ObservationHelper.getInstance(context);
        Date lastModifiedDate = observationHelper.getLatestCleanLastModified(context, event);
        Log.d(LOG_NAME, "Fetching all observations after: " + iso8601Format.format(lastModifiedDate));

        ObservationService service = retrofit.create(ObservationService.class);
        try {
            Response<Collection<Observation>> response = service.getObservations(event.getRemoteId(), iso8601Format.format(lastModifiedDate)).execute();

            if (response.isSuccess()) {
                observations = response.body();
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (IOException e) {
            Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch operation.", e);
        }

        return observations;
    }

    public Observation saveObservation(Observation observation) {
        if (StringUtils.isEmpty(observation.getRemoteId())) {
            return createObservation(observation);
        } else {
            return updateObservation(observation);
        }
    }

    private Observation createObservation(Observation observation) {
        ObservationHelper observationHelper = ObservationHelper.getInstance(context);

        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ObservationConverterFactory.create(observation.getEvent()))
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            ObservationService service = retrofit.create(ObservationService.class);
            Response<Observation> response = service.createObservationId(observation.getEvent().getRemoteId()).execute();

            if (response.isSuccess()) {
                Observation returnedObservation = response.body();
                observation.setRemoteId(returnedObservation.getRemoteId());
                observation.setUrl(returnedObservation.getUrl());

                try {
                    observation = observationHelper.update(observation);

                    // Got the observation id from the server, lets send the observation
                    observation = updateObservation(observation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem updating observation after server response", oe);
                }
            } else {
                Log.e(LOG_NAME, "Bad request.");

                ObservationError observationError = new ObservationError();
                observationError.setStatusCode(response.code());
                observationError.setDescription(response.message());

                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(LOG_NAME, errorBody);

                    observationError.setMessage(errorBody);
                }

                try {
                    observationHelper.update(observation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem updating observation error", oe);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_NAME, "Failure getting observation id from server.", e);

            ObservationError observationError = new ObservationError();
            observationError.setMessage("The Internet connection appears to be offline.");
            observation.setError(observationError);
            try {
                observationHelper.update(observation);
            } catch (ObservationException oe) {
                Log.e(LOG_NAME, "Problem updating observation error", oe);
            }
        }

        return observation;
    }

    private Observation updateObservation(Observation observation) {

        ObservationHelper observationHelper = ObservationHelper.getInstance(context);
        Observation savedObservation = null;

        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ObservationConverterFactory.create(observation.getEvent()))
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            ObservationService service = retrofit.create(ObservationService.class);
            Response<Observation> response = service.updateObservation(observation.getEvent().getRemoteId(), observation.getRemoteId(), observation).execute();

            if (response.isSuccess()) {
                Observation returnedObservation = response.body();
                returnedObservation.setDirty(Boolean.FALSE);
                returnedObservation.setId(observation.getId());

                try {
                    savedObservation = observationHelper.update(returnedObservation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem updating observation after server response", oe);
                }
            } else {
                Log.e(LOG_NAME, "Bad request.");

                ObservationError observationError = new ObservationError();
                observationError.setStatusCode(response.code());
                observationError.setDescription(response.message());

                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(LOG_NAME, errorBody);

                    observationError.setMessage(errorBody);
                }

                try {
                    observation.setError(observationError);
                    observationHelper.update(observation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem updating observation error", oe);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_NAME, "Failure saving observation.", e);

            ObservationError observationError = new ObservationError();
            observationError.setMessage("The Internet connection appears to be offline.");
            observation.setError(observationError);
            try {
                observationHelper.update(observation);
            } catch (ObservationException oe) {
                Log.e(LOG_NAME, "Problem updating observation error", oe);
            }
        }

        return savedObservation;
    }

    public void archiveObservation(Observation observation) {
        ObservationHelper observationHelper = ObservationHelper.getInstance(context);

        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            JsonObject state = new JsonObject();
            state.addProperty("name", "archive");

            ObservationService service = retrofit.create(ObservationService.class);
            Response<JsonObject> response = service.archiveObservation(observation.getEvent().getRemoteId(), observation.getRemoteId(), state).execute();

            if (response.isSuccess()) {
                try {
                    observationHelper.delete(observation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem deleting observation after server archive response", oe);
                }
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                try {
                    // Observation does not exist on the server, delete it
                    observationHelper.delete(observation);
                } catch (ObservationException e) {
                    Log.e(LOG_NAME, "Problem deleting local observation", e);
                }
            } else {
                Log.e(LOG_NAME, "Bad request.");

                ObservationError observationError = new ObservationError();
                observationError.setStatusCode(response.code());
                observationError.setDescription(response.message());

                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(LOG_NAME, errorBody);

                    observationError.setMessage(errorBody);
                }

                try {
                    observationHelper.update(observation);
                } catch (ObservationException oe) {
                    Log.e(LOG_NAME, "Problem updating observation error", oe);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_NAME, "Failure archiving observation.", e);

            ObservationError observationError = new ObservationError();
            observationError.setMessage("The Internet connection appears to be offline.");
            observation.setError(observationError);
            try {
                observationHelper.update(observation);
            } catch (ObservationException oe) {
                Log.e(LOG_NAME, "Problem archiving observation error", oe);
            }
        }
    }

    public InputStream getObservationIcons(String eventId) throws IOException {
        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        ObservationService service = retrofit.create(ObservationService.class);
        Response<ResponseBody> response = service.getObservationIcons(eventId).execute();

        InputStream inputStream = null;
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

    public ResponseBody getAttachment(Attachment attachment) throws IOException {
        String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(HttpClientManager.getInstance(context).httpClient())
                .build();

        ObservationService service = retrofit.create(ObservationService.class);

        String eventId = attachment.getObservation().getEvent().getRemoteId();
        String observationId = attachment.getObservation().getRemoteId();
        String attachmentId = attachment.getRemoteId();
        Response<ResponseBody> response = service.getAttachment(eventId, observationId, attachmentId).execute();

        if (response.isSuccess()) {
            return response.body();
        } else {
            Log.e(LOG_NAME, "Bad request.");
            if (response.errorBody() != null) {
                Log.e(LOG_NAME, response.errorBody().string());
            }
        }

        return null;
    }

    public Attachment createAttachment(Attachment attachment) {
        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(AttachmentConverterFactory.create())
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            ObservationService service = retrofit.create(ObservationService.class);

            String eventId = attachment.getObservation().getEvent().getRemoteId();
            String observationId = attachment.getObservation().getRemoteId();

            Map<String, RequestBody> parts = new HashMap<>();
            File attachmentFile = new File(attachment.getLocalPath());
            String mimeType = MediaUtility.getMimeType(attachment.getLocalPath());
            RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), attachmentFile);
            parts.put("attachment\"; filename=\"" + attachmentFile.getName() + "\"", fileBody);

            Response<Attachment> response = service.createAttachment(eventId, observationId, parts).execute();

            if (response.isSuccess()) {
                Attachment returnedAttachment = response.body();
                attachment.setContentType(returnedAttachment.getContentType());
                attachment.setName(returnedAttachment.getName());
                attachment.setRemoteId(returnedAttachment.getRemoteId());
                attachment.setRemotePath(returnedAttachment.getRemotePath());
                attachment.setSize(returnedAttachment.getSize());
                attachment.setUrl(returnedAttachment.getUrl());
                attachment.setDirty(returnedAttachment.isDirty());

                DaoStore.getInstance(context).getAttachmentDao().update(attachment);
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Failure saving observation.", e);
        }

        return attachment;
    }

    public Observation toogleFavorite(ObservationFavorite favorite) {
        ObservationHelper observationHelper = ObservationHelper.getInstance(context);
        Observation observation = favorite.getObservation();
        Observation savedObservation = null;

        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ObservationConverterFactory.create(observation.getEvent()))
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            ObservationService service = retrofit.create(ObservationService.class);

            Response<Observation> response;
            if (favorite.isFavorite()) {
                response = service.favoriteObservation(observation.getEvent().getRemoteId(), observation.getRemoteId()).execute();
            } else {
                response = service.unfavoriteObservation(observation.getEvent().getRemoteId(), observation.getRemoteId()).execute();
            }

            if (response.isSuccess()) {
                Observation updatedObservation = response.body();
                observation.setLastModified(updatedObservation.getLastModified());
                observationHelper.updateFavorite(favorite);
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Failure toogling observation favorite.", e);
        }

        return savedObservation;
    }

    public Observation toogleImportant(Observation observation) {
        ObservationHelper observationHelper = ObservationHelper.getInstance(context);
        ObservationImportant important = observation.getImportant();
        Observation savedObservation = null;

        try {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.serverURLKey), context.getString(R.string.serverURLDefaultValue));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ObservationImportantConverterFactory.create(observation.getEvent()))
                    .client(HttpClientManager.getInstance(context).httpClient())
                    .build();

            ObservationService service = retrofit.create(ObservationService.class);

            Response<Observation> response;
            if (important.isImportant()) {
                JsonObject jsonImportant = new JsonObject();
                jsonImportant.addProperty("description", important.getDescription());

                response = service.addImportant(observation.getEvent().getRemoteId(), observation.getRemoteId(), jsonImportant).execute();
            } else {
                response = service.removeImportant(observation.getEvent().getRemoteId(), observation.getRemoteId()).execute();
            }

            if (response.isSuccess()) {
                Observation returnedObservation = response.body();
                observation.setLastModified(returnedObservation.getLastModified());
                observationHelper.updateImportant(observation);
            } else {
                Log.e(LOG_NAME, "Bad request.");
                if (response.errorBody() != null) {
                    Log.e(LOG_NAME, response.errorBody().string());
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Failure toogling observation important.", e);
        }

        return savedObservation;
    }
}