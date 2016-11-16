package provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CoolerBy on 21.09.2016.
 */
public class WheelyProvider extends ContentProvider {

    // БД
    static final String DB_NAME = "mydb";
    static final int DB_VERSION = 1;

    // Таблица
    static final String MARKER_TABLE = "markers";


    // Скрипт создания таблицы
    static final String TABLE_MARKER_CREATE = "create table " + MARKER_TABLE + "("
            + BaseModel.ID + " integer primary key autoincrement, "
            + LocationModel.LAT + " REAL,"
            + LocationModel.LON + " REAL)";


    static final String AUTHORITY = "ru.wheely.wheelytest";

    // path

    // Общий Uri
    public static final Uri WHEELY_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY );

    public static final Uri WHEELY_CONTENT_URI_LOCATION =
            Uri.withAppendedPath(WHEELY_CONTENT_URI,MARKER_TABLE);


    // Типы данных
    // набор строк
    static final String WHEELY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY ;

    // одна строка
    static final String WHEELY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY ;

    static final int URI_MARKER = 1;
    public static final int URI_MARKER_ID = 2;



    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MARKER_TABLE, URI_MARKER);
        uriMatcher.addURI(AUTHORITY, MARKER_TABLE + "/#", URI_MARKER_ID);
    }

    public static final Map<Class,String[]> projection = new HashMap<Class,String[]>(10);

    static{
        String[] auth = {BaseModel.ID, LocationModel.LAT,LocationModel.LON};
        projection.put(BaseModel.class,auth);

    }


    DBHelper dbHelper;
    SQLiteDatabase db;
    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = "";

        switch (uriMatcher.match(uri)) {
            case URI_MARKER: // общий Uri
                table = MARKER_TABLE;
                break;
            case URI_MARKER_ID: // Uri с ID
                String id = uri.getLastPathSegment();
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = URI_MARKER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + URI_MARKER_ID + " = " + id;
                }
                table = MARKER_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(table, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(),
                WHEELY_CONTENT_URI);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_MARKER:
                return WHEELY_CONTENT_TYPE;
            case URI_MARKER_ID:
                return WHEELY_CONTENT_ITEM_TYPE;
        }
        return null;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri resultUri = null;
        db = dbHelper.getWritableDatabase();

        UriMatcher s = uriMatcher;
        int match = uriMatcher.match(uri);
        long rowID = 0;
        switch (match)
        {
            case URI_MARKER:
                rowID = db.insertOrThrow(MARKER_TABLE, null, values);
                resultUri = WHEELY_CONTENT_URI;
                break;
            case URI_MARKER_ID:
                rowID = db.insertOrThrow(MARKER_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(WHEELY_CONTENT_URI, rowID);
                break;
            default:
                throw new IllegalArgumentException("wrong uri");
        }

        // уведомляем ContentResolver, что данные по адресу resultUri изменились


        //getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = "";
        String id;
        switch (uriMatcher.match(uri)) {
            case URI_MARKER:
                table = MARKER_TABLE;
                break;
            case URI_MARKER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseModel.ID + " = " + id;
                } else {
                    selection = selection + " AND " + BaseModel.ID + " = " + id;
                }
                table = MARKER_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(table, selection, selectionArgs);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table = "";
        switch (uriMatcher.match(uri)) {
            case URI_MARKER:
                table = MARKER_TABLE;
                break;
            case URI_MARKER_ID:
                table = MARKER_TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseModel.ID + " = " + id;
                } else {
                    selection = selection + " AND " + BaseModel.ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        int cnt = db.update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(TABLE_MARKER_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}
