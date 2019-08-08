package com.ben.yana;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Map;

//handles talking to the SQLite database
public class DatabaseAccess
{
    private Context context;
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private static final String DBName = "NoteStorage";
    private static final int DBVersion = 1;

    //DATABASE COLUMN/TABLE NAMES
    //note table
    public static final String NOTE_TABLE = "Note";
    public static final String NOTE_ID = "noteID";
    public static final String NOTE_TITLE = "noteTitle";
    public static final String CR_DATE = "creationDate";
    public static final String STATUS = "status";

    //category table
    public static final String CAT_TABLE = "NoteCategory";
    public static final String CAT_ID = "categoryID";
    public static final String CAT_NAME = "categoryName";
    public static final String CAT_DESC = "categoryDesc" ;

    //component table
    public static final String COMP_TABLE = "NoteComponent";
    public static final String COMP_ID = "compID";
    public static final String COMP_TYPE = "type";
    public static final String COMP_PATH = "path";
    public static final String COMP_ORDER = "compOrder";

    public DatabaseAccess(Context context)
    {
        context = context;
        helper = new DatabaseHelper(context);
    }

    //inserting a new note
    public long insertNewNote(Map<String,String> note, Map<String,Map<String,String>> components)
    {
        String title = note.get("Title");
        String category = note.get("Category");
        String status = note.get("Status");
        String date = note.get("Date");

        ContentValues noteVal = new ContentValues();
        noteVal.put(NOTE_TITLE, title);
        noteVal.put(STATUS, status);
        noteVal.put(CR_DATE, date);
        noteVal.put(CAT_ID,category);

        try
        {
            long noteID = db.insertOrThrow(NOTE_TABLE, null, noteVal);
            return noteID;

        }
        catch(SQLException e)
        {
            Log.d("DatabaseAccess", e.toString());
            return -1;

        }

    }


    //getting all notes, only really used for testing purposes
    public Cursor getNotes()
    {
        return db.rawQuery("select * from " + NOTE_TABLE,new String[]{});
    }

    //get notes for the home page, all the are not archived or deleted
    public Cursor getHomeNotes()
    {
        return db.query(NOTE_TABLE, null, "status like ?", new String[]{"Base"},null,null,null);
    }

    //get notes for the archived page
    public Cursor getArchivedNotes()
    {
        return db.query(NOTE_TABLE, null, "status like 'Archived'", null,null,null,null);
    }

    //get notes for the deleted page
    public Cursor getDeletedNotes()
    {
        return db.query(NOTE_TABLE,null, "status like 'Deleted'", null,null,null,null);
    }

    //get components for a given noteID
    public Cursor getComponents(String noteID)
    {
        return db.query(COMP_TABLE, null, "noteID = ?", new String[]{noteID},null,null,null);
    }

