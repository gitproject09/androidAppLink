/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sopan.app_link.content_provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.sopan.app_link.database.FoodIngredientTable;
import com.sopan.app_link.database.FoodInstructionsTable;
import com.sopan.app_link.database.FoodTable;

/**
 * Created by Sopan on 10/21/14.
 */
public class FoodContentProvider extends ContentProvider {

    // database
    private RecipeDatabaseHelper database;

    // used for the UriMacher
    private static final int RECIPES = 10;
    private static final int RECIPE_ID = 20;
    private static final int RECIPE_INGREDIENTS = 30;
    private static final int RECIPE_INSTRUCTIONS = 40;

    private static final String AUTHORITY = "com.sopan.app_link";

    private static final String BASE_PATH = "recipe";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, RECIPES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/ingredients/*", RECIPE_INGREDIENTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/instructions/*", RECIPE_INSTRUCTIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", RECIPE_ID);
    }

    @Override
    public boolean onCreate() {
        database = new RecipeDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = sURIMatcher.match(uri);
        if (uriType == RECIPES) {
        } else if (uriType == RECIPE_ID) {
            return getRecipe(uri);
        } else if (uriType == RECIPE_INGREDIENTS) {
            return getIngredientsByRecipe(uri);
        } else if (uriType == RECIPE_INSTRUCTIONS) {
            return getInstructionsByRecipe(uri);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return null;
    }

    public Cursor getRecipe(Uri uri) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FoodTable.TABLE);
        String[] projection = { FoodTable.ID, FoodTable.TITLE,
                FoodTable.DESCRIPTION, FoodTable.PHOTO,
                FoodTable.PREP_TIME};
        SQLiteDatabase db = database.getReadableDatabase();
        queryBuilder.appendWhere(FoodTable.ID + "='"
                + uri.getLastPathSegment() + "'");
        Cursor cursor = queryBuilder.query(db, projection, null,
                null, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    public Cursor getIngredientsByRecipe(Uri uri) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FoodTable.TABLE + ", " + FoodIngredientTable.TABLE);
        queryBuilder.appendWhere(FoodTable.ID + "='" + uri.getLastPathSegment() + "' AND " + FoodIngredientTable.RECIPE_ID + "=" + FoodTable.ID + "");
        String[] projection = {FoodIngredientTable.AMOUNT, FoodIngredientTable.DESCRIPTION};
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, null, null, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public Cursor getInstructionsByRecipe(Uri uri) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FoodTable.TABLE + ", " + FoodInstructionsTable.TABLE);
        queryBuilder.appendWhere(FoodTable.ID + "='" + uri.getLastPathSegment() + "' AND " + FoodInstructionsTable.RECIPE_ID + "=" + FoodTable.ID + "");
        String[] projection = {FoodInstructionsTable.NUM, FoodInstructionsTable.DESCRIPTION, FoodInstructionsTable.PHOTO};
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, null, null, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return BASE_PATH;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * This helper loads the SQLite database included with the app
     * in the assets folder.
     */
    public class RecipeDatabaseHelper extends SQLiteAssetHelper {

        private static final String DATABASE_NAME = "recipes.db";
        private static final int DATABASE_VERSION = 1;

        public RecipeDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    }
}
