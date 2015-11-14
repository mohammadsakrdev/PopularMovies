package com.sakr.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by mohammad sakr on 06/10/2015.
 */
public class Utility
{
    public static String getRating(double voteAverage)
    {
        return "" + voteAverage + "/10";
    }

    public static String getReleaseDate(String releaseDate)
    {
        String str = "";
        try
        {
            str = new SimpleDateFormat("yyyy").format(new SimpleDateFormat("yyyy").parse(releaseDate));
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return str;
    } // end method getReleaseDate

    public static Uri buildPosterUri(String size, String posterPath)
    {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(posterPath)
                .build();

        return builtUri;
    } // end method Uri buildPosterUri

    public static String getLink(String key)
    {
        return "https://www.youtube.com/watch?v=" + key;
    } // end method Uri buildPosterUri

    public static String getSortingSetting(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getString(context.getString(R.string.pref_sorting_key),
                        context.getString(R.string.pref_sorting_default_value));
    } // end method getSortingSetting

} // end class Utility
