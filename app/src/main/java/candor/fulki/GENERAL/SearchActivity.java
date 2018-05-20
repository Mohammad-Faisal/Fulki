package candor.fulki.GENERAL;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.PROFILE.ListPeopleAdapter;
import candor.fulki.PROFILE.ShowPleopleListActivity;
import candor.fulki.PROFILE.UserBasic;
import candor.fulki.R;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    FirebaseFirestore firebaseFirestore;
    public Query query;
    EditText mSearchBoxText;



    private RecyclerView mPeopleList;
    private LinearLayoutManager mLinearLayout;
    private ListPeopleAdapter mPeopleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        firebaseFirestore = FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle("Search people");
        getSupportActionBar().setHomeButtonEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);

        mSearchBoxText = findViewById(R.id.search_text_input);
        //mSearchBoxText.addTextChangedListener(filterTextWatcher);



        Client client = new Client( "YWTL46QL1P" , "fcdc55274ed56d6fb92f51c0d0fc46a0" );
        Index index = client.getIndex("users");


        mSearchBoxText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Index index = client.getIndex("users");
                Query query = new Query(s.toString())
                        .setAttributesToRetrieve("name", "user_name" , "thumb_image" , "division" , "contact_no" , "blood_group" , "user_id")
                        .setHitsPerPage(10);
                index.searchAsync(query, (content, error) -> {
                    try {
                        JSONArray hits = content.getJSONArray("hits");
                        List < UserBasic > userBasicList = new ArrayList<>();
                        Log.d(TAG, "afterTextChanged:     "+hits.length());
                        for(int i=0;i<hits.length();i++){
                            JSONObject jsonObject = hits.getJSONObject(i);
                            Log.d(TAG, "afterTextChanged:  "+jsonObject.toString());
                            UserBasic userBasic = new UserBasic();
                            userBasic.setmUserName(jsonObject.getString("name"));
                            userBasic.setmUserID(jsonObject.getString("user_id"));
                            userBasic.setmUserThumbImage(jsonObject.getString("thumb_image"));
                            userBasicList.add(userBasic);
                        }

                        Log.d(TAG, "afterTextChanged:    "+userBasicList.size());

                        mPeopleAdapter = new ListPeopleAdapter(userBasicList , SearchActivity.this , SearchActivity.this);
                        mPeopleList = findViewById(R.id.search_activity_recycler);
                        mLinearLayout = new LinearLayoutManager(SearchActivity.this);
                        mPeopleList.hasFixedSize();
                        mPeopleList.setLayoutManager(mLinearLayout);
                        mPeopleList.setAdapter(mPeopleAdapter);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            }









        });



    }





}
