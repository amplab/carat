package edu.berkeley.cs.amplab.carat.android;

import java.util.ArrayList;

import edu.berkeley.cs.amplab.carat.android.suggestions.Hog;
import edu.berkeley.cs.amplab.carat.android.suggestions.HogAdapter;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CaratHogsActivity extends ListActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestions);

		//setListAdapter(new ArrayAdapter<String>(this, R.layout.listitem2,
		//		R.id.list_content, COUNTRIES));

		
		//lv.setTextFilterEnabled(true);
		
		final ListView lv = getListView();
		lv.setCacheColorHint(0);
	
	   ArrayList<Hog> searchResults = getHogs();
       
       lv.setAdapter(new HogAdapter(this, searchResults));
      
       lv.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> a, View v, int position, long id) {
         Object o = lv.getItemAtPosition(position);
         Hog fullObject = (Hog)o;
         Toast.makeText(CaratHogsActivity.this, "You have chosen: " + " " + fullObject.getName(), Toast.LENGTH_LONG).show();
        } 
       });
       
       lv.setOnTouchListener(SwipeListener.instance);
   }
  
   private ArrayList<Hog> getHogs(){
    ArrayList<Hog> results = new ArrayList<Hog>();
    
    Hog sr1 = new Hog();
    sr1.setName("John Smith");
    sr1.setConfidence(0.91);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Jane Doe");
    sr1.setConfidence(0.89);
    sr1.setIconResource(R.drawable.about);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Steve Young");
    sr1.setConfidence(0.79);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Fred Jones");
    sr1.setConfidence(0.73);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Facebook");
    sr1.setConfidence(0.69);
   
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Twitter");
    sr1.setConfidence(0.60);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Angry Birds");
    sr1.setConfidence(0.56);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Microsoft");
    sr1.setConfidence(0.4);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Google");
    sr1.setConfidence(0.3);
    results.add(sr1);
    
    sr1 = new Hog();
    sr1.setName("Amazon");
    sr1.setConfidence(0.2);
    results.add(sr1);
    
    return results;
   }
}
