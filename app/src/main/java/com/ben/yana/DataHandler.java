package com.ben.yana;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//used to get and parse data before returning it to the caller
public class DataHandler
{
    static DataHandler dh = null;
    DatabaseAccess db;
    Context context;

    static DataHandler getInstance(Context context)
    {
        if(dh == null)
        {
            dh = new DataHandler(context);
        }

        return dh;
    }

    public DataHandler(Context context)
    {
        this.context = context;
        db = new DatabaseAccess(context);

        db = db.open();
    }

    //parse a response containing notes
    public List<List<String>> parseNotesCursor(Cursor c)
    {
        List<List<String>> data = new ArrayList<List<String>>();

        c.moveToFirst();

        while(!c.isAfterLast())
        {
            List<String> note = new ArrayList<String>();

            note.add(c.getString(c.getColumnIndex(db.NOTE_TITLE)));
            note.add(c.getString(c.getColumnIndex(db.CAT_ID)));
            note.add(Integer.toString(c.getInt(c.getColumnIndex(db.NOTE_ID))));

            data.add(note);

            c.moveToNext();
        }

        return data;
    }

    //get category name from id
    public String getCategoryName(String catID)
    {
        Cursor c = db.getCategoryName(catID);
        String catName = "";

        c.moveToFirst();

        while(!c.isAfterLast())
        {
            catName = c.getString(c.getColumnIndex(db.CAT_NAME));
            c.moveToNext();
        }

        return catName;
    }

    //get data
    public List<List<String>> getData(String type, String cat)
    {
        List<List<String>> data = new ArrayList<List<String>>();

        //decide which data to get
        switch(type)
        {
            case "Home":
            {
                Cursor c = db.getHomeNotes();
                data = parseNotesCursor(c);
                break;
            }

            case "Archived":
            {
                Cursor c = db.getArchivedNotes();
                data = parseNotesCursor(c);
                break;
            }

            case "Categories":
            {
                Cursor c = db.getCategoryNotes(cat);
                data = parseNotesCursor(c);
                break;
            }

            case "Deleted":
            {
                Cursor c = db.getDeletedNotes();
                data = parseNotesCursor(c);
                break;
            }
        }
        return data;
    }

    //insert new note
    public long insertNewNote(String title, String category)
    {
        Map<String,String> note = new LinkedHashMap<String,String>();

        note.put("Title",title);
        note.put("Category",category);
        note.put("Status","Base");
        note.put("Date",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));

        return db.insertNewNote(note, null);
    }

    //get data for drawer
    public Map<String,List<List<String>>> getMenuData()
    {
        Map<String,List<List<String>>> data = new LinkedHashMap<String,List<List<String>>>();

        List<List<String>> categorySub = getCategories();

        List<List<String>> empty = new ArrayList<List<String>>();
        data.put("Home",empty);
        data.put("Archived",empty);
        data.put("Categories",categorySub);
        data.put("Deleted",empty);

        return data;
    }

    //get note components
    public Map<String,Map<String,String>> getNoteComponents(String noteID)
    {
        Map<String,Map<String,String>> data = new LinkedHashMap<String,Map<String,String>>();
        Cursor c = db.getComponents(noteID);

        c.moveToFirst();

        for(int i = 0; !c.isAfterLast(); i++)
        {
            Map<String,String> component =  new LinkedHashMap<String,String>();
            String header = "Component" + i;

            component.put("ID",c.getString(c.getColumnIndex(db.COMP_ID)));
            component.put("type",c.getString(c.getColumnIndex(db.COMP_TYPE)));
            component.put("path",c.getString(c.getColumnIndex(db.COMP_PATH)));
            component.put("order",c.getString(c.getColumnIndex(db.COMP_ORDER)));

            data.put(header, component);
            c.moveToNext();
        }

        return data;
    }

    //these methods are simple forwards to the database class
    //but for the sake of consistency I have all database access go through
    //the data handler
    public boolean deleteComponent(String compID)
    {
        return db.deleteComponent(compID);
    }

    public boolean deleteNote(String noteID)
    {
        return db.deleteNote(noteID);
    }

    public boolean archiveNote(String noteID)
    {
        return db.archiveNote(noteID);
    }

    public boolean unDeleteNote(String noteID)
    {
        return db.unDeleteNote(noteID);
    }

    public boolean unArchiveNote(String noteID)
    {
        return db.unDeleteNote(noteID);
    }

    public boolean permaDeleteNote(String noteID)
    {
        return db.permaDeleteNote(noteID);
    }

    public void updateCat(String catID, String noteID)
    {
        db.updateCat(catID, noteID);
    }

    public int clearDeleted()
    {
        return db.clearDeleted();
    }

    public boolean insertNewCatgeory(String name, String desc)
    {
        return db.insertNewCategory(name,desc);
    }

    //get all categories
    public List<List<String>> getCategories()
    {
        List<List<String>> categories = new ArrayList<List<String>>();
        Cursor c = db.getCategories();

        c.moveToFirst();

        while(!c.isAfterLast())
        {
            List<String> category = new ArrayList<String>();
            category.add(c.getString(c.getColumnIndex(db.CAT_ID)));
            category.add(c.getString(c.getColumnIndex(db.CAT_NAME)));
            category.add(c.getString(c.getColumnIndex(db.CAT_DESC)));

            categories.add(category);
            c.moveToNext();
        }

        return categories;
    }

    public long insertComponent(String noteID, String type, String path)
    {
        return db.insertComponent(noteID,type,path);
    }
}
