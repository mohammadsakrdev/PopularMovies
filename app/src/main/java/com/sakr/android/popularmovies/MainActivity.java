package com.sakr.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback
{
    private String mSorting;

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mSorting = Utility.getSortingSetting(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( findViewById(R.id.movie_detail_container) != null )
        {
            mTwoPane = true;

            if ( savedInstanceState == null )
            {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            } // end if

        } // end if
        else
        {
            mTwoPane = false;
        } // end else

    } // end method onCreate

    @Override
    protected void onResume()
    {
        super.onResume();

        String sorting = Utility.getSortingSetting(this);

        if( sorting != null && !sorting.equals(mSorting))
        {
            MoviesFragment moviesFragment = (MoviesFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movies);
            if (moviesFragment != null)
            {
                moviesFragment.onSortingChanged();
            } // end if
            mSorting = sorting;
        } // end if
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri movieUri)
    {
        if(mTwoPane)
        {
            Bundle args = new Bundle();

            args.putParcelable(DetailFragment.DETAIL_URI,movieUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } // end if
        else
        {
            Intent intent = new Intent(this,DetailActivity.class);
            intent.setData(movieUri);
            startActivity(intent);
        } // end else
    } // end method onItemSelected
} // end class MainActivity
