package com.sakr.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sakr.android.popularmovies.adapter.ReviewsCursorAdapter;
import com.sakr.android.popularmovies.adapter.TrailersCursorAdapter;
import com.sakr.android.popularmovies.data.MovieContract.MovieEntry;
import com.sakr.android.popularmovies.data.MovieContract.ReviewEntry;
import com.sakr.android.popularmovies.data.MovieContract.TrailerEntry;
import com.sakr.android.popularmovies.service.DetailsService;
import com.sakr.android.popularmovies.view.CustomListView;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private TextView mMovieTitle;
    private ImageView mMovieImagePoster;
    private TextView mMovieReleaseDate;
    private TextView mMovieLength;
    private TextView mMovieRating;
    private Button mFavoriteBtn;
    private TextView mMovieOverview;
    public static String mShareStr;

    private long _id;
    private Uri mUri;
    private int mFavorite;

    public static final String DETAIL_URI = "URI";

    private ShareActionProvider mShareActionProvider;

    private CustomListView mReviewsListView;
    private CustomListView mTrailersListView;

    private TrailersCursorAdapter mTrailersAdapter;
    private ReviewsCursorAdapter mReviewsAdapter;

    private static final int MOVIE_LOADER = 0;
    private static final int TRAILERS_LOADER = 1;
    private static final int REVIEWS_LOADER = 2;

    private static final String[] MOVIE_COLUMNS =
            {
                    MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_MOVIE_OVERVIEW,
                    MovieEntry.COLUMN_RELEASE_DATE,
                    MovieEntry.COLUMN_POSTER_PATH,
                    MovieEntry.COLUMN_MOVIE_TITLE,
                    MovieEntry.COLUMN_VOTE_AVERAGE,
                    MovieEntry.COLUMN_RUNTIME,
                    MovieEntry.COLUMN_FAVORITE
            };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_OVERVIEW = 2;
    public static final int COL_RELEASE_DATE = 3;
    public static final int COL_POSTER_PATH = 4;
    public static final int COL_TITLE = 5;
    public static final int COL_VOTE_AVERAGE = 6;
    public static final int COL_RUNTIME = 7;
    public static final int COL_FAVORITE = 8;

    private static final String[] TRAILERS_COLUMNS =
            {
                    TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
                    TrailerEntry.COLUMN_TRAILER_NAME,
                    TrailerEntry.COLUMN_TRAILER_KEY
            };

    public static final int COL_TRAILER_ID = 0;
    public static final int COL_TRAILER_NAME = 1;
    public static final int COL_TRAILER_KEY = 2;

    private static final String[] REVIEWS_COLUMNS =
            {
                    ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
                    ReviewEntry.COLUMN_AUTHOR,
                    ReviewEntry.COLUMN_CONTENT
            };

    public static final int COL_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;

    public DetailFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle args = getArguments();
        if ( args != null)
        {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }

        mMovieTitle = (TextView) rootView.findViewById(R.id.movieTitle);
        mMovieImagePoster = (ImageView) rootView.findViewById(R.id.movieImagePoster);
        mMovieReleaseDate = (TextView) rootView.findViewById(R.id.movieReleaseDate);
        mMovieLength = (TextView) rootView.findViewById(R.id.movieLength);
        mMovieRating = (TextView) rootView.findViewById(R.id.movieRating);
        mMovieOverview = (TextView) rootView.findViewById(R.id.movieOverview);
        mFavoriteBtn = (Button) rootView.findViewById(R.id.favoriteBtn);
        mFavoriteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ContentValues values = new ContentValues();
                if (mFavorite == 0)
                {
                    mFavorite = 1;
                    values.put(MovieEntry.COLUMN_FAVORITE,mFavorite);
                    getActivity().getApplicationContext().getContentResolver().
                            update(MovieEntry.CONTENT_URI,
                                    values,
                                    MovieEntry._ID + " = ? ",
                                    new String[]{Long.toString(_id)});
                    updateFavoriteButton();
                }
                else
                {
                    mFavorite = 0;
                    values.put(MovieEntry.COLUMN_FAVORITE,mFavorite);
                    getActivity().getApplicationContext().getContentResolver().
                            update(MovieEntry.CONTENT_URI,
                                    values,
                                    MovieEntry._ID + " = ? ",
                                    new String[]{Long.toString(_id)});
                    updateFavoriteButton();
                }
            }
        });
        mReviewsListView = (CustomListView) rootView.findViewById(R.id.reviewListView);
        mReviewsListView.setExpanded(true);

        mTrailersListView = (CustomListView) rootView.findViewById(R.id.trailer_list_view);
        mTrailersListView.setExpanded(true);

        mTrailersAdapter = new TrailersCursorAdapter(getActivity().getApplicationContext(), null, 0);
        mTrailersListView.setAdapter(mTrailersAdapter);

        mReviewsAdapter = new ReviewsCursorAdapter(getActivity(), null, 0);
        mReviewsListView.setAdapter(mReviewsAdapter);

        mTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utility.getLink(cursor.getString(COL_TRAILER_KEY))));
                    startActivity(intent);
                } // end if

            } // end method onItemClick
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(MOVIE_LOADER,null,this);
        getLoaderManager().initLoader(TRAILERS_LOADER, null,this);
        getLoaderManager().initLoader(REVIEWS_LOADER,null,this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mUri != null)
        {
            updateUI();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private void updateUI()
    {
        Intent intent = new Intent(getActivity().getApplicationContext(), DetailsService.class);
        intent.setData(mUri);
        getActivity().startService(intent);

    } // end method updateUI


    private Intent createShareTrailerIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareStr);
        return shareIntent;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case MOVIE_LOADER:
                if(mUri == null)
                {
                    return null;

                }
                else
                {
                    _id = MovieEntry.getMovieIdFromUri(mUri);
                    return new CursorLoader(getActivity().getApplicationContext(),
                            mUri,
                            MOVIE_COLUMNS,
                            null,
                            null,
                            null);
                }
            case TRAILERS_LOADER:
            return new CursorLoader(getActivity().getApplicationContext(),
                    TrailerEntry.CONTENT_URI,
                    TRAILERS_COLUMNS,
                    TrailerEntry.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{String.valueOf(_id)},
                    null);

            case REVIEWS_LOADER:
            return new CursorLoader(getActivity().getApplicationContext(),
                    ReviewEntry.CONTENT_URI,
                    REVIEWS_COLUMNS,
                    ReviewEntry.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{String.valueOf(_id)},
                    null);
            default:
                return null;
        } // end switch
    } // end method onCreateLoader

    @Override
    public void onLoadFinished(Loader loader, Cursor data)
    {
        switch (loader.getId())
        {
            case MOVIE_LOADER:
                if (!data.moveToFirst())
                {
                    return;
                }
                mMovieTitle.setText(data.getString(COL_TITLE));
                mMovieReleaseDate.setText(Utility.getReleaseDate(data.getString(COL_RELEASE_DATE)));
                mMovieRating.setText(Utility.getRating(data.getDouble(COL_VOTE_AVERAGE)));
                mMovieOverview.setText(data.getString(COL_OVERVIEW));
                mFavorite = data.getInt(COL_FAVORITE);
                updateFavoriteButton();
                String runtimeStr = data.getString(COL_RUNTIME);
                if(runtimeStr == null)
                {
                    mMovieLength.setText("");
                }
                else
                {
                    mMovieLength.setText(runtimeStr + "min");
                }


                Picasso.with(getActivity().getApplicationContext())
                        .load(Utility.buildPosterUri(
                                getString(R.string.api_poster_default_size),
                                data.getString(COL_POSTER_PATH)))
                        .error(R.drawable.movie_placeholder)
                        .placeholder(R.drawable.loader)
                        .into(mMovieImagePoster);
                break;
            case TRAILERS_LOADER:

                mTrailersAdapter.swapCursor(data);
                if (mShareActionProvider != null)
                {
                    mShareActionProvider.setShareIntent(createShareTrailerIntent());
                }
                break;
            case REVIEWS_LOADER:

                mReviewsAdapter.swapCursor(data);
                break;
        } // end switch

    } // end method onLoadFinished

    @Override
    public void onLoaderReset(Loader loader)
    {
        switch (loader.getId())
        {
            case TRAILERS_LOADER:
            mTrailersAdapter.swapCursor(null);
                break;
            case REVIEWS_LOADER:
            mReviewsAdapter.swapCursor(null);
                break;
        } // end switch

    } // end method onLoaderReset

    private  void updateFavoriteButton()
    {
        if (mFavorite == 0)
        {
            mFavoriteBtn.setText(getString(R.string.favorite_button_text));
        }
        else
        {
            mFavoriteBtn.setText(getString(R.string.un_favorite_button_text));
        }
    } // end method updateFavoriteButton
}
