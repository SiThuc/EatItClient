package phamthuc.android.eatitclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.firebase.ui.auth.util.ui.BucketedTextChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Database.CartDataSource;
import phamthuc.android.eatitclient.Database.CartDatabase;
import phamthuc.android.eatitclient.Database.LocalCartDataSource;
import phamthuc.android.eatitclient.EventBus.BestDealItemClick;
import phamthuc.android.eatitclient.EventBus.CategoryClick;
import phamthuc.android.eatitclient.EventBus.CounterCartEvent;
import phamthuc.android.eatitclient.EventBus.FoodItemClick;
import phamthuc.android.eatitclient.EventBus.HideFABCart;
import phamthuc.android.eatitclient.EventBus.MenuItemBack;
import phamthuc.android.eatitclient.EventBus.PopularCategoryClick;
import phamthuc.android.eatitclient.Model.CategoryModel;
import phamthuc.android.eatitclient.Model.FoodModel;
import phamthuc.android.eatitclient.Model.PopularCategoryModel;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    NavController navController;

    android.app.AlertDialog dialog;

    int menuClickId = -1;

    private CartDataSource cartDataSource;

    @BindView( R.id.fab )
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );

        dialog = new SpotsDialog.Builder().setContext( this ).setCancelable( false ).build();

        ButterKnife.bind( this );

        cartDataSource = new LocalCartDataSource( CartDatabase.getInstance( this ).cartDAO() );

        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate( R.id.nav_cart );
                /*Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();*/
            }
        } );
        drawer = findViewById( R.id.drawer_layout );
        NavigationView navigationView = findViewById( R.id.nav_view );
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_list,R.id.nav_food_detail, R.id.nav_cart, R.id.nav_view_orders )
                .setDrawerLayout( drawer )
                .build();
        navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );
        navigationView.setNavigationItemSelectedListener( this );
        navigationView.bringToFront();

        View hearderView = navigationView.getHeaderView( 0 );
        TextView txt_user = (TextView) hearderView.findViewById( R.id.txt_user );
        Common.setSpanString("Hey", Common.currentUser.getName(),txt_user);

        countCartItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        return NavigationUI.navigateUp( navController, mAppBarConfiguration )
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked( true );
        drawer.closeDrawers();
        switch (item.getItemId()){
            case R.id.nav_home:
                if(item.getItemId() != menuClickId)
                    navController.navigate( R.id.nav_home );
                break;
            case R.id.nav_menu:
                if(item.getItemId() != menuClickId)
                    navController.navigate( R.id.nav_menu );
                break;
            case R.id.nav_cart:
                if(item.getItemId() != menuClickId)
                    navController.navigate( R.id.nav_cart );
                break;

            case R.id.nav_view_orders:
                if(item.getItemId() != menuClickId)
                    navController.navigate( R.id.nav_view_orders );
                break;

            case R.id.nav_sign_out:
                signOut();
                break;
        }
        menuClickId = item.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Signout" )
                .setMessage( "Do you really want to sign out" )
                .setNegativeButton( "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                } ).setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent( HomeActivity.this, MainActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity( intent );
                finish();
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //EventBus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register( this );
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister( this );
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event){
        if(event.isSuccess()){
            navController.navigate( R.id.nav_food_list );
            //Toast.makeText( this, "Click on: "+event.getCategoryModel().getName(),Toast.LENGTH_SHORT ).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event){
        if(event.isHidden()){
            fab.hide();
        }else{
            fab.show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event){
        if(event.isSuccess()){
            navController.navigate( R.id.nav_food_detail );
            //Toast.makeText( this, "Click on: "+event.getCategoryModel().getName(),Toast.LENGTH_SHORT ).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event){
        if(event.isSuccess()){
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event){
        if(event.getBestDealModel() != null){
            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child( event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Common.categorySelected = snapshot.getValue( CategoryModel.class );
                                Common.categorySelected.setMenu_id( snapshot.getKey() );
                                //Load food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child( event.getBestDealModel().getMenu_id() )
                                        .child( "foods" )
                                        .orderByChild( "id" )
                                        .equalTo( event.getBestDealModel().getFood_id() )
                                        .limitToLast( 1 )
                                        .addListenerForSingleValueEvent( new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    for(DataSnapshot itemSnapShot:snapshot.getChildren()){
                                                        Common.selectedFood = itemSnapShot.getValue( FoodModel.class );
                                                        Common.selectedFood.setKey( itemSnapShot.getKey() );
                                                    }
                                                    navController.navigate( R.id.nav_food_detail );

                                                }else{

                                                    Toast.makeText( HomeActivity.this, "Item does not exists", Toast.LENGTH_SHORT ).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText( HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT ).show();

                                            }
                                        } );

                            }else{
                                dialog.dismiss();
                                Toast.makeText( HomeActivity.this, "Item does not exist", Toast.LENGTH_SHORT ).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText( HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT ).show();

                        }
                    } );
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event){
        if(event.getPopularCategoryModel() != null){
            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child( event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Common.categorySelected = snapshot.getValue( CategoryModel.class );
                                Common.categorySelected.setMenu_id( snapshot.getKey() );
                                //Load food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child( event.getPopularCategoryModel().getMenu_id() )
                                        .child( "foods" )
                                        .orderByChild( "id" )
                                        .equalTo( event.getPopularCategoryModel().getFood_id() )
                                        .limitToLast( 1 )
                                        .addListenerForSingleValueEvent( new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    for(DataSnapshot itemSnapShot:snapshot.getChildren()){
                                                        Common.selectedFood = itemSnapShot.getValue( FoodModel.class );
                                                        Common.selectedFood.setKey( itemSnapShot.getKey() );
                                                    }
                                                    navController.navigate( R.id.nav_food_detail );

                                                }else{

                                                    Toast.makeText( HomeActivity.this, "Item does not exists", Toast.LENGTH_SHORT ).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText( HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT ).show();

                                            }
                                        } );

                            }else{
                                dialog.dismiss();
                                Toast.makeText( HomeActivity.this, "Item does not exist", Toast.LENGTH_SHORT ).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText( HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT ).show();

                        }
                    } );
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart( Common.currentUser.getUid() )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount( integer );

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains( "Query returned empty" ))
                            Toast.makeText( HomeActivity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT ).show();
                        else
                            fab.setCount( 0 );
                    }
                } );
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event){
        menuClickId = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() >0)
            getSupportFragmentManager().popBackStack();
    }


}











