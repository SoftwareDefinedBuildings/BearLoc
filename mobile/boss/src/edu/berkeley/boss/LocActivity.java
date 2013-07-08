package edu.berkeley.boss;

import edu.berkeley.boss.R;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class LocActivity extends Activity implements OnClickListener {
  private static final int[] DEMO_NODES = new int[] { 0, 0, 1, 1, 1, 2, 2, 1,
      1, 2, 1, 0, 0, 0, 1, 2, 3, 2, 0, 0, 1, 2, 0, 1, 2, 0, 1 };
  private static final int LEVEL_NUMBER = 4;

  private TreeViewList mTreeViewList;
  private TreeStateManager<Long> mTreeStateManager;
  private LocTreeViewAdapter mLocTreeViewAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    mTreeViewList = (TreeViewList) findViewById(R.id.loc_tree_view_list);

    mTreeStateManager = new InMemoryTreeStateManager<Long>();
    final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(
        mTreeStateManager);
    for (int i = 0; i < DEMO_NODES.length; i++) {
      treeBuilder.sequentiallyAddNextNode((long) i, DEMO_NODES[i]);
    }
    mLocTreeViewAdapter = new LocTreeViewAdapter(this, mTreeStateManager,
        LEVEL_NUMBER);
    mTreeViewList.setAdapter(mLocTreeViewAdapter);
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    
  }
}

/*
 * import org.json.JSONException; import org.json.JSONObject;
 * 
 * import edu.berkeley.boss.R;
 * 
 * import edu.berkeley.boss.BOSSLocClient.LocClientListener; import
 * android.app.Activity; import android.graphics.Bitmap; import
 * android.os.Bundle; import android.os.Handler; import android.view.View;
 * import android.widget.AdapterView; import
 * android.widget.AdapterView.OnItemSelectedListener; import
 * android.widget.ArrayAdapter; import android.widget.Spinner;
 * 
 * public class LocActivity extends Activity implements OnItemSelectedListener,
 * LocClientListener {
 * 
 * private static final long LOC_ITVL = 2000L; // millisecond
 * 
 * private Spinner mSemSpinner; private MapImageView mMapImageView;
 * 
 * private BOSSLocClient mLocClient; private SynAmbience mSynAmbience;
 * 
 * private JSONObject mLatestLocInfo; private JSONObject latestMetadata; private
 * String mCurSemantic;
 * 
 * private Handler mHandler; private final Runnable mLocalizationTimeTask = new
 * Runnable() { public void run() { final JSONObject synAmbiencePack =
 * mSynAmbience.get(); mLocClient.getLocation(synAmbiencePack); } };
 * 
 * private boolean mActive;
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState); setContentView(R.layout.loc);
 * 
 * mSemSpinner = (Spinner) findViewById(R.id.sem_spinner); mMapImageView =
 * (MapImageView) findViewById(R.id.map_view);
 * 
 * mLocClient = new BOSSLocClient(this); mSynAmbience = new SynAmbience(this);
 * 
 * mHandler = new Handler();
 * 
 * ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 * R.array.semantics, android.R.layout.simple_spinner_item); adapter
 * .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 * mSemSpinner.setAdapter(adapter);
 * 
 * mSemSpinner.setOnItemSelectedListener(this);
 * mLocClient.setOnDataReturnedListener(this); }
 * 
 * @Override protected void onResume() { super.onResume();
 * 
 * mActive = true;
 * 
 * mSynAmbience.resume(); mHandler.postDelayed(mLocalizationTimeTask, 0); }
 * 
 * @Override protected void onPause() { // TODO deal with all rotation issues
 * super.onPause();
 * 
 * mActive = false;
 * 
 * mSynAmbience.pause(); mHandler.removeCallbacks(mLocalizationTimeTask); }
 * 
 * @Override public void onItemSelected(AdapterView<?> parent, View view, int
 * pos, long id) { final String newSemantic =
 * parent.getItemAtPosition(pos).toString(); onSemanticChanged(newSemantic); }
 * 
 * @Override public void onNothingSelected(AdapterView<?> parent) { // TODO
 * Auto-generated method stub }
 * 
 * private void onSemanticChanged(String newSemantic) { if ((mCurSemantic ==
 * null) || !(mCurSemantic.equals(newSemantic))) { mCurSemantic = newSemantic;
 * drawZones(mCurSemantic); } }
 * 
 * @Override public void onLocationReturned(JSONObject locInfo) { if (mActive ==
 * true) { if (locInfo != null && (mLatestLocInfo == null ||
 * !locInfo.toString().equals( mLatestLocInfo.toString()))) { mLatestLocInfo =
 * locInfo; try { JSONObject loc = locInfo.getJSONObject("location");
 * mLocClient.getMetadata(loc, "floor"); } catch (JSONException e) { // TODO
 * Auto-generated catch block } }
 * 
 * mHandler.postDelayed(mLocalizationTimeTask, LOC_ITVL); } }
 * 
 * @Override public void onMetadataReturned(JSONObject metadata) { if (mActive
 * == true) { if (metadata != null && (latestMetadata == null ||
 * !metadata.toString().equals( latestMetadata.toString()))) { latestMetadata =
 * metadata; mLocClient.getMap(metadata); } } }
 * 
 * @Override public void onMapReturned(Bitmap bitmap) { if (bitmap == null) {
 * mCurSemantic = null; } else { mMapImageView.setMap(bitmap); } }
 * 
 * private void drawZones(String semantic) {
 * 
 * }
 * 
 * }
 */
