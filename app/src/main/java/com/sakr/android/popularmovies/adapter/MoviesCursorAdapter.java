package com.sakr.android.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sakr.android.popularmovies.MoviesFragment;
import com.sakr.android.popularmovies.R;
import com.sakr.android.popularmovies.Utility;
import com.squareup.picasso.Picasso;

/**
 * Created by mohammad sakr on 08/10/2015.
 */
public class MoviesCursorAdapter extends CursorAdapter
{
    public MoviesCursorAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    } // end public arg-constructor

    public static class ViewHolder
    {
        ImageView posterView;

        public ViewHolder(View view)
        {
            posterView = (ImageView)view.findViewById(R.id.imageView);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    } // end method newView

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder holder = (ViewHolder)view.getTag();
        String path = cursor.getString(MoviesFragment.COL_POSTER_PATH);
        Uri posterUri = Utility.buildPosterUri(context.getString(R.string.api_poster_default_size), path);
        Picasso.with(context)
                .load(posterUri)
                .error(R.drawable.movie_placeholder)
                .placeholder(R.drawable.loader)
                .into(holder.posterView);

    } // end method bindView

} // end class MoviesCursorAdapter
