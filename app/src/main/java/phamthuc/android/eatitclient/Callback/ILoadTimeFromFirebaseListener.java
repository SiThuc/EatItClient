package phamthuc.android.eatitclient.Callback;

import phamthuc.android.eatitclient.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
