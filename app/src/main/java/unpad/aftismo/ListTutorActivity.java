package unpad.aftismo;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import unpad.aftismo.adapter.TutorAdapter;
import unpad.aftismo.model.Tutor;
import unpad.aftismo.retrofit.ApiInterface;
import unpad.aftismo.utils.Common;

public class ListTutorActivity extends AppCompatActivity {

    RecyclerView recyclerSearch;
    ApiInterface mService;
    CompositeDisposable compositeDisposable;
    List<String> suggestList = new ArrayList<>();
    List<Tutor> localDataSource = new ArrayList<>();
    MaterialSearchBar searchBar;

    TutorAdapter searchAdapter, adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tutor);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        compositeDisposable = new CompositeDisposable();
        mService = Common.getApi();

        recyclerSearch = findViewById(R.id.list_tutor);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearch.setHasFixedSize(true);

        searchBar = findViewById(R.id.searchBar);
        
        loadAllTutor();

        searchBar.setCardViewElevation(8);
        /*searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<>();
                for(String search:suggestList){
                    if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled)
                    recyclerSearch.setAdapter(adapter); //Restore all list
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        searchBar.hideSuggestionsList();
    }

    private void startSearch(CharSequence text) {
        List<Tutor> result = new ArrayList<>();

        for(Tutor tutor:localDataSource) {
            if (Pattern.compile(Pattern.quote(text.toString()), Pattern.CASE_INSENSITIVE).matcher(tutor.Lokasi).find())
                result.add(tutor);
        }
        searchAdapter = new TutorAdapter(this, result);
        recyclerSearch.setAdapter(searchAdapter);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadAllTutor() {
        compositeDisposable.add(mService.getAllTutor()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Tutor>>() {
                    @Override
                    public void accept(List<Tutor> tutors) throws Exception {
                        displayTutor(tutors);
                        //buildSuggestList(tutors);
                    }
                }));
    }

    private void buildSuggestList(List<Tutor> tutors) {
        for(Tutor tutor:tutors)
            suggestList.add(tutor.Lokasi);
        searchBar.setLastSuggestions(suggestList);
    }

    private void displayTutor(List<Tutor> tutors) {
        localDataSource = tutors;
        adapter = new TutorAdapter(this, tutors);
        recyclerSearch.setAdapter(adapter);
    }

    //Exit when BACK buton clicked
    boolean isBackButtonClicked = false;

    @Override
    public void onBackPressed() {
        if(isBackButtonClicked) {
            super.onBackPressed();
            return;
        }
        this.isBackButtonClicked = true;
        Toast.makeText(this, "Please click BACK again to exit this application", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostResume() {
        isBackButtonClicked = false;
        super.onResume();
    }
}
