package phamthuc.android.eatitclient.Callback;

import java.util.List;

import phamthuc.android.eatitclient.Model.BestDealModel;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