    //update status of a note to deleted
    public boolean deleteNote(String noteID)
    {
        ContentValues updVal = new ContentValues();
        updVal.put(STATUS,"Deleted");
        int notesUpdated = db.update(NOTE_TABLE, updVal,"noteID = ?", new String[]{noteID});

        if(notesUpdated == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //update status of a note to archived
    public boolean archiveNote(String noteID)
    {
        ContentValues updVal = new ContentValues();
        updVal.put(STATUS,"Archived");
        int notesUpdated = db.update(NOTE_TABLE, updVal,"noteID = ?", new String[]{noteID});

        if(notesUpdated == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //update status of note to Base(Home)
    public boolean unArchiveNote(String noteID)
    {
        ContentValues updVal = new ContentValues();
        updVal.put(STATUS,"Base");
        int notesUpdated = db.update(NOTE_TABLE, updVal,"noteID = ?", new String[]{noteID});

        if(notesUpdated == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //update status of note to Base(Home)
    public boolean unDeleteNote(String noteID)
    {
        ContentValues updVal = new ContentValues();
        updVal.put(STATUS,"Base");
        int notesUpdated = db.update(NOTE_TABLE, updVal,"noteID = ?", new String[]{noteID});

        if(notesUpdated == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //actually permanently delete a note and all components related to it from the database
    public boolean permaDeleteNote(String noteID)
    {
        int componentsDeleted = db.delete(COMP_TABLE,"noteID = ?", new String[]{noteID});
        int notesDeleted = db.delete(NOTE_TABLE,"noteID = ?", new String[]{noteID});

        //more than 0 components should be deleted and 1 note should be deleted, otherwise something went wrong
        if(componentsDeleted > 0 && notesDeleted == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //permanently deleted notes with deleted status
    public int clearDeleted()
    {
        return db.delete(NOTE_TABLE,"status = 'Deleted'",null);
    }

    //delete a component
    public boolean deleteComponent(String compID)
    {
        int i = db.delete(COMP_TABLE,COMP_ID + " = ?", new String[]{compID});

        if(i == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //update a notes category
    public void updateCat(String catID, String noteID)
    {
        ContentValues updVal = new ContentValues();
        updVal.put(CAT_ID,catID);
        db.update(NOTE_TABLE,updVal,NOTE_ID + " = ?", new String[]{noteID});
    }

    //make a new category
    public boolean insertNewCategory(String name, String desc)
    {
        ContentValues catVal = new ContentValues();

        catVal.put(CAT_NAME, name);
        catVal.put(CAT_DESC, desc);

        try
        {
            long inserted = db.insertOrThrow(CAT_TABLE,null, catVal);
            Log.d("CATEGORY", Long.toString(inserted));
            return true;
        }
        catch(SQLException e)
        {
            Log.d("CATEGORY", e.toString());
            return false;
        }
    }

    //get a category name from a category id
    public Cursor getCategoryName(String catID)
    {
        return db.query(CAT_TABLE,new String[]{CAT_NAME}, CAT_ID +" = ?", new String[]{catID},null,null,null);
    }

    //get all categories
    public Cursor getCategories()
    {
        return db.query(CAT_TABLE, null,null,null,null,null,null);
    }

    //get notes for a specifi category
    public Cursor getCategoryNotes(String cat)
    {
        return db.query(NOTE_TABLE, null, CAT_ID +" = ? AND " + STATUS + " = ?" , new String[]{cat, "Base"},null,null,null);
    }

    //make a new component
    public long insertComponent(String noteID, String type, String path)
    {
        ContentValues compVal = new ContentValues();

        compVal.put(NOTE_ID, noteID);
        compVal.put(COMP_TYPE, type);
        compVal.put(COMP_PATH, path);
        compVal.put(COMP_ORDER, 0);

        long compID = -1;

        try
        {
            compID = db.insertOrThrow(COMP_TABLE, null, compVal);
            Log.d("COMPID", Long.toString(compID));
            return compID;
        }
        catch(SQLException e)
        {
            Log.d("INSERTCOMP",e.toString());
            return compID;
        }
    }

    //SETUP METHODS
    public DatabaseAccess open()
    {
        try
        {
            db = helper.getWritableDatabase();
        }
        catch(SQLException e)
        {
            Log.d("DatabaseAccess",e.toString());
        }

        return this;
    }

    public void close()
    {
        helper.close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, DBName, null, DBVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try
            {
                db.execSQL(create(CAT_TABLE));
                db.execSQL(create(NOTE_TABLE));
                db.execSQL(create(COMP_TABLE));
                Log.d("DatabaseAccess", "CREATES COMPLETE");

            }
            catch(SQLException e)
            {
                Log.d("DatabaseAccess", e.toString());
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
           //do nothing
        }

        //create tables
        private String create(String table)
        {
            String statement = "";

            switch(table)
            {
                case NOTE_TABLE:
                {
                    statement =
                            "create table " + NOTE_TABLE +
                            "(" +
                                NOTE_ID + " integer primary key autoincrement," +
                                CAT_ID + " integer," +
                                NOTE_TITLE + " text," +
                                CR_DATE + " datetime," +
                                STATUS + " text," +
                                "foreign key(" + CAT_ID + ") references "+ CAT_TABLE +"("+ CAT_ID +")" +

                            ")";
                    break;
                }

                case CAT_TABLE:
                {
                    statement =
                            "create table " + CAT_TABLE +
                            "(" +
                                CAT_ID + " integer primary key autoincrement," +
                                CAT_NAME + " text," +
                                CAT_DESC + " text" +
                            ")";
                    break;
                }

                case COMP_TABLE:
                {
                    statement =
                            "create table " + COMP_TABLE +
                                    "(" +
                                    COMP_ID + " integer primary key autoincrement," +
                                    NOTE_ID + " integer," +
                                    COMP_TYPE + " text," +
                                    COMP_PATH + " text," + //note content will be stored in a text file.(JSON, plain text, image etc)
                                    COMP_ORDER + " integer," +
                                    "foreign key(" + NOTE_ID + ") references " + NOTE_TABLE + "(" + NOTE_ID + ")" +
                                    ")";
                }
            }

            return statement;
        }
    }


}
