package com.sakr.android.popularmovies.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mohammad sakr on 03/09/2015.
 */
public class Movie
{
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_POSTER_PATH = "poster_path";
    public static final String KEY_VOTE_AVERAGE = "vote_average";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String KEY_RUNTIME = "runtime";


    public  long id;
    public  String title;
    public  String overview = "";
    public  String poster_path;
    public  double vote_average;
    public  String release_date;



    public Movie(long id,
                 String title, String overview, String poster_path,
                 double vote_average, String release_date)
    {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.release_date = release_date;
    }

    public String getOverview()
    {
        if (TextUtils.isEmpty(overview) || overview.equals("null"))
        {
            return "";
        }
        return overview;
    }


    public static Movie fromJson(JSONObject jsonObject) throws JSONException
    {
        return new Movie(
                jsonObject.getLong(KEY_ID),
                jsonObject.getString(KEY_TITLE),
                jsonObject.getString(KEY_OVERVIEW),
                jsonObject.getString(KEY_POSTER_PATH),
                jsonObject.getDouble(KEY_VOTE_AVERAGE),
                jsonObject.getString(KEY_RELEASE_DATE)
        );
    }

    @Override
    public String toString()
    {
        return this.overview + " " + this.overview + " " + this.poster_path;
    }
} // end class Movie
