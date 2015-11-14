package com.sakr.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.sakr.android.popularmovies.data.MovieContract.MovieEntry;
import com.sakr.android.popularmovies.data.MovieContract.ReviewEntry;
import com.sakr.android.popularmovies.data.MovieContract.TrailerEntry;


/**
 * Created by mohammad sakr on 27/09/2015.
 */
public class MovieProvider extends ContentProvider
{
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_MOVIE_ID = 101;

    static final int TRAILER = 200;
    static final int TRAILER_WITH_MOVIE_ID = 201;

    static final int REVIEW = 300;
    static final int REVIEW_WITH_MOVIE_ID = 301;

    private static final SQLiteQueryBuilder sTrailerByMovieQueryBuilder;

    static
    {
        sTrailerByMovieQueryBuilder = new SQLiteQueryBuilder();
        sTrailerByMovieQueryBuilder.setTables(
                TrailerEntry.TABLE_NAME + " INNER JOIN " +
                        MovieEntry.TABLE_NAME +
                        " ON " + TrailerEntry.TABLE_NAME +
                        "." + TrailerEntry.COLUMN_MOVIE_ID +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);
    }

    private static final SQLiteQueryBuilder sReviewByMovieQueryBuilder;

    static
    {
        sReviewByMovieQueryBuilder = new SQLiteQueryBuilder();
        sReviewByMovieQueryBuilder.setTables(
                ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieEntry.TABLE_NAME +
                        " ON " + ReviewEntry.TABLE_NAME +
                        "." + ReviewEntry.COLUMN_MOVIE_ID +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);
    }

    //movie._id = ?
    private static final String sMovie_IdSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry._ID + " = ? ";

    //movie.movie_id = ?
    private static final String sMovieIdSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry._ID + " = ? ";

    //movie.sorting = ?
    private static final String sMoviePopularitySelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry.COLUMN_SORTING + " = ? ";

    //movie.sorting = ?
    private static final String sMovieFavoriteSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry.COLUMN_FAVORITE + " = ? ";

