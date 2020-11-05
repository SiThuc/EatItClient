package phamthuc.android.eatitclient.Callback;

import java.util.List;

import phamthuc.android.eatitclient.Model.CategoryModel;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
