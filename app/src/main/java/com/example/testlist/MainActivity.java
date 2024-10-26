package com.example.testlist;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    final String[] foodTitle = {"", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // With Strings
//        ArrayList<String> data = new ArrayList<>();
//        data.add("Apple"); data.add("Banana"); data.add("Cantaloupe");
//
//        // 2nd parameter is an XML file: what each individual list item should look like
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
//
//        ListView lv = findViewById(R.id.listView);
//        lv.setAdapter(adapter);

        ArrayList<FoodItem> data = new ArrayList<>();
        data.add(new FoodItem(R.drawable.p, "Pizza"));
        data.add(new FoodItem(R.drawable.b, "Burger"));

        FoodListAdapter adapter = new FoodListAdapter(this, R.layout.list_item, data);

        ListView lv = findViewById(R.id.listView);
        lv.setAdapter(adapter);

        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    foodTitle[0] = new String(myVisionTester(R.drawable.boorsoki));

//                    foodTitle[1] = new String(myVisionTester(R.drawable.salad));

                    Log.v("Vision Test Real Result", myVisionTester(R.drawable.boorsoki));
                    Log.v("Vision Test Result", foodTitle[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            data.add(new FoodItem(R.drawable.boorsoki, foodTitle[0]));
//                            data.add(new FoodItem(R.drawable.salad, foodTitle[1]));
//                            adapter.notifyDataSetChanged();
                        }
                    });


                } catch (Exception ioe) {
                    Log.v("Vision Test", ioe.getMessage());
                }
            }
        });


    }

    String myVisionTester(int drawableID) throws IOException, JSONException {
        //1. ENCODE image.
        Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(drawableID)).getBitmap();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());

        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(1);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);

        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer("YOUR_API_KEY"));
        Vision vision = builder.build();

        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();

        JSONObject jsonObject = new JSONObject(response);
        Log.v("Response", response.toPrettyString());
        String description = jsonObject.getString("responses");

        return response.toPrettyString();
//        Log.v("MYTAG", response.toPrettyString());
    }

}
