package edu.berkeley.boss;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.boss.BOSSLocClient.LocClientListener;
import edu.berkeley.boss.R;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LocActivity extends Activity implements OnClickListener,
    LocClientListener {
  private Button mReloadButton;
  private TreeViewList mTreeViewList;
  private LocTreeViewAdapter mLocTreeViewAdapter;
  private TreeStateManager<LocNode> mTreeStateManager;
  private TreeBuilder<LocNode> mTreeBuilder;

  private BOSSLocClient mLocClient;
  private SynAmbience mSynAmbience;

  private ProgressDialog mProgressDialog;

  private boolean mActive;
  private boolean mIsLocalizing;

  public static class LocNode {
    int id;
    String semantic;
    String zone;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    mReloadButton = (Button) findViewById(R.id.reload_button);
    mReloadButton.setOnClickListener(this);

    mTreeViewList = (TreeViewList) findViewById(R.id.loc_tree_view_list);
    mTreeStateManager = new InMemoryTreeStateManager<LocNode>();
    mTreeBuilder = new TreeBuilder<LocNode>(mTreeStateManager);

    mLocClient = new BOSSLocClient(this);
    mLocClient.setOnDataReturnedListener(this);

    mSynAmbience = new SynAmbience(this);

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getResources().getString(
        R.string.progess_message));
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.reload_button:
      if (mIsLocalizing == false) {
        mProgressDialog.show();
        mIsLocalizing = true;

        final JSONObject synAmbiencePack = mSynAmbience.get();
        mLocClient.getLocation(synAmbiencePack);
      }
      break;
    }
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    if (locInfo != null && mActive == true) {
      try {
        final JSONObject loc = locInfo.getJSONObject("location");

        mTreeBuilder.clear();
        final int depth = buildTree(null, loc);
        mLocTreeViewAdapter = new LocTreeViewAdapter(this, mTreeStateManager,
            depth);
        mTreeViewList.setAdapter(mLocTreeViewAdapter);
        mTreeStateManager.refresh();

      } catch (JSONException e) {
        // TODO Auto-generated catch block
      }
    }
    mIsLocalizing = false;
    mProgressDialog.dismiss();
  }

  private int buildTree(final LocNode parent, final JSONObject loc) {

    int depth = 0;

    if (loc == null || loc.length() == 0) {
      return depth;
    } else {
      try {
        final Iterator<?> iter = loc.keys();
        while (iter.hasNext()) {
          String locItemStr = (String) iter.next();

          JSONArray locItem = new JSONArray(locItemStr);
          LocNode locNode = new LocNode();
          locNode.id = mTreeStateManager.getAllNodesCount() + 1;
          locNode.semantic = locItem.getString(0);
          locNode.zone = locItem.getString(1);
          mTreeBuilder.addRelation(parent, locNode);

          JSONObject subLocInfo = loc.getJSONObject(locItemStr);
          int tmpDepth = buildTree(locNode, subLocInfo);
          if (tmpDepth > depth) {
            depth = tmpDepth;
          }
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return depth + 1;
    }
  }

  @Override
  public void onMetadataReturned(JSONObject metadata) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onMapReturned(Bitmap bitmap) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onResume() {
    super.onResume();

    mActive = true;
    mIsLocalizing = false;

    mSynAmbience.resume();
  }

  @Override
  protected void onPause() {
    // TODO deal with all rotation issues
    super.onPause();

    mActive = false;

    mProgressDialog.dismiss();

    mSynAmbience.pause();
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
