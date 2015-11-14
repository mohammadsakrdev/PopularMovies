package com.sakr.android.popularmovies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.sakr.android.popularmovies.R;
import com.sakr.android.popularmovies.data.MovieContract;
import com.sakr.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by mohammad sakr on 10/10/2015.
 */
public class MoviesService extends IntentService
{
    public MoviesService()
    {
        super("MoviesService");

    }
    public MoviesService(String name)
    {
        super("MoviesService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try
        {
            // Construct the URL for the OpenWeatherMap query
            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_KEY = "api_key";
            final String API_SORTING = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString(
                            getString(R.string.pref_sorting_key),
                            getString(R.string.pref_sorting_default_value)
                    );
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, API_SORTING)
                    .appendQueryParameter(API_KEY, getString(R.string.moviedb_api_key))
                    .build();
            URL url = new URL(builtUri.toString());
            // Create the request, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
            {
                // Nothing to do.
                return;
            } //end if

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
            {

                buffer.append(line + "\n");
            } //end while

            if (buffer.length() == 0)
            {
                // Stream was empty.  No point in parsing.
                return ;
            } //end if
            movieJsonStr = buffer.toString();
            try
            {
                getMoviesDataFromString(movieJsonStr);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }

        }//end try
        catch (IOException e)
        {
            // If the code didn't successfully get the data, there's no point in attempting to parse it.
            return ;
        }//end catch
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }//end if
            if (reader != null)
            {
                try
                {
                    reader.close();
                }//end try
                catch (final IOException e)
                {
                }//end catch
            } //end if
        }// end finally

    } // end method onHandleIntent

    private void getMoviesDataFromString(String moviesJsonStr) throws JSONException
    {
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesJsonArray = moviesJson.getJSONArray("results");

        // Insert the new movies information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesJsonArray.length());

        Movie[] movies = new Movie[moviesJsonArray.length()];
        for (int i = 0; i < moviesJsonArray.length(); i++)
        {
            movies[i] = Movie.fromJson(moviesJsonArray.getJSONObject(i));
            cVVector.add(movieToContentValue(movies[i]));
        }

        // add to database
        if (cVVector.size() > 0)
        {
            String sorting = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_sorting_key),
                            getString(R.string.pref_sorting_default_value));
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_SORTING + " = ? AND " +
                            MovieContract.MovieEntry.COLUMN_FAVORITE + " != ?",
                    new String[]{sorting,Integer.toString(1)});

            getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
        }
    } // end method getMoviesDataFromString

    private ContentValues movieToContentValue(Movie movie)
    {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.id);
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.title);
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.poster_path);
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.release_date);
        movieValues.put(MovieContract.MovieEntry.COLUMN_SORTING, PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_sorting_key),
                        getString(R.string.pref_sorting_default_value)
                ));
        return movieValues;
    } // end method movieToContentValue

    public static class MovieAlarmReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Intent sendIntent = new Intent(context, MoviesService.class);
            context.startService(sendIntent);
        }
    }

} // end class MoviesService
