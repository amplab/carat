package edu.berkeley.cs.amplab.carat.android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import edu.berkeley.cs.amplab.carat.android.suggestions.*;

public class CaratSuggestionsActivity extends ListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestions);

		//setListAdapter(new ArrayAdapter<String>(this, R.layout.listitem2,
		//		R.id.list_content, COUNTRIES));

		
		//lv.setTextFilterEnabled(true);
		
		final ListView lv = getListView();
		lv.setCacheColorHint(0);
	
	   ArrayList<Suggestion> searchResults = getSuggestions();
       
       lv.setAdapter(new SuggestionAdapter(this, searchResults));
      
       lv.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> a, View v, int position, long id) {
         Object o = lv.getItemAtPosition(position);
         Suggestion fullObject = (Suggestion)o;
         Toast.makeText(CaratSuggestionsActivity.this, "You have chosen: " + " " + fullObject.getName(), Toast.LENGTH_LONG).show();
         Intent myIntent = new Intent(v.getContext(), CaratKillAppActivity.class);
		 //findViewById(R.id.scrollView1).startAnimation(CaratMainActivity.outtoLeft);
         startActivityForResult(myIntent, 0);
        } 
       });
       
       lv.setOnTouchListener(SwipeListener.instance);
   }
  
   private ArrayList<Suggestion> getSuggestions(){
    ArrayList<Suggestion> results = new ArrayList<Suggestion>();
    
    Suggestion sr1 = new Suggestion();
    sr1.setName("John Smith");
    sr1.setBenefit("1d 17h 30m 8s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Jane Doe");
    sr1.setBenefit("1d 5h 22m 6s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Steve Young");
    sr1.setBenefit("12h 54m 12s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Fred Jones");
    sr1.setBenefit("8h 31m 6s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Facebook");
    sr1.setBenefit("3h 31m 6s");
   
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Twitter");
    sr1.setBenefit("1h 10m 6s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Angry Birds");
    sr1.setBenefit("1h 2m");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Microsoft");
    sr1.setBenefit("1h 0m 1s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Google");
    sr1.setBenefit("0h 54m 10s");
    results.add(sr1);
    
    sr1 = new Suggestion();
    sr1.setName("Amazon");
    sr1.setBenefit("0h 46m 12s");
    results.add(sr1);
    
    return results;
   }
}
