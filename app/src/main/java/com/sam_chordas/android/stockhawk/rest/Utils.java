package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    ContentProviderOperation batchOpr = buildBatchOperation(jsonObject);
                    if( batchOpr != null){
                        batchOperations.add(batchOpr);
                    }
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        if( change != null ){
            String weight = change.substring(0, 1);
            String ampersand = "";
            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format("%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
        }
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        String change = null;
        try {
            change = jsonObject.getString("Change");
            if( change != "null"){
                builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        jsonObject.getString("ChangeinPercent"), true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
                return builder.build();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ContentProviderOperation> historyJsonToContentValues(Context context, String getResponse) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<ContentProviderOperation>();
        JSONObject jsonResponse = null;
        JSONObject jsonQuery = null;
        JSONArray resultsArray = null;

        try {
            jsonResponse = new JSONObject(getResponse);
            if (jsonResponse != null && jsonResponse.length() != 0) {
                jsonQuery = jsonResponse.getJSONObject("query");
                resultsArray = jsonQuery.getJSONObject("results").getJSONArray("quote");
                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        batchOperations.add(buildHistoryBatchOperation(jsonObject));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return batchOperations;

    }

    private static ContentProviderOperation buildHistoryBatchOperation(JSONObject jsonObject) throws JSONException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.QuoteHistory.CONTENT_URI
        );
        builder.withValue(QuoteHistoryColumns.SYMBOL, jsonObject.getString("Symbol"));
        builder.withValue(QuoteHistoryColumns.DATE, jsonObject.getString("Date"));
        builder.withValue(QuoteHistoryColumns.BID_PRICE, truncateBidPrice(jsonObject.getString("Adj_Close")));

        return builder.build();
    }

    public static boolean isNetworkAvailable( Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
