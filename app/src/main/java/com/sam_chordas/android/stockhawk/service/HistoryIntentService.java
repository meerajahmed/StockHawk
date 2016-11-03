package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.TaskParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class HistoryIntentService extends IntentService {

    private static final String ACTION_FETCH_DATA = "com.sam_chordas.android.stockhawk.service.action.FETCH_DATA";

    private static final String EXTRA_SYMBOL = "com.sam_chordas.android.stockhawk.service.extra.SYMBOL";

    public HistoryIntentService() {
        super(HistoryIntentService.class.getSimpleName());
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchData(Context context, String symbol) {
        Intent intent = new Intent(context, HistoryIntentService.class);
        intent.setAction(ACTION_FETCH_DATA);
        intent.putExtra(EXTRA_SYMBOL, symbol);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_DATA.equals(action)) {
                final String symbol = intent.getStringExtra(EXTRA_SYMBOL);
                handleActionFetchData(symbol);
            }
        }
    }

    /**
     * Handle action Fetch Data in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchData(String symbol) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(currentDate);
        calStart.add(Calendar.MONTH, -1);
        String startDate = dateFormat.format(calStart.getTime());

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(currentDate);
        calEnd.add(Calendar.DATE, 0);
        String endDate = dateFormat.format(calEnd.getTime());

        Bundle args = new Bundle();
        args.putString(HistoryTaskService.EXTRA_SYMBOL, symbol);
        args.putString(HistoryTaskService.EXTRA_START_DATE, startDate);
        args.putString(HistoryTaskService.EXTRA_END_DATE, endDate);

        HistoryTaskService historyTaskService = new HistoryTaskService(this);
        historyTaskService.onRunTask(new TaskParams("args", args));


    }
}
