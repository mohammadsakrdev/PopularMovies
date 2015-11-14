package com.sakr.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.sakr.android.popularmovies.adapter.MoviesCursorAdapter;
import com.sakr.android.popularmovies.data.MovieContract;
import com.sakr.android.popularmovies.data.MovieContract.MovieEntry;
import com.sakr.android.popularmovies.service.MoviesService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{

    public MoviesFragment()
    {
    }

    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private GridView mGridView;
    private MoviesCursorAdapter mMoviesCursorAdapter;

    private static final int MOVIES_LOADER = 0;


    private static final String[] MOVIE_COLUMNS =
            {
                    MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_POSTER_PATH
            };
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_POSTER_PATH = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
        mMoviesCursorAdapter = new MoviesCursorAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mMoviesCursorAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null)
                {
                    ((Callback) getActivity())
                            .onItemSelected(MovieEntry.buildMovieUri(cursor.getLong(COL_ID)));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
        {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        } // end if

        return rootView;
    } // end method onCreateView

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (mPosition != GridView.INVALID_POSITION)
        {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

//    @Override
//    public void onStart()
//    {
//        super.onStart();
//        updateMovies();
//    }


    private void updateMovies()
    {
        Intent intent = new Intent(getActivity().getApplicationContext(), MoviesService.class);
        getActivity().startService(intent);
    } // end method updateMovies

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String sorting = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).
                getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_default_value));
        if (!sorting.equals(getString(R.string.pref_sorting_value_favorite)))
        {
            return new CursorLoader(getActivity().getApplicationContext(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_SORTING + " = ? ",
                    new String[]{sorting},
                    null);
        } else
        {
            return new CursorLoader(getActivity().getApplicationContext(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    MovieEntry.COLUMN_FAVORITE + " = ? ",
                    new String[]{Integer.toString(1)},
                    null);
        }

    } // end method onCreateLoader

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        mMoviesCursorAdapter.swapCursor(data);

        if (mPosition != GridView.INVALID_POSITION)
        {
            mGridView.smoothScrollToPosition(mPosition);
        }

    } // end method onLoadFinished

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mMoviesCursorAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback
    {
        public void onItemSelected(Uri movieUri);

    } // end interface Callback

    void onSortingChanged()
    {
        String sorting = Utility.getSortingSetting(getActivity().getApplicationContext());
        if (!sorting.equals(getString(R.string.pref_sorting_value_favorite)))
        {
            updateMovies();
        } // end if

        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    } // end method onSortingChanged

} //end class MoviesFragment
