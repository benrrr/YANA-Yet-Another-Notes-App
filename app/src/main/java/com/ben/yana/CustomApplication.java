package com.ben.yana;

import android.app.Application;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//override the application class to provide global scope for some variables
public class CustomApplication extends Application
{
    //this data needs to be accessed globally to prevent inconsitency between versions.
    //without this it makes it harder when updating/removing/adding to lists and refreshing them
    //as each place they are referenced is handling different sets of data.
    public List<List<String>> selected = new ArrayList<List<String>>();
    public List<List<String>> mainContent = new ArrayList<List<String>>();
    public Map<String,Map<String,String>> currentComponents = new LinkedHashMap<String,Map<String,String>>();
    public Boolean SelectMode = false;
    public ListAdapter currentAdapter;
    public String contentTitle;
    public String currentCategory = "None";
}
