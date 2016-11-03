package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;

public class StockDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = StockDetailActivity.class.getSimpleName();

    public static final String EXTRA_SYMBOL = "SYMBOL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        if (savedInstanceState == null) {
            Log.v(LOG_TAG, getIntent().getStringExtra(StockDetailActivity.EXTRA_SYMBOL));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_container, StockDetailFragment.newInstance(getIntent().getStringExtra(StockDetailActivity.EXTRA_SYMBOL)))
                    .commit();
        }

    }
}
