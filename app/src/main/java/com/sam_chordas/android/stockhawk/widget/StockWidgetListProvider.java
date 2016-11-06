package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by mahme4 on 11/5/2016.
 */
public class StockWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor cursor;
    private String[] projection = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.ISUP
    };

    public StockWidgetListProvider(Context applicationContext, Intent intent) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        long identityToken = Binder.clearCallingIdentity();
        cursor = mContext.getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                projection,
                QuoteColumns.ISCURRENT + " = ? ",
                new String[]{"1"},
                null);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if( position == AdapterView.INVALID_POSITION || cursor == null ||
                !cursor.moveToPosition(position)){
             return null;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_list_item);
        String symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
        views.setTextViewText(R.id.stock_symbol, symbol);
        views.setTextViewText(R.id.stock_change, cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
        views.setOnClickFillInIntent(R.id.stock_widget,
                new Intent().setData(QuoteProvider.Quotes.withSymbol(symbol)));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
       return new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if( cursor.moveToPosition(position) ){
            return cursor.getLong(cursor.getColumnIndex(QuoteColumns._ID));
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
