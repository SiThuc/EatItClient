package phamthuc.android.eatitclient.Callback;

import java.util.List;

import phamthuc.android.eatitclient.Model.Order;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadOrderFailed(String message);
}
