package com.klinker.android.twitter.data.sq_lite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.klinker.android.twitter.utils.HtmlUtils;

import twitter4j.Status;

public class MentionsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MentionsSQLiteHelper dbHelper;
    public String[] allColumns = {MentionsSQLiteHelper.COLUMN_ID, MentionsSQLiteHelper.COLUMN_UNREAD, MentionsSQLiteHelper.COLUMN_TWEET_ID, MentionsSQLiteHelper.COLUMN_ACCOUNT, MentionsSQLiteHelper.COLUMN_TYPE,
            MentionsSQLiteHelper.COLUMN_TEXT, MentionsSQLiteHelper.COLUMN_NAME, MentionsSQLiteHelper.COLUMN_PRO_PIC,
            MentionsSQLiteHelper.COLUMN_SCREEN_NAME, MentionsSQLiteHelper.COLUMN_TIME, MentionsSQLiteHelper.COLUMN_PIC_URL,
            MentionsSQLiteHelper.COLUMN_RETWEETER, MentionsSQLiteHelper.COLUMN_URL, HomeSQLiteHelper.COLUMN_USERS, HomeSQLiteHelper.COLUMN_HASHTAGS };

    public MentionsDataSource(Context context) {
        dbHelper = new MentionsSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createTweet(Status status, int account, boolean initial) {
        ContentValues values = new ContentValues();
        String originalName = "";
        long id = status.getId();
        long time = status.getCreatedAt().getTime();

        String[] html = HtmlUtils.getHtmlStatus(status);
        String text = html[0];
        String media = html[1];
        String otherUrl = html[2];
        String hashtags = html[3];
        String users = html[4];

        values.put(MentionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(MentionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(MentionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(MentionsSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(MentionsSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getBiggerProfileImageURL());
        values.put(MentionsSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(MentionsSQLiteHelper.COLUMN_TIME, time);
        values.put(MentionsSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(MentionsSQLiteHelper.COLUMN_UNREAD, 0);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_URL, otherUrl);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_USERS, users);
        values.put(MentionsSQLiteHelper.COLUMN_HASHTAGS, hashtags);

        database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
    }

    public void createTweet(Status status, int account) {
        ContentValues values = new ContentValues();
        String originalName = "";
        long id = status.getId();
        long time = status.getCreatedAt().getTime();

        String[] html = HtmlUtils.getHtmlStatus(status);
        String text = html[0];
        String media = html[1];
        String otherUrl = html[2];
        String hashtags = html[3];
        String users = html[4];

        values.put(MentionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(MentionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(MentionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(MentionsSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(MentionsSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getBiggerProfileImageURL());
        values.put(MentionsSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(MentionsSQLiteHelper.COLUMN_TIME, time);
        values.put(MentionsSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(MentionsSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_URL, otherUrl);
        values.put(MentionsSQLiteHelper.COLUMN_USERS, users);
        values.put(MentionsSQLiteHelper.COLUMN_HASHTAGS, hashtags);

        database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
    }

    public void deleteTweet(long tweetId) {
        long id = tweetId;
        database.delete(MentionsSQLiteHelper.TABLE_MENTIONS, MentionsSQLiteHelper.COLUMN_TWEET_ID
                + " = " + id, null);
    }

    public void deleteAllTweets(int account) {
        database.delete(MentionsSQLiteHelper.TABLE_MENTIONS,
                MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
    }

    public Cursor getCursor(int account) {
        if (database == null) {
            open();
        }
        Cursor cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                allColumns, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");

        return cursor;
    }

    public Cursor getUnreadCursor(int account) {

        Cursor cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                allColumns, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + MentionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"}, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");

        return cursor;
    }

    public int getUnreadCount(int account) {

        Cursor cursor = getUnreadCursor(account);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public void markRead(int account, int position) {
        Cursor cursor = getUnreadCursor(account);

        if (cursor.moveToPosition(position)) {
            long tweetId = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));

            ContentValues cv = new ContentValues();
            cv.put(MentionsSQLiteHelper.COLUMN_UNREAD, 0);

            database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        }

        cursor.close();
    }

    // true is unread
    // false have been read
    public void markMultipleRead(int current, int account) {

        Cursor cursor = getUnreadCursor(account);

        try {
            if (cursor.moveToPosition(current)) {
                do {

                    long tweetId = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));

                    ContentValues cv = new ContentValues();
                    cv.put(HomeSQLiteHelper.COLUMN_UNREAD, 0);

                    database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // there is nothing in the unread array
        }

        cursor.close();
    }

    public void markAllRead(int account) {

        ContentValues cv = new ContentValues();
        cv.put(MentionsSQLiteHelper.COLUMN_UNREAD, 0);

        database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + MentionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
    }

    public String getNewestName(int account) {

        Cursor cursor = getUnreadCursor(account);
        String name = "";

        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_SCREEN_NAME));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return name;
    }

    public String getNewestMessage(int account) {

        Cursor cursor = getUnreadCursor(account);
        String message = "";

        try {
            if (cursor.moveToFirst()) {
                message = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return message;
    }

    public long[] getLastIds(int account) {
        long[] ids = new long[] {0, 0};

        Cursor cursor = getCursor(account);

        try {
            if (cursor.moveToLast()) {
                ids[0] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
            }

            if (cursor.moveToPrevious()) {
                ids[1] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return ids;
    }

    public boolean tweetExists(long tweetId, int account) {
        Cursor cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                allColumns, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + MentionsSQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");

        return cursor.getCount() > 0;
    }
}
