package edu.berkeley.boss;

import edu.berkeley.boss.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);

    TextView aboutView = (TextView) findViewById(R.id.about_view);
    Linkify.addLinks(aboutView, Linkify.ALL);
  }
}
