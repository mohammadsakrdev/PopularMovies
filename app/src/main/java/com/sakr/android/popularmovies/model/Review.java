package com.sakr.android.popularmovies.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mohammad sakr on 27/09/2015.
 */
public class Review
{
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";

    public long movieId;
    public String author;
    public String content;

    public Review(long id, String author, String content)
    {
        this.movieId = id;
        this.author = author;
        this.content = content;
    } // end public constructor

    public static Review fromJson(long movieId,JSONObject jsonObject) throws JSONException
    {
        return new Review(
                movieId,
                jsonObject.getString(AUTHOR),
                jsonObject.getString(CONTENT)
                );
    } //end method fromJson

}
