package knu.cse.listenthis.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import knu.cse.listenthis.PhysicalArchitecture.ClientController;
import knu.cse.listenthis.ProblemDomain.Constants;
import knu.cse.listenthis.ProblemDomain.Location;
import knu.cse.listenthis.ProblemDomain.Music;
import knu.cse.listenthis.ProblemDomain.Posts;
import knu.cse.listenthis.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static knu.cse.listenthis.ProblemDomain.Constants.GET_PICTURE_URI;


public class WritingNewPostActivity extends AppCompatActivity {

    private static Handler handler;

    private ClientController client;

    private Music music;
    private Location location;

    private ImageButton finButton;
    private ImageButton delButton;

    private ImageButton postingPictureButton;
    private ImageButton postingMusicButton;
    private ImageButton postingLocationButton;

    private TextView postingMusicText;
    private TextView postingLocationText;
    private EditText postingOpinionText;
    private Posts posts = new Posts();

    Bitmap selPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_new_post);

        getWindow().setStatusBarColor(Color.parseColor("#516FA5"));

        client = ClientController.getClientControl();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                client.setHandler(null);

                if(msg.what== Constants.RECEIVE_SUCCESSS){
                    Log.d("test", "post handler");
                    finish();
                }
                else if(msg.what==Constants.RECEIVE_ERROR){
                    // TODO when received err message
                }
            }
        };

        postingMusicText = (TextView)findViewById(R.id.postingMusicText);
        postingLocationText = (TextView)findViewById(R.id.postingLocationText);
        postingOpinionText = (EditText)findViewById(R.id.postingOpinionText);

        finButton=(ImageButton)findViewById(R.id.postingFinish);
        finButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selPhoto == null) {
                    Toast.makeText(getApplicationContext(), "사진을 첨부하세요", Toast.LENGTH_SHORT).show();
                    postingPictureButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
                    return;
                }

                posts.setLocationInfo(location);
                posts.setMusic(music);
                posts.setComment(postingOpinionText.getText().toString());
                posts.setUserID(client.getMe().getUserIndex());

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                posts.setCreateTime(sdfNow.format(date));

                client.setHandler(handler);
                client.post(posts);
            }
        });

        delButton=(ImageButton)findViewById(R.id.postingBack);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        postingPictureButton = (ImageButton)findViewById(R.id.postingPictureButton);
        postingPictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, GET_PICTURE_URI);
            }
        });

        postingMusicButton = (ImageButton)findViewById(R.id.postingMusicButton);
        postingMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchMusicActivity.class);

                startActivityForResult(intent, Constants.GET_SONG);
            }
        });

        postingLocationButton = (ImageButton)findViewById(R.id.postingLocationButton);
        postingLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchLocationActivity.class);
                intent.putExtra("MODE", Constants.SEARCH_MAP_MODE);

                startActivityForResult(intent, Constants.GET_LOCATION);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_PICTURE_URI) {
                try {
                    Uri selPhotoUri = data.getData();
                    selPhoto = MediaStore.Images.Media.getBitmap( getContentResolver(), selPhotoUri );

                    byte[] image=bitmapToByteArray(selPhoto);
                    posts.setIImage(image);

                    ImageView imageView = (ImageView) findViewById(R.id.postingPicture) ;
                    imageView.setImageBitmap(selPhoto);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == Constants.GET_SONG) {
                music = (Music)(data.getSerializableExtra("SONG"));
                postingMusicText.setText(music.getArtistName() + " - " + music.getMusicName());
                Log.d("test", music.toString());
            }
            else if(requestCode == Constants.GET_LOCATION) {
                location = (Location) (data.getSerializableExtra("LOCATION"));
                postingLocationText.setText(location.getTitle());
                Log.d("test", location.toString());
            }
        }
    }

    public byte[] bitmapToByteArray( Bitmap bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress( Bitmap.CompressFormat.JPEG, 50, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }
}
