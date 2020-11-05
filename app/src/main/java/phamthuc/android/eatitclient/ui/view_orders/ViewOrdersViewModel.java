package phamthuc.android.eatitclient.ui.view_orders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import phamthuc.android.eatitclient.Model.Order;

public class ViewOrdersViewModel extends ViewModel {
    private MutableLiveData<List<Order>> mutableLiveDataOrderList;

    public ViewOrdersViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>(  );
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {
        this.mutableLiveDataOrderList.setValue( orderList );
    }
}