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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class LocActivity extends Activity implements View.OnClickListener,
    DialogInterface.OnClickListener, LocClientListener {
  private Button mReloadButton;
  private Button mReportButton;
  private TreeViewList mTreeViewList;
  private LocTreeViewAdapter mLocTreeViewAdapter;
  private TreeStateManager<LocNode> mTreeStateManager;
  private TreeBuilder<LocNode> mTreeBuilder;

  private BOSSLocClient mLocClient;
  private SynAmbience mSynAmbience;

  private ProgressDialog mProgressDialog;

  private JSONObject mCurLocInfo;

  private static enum State {
    PAUSED, IDLE, LOCALIZE, REPORT, CHANGE_LOC, MAP_VIEW
  }

  private State mState;

  public static class LocNode {
    int id;
    String semantic;
    String zone;
    JSONArray semArray;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    mReloadButton = (Button) findViewById(R.id.reload_button);
    mReloadButton.setOnClickListener(this);

    mReportButton = (Button) findViewById(R.id.report_button);
    mReportButton.setOnClickListener(this);

    mTreeViewList = (TreeViewList) findViewById(R.id.loc_tree_view_list);
    mTreeStateManager = new InMemoryTreeStateManager<LocNode>();
    mTreeBuilder = new TreeBuilder<LocNode>(mTreeStateManager);
    registerForContextMenu(mTreeViewList);

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
      if (mState == State.IDLE) {
        mState = State.LOCALIZE;

        final JSONObject synAmbiencePack = mSynAmbience.get();
        mLocClient.getLocation(synAmbiencePack);

        mProgressDialog.show();
      }
      break;
    case R.id.report_button:
      if (mState == State.IDLE) {
        mState = State.REPORT;

        final JSONObject synAmbiencePack = mSynAmbience.get();
        // TODO get curLocInfo from adapater
        mLocClient.reportLocation(synAmbiencePack, mCurLocInfo);

        mProgressDialog.show();
      }
      break;
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    if (mState == State.LOCALIZE) {
      mProgressDialog.dismiss();

      if (locInfo != null) {
        mCurLocInfo = locInfo;
        try {
          final JSONObject loc = locInfo.getJSONObject("location");

          mTreeBuilder.clear();
          final int depth = buildTree(null, loc, new JSONArray());
          mLocTreeViewAdapter = new LocTreeViewAdapter(this, mTreeStateManager,
              depth);
          mTreeViewList.setAdapter(mLocTreeViewAdapter);
          mTreeStateManager.refresh();

        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      mState = State.IDLE;
    } else {
      Log.e(this.toString(), "Location returned on non-LOCALIZE state.");
    }
  }

  private int buildTree(final LocNode parent, final JSONObject loc,
      final JSONArray semArray) {

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

          // Do not change semArray itself
          final JSONArray curSemArray = new JSONArray(semArray.toString());
          curSemArray.put(locItem);
          locNode.semArray = curSemArray;
          mTreeBuilder.addRelation(parent, locNode);

          final JSONObject subLocInfo = loc.getJSONObject(locItemStr);
          int tmpDepth = buildTree(locNode, subLocInfo, curSemArray);
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
  public void onReportDone(boolean error) {
    if (mState == State.REPORT) {
      mProgressDialog.dismiss();

      // TODO implement

      mState = State.IDLE;
    } else {
      Log.e(this.toString(), "Report done on non-REPORT state.");
    }
  }

  @Override
  public void onMetadataReturned(JSONObject metadata) {
    if (mState == State.CHANGE_LOC) {
      final JSONArray zoneJSONArray = metadata.names();
      final int length = zoneJSONArray.length();
      final String[] zoneList = new String[length];
      for (int i = 0; i < length; i++) {
        try {
          zoneList[i] = zoneJSONArray.getString(i);
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Choose Zone");
      builder.setItems(zoneList, this);
      AlertDialog alert = builder.create();
      alert.show();
    } else if (mState == State.MAP_VIEW) {
      // TODO implement
    }
  }

  @Override
  public void onMapReturned(Bitmap bitmap) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v,
      final ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.context_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
        .getMenuInfo();
    switch (item.getItemId()) {
    case R.id.context_menu_change:
      if (mState == State.IDLE) {
        mState = State.CHANGE_LOC;

        try {
          final JSONObject loc = mCurLocInfo.getJSONObject("location");
          final LocNode nodeInfo = mLocTreeViewAdapter.getItem(info.position);
          mLocClient.getMetadata(loc, nodeInfo.semArray);
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        mProgressDialog.show();
      }
      return true;
    case R.id.context_menu_map_view:
      if (mState == State.IDLE) {
        mState = State.MAP_VIEW;

        // TODO implement

        mProgressDialog.show();

        mProgressDialog.dismiss();
        mState = State.IDLE;
      }
      return true;
    default:
      return super.onContextItemSelected(item);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    mState = State.IDLE;

    mSynAmbience.resume();
  }

  @Override
  protected void onPause() {
    // TODO deal with all rotation issues
    super.onPause();

    mState = State.PAUSED;

    mProgressDialog.dismiss();

    mSynAmbience.pause();
  }
}
