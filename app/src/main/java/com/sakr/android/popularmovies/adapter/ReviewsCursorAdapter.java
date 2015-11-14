package com.sakr.android.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sakr.android.popularmovies.DetailFragment;
import com.sakr.android.popularmovies.R;

/**
 * Created by mohammad sakr on 09/10/2015.
 */
public class ReviewsCursorAdapter extends CursorAdapter
{
    public ReviewsCursorAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    } // end public constructor

    public static class ViewHolder
    {
        TextView authorNameTxt;
        TextView contentTxt;

        public ViewHolder(View view)
        {
            authorNameTxt = (TextView) view.findViewById(R.id.review_author_textview);
            contentTxt = (TextView) view.findViewById(R.id.review_content_textview);
        }

    } //end class ViewHolder

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review,parent,false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }// end method newView

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder holder = (ViewHolder)view.getTag();
        holder.authorNameTxt.setText(cursor.getString(DetailFragment.COL_REVIEW_AUTHOR));
        holder.contentTxt.setText(cursor.getString(DetailFragment.COL_REVIEW_CONTENT));
    }// end method bindView

} // end class ReviewsCursorAdapter
