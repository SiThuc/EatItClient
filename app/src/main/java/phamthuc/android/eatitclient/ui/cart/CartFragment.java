package phamthuc.android.eatitclient.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import phamthuc.android.eatitclient.Adapter.MyCartAdapter;
import phamthuc.android.eatitclient.Callback.ILoadTimeFromFirebaseListener;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Common.MySwipeHelper;
import phamthuc.android.eatitclient.Database.CartDataSource;
import phamthuc.android.eatitclient.Database.CartDatabase;
import phamthuc.android.eatitclient.Database.CartItem;
import phamthuc.android.eatitclient.Database.LocalCartDataSource;
import phamthuc.android.eatitclient.EventBus.CounterCartEvent;
import phamthuc.android.eatitclient.EventBus.HideFABCart;
import phamthuc.android.eatitclient.EventBus.MenuItemBack;
import phamthuc.android.eatitclient.EventBus.UpdateItemInCart;
import phamthuc.android.eatitclient.Model.Order;
import phamthuc.android.eatitclient.R;
import phamthuc.android.eatitclient.Remote.FCMResponse;
import phamthuc.android.eatitclient.Remote.FCMSendData;
import phamthuc.android.eatitclient.Remote.IFCMService;
import phamthuc.android.eatitclient.Remote.RetrofitFCMClient;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    ILoadTimeFromFirebaseListener listener;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    IFCMService ifcmService;


    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);

        RadioButton rdi_home = (RadioButton) view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton) view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton) view.findViewById(R.id.rdi_ship_this_address);

        RadioButton rdi_cod = (RadioButton) view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = (RadioButton) view.findViewById(R.id.rdi_braintree);

        //Data
        edt_address.setText(Common.currentUser.getAddress());

        //Event
        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edt_address.setText("");
                txt_address.setVisibility(View.GONE);

            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);

                        }).addOnCompleteListener(task -> {
                    String coordinates = new StringBuilder()
                            .append(task.getResult().getLatitude())
                            .append("/")
                            .append(task.getResult().getLongitude()).toString();

                    Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                            task.getResult().getLongitude()));

                    Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                        @Override
                        public void onSuccess(String s) {
                            edt_address.setText(coordinates);
                            txt_address.setText(s);
                            txt_address.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            edt_address.setText(coordinates);
                            txt_address.setText(e.getMessage());
                            txt_address.setVisibility(View.VISIBLE);
                        }
                    });
                });
            }
        });


        builder.setView(view);
        builder.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();

        }).setPositiveButton("OK", (dialog, which) -> {
            //Toast.makeText( getContext(), "Implement later", Toast.LENGTH_SHORT ).show();
            if (rdi_cod.isChecked()) {
                paymentCOD(edt_address.getText().toString(), edt_comment.getText().toString());
            }

        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    //When we have all cartItems, we will get total price
                    cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Double>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Double totalPrice) {
                                    double finalPrice = totalPrice; // We will modify this formula for discount late
                                    Order order = new Order();
                                    order.setUserId(Common.currentUser.getUid());
                                    order.setUserName(Common.currentUser.getName());
                                    order.setUserPhone(Common.currentUser.getPhone());
                                    order.setShippingAddress(address);
                                    order.setComment(comment);

                                    if (currentLocation != null) {
                                        order.setLat(currentLocation.getLatitude());
                                        order.setLng(currentLocation.getLongitude());
                                    } else {
                                        order.setLat(-0.1f);
                                        order.setLng(-0.1f);
                                    }

                                    order.setCartItemList(cartItems);
                                    order.setTotalPayment(totalPrice);
                                    order.setDiscount(0);
                                    order.setFinalPayment(finalPrice);
                                    order.setCod(true);
                                    order.setTransactionId("Cash On Delivery");

                                    // Submit this order object to Firebase
                                    syncLocalTimeWithGlobalTime(order);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (!e.getMessage().contains("Query returned empty result set"))
                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }, throwable -> {
                    Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));

    }

    private void syncLocalTimeWithGlobalTime(Order order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimateServerTimeMs = System.currentTimeMillis() + offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy HH:mm");
                Date resultDate = new Date(estimateServerTimeMs);
                Log.d("TEST_DATE", "" + sdf.format(resultDate));

                listener.onLoadTimeSuccess(order, estimateServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadTimeFailed(error.getMessage());

            }
        });
    }

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    //Write success
                    cartDataSource.cleanCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    Map<String, String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE, "New Order");
                                    notiData.put(Common.NOTI_CONTENT, "You have new order from" + Common.currentUser.getPhone());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(), notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(fcmResponse -> {
                                                Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                            }, throwable -> {
                                                Toast.makeText(getContext(), "Order was sent but failure to send notification", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });

    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0); // Always get first item
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            } else {
                result = "Address not found";
            }

        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    private Unbinder unbinder;

    private CartViewModel mViewModel;
    private MyCartAdapter adapter;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.cart_fragment, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        listener = this;

        mViewModel.initCartDataSource(getContext());
        mViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if (cartItems == null || cartItems.isEmpty()) {
                    recycler_cart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);
                } else {
                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    adapter = new MyCartAdapter(getContext(), cartItems);
                    recycler_cart.setAdapter(adapter);
                }
            }
        });

        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); // Hide home menu already inflate
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        EventBus.getDefault().postSticky(new HideFABCart(true));
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart(); // Update total price
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            Toast.makeText(getContext(), "Delete item from Cart successful!", Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }));

            }
        };

        sumAllItemInCart();
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: €").append(aDouble));

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        mViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            // First, save state of Recycler view
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total: €")
                                .append(Common.formatPrice(price)));

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {
        order.setCreatedDate(estimateTimeInMs);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}