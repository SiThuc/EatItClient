package phamthuc.android.eatitclient.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Database.CartDataSource;
import phamthuc.android.eatitclient.Database.CartDatabase;
import phamthuc.android.eatitclient.Database.CartItem;
import phamthuc.android.eatitclient.Database.LocalCartDataSource;

public class CartViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;

    public  CartViewModel(){
        compositeDisposable = new CompositeDisposable(  );
    }

    public void initCartDataSource(Context context){
        cartDataSource = new LocalCartDataSource( CartDatabase.getInstance( context ).cartDAO() );
    }

    public void onStop(){
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems(){
        if(mutableLiveDataCartItems == null)
            mutableLiveDataCartItems = new MutableLiveData<>(  );
        getAllCartItems();
        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {
        compositeDisposable.add( cartDataSource.getAllCart( Common.currentUser.getUid() )
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe( cartItems -> {
            mutableLiveDataCartItems.setValue( cartItems );
        }, throwable -> {
            mutableLiveDataCartItems.setValue( null );
        } ));
    }
}