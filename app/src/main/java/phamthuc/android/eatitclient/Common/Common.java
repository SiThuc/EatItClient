package phamthuc.android.eatitclient.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import phamthuc.android.eatitclient.Model.AddonModel;
import phamthuc.android.eatitclient.Model.CategoryModel;
import phamthuc.android.eatitclient.Model.FoodModel;
import phamthuc.android.eatitclient.Model.SizeModel;
import phamthuc.android.eatitclient.Model.TokenModel;
import phamthuc.android.eatitclient.Model.UserModel;
import phamthuc.android.eatitclient.R;
import phamthuc.android.eatitclient.Services.MyFCMServices;

public class Common {
    public static final String USER_REFERENCES = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEAL_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static String currentToken;

    public static String formatPrice(double price) {
        if(price != 0){
            DecimalFormat df = new DecimalFormat( "#,##0.00" );
            df.setRoundingMode( RoundingMode.UP );
            String finalPrice = new StringBuilder( df.format( price ) ).toString();
            return finalPrice.replace( ",", "." );
        }
        else
            return "0.00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;
        if(userSelectedSize == null && userSelectedAddon == null)
            return 0.0;
        else if(userSelectedSize == null){
            // If userSelectedAddon != null, we nee sum price
            for(AddonModel addonModel: userSelectedAddon)
                result += addonModel.getPrice();
            return result;
        }else if(userSelectedAddon == null){
            return userSelectedSize.getPrice() * 1.0;
        }else{
            result = userSelectedSize.getPrice()*1.0;
            for(AddonModel addonModel: userSelectedAddon)
                result += addonModel.getPrice();
            return result;
        }
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder(  );
        builder.append( welcome );
        SpannableString spannableString = new SpannableString( name );
        StyleSpan boldSpan = new StyleSpan( Typeface.BOLD );
        spannableString.setSpan( boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
        builder.append(spannableString);
        textView.setText( builder, TextView.BufferType.SPANNABLE );

    }

    public static String createOrderNumber() {
        return new StringBuilder(  )
                .append( System.currentTimeMillis() )
                .append( Math.abs( new Random(  ).nextInt() ) )
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i){
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
        }

        return "unknown";
    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus){
            case 0: return "Placed";
            case 1: return "Shipping";
            case 2: return "Shipped";
            case -1: return "Cancelled";
        }
        return "Unknown";
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity( context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        String NOTIFICATION_CHANNEL_ID = "eat_it_new";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID, "Eat It V2", NotificationManager.IMPORTANCE_DEFAULT );
            notificationChannel.setDescription( "Eat It V2" );
            notificationChannel.enableLights( true );
            notificationChannel.setLightColor( Color.RED );
            notificationChannel.setVibrationPattern( new long[]{0, 1000, 500, 1000} );
            notificationChannel.enableVibration( true );

            notificationManager.createNotificationChannel( notificationChannel );

        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID);
        }
        builder.setContentTitle( title )
                .setContentText( content )
                .setAutoCancel( true )
                .setSmallIcon( R.mipmap.ic_launcher_round)
                .setLargeIcon( BitmapFactory.decodeResource( context.getResources(), R.drawable.ic_baseline_restaurant_menu_24 ) );
        if(pendingIntent != null)
            builder.setContentIntent( pendingIntent );
        Notification notification = builder.build();
        notificationManager.notify( id, notification );

    }

    public static void updateToken(Context context, String newToken) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child( Common.currentUser.getUid() )
                .setValue( new TokenModel(Common.currentUser.getPhone(), newToken) )
                .addOnFailureListener( e -> {
                    Toast.makeText( context, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                } );
    }

    public static String createTopicOrder() {
        return new StringBuilder( "/topics/new_order" ).toString();
    }
}
