package phamthuc.android.eatitclient.ui.foodlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Model.FoodModel;

public class FoodListViewModel extends ViewModel {
    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodListViewModel() {
    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if(mutableLiveDataFoodList == null)
            mutableLiveDataFoodList = new MutableLiveData<>(  );
        mutableLiveDataFoodList.setValue( Common.categorySelected.getFoods());
        return mutableLiveDataFoodList;
    }
}
