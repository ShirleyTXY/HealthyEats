package com.example.tanxueying.healthyeats;

import android.text.TextUtils;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rekognition.model.ImageTooLargeException;
import com.amazonaws.services.rekognition.model.InvalidImageFormatException;
import com.amazonaws.services.rekognition.model.InvalidParameterException;
import com.amazonaws.services.rekognition.model.AccessDeniedException;
import com.amazonaws.services.rekognition.model.InternalServerErrorException;
import com.amazonaws.services.rekognition.model.ProvisionedThroughputExceededException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ThrottlingException;
import com.google.firebase.auth.FirebaseAuth;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.List;
import android.os.StrictMode;



import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserInputActivity extends AppCompatActivity {
    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;

    private EditText input;
    private ListView foodList;
    private String selectedFood;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinput);

        input = (EditText)findViewById(R.id.text_input);
        foodList = (ListView)findViewById(R.id.food_list);

        final ImageButton button_find = (ImageButton) findViewById(R.id.find);
        button_find.setOnClickListener(new View.OnClickListener() {
            final String text = input.getText().toString().trim();

            public void onClick(View v) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(getApplicationContext(), "Please Enter Food Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                findFood(text);
            }
        });

        final ImageButton button_back = (ImageButton)findViewById(R.id.back_btn);
        button_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserInputActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        final ImageButton button_done = (ImageButton)findViewById(R.id.done);
        button_done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserInputActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        final ImageButton camera = (ImageButton)findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        final TextView view1 = (TextView) findViewById(R.id.textView6);
        view1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserInputActivity.this, FoodInfoActivity.class);
                startActivity(intent);
            }
        });

        final TextView view2 = (TextView) findViewById(R.id.textView7);
        view2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserInputActivity.this, FoodInfoActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                      bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(UserInputActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CAMERA) {
            bitmap = (Bitmap) data.getExtras().get("data");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        ByteBuffer imageBytes = ByteBuffer.wrap(byteArray);
        processImage(imageBytes);
    }

    private void findFood(String text) {

        String url = generateUrl(text);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                parseJsonData(string);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "can not parse the url!!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(UserInputActivity.this);
        rQueue.add(request);

    }

    private void parseJsonData(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONArray foodArray = object.getJSONArray("hints");
            final List<JSONObject> al = new ArrayList<>();
            List<String> list = new ArrayList<>();
            int length = 20;
            if (foodArray.length() < 20) {
                length = foodArray.length();
            }
            for(int i = 0; i < length; ++i) {
                JSONObject cur = foodArray.getJSONObject(i);
                al.add(cur);
                System.out.println(cur.getJSONObject("food").get("label").toString());
                list.add(cur.getJSONObject("food").get("label").toString());
            }
            ArrayAdapter adapter = new ArrayAdapter(UserInputActivity.this, android.R.layout.simple_list_item_1, list);
            foodList.setAdapter(adapter);
            foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(UserInputActivity.this, FoodInfoActivity.class);
                    // transfer the data to the next page

                    intent.putExtra("foodJson", new Gson().toJson(al.get(i)));
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dialog.dismiss();
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Album",
                "Camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Album();
                                break;
                            case 1:
                                Camera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }



    public void Album() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void Camera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private void processImage(ByteBuffer imageBytes) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:75d2373d-07d5-47fc-a23e-1c7bde459498", // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonRekognition rekognitionClient = new AmazonRekognitionClient(credentialsProvider);

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(imageBytes))
                .withMaxLabels(10)
                .withMinConfidence(50F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();
            showResultDialog(labels);
//            for (Label label: labels) {
//                System.out.println(label.getName() + ": " + label.getConfidence().toString());
//            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void showResultDialog(List<Label> labels){
        final int n = labels.size();
        final String[] resultDialogItems = new String[n];
        final boolean[] selected = new boolean[n];
        for (int i = 0; i < n; i++) {
            resultDialogItems[i] =  labels.get(i).getName();
        }

        AlertDialog.Builder resultDialog = new AlertDialog.Builder(this);
        resultDialog.setTitle("Select Food");


        resultDialog.setMultiChoiceItems(resultDialogItems, selected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selectedFood = resultDialogItems[which];
                        findFood(selectedFood);
//                        Toast.makeText(UserInputActivity.this, resultDialogItems[which] + isChecked, Toast.LENGTH_SHORT).show();
                    }
                });
        resultDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                Toast.makeText(UserInputActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }
        });
        resultDialog.create().show();


//        resultDialog.setItems(resultDialogItems,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        for (int i = 0; i < n; i++) {
//                            if (which == i) {
//                                // to do
//                                break;
//                            }
//                        }
//                    }
//                });
//        resultDialog.show();
    }



    private String generateUrl(String input) {
        StringBuilder sb = new StringBuilder("https://api.edamam.com/api/food-database/parser?ingr=");
        String[] wordList = input.trim().split(" ");
        for (int i = 0; i < wordList.length-1; i++) {
            sb.append(wordList[i]);
            sb.append("%20");
        }
        sb.append(wordList[wordList.length-1]);
        sb.append("&app_id={");
        sb.append(Constant.getId());
        sb.append("}&app_key={");
        sb.append(Constant.getKey());
        sb.append("}");
        return sb.toString();
    }
}
