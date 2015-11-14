package com.sakr.android.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.sakr.android.popularmovies.DetailFragment;
import com.sakr.android.popularmovies.R;
import com.sakr.android.popularmovies.data.MovieContract;
import com.sakr.android.popularmovies.model.Movie;
import com.sakr.android.popularmovies.model.Review;
import com.sakr.android.popularmovies.model.Trailer;

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
 * Created by mohammad sakr on 11/10/2015.
 */
public class DetailsService extends IntentService
{
    private long _id;
    private long mMovieId;
    private Uri mUri;
    private String mRuntime;



    public DetailsService()
    {
        super("DetailsService");
    }

    public DetailsService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        mUri = intent.getData();
        //query the movie
        Cursor cursor = getContentResolver().query(
                mUri,
                new String[]{MovieContract.MovieEntry._ID,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                        MovieContract.MovieEntry.COLUMN_RUNTIME},
                null,
                null,
                null);
        if(cursor.moveToFirst())
        {
            _id = cursor.getInt(0);
            mMovieId = cursor.getInt(1);
            mRuntime = cursor.getString(2);
        } // end if

        fetchMovieRuntime();
        fetchTrailers();
        fetchReviews();

    } // end method onHandleIntent

    private void setMovieRuntime(long movieId,String runtime)
    {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, runtime);

                getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                values,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{String.valueOf(movieId)});

    } // end method setMovieRuntime

    private void fetchMovieRuntime()
    {
        if (mRuntime != null)
        {
            return;
        }
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String runtimeJsonStr = null;

        try
        {
            // Construct the URL
            final String BASE_URL = "http://api.themoviedb.org/3/movie/" + Long.toString(mMovieId) +"?";
            final String API_KEY = "api_key";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                return ;
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
            runtimeJsonStr = buffer.toString();

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
        try
        {
            JSONObject jsonObject = new JSONObject(runtimeJsonStr);
            String runtimeStr = jsonObject.getString(Movie.KEY_RUNTIME);
            setMovieRuntime(mMovieId,runtimeStr);
        } //end try
        catch (JSONException e)
        {
            e.printStackTrace();
        } //end catch
    }

    private void getTrailersDataFromString(String trailersJsonStr) throws JSONException
    {
        JSONObject trailerJson = new JSONObject(trailersJsonStr);
        JSONArray trailerJsonJSONArray = trailerJson.getJSONArray("results");

        // Insert the new trailers information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(trailerJsonJSONArray.length());

        Trailer[] trailers = new Trailer[trailerJsonJSONArray.length()];
        for (int i = 0; i < trailerJsonJSONArray.length(); i++)
        {
            trailers[i] = Trailer.fromJson(_id, trailerJsonJSONArray.getJSONObject(i));
            cVVector.add(trailerToContentValue(trailers[i]));
        }

        if(trailers.length > 0 )
        {
            DetailFragment.mShareStr = trailers[0].getLink();
        }
        // add to database
        if (cVVector.size() > 0)
        {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
        }

    } // end method getTrailersDataFromString

    private ContentValues trailerToContentValue(Trailer trailer)
    {
        ContentValues trailersValues = new ContentValues();
        trailersValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, trailer.movieId);
        trailersValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY, trailer.key);
        trailersValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailer.name);
        return trailersValues;
    } // end method trailerToContentValue

    private void fetchTrailers()
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailerJsonStr = null;

        try
        {
            // Construct the URL
            final String BASE_URL = "http://api.themoviedb.org/3/movie/" + Long.toString(mMovieId) + "/videos?";
            final String API_KEY = "api_key";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                return ;
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
            trailerJsonStr = buffer.toString();

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
        try
        {
            getTrailersDataFromString(trailerJsonStr);
        } //end try
        catch (JSONException e)
        {
            e.printStackTrace();
        } //end catch

    } // end method fetchTrailers

    private void getReviewsDataFromString(String reviewsJsonStr) throws JSONException
    {
        JSONObject reviewJsonObject = new JSONObject(reviewsJsonStr);
        JSONArray reviewsJsonJSONArray = reviewJsonObject.getJSONArray("results");

        // Insert the new reviews information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewsJsonJSONArray.length());

        Review[] reviews = new Review[reviewsJsonJSONArray.length()];
        for (int i = 0; i < reviewsJsonJSONArray.length(); i++)
        {
            reviews[i] = Review.fromJson(_id, reviewsJsonJSONArray.getJSONObject(i));
            cVVector.add(reviewToContentValue(reviews[i]));
        }
        // add to database
        if (cVVector.size() > 0)
        {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
        }

    } // end method getTrailersDataFromString

    private ContentValues reviewToContentValue(Review review)
    {
        ContentValues reviewsValues = new ContentValues();
        reviewsValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, review.movieId);
        reviewsValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.author);
        reviewsValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.content);
        return reviewsValues;
    } // end method reviewToContentValue

    private void fetchReviews()
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewsJsonStr = null;

        try
        {
            // Construct the URL for the OpenWeatherMap query
            final String BASE_URL = "http://api.themoviedb.org/3/movie/" + Long.toString(mMovieId) + "/reviews";
            final String API_KEY = "api_key";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY,getString(R.string.moviedb_api_key))
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
                return ;
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
            reviewsJsonStr = buffer.toString();

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
        try
        {
            getReviewsDataFromString(reviewsJsonStr);
        } //end try
        catch (JSONException e)
        {
            e.printStackTrace();
        } //end catch
    }

} // end class DetailsService
