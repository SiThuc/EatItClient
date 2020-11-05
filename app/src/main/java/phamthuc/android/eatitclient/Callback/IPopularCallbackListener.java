package phamthuc.android.eatitclient.Callback;

import java.util.List;

import phamthuc.android.eatitclient.Model.PopularCategoryModel;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);
}
