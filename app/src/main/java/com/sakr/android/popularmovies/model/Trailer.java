package com.sakr.android.popularmovies.model;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mohammad sakr on 27/09/2015.
 */
public class Trailer
{
    public static final String TRAILER_KEY = "key";
    public static final String TRAILER_NAME = "name";

    public String name;
    public String key;
    public long movieId;


    public Trailer(long movieId,String key, String name)
    {
        this.movieId = movieId;
        this.key = key;
        this.name = name;
    } // end public argument constructor

    public Uri buildTrailerUri(String size)
    {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath("")
                .build();

        return builtUri;
    } // end method buildTrailerUri

    public static Trailer fromJson(long movieId,JSONObject jsonObject) throws JSONException
    {
        return new Trailer(
                movieId,
                jsonObject.getString(TRAILER_KEY),
                jsonObject.getString(TRAILER_NAME)
        );
    } //end method fromJson

    public String getLink()
    {
        return "https://www.youtube.com/watch?v=" + key;
    }


} //end class Trailer

