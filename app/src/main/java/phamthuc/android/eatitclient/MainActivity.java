package phamthuc.android.eatitclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Model.TokenModel;
import phamthuc.android.eatitclient.Model.UserModel;

public class MainActivity extends AppCompatActivity {
    private static int APP_REQUEST_CODE = 7171; // Any number
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<AuthUI.IdpConfig> providers;
    private String token;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener( listener );
    }

    @Override
    protected void onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener( listener );
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build() );
        userRef = FirebaseDatabase.getInstance().getReference( Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable( false ).setContext( this ).build();

        listener = firebaseAuth -> {

            Dexter.withActivity( this )
                    .withPermission( Manifest.permission.ACCESS_FINE_LOCATION )
                    .withListener( new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if(user != null){
                                //Already login
                                FirebaseInstanceId.getInstance(  )
                                        .getInstanceId()
                                        .addOnCompleteListener( task -> {
                                            if(task.isSuccessful()){
                                                token = task.getResult().getToken();
                                            }

                                        } )
                                        .addOnFailureListener( e ->
                                                Toast.makeText( MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show() );
                                checkUserFromFirebase(user, token);
                            }else{
                                // Not login
                                phoneLogin();
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText( MainActivity.this, "You must enable this permission to use app", Toast.LENGTH_SHORT ).show();

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    } ).check();
        };
    }

    private void phoneLogin() {
        startActivityForResult( AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders( providers ).build(), APP_REQUEST_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == APP_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent( data );
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }else{
                Toast.makeText( this, "Failed to sign in", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void checkUserFromFirebase(FirebaseUser user, String token) {
        dialog.show();
        userRef.child( user.getUid() )
                .addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel, token);
                        }else{
                            showRegisterDialog(user);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText( MainActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
                } );
    }

    private void showRegisterDialog(FirebaseUser user){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder( this );
        builder.setTitle( "Register" );
        builder.setMessage( "Please fill information" );

        //Init view
        View itemView = LayoutInflater.from( this ).inflate( R.layout.layout_register, null );
        EditText edt_name = (EditText) itemView.findViewById( R.id.edt_name );
        EditText edt_address = (EditText) itemView.findViewById( R.id.edt_address );
        EditText edt_phone = (EditText) itemView.findViewById( R.id.edt_phone );

        //Set Data
        edt_phone.setText( user.getPhoneNumber() );
        builder.setNegativeButton( "CANCEL", (dialogInterface, i) ->{
            dialogInterface.dismiss();
        });

        builder.setPositiveButton( "REGISTER", (dialogInterface, i) ->{
            if(TextUtils.isEmpty( edt_name.getText().toString() )){
                Toast.makeText( MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT ).show();
                return;
            }else if(TextUtils.isEmpty( edt_address.getText().toString() )){
                Toast.makeText( MainActivity.this, "Please enter your address", Toast.LENGTH_SHORT ).show();
                return;
            }
            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(edt_phone.getText().toString());

            userRef.child( user.getUid() )
                    .setValue( userModel )
                    .addOnCompleteListener( task -> {
                        if(task.isSuccessful()){
                            dialogInterface.dismiss();
                            Toast.makeText( MainActivity.this, "Congratulation! Register success", Toast.LENGTH_SHORT ).show();
                            FirebaseInstanceId.getInstance(  )
                                    .getInstanceId()
                                    .addOnCompleteListener( task2 -> {
                                        if(task2.isSuccessful()){
                                            token = task2.getResult().getToken();
                                        }

                                    } )
                                    .addOnFailureListener( e ->
                                            Toast.makeText( MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show() );

                            goToHomeActivity(userModel, token);
                        }
                    } );
        } );
        builder.setView( itemView );
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel, String stoken) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener( e -> {
                    Toast.makeText( this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                    Common.currentUser = userModel;
                    Common.currentToken = stoken;
                    startActivity( new Intent( MainActivity.this, HomeActivity.class ) );
                    finish();
                } )
                .addOnCompleteListener( task -> {
                    Common.currentUser = userModel;
                    Common.currentToken = stoken;
                    Common.updateToken( MainActivity.this, task.getResult().getToken() );
                    startActivity( new Intent( MainActivity.this, HomeActivity.class ) );
                    finish();
                } );
    }
}