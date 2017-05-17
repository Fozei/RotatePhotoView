/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uk.co.senab.photoview.sample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

public class ActivityTransitionActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        RecyclerView list = (RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new GridLayoutManager(this, 2));
        ImageAdapter imageAdapter = new ImageAdapter(new ImageAdapter.Listener() {
            @Override
            public void onImageClicked(View view) {
                transition(view);
            }
        });
        list.setAdapter(imageAdapter);
    }

    private void transition(View view) {
        if (Build.VERSION.SDK_INT < 21) {
            Toast.makeText(ActivityTransitionActivity.this, "21+ only, keep out", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ActivityTransitionActivity.this, ActivityTransitionToActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(ActivityTransitionActivity.this, view, getString(R.string.transition_test));
            startActivity(intent, options.toBundle());
        }
    }
}