    //trailer.movie_id = ?
    private static final String sTrailerWithMovieIdSelection =
            TrailerEntry.TABLE_NAME +
                    "." + TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    //review.movie_id = ?
    private static final String sReviewWithMovieIdSelection =
            ReviewEntry.TABLE_NAME +
                    "." + ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    static UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/#", TRAILER_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/#", REVIEW_WITH_MOVIE_ID);
        return matcher;
    }

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new MovieDBHelper(getContext());
        return false;
    } // end method onCreate

    @Override
    public String getType(Uri uri)
    {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            // Student: Uncomment and fill out these two cases
            case MOVIE:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case MOVIE_WITH_MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_WITH_MOVIE_ID:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE_ID:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    } // end method getType

    /**
     * Queries the DB for the data requested by the caller of the method
     *
     * @param uri           The Uri of the data queried, containing information about where to search
     * @param projection    An array of required columns from the table ({@code null} for all columns)
     * @param selection     The where clause
     * @param selectionArgs The arguments of the where clause
     * @param sortOrder     The order of sorting the movies
     * @return A {@code Cursor} instance with the data
     */

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        switch (sUriMatcher.match(uri))
        {
            case MOVIE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            } // end case

            case MOVIE_WITH_MOVIE_ID:
            {
                long id = MovieContract.MovieEntry.getMovieIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        sMovie_IdSelection,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            } // end case

            case TRAILER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }// end case

            case TRAILER_WITH_MOVIE_ID:
            {
                long id = TrailerEntry.getMovieIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        sTrailerWithMovieIdSelection,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            } // end case
            case REVIEW:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }// end case

            case REVIEW_WITH_MOVIE_ID:
            {
                long id = ReviewEntry.getMovieFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        sReviewWithMovieIdSelection,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            } // end case

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        } // end switch

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    } //end method query

    /**
     * Insert a new record into the table pointed by the Uri
     *
     * @param uri    The Uri that should point into a table
     * @param values Record to add into the table
     * @return A Content Uri with the ID of the inserted row
     */

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match)
        {
            case MOVIE:
            {
                //query the movie
                Cursor cursor = db.query(MovieEntry.TABLE_NAME,
                        new String[]{MovieEntry._ID,
                                MovieEntry.COLUMN_MOVIE_ID},
                        MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        new String[]{values.get(MovieEntry.COLUMN_MOVIE_ID).toString()},
                        null,
                        null,
                        null);
                if (cursor.moveToFirst())
                {
                    returnUri = MovieEntry.buildMovieUri(cursor.getInt(cursor.getColumnIndex(MovieEntry._ID)));

                    db.update(MovieEntry.TABLE_NAME,
                            values,
                            sMovie_IdSelection,
                            new String[]{Long.toString(cursor.getInt(0))});
                } // end if
                else
                {
                    long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = MovieEntry.buildMovieUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            } // end case
            case TRAILER:
            {
                //query the trailer
                Cursor cursor = db.query(TrailerEntry.TABLE_NAME,
                        new String[]{TrailerEntry._ID,
                                TrailerEntry.COLUMN_MOVIE_ID,
                                TrailerEntry.COLUMN_TRAILER_KEY,
                                TrailerEntry.COLUMN_TRAILER_NAME},
                        TrailerEntry.COLUMN_MOVIE_ID + " = ? AND " +
                                TrailerEntry.COLUMN_TRAILER_KEY + " = ? AND " +
                                TrailerEntry.COLUMN_TRAILER_NAME + " = ? ",
                        new String[]{values.get(MovieEntry.COLUMN_MOVIE_ID).toString(),
                                values.get(TrailerEntry.COLUMN_TRAILER_KEY).toString(),
                                values.get(TrailerEntry.COLUMN_TRAILER_NAME).toString()},
                        null,
                        null,
                        null);
                if (!cursor.moveToFirst())
                {
                    long _id = db.insert(
                            TrailerEntry.TABLE_NAME,
                            null,
                            values);
                    if (_id > 0)
                        returnUri = TrailerEntry.buildTrailerUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                } // end if
                else
                {
                    returnUri = TrailerEntry.buildTrailerUri(cursor.getLong(0));
                }
                break;
            }
            case REVIEW:
            {
                Cursor cursor = db.query(ReviewEntry.TABLE_NAME,
                        new String[]{ReviewEntry._ID,
                                ReviewEntry.COLUMN_MOVIE_ID,
                                ReviewEntry.COLUMN_AUTHOR,
                                ReviewEntry.COLUMN_CONTENT},
                        ReviewEntry.COLUMN_MOVIE_ID + " = ? AND " +
                                ReviewEntry.COLUMN_AUTHOR + " = ? AND " +
                                ReviewEntry.COLUMN_CONTENT + " = ? ",
                        new String[]{values.get(ReviewEntry.COLUMN_MOVIE_ID).toString(),
                                values.get(ReviewEntry.COLUMN_AUTHOR).toString(),
                                values.get(ReviewEntry.COLUMN_CONTENT).toString()},
                        null,
                        null,
                        null);
                if (!cursor.moveToFirst())
                {
                    long _id = db.insert(
                            ReviewEntry.TABLE_NAME,
                            null, values);
                    if (_id > 0)
                        returnUri = ReviewEntry.buildReviewUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                } else
                {
                    returnUri = ReviewEntry.buildReviewUri(cursor.getLong(0));
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    } //end method insert

    /**
     * Deletes the records in the table pointed by the URI and match the other args
     *
     * @param uri           Uri of the table where to search the records to delete
     * @param selection     A selection criteria to apply when filtering rows. If {@code null} then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The values
     *                      will be bound as Strings.
     * @return The number of deleted rows
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection)
        {
            selection = "1";
        }
        switch (match)
        {
            case MOVIE:
            {
                rowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }

            case TRAILER:
            {
                rowsDeleted = db.delete(
                        TrailerEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }

            case REVIEW:
            {
                rowsDeleted = db.delete(
                        ReviewEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    } // end method delete

    /**
     * Updates a record on the table pointed by the URI.
     *
     * @param uri           Uri of the table where to search the records to update
     * @param values        The new set of {@code ContentValues} to replace the old ones
     * @param selection     A selection criteria to apply when filtering rows. If {@code null} then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The values
     *                      will be bound as Strings.
     * @return The number of updated rows
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match)
        {
            case MOVIE:
            {
                rowsUpdated = db.update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case TRAILER:
            {
                rowsUpdated = db.update(
                        TrailerEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case REVIEW:
            {
                rowsUpdated = db.update(
                        ReviewEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    } // end method update

    /**
     * This method is used for mass insertion into the database
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database. This must not be null.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        try
        {
            db.beginTransaction();

            for (ContentValues value : values)
            {
                Uri id = insert(uri, value);

                if (id != null)
                {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } //end try
        finally
        {
            db.endTransaction();
        }

        if (getContext() != null)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    } // end method bulkInsert

} //end class MovieProvider
