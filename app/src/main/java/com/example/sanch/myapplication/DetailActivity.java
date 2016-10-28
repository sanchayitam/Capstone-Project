package com.example.sanch.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

    if (savedInstanceState == null) {
        Bundle arguments = new Bundle();
        arguments.putString(DetailActivityFragment.DETAIL_VOCAB,
                getIntent().getStringExtra(DetailActivityFragment.DETAIL_VOCAB));
        arguments.putString(DetailActivityFragment.SORT_BY,
                getIntent().getStringExtra(DetailActivityFragment.SORT_BY));

        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vocab_detail_container, fragment,DetailActivityFragment.TAG)
                .commit();
        }
    }
}
