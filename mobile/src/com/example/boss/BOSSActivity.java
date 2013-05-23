package com.example.boss;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class BOSSActivity extends Activity implements OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set up click listeners for all the buttons
    View locButton = findViewById(R.id.loc_button);
    locButton.setOnClickListener(this);
    View aboutButton = findViewById(R.id.about_button);
    aboutButton.setOnClickListener(this);
    View exitButton = findViewById(R.id.exit_button);
    exitButton.setOnClickListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public void onClick(View v) {
    Intent intent;
    switch (v.getId()) {
    case R.id.loc_button:
      intent = new Intent(this, LocActivity.class);
      startActivity(intent);
      break;
    case R.id.about_button:
      intent = new Intent(this, AboutActivity.class);
      startActivity(intent);
      break;
    case R.id.exit_button:
      finish();
      break;
    }

  }
}
