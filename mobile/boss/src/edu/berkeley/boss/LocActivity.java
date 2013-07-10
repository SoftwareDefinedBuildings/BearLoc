package edu.berkeley.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class LocActivity extends Activity implements View.OnClickListener,
    DialogInterface.OnClickListener, DialogInterface.OnCancelListener,
    LocClientListener {
  private Button mReloadButton;
  private Button mReportButton;
  private TreeViewList mTreeViewList;
  private LocTreeViewAdapter mLocTreeViewAdapter;
  private TreeStateManager<LocNode> mTreeStateManager;
  private TreeBuilder<LocNode> mTreeBuilder;

  private BOSSLocClient mLocClient;
  private SynAmbience mSynAmbience;

  private ProgressDialog mProgressDialog;
  private AlertDialog.Builder mDialogBuilder;
  private AlertDialog mSelectDialog;
  // TODO embed mSelectItems with self-defined Adpater
  private List<String> mSelectItems;
  private ArrayAdapter<String> mSelectAdapter;
  private JSONArray mCurSemTarget;

  // TODO embed mCurLoc with LocTreeViewAdpater
  private JSONObject mCurLoc;

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
    mProgressDialog.setOnCancelListener(this);

    mDialogBuilder = new AlertDialog.Builder(this);
    mDialogBuilder.setTitle("Choose Zone");
    mDialogBuilder.setOnCancelListener(this);

    mSelectItems = new ArrayList<String>();
    mSelectAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, mSelectItems);
    mDialogBuilder.setAdapter(mSelectAdapter, this);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.reload_button:
      if (mState == State.IDLE) {
        mState = State.LOCALIZE;
        mProgressDialog.show();

        final JSONObject synAmbiencePack = mSynAmbience.get();
        mLocClient.getLocation(synAmbiencePack);
      }
      break;
    case R.id.report_button:
      if (mState == State.IDLE) {
        mState = State.REPORT;
        mProgressDialog.show();

        final JSONObject synAmbiencePack = mSynAmbience.get();
        // TODO get curLocInfo from adapter
        mLocClient.reportLocation(synAmbiencePack, mCurLoc);
      }
      break;
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (dialog == mSelectDialog) {
      if (mState == State.CHANGE_LOC) {
        final String newZone = mSelectItems.get(which);

        changeLocation(mCurLoc, mCurSemTarget, 0, newZone);

        mState = State.IDLE;
      } else {
        Log.e(this.toString(), "Dialog clicked on non-CHANGE_LOC state.");
      }
    }
  }

  private void changeLocation(final JSONObject loc, final JSONArray semTarget,
      final int semTargetIdx, final String newZone) {
    try {
      if (semTarget.get(semTargetIdx) instanceof String) {
        final String semantic = semTarget.getString(semTargetIdx);

        // Remove old location item
        final Iterator<?> iter = loc.keys();
        while (iter.hasNext()) {
          String locItemStr = (String) iter.next();

          // Every location item is a String of JSONArray formated as
          // "(semantic, zone)"
          JSONArray locItem = new JSONArray(locItemStr);
          if (locItem.getString(0).equals(semantic)) {
            loc.remove(locItemStr);
            break;
          }
        }

        // add new location item
        final JSONArray newLocItem = new JSONArray();
        newLocItem.put(semantic);
        newLocItem.put(newZone);
        loc.put(newLocItem.toString(), new JSONObject());
      } else {

        final String semantic = semTarget.getJSONArray(semTargetIdx).getString(
            0);
        final String zone = semTarget.getJSONArray(semTargetIdx).getString(1);

        final Iterator<?> iter = loc.keys();
        while (iter.hasNext()) {
          String locItemStr = (String) iter.next();

          // Every location item is a String of JSONArray formated as
          // "(semantic, zone)"
          JSONArray locItem = new JSONArray(locItemStr);
          if (locItem.getString(0).equals(semantic)
              && locItem.getString(1).equals(zone)) {
            changeLocation(loc.getJSONObject(locItemStr), semTarget,
                semTargetIdx + 1, newZone);
            break;
          }
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    onLocationChanged();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (dialog == mProgressDialog) {
      if (mState == State.LOCALIZE || mState == State.REPORT
          || mState == State.CHANGE_LOC) {
        mState = State.IDLE;
      } else {
        Log.e(this.toString(),
            "Dialog canceled on non-LOCALIZE, non-REPORT, and non-CHANGE_LOC state.");
      }
    } else if (dialog == mSelectDialog) {
      if (mState == State.CHANGE_LOC) {
        mState = State.IDLE;
      } else {
        Log.e(this.toString(), "Dialog canceled on non-CHANGE_LOC state.");
      }
    }
  }

  @Override
  public void onLocationReturned(final JSONObject locInfo) {
    if (mState == State.LOCALIZE) {
      if (locInfo != null) {
        try {
          mCurLoc = locInfo.getJSONObject("location");

          // TODO Ideally it should be implemented in adapter?
          onLocationChanged();

        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      mProgressDialog.dismiss();
      mState = State.IDLE;
    } else {
      Log.e(this.toString(), "Location returned on non-LOCALIZE state.");
    }
  }

  // TODO maybe there is properer palce to hold this function, like inside an
  // adapter
  private void onLocationChanged() {
    mTreeBuilder.clear();
    final int depth = buildTree(null, mCurLoc, new JSONArray());
    mLocTreeViewAdapter = new LocTreeViewAdapter(this, mTreeStateManager, depth);
    mTreeViewList.setAdapter(mLocTreeViewAdapter);
    mTreeStateManager.refresh();
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

          // Every location item is a String of JSONArray formated as
          // "(semantic, zone)"
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
  public void onReportDone(final boolean success) {
    if (mState == State.REPORT) {
      mProgressDialog.dismiss();

      mState = State.IDLE;
    } else {
      Log.e(this.toString(), "Report done on non-REPORT state.");
    }
  }

  @Override
  public void onMetadataReturned(final JSONObject metadata) {
    if (mState == State.CHANGE_LOC) {
      if (metadata != null) {
        try {
          final String semantic = mCurSemTarget.getString(mCurSemTarget
              .length() - 1);
          final JSONArray zoneJSONArray = metadata.getJSONObject("child")
              .getJSONObject(semantic).names();
          final int length = zoneJSONArray.length();
          mSelectItems.clear();
          for (int i = 0; i < length; i++) {
            mSelectItems.add(zoneJSONArray.getString(i));
          }
          Collections.sort(mSelectItems);

          mSelectAdapter.notifyDataSetChanged();
          mSelectDialog = mDialogBuilder.create();

          mProgressDialog.dismiss();
          mSelectDialog.show();
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } else if (mState == State.MAP_VIEW) {
      // TODO implement
    }
  }

  @Override
  public void onMapReturned(final Bitmap bitmap) {
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
        mProgressDialog.show();

        final LocNode nodeInfo = mLocTreeViewAdapter.getItem(info.position);
        try {
          // target format: [[Sem1, Zone1], ..., [SemN, ZoneN], TargetSem]
          final JSONArray target = nodeInfo.semArray;
          final int lastIdx = target.length() - 1;
          String targetSem = target.getJSONArray(lastIdx).getString(0);
          target.put(lastIdx, targetSem);
          mLocClient.getMetadata(target);
          mCurSemTarget = target;
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      return true;
    case R.id.context_menu_map_view:
      if (mState == State.IDLE) {
        mState = State.MAP_VIEW;
        mProgressDialog.show();

        // TODO implement

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
