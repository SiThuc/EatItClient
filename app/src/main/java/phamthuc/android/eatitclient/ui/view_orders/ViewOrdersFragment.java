package phamthuc.android.eatitclient.ui.view_orders;

import androidx.lifecycle.ViewModelProviders;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitclient.Adapter.MyOrderAdapter;
import phamthuc.android.eatitclient.Callback.ILoadOrderCallbackListener;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.EventBus.MenuItemBack;
import phamthuc.android.eatitclient.Model.Order;
import phamthuc.android.eatitclient.R;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {
    Unbinder unbinder;
    Dialog dialog;

    @BindView( R.id.recycler_orders )
    RecyclerView recycler_orders;

    private ViewOrdersViewModel viewOrdersViewModel;
    ILoadOrderCallbackListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewOrdersViewModel = ViewModelProviders.of( this ).get( ViewOrdersViewModel.class );
        View root = inflater.inflate( R.layout.view_orders_fragment, container, false );
        unbinder = ButterKnife.bind( this,root );
        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe( getViewLifecycleOwner(), orderList -> {

            MyOrderAdapter adapter = new MyOrderAdapter( getContext(), orderList );
            recycler_orders.setAdapter( adapter );
        } );
        
        
        return root;
    }

    private void loadOrdersFromFirebase() {
        List<Order> orderList = new ArrayList<>(  );
        FirebaseDatabase.getInstance().getReference( Common.ORDER_REF)
                .orderByChild( "userId" )
                .equalTo( Common.currentUser.getUid() )
                .limitToLast( 100 )
                .addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot orderSnapShot: snapshot.getChildren()){
                            Order order = orderSnapShot.getValue(Order.class);
                            order.setOrderNumber( orderSnapShot.getKey() );
                            orderList.add( order );
                        }
                        listener.onLoadOrderSuccess( orderList );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed( error.getMessage() );
                    }
                } );
    }

    private void initViews(View root) {
        listener = this;
        dialog = new SpotsDialog.Builder().setCancelable( false ).setContext( getContext() ).build();

        recycler_orders.setHasFixedSize( true );
        LinearLayoutManager layoutManager = new LinearLayoutManager( getContext() );
        recycler_orders.setLayoutManager( layoutManager );
        recycler_orders.addItemDecoration( new DividerItemDecoration( getContext(), layoutManager.getOrientation() ) );
    }

    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList( orderList );
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText( getContext(), ""+message, Toast.LENGTH_SHORT ).show();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky( new MenuItemBack() );
        super.onDestroy();
    }
}