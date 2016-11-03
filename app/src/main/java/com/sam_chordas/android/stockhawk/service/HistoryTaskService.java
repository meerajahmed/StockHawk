package com.sam_chordas.android.stockhawk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.StringBuilderPrinter;

import com.facebook.stetho.inspector.protocol.module.Network;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class HistoryTaskService extends GcmTaskService {

    public static final String EXTRA_SYMBOL = "com.sam_chordas.android.stockhawk.service.extra.SYMBOL";
    public static final String EXTRA_START_DATE = "com.sam_chordas.android.stockhawk.service.extra.START_DATE";
    public static final String EXTRA_END_DATE = "com.sam_chordas.android.stockhawk.service.extra.END_DATE";

    private Context mContext;
    private OkHttpClient client = new OkHttpClient();

    public HistoryTaskService() {
    }

    public HistoryTaskService(Context context) {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        final String symbol = taskParams.getExtras().getString(EXTRA_SYMBOL);
        final String startDate = taskParams.getExtras().getString(EXTRA_START_DATE);
        final String endDate = taskParams.getExtras().getString(EXTRA_END_DATE);

        final String baseUrl = "https://query.yahooapis.com/v1/public/yql?q=";

        final String query = "select * from yahoo.finance.historicaldata where symbol=\"" +
                symbol + "\" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"";

        final String returnFormat = "&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys&callback=";

        StringBuilder urlStr = new StringBuilder();

        try {
            urlStr.append(baseUrl);
            urlStr.append(URLEncoder.encode(query, "UTF-8"));
            urlStr.append(returnFormat);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = urlStr.toString();

        int result = GcmNetworkManager.RESULT_FAILURE;

        String getResponse = null;
        try {
            getResponse = fetchData(url);
            result = GcmNetworkManager.RESULT_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mContext == null) {
            mContext = this;
        }

        mContext.getContentResolver()
                .delete(QuoteProvider.QuoteHistory.withSymbol(symbol), null, null);

        try {
            mContext.getContentResolver()
                    .applyBatch(QuoteProvider.AUTHORITY, Utils.historyJsonToContentValues(mContext, getResponse));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String fetchData(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        client.networkInterceptors().add(new StethoInterceptor());
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
