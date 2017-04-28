package se.chalmers.eda397.team9.cardsagainsthumanity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import se.chalmers.eda397.team9.cardsagainsthumanity.Classes.Game;
import se.chalmers.eda397.team9.cardsagainsthumanity.Classes.Player;
import se.chalmers.eda397.team9.cardsagainsthumanity.Classes.WhiteCard;


/**
 * Created by emy on 23/04/17.
 */

public class GameActivity extends AppCompatActivity {

    public ArrayList<WhiteCard> whiteCards;


    ImageButton favoriteButtons[];

    private Game game;
    private Player player;
    private Boolean[] selectedCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear);
        game = (Game) getIntent().getExtras().get("THIS.GAME");
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("usernameFile", Context.MODE_PRIVATE);
        player = game.getPlayerByUserName(prefs.getString("name", null));
        TextView blackCardTextView = (TextView) findViewById(R.id.textviewBlackCard);
        blackCardTextView.setText(Html.fromHtml(game.getBlackCard().getText()));
        TextView pickTextView = (TextView) findViewById(R.id.pickTextView);
        String t = "Pick: " + game.getBlackCard().getPick();
        pickTextView.setText(t);
        whiteCards = player.getWhiteCards();
        favoriteButtons = new ImageButton[whiteCards.size()];
        selectedCards = new Boolean[whiteCards.size()];
        for (int i = 0; i < whiteCards.size(); i++) {
            selectedCards[i] = false;
            //Child relative layout that contains white card and favorite's symbol (heart)
            RelativeLayout childLayout = new RelativeLayout(this);

            //Create objects that contain the images
            ImageView imgWhiteCard = new ImageView(this);
            ImageButton imgFavoriteBorder = new ImageButton(this);
            TextView cardText = new TextView(this);

            imgWhiteCard.setId(i);
            imgFavoriteBorder.setId(i);

            //Layout settings of the images
            imgWhiteCard.setPadding(convertDpToPixels(2,this), convertDpToPixels(2,this), convertDpToPixels(2,this), convertDpToPixels(2,this)); //.setPadding(left, top, right, bottom)
            RelativeLayout.LayoutParams paramsWhiteCard = new RelativeLayout.LayoutParams(convertDpToPixels(150,this), convertDpToPixels(360,this)); //.LayoutParams(width, height) for white cards
            paramsWhiteCard.setMargins(convertDpToPixels(10,this), convertDpToPixels(10,this), convertDpToPixels(1,this), convertDpToPixels(7,this)); //.setMargins(left, top, right, bottom)
            //paramsWhiteCard.setMargins(1, 1, 1, 75); //.setMargins(left, top, right, bottom)
            imgWhiteCard.setLayoutParams(paramsWhiteCard);

            imgFavoriteBorder.setPadding(convertDpToPixels(2,this), convertDpToPixels(2,this), convertDpToPixels(2,this), convertDpToPixels(2,this)); //.setPadding(left, top, right, bottom)
            RelativeLayout.LayoutParams paramsFavoriteBorder = new RelativeLayout.LayoutParams(convertDpToPixels(100,this), convertDpToPixels(100,this)); //(width,height) for favorite border
            paramsFavoriteBorder.setMargins(convertDpToPixels(5,this), convertDpToPixels(10,this), convertDpToPixels(1,this), convertDpToPixels(1,this)); //.setMargins(left, top, right, bottom)
            imgFavoriteBorder.getBackground().setAlpha(0); //ImageButton background full transparent
            imgFavoriteBorder.setLayoutParams(paramsFavoriteBorder);

            cardText.setPadding(convertDpToPixels(30,this),0,convertDpToPixels(30,this),0);
            RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(convertDpToPixels(150,this), convertDpToPixels(360,this)); //.LayoutParams(width, height) for text in white card
            cardText.setLayoutParams(paramsText);
            paramsText.setMargins(0,convertDpToPixels(100,this),0,0);
            cardText.setText(Html.fromHtml(whiteCards.get(i).getWord()));
            cardText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cardText.setTextColor(Color.BLACK);

            //Insert images in the objects
            imgWhiteCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.white_card));
            imgFavoriteBorder.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_favorite_border));

            //imgFavoriteBorder.bringToFront();

            //Add listener with method onClick for favorite buttons
            imgFavoriteBorder.setOnClickListener(favoriteClick);

            //Add objects to the view
            childLayout.addView(imgWhiteCard);
            childLayout.addView(imgFavoriteBorder);
            childLayout.addView(cardText);
            layout.addView(childLayout);

            //findViewById(R.id.imgFavoriteBorder).setOnClickListener(favoriteClick);

            favoriteButtons[i] = imgFavoriteBorder;

        }

    }


    //Button listener with method onClick for favorite buttons
    View.OnClickListener favoriteClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            ImageButton favoriteButton = (ImageButton) view; //Cast

            int picked = 0;
            boolean selected = false;
            for (int i = 0; i < whiteCards.size(); i++) {
                if(selectedCards[i]){
                    picked++;
                }
                if (favoriteButton == favoriteButtons[i] && selectedCards[i]){
                    selected = true;
                }
            }
            if (picked < game.getBlackCard().getPick() || selected) {
                for (int i = 0; i < whiteCards.size(); i++) {
                    if (favoriteButton == favoriteButtons[i]) {
                        selectedCards[i] = !selectedCards[i];
                        if (selectedCards[i]) {
                            favoriteButtons[i].setImageResource(R.mipmap.ic_favorite);
                            player.getSelectedCards().add(whiteCards.get(i));
                        } else {
                            favoriteButtons[i].setImageResource(R.mipmap.ic_favorite_border);
                            player.getSelectedCards().remove(whiteCards.get(i));
                        }
                    }
                }
                updateBlackCardText();
            } else {
                Toast.makeText(getApplicationContext(), "You can only select " + game.getBlackCard().getPick() + " cards.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    //Method that convert Pixels to DP
    /*
    public static int convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int)dp;
    }*/

    //Method that convert DP to Pixels
    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        //float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, resources.getDisplayMetrics());
        return (int)px;
    }

    private void updateBlackCardText() {
        String[] blackText = game.getBlackCard().getText().split("_");
        if (blackText.length>1) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < blackText.length; j++) {
                sb.append(blackText[j]);
                if (j < player.getSelectedCards().size()) {
                    sb.append(player.getSelectedCards().get(j).getWord());
                } else if (j < blackText.length-1) {
                    sb.append("_");
                }
            }
            TextView blackCardTextView = (TextView) findViewById(R.id.textviewBlackCard);
            blackCardTextView.setText(Html.fromHtml(sb.toString()));
        }
    }

    //Main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.changeName:
                try{
                    File prefsFile = new File("/data/data/se.chalmers.eda397.team9.cardsagainsthumanity/shared_prefs/usernameFile.xml");
                    prefsFile.delete();
                }
                catch (Exception e){

                }

                Intent intent = new Intent(this, IndexActivity.class);
                startActivity(intent);

                return true;
            case R.id.changeTable:
                //Do something
                return true;
            case R.id.settings:
                //Do something
                return true;
            case R.id.help:
                //Do something
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
