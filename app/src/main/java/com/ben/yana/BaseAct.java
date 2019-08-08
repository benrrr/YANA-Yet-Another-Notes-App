package com.ben.yana;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//main activity
public class BaseAct extends AppCompatActivity
{
    private DataHandler dr;

    //Side bar and toolbar attributes
    private Map<String,List<List<String>>> drawerItems;
    private List<String> drawerParents;
    private DrawerLayout drawer;
    private ExpandableListView drawerList;
    public ExpandableListAdapter navAd;

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //initialising data
        ((CustomApplication)getApplication()).contentTitle = "Home";
        dr = DataHandler.getInstance(getApplicationContext());

        drawerItems = new LinkedHashMap<String,List<List<String>>>();
        drawerParents = new ArrayList<String>();
        drawerList = (ExpandableListView)findViewById(R.id.drawerList);

        //setting up views as necessary
        setupToolbar();
        setupDrawer(gListener,cListener);
        linkToolbarDrawer();

        //loading the home fragment as default
        BaseFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction().add(R.id.frame,(Fragment)homeFragment, "Current Fragment").commit();

        //setting listeners
        Button newNote = (Button)findViewById(R.id.newNote);
        newNote.setOnClickListener(newNoteListener);

        Button newCat = (Button)findViewById(R.id.newCat);
        newCat.setOnClickListener(newCatListener);
    }

    //sets up the toolbar
    protected void setupToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(((CustomApplication)getApplication()).contentTitle);

        setSupportActionBar(toolbar);

        //enabling the button which will open and close the drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //sets up the drawer with required data
    protected void setupDrawer(ExpandableListView.OnGroupClickListener gListener, ExpandableListView.OnChildClickListener cListener)
    {
        drawerItems = dr.getMenuData();
        drawerParents = new ArrayList<>(drawerItems.keySet());
        drawer = (DrawerLayout) findViewById(R.id.drawer);

        //setup the list
        navAd = new NavAdapter(getApplicationContext(), drawerParents, drawerItems);
        drawerList.setAdapter(navAd);

        //setting listeners on the list
        drawerList.setOnGroupClickListener(gListener);
        drawerList.setOnChildClickListener(cListener);
        drawerList.setOnGroupExpandListener(geListener);

    }

    //links the button on the toolbar with the drawer
    protected void linkToolbarDrawer()
    {
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawerOpen, R.string.drawerClose)
        {
            public void onDrawerOpened(View drawerView)
            {
                toolbar.setTitle("YANA");
            }
            public void onDrawerClosed(View view)
            {
                toolbar.setTitle(((CustomApplication)getApplication()).contentTitle);
            }
        };

        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    //LISTENERS
    //when a group is clicked in the drawer
    ExpandableListView.OnGroupClickListener gListener = new ExpandableListView.OnGroupClickListener()
    {
        @Override
        public boolean onGroupClick(ExpandableListView exV, View v, int gPos, long id)
        {
            //check what group was clicked and set that as the new content
            ((CustomApplication)getApplication()).contentTitle = drawerParents.get(gPos);
            ((CustomApplication)getApplication()).currentCategory = "None";

            //launch new fragment, or if category expand
            Fragment newFragment = null;
            switch(((CustomApplication)getApplication()).contentTitle)
            {
                case "Home":
                {
                    newFragment = new HomeFragment();
                    break;
                }

                case "Archived":
                {
                    newFragment = new ArchiveFragment();
                    break;
                }

                case "Categories":
                {
                    //do nothing so it expands category list, fragment change handled in on child click
                    break;
                }

                case "Deleted":
                {
                    newFragment = new DeletedFragment();
                    break;
                }
            }

            if(newFragment != null)
            {
                (((CustomApplication) getApplication()).selected) = new ArrayList<List<String>>();
                (((CustomApplication) getApplication()).SelectMode) = false;

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, newFragment, "Current Fragment")
                        .addToBackStack(null)
                        .commit();
            }

            //if not category, close the drawer and if the overlay is visible make it invisible, make the main content frame clickable again
            if(!((CustomApplication)getApplication()).contentTitle.equals("Categories"))
            {
                drawer.closeDrawer(GravityCompat.START);

                FrameLayout overlay = (FrameLayout)findViewById(R.id.overlay);

                if(overlay.getVisibility() == View.VISIBLE)
                {
                    overlay.setVisibility(View.INVISIBLE);

                    FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
                    frame.setClickable(true);
                }
            }
            return false;
        }
    };

    ExpandableListView.OnChildClickListener cListener = new ExpandableListView.OnChildClickListener()
    {
        @Override
        public boolean onChildClick(ExpandableListView p, View v, int gPos, int cPos,long id)
        {
            //check which child was clicked, set appropriate variables
            ((CustomApplication)getApplication()).contentTitle = drawerParents.get(gPos);
            String childChosen = drawerItems.get(((CustomApplication)getApplication()).contentTitle).get(cPos).get(0);
            Fragment newFragment = null;

            ((CustomApplication)getApplication()).currentCategory = childChosen;

            newFragment = new CategoryFragment();

            (((CustomApplication) getApplication()).selected) = new ArrayList<List<String>>();
            (((CustomApplication) getApplication()).SelectMode) = false;


            //load category fragment after setting the current category fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, newFragment, "Current Fragment")
                    .addToBackStack(null)
                    .commit();

            //close drawer, hide overlay, etc
            drawer.closeDrawer(GravityCompat.START);

            FrameLayout overlay = (FrameLayout)findViewById(R.id.overlay);

            if(overlay.getVisibility() == View.VISIBLE)
            {
                overlay.setVisibility(View.INVISIBLE);

                FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
                frame.setClickable(true);
            }

            return false;
        }
    };

    //when a group is expanded close all other groups, not really doing anything in the current state as only category will expand, but worth keeping just in case
    ExpandableListView.OnGroupExpandListener geListener = new ExpandableListView.OnGroupExpandListener()
    {
        @Override
        public void onGroupExpand(int gPos)
        {
            for(int i = 0; i < navAd.getGroupCount(); i++)
            {
                if(i != gPos)
                {
                    if(drawerList.isGroupExpanded(i))
                    {
                        drawerList.collapseGroup(i);
                    }
                }
            }
        }
    };

    //when new note button is clicked
    View.OnClickListener newNoteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //hide overlay
            FrameLayout overlay = (FrameLayout)findViewById(R.id.overlay);
            overlay.setVisibility(View.INVISIBLE);

            //set maincontent frame as clickable again
            FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
            frame.setClickable(true);

            //go to note activity
            EditText input = (EditText)findViewById(R.id.newTitle);

            Intent i = new Intent(BaseAct.this,NoteActivity.class);
            i.putExtra("newTitle",input.getText().toString());
            startActivity(i);
        }
    };

    //when new category is clicked
    View.OnClickListener newCatListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //take the input and send to data handler
            EditText input = (EditText)findViewById(R.id.newTitle);
            dr.insertNewCatgeory(input.getText().toString(), "");

            //recreate to so the category will load in the drawer
            recreate();

            //close the overlay etc
            onBackPressed();
        }
    };

    @Override
    public void onBackPressed()
    {
        FrameLayout overlay = (FrameLayout)findViewById(R.id.overlay);

        //first priority close the drawer if its open
        if(drawer.isDrawerOpen(GravityCompat.START) == true)
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        // then the over lay if its visible
        else if(overlay.getVisibility() == View.VISIBLE)
        {
            overlay.setVisibility(View.INVISIBLE);

            FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
            frame.setClickable(true);
        }
        //then turn off select mode if on
        else if(((CustomApplication)this.getApplication()).SelectMode == true)
        {
            ((CustomApplication)this.getApplication()).selected = new ArrayList<List<String>>();
            ((CustomApplication)this.getApplication()).SelectMode = false;
            ((NoteListAdapter)((CustomApplication)this.getApplication()).currentAdapter).refresh();
        }
        //otherwise general back press
        else
        {
            super.onBackPressed();
        }
    }
}
