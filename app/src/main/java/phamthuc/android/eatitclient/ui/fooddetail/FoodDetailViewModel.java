package phamthuc.android.eatitclient.ui.fooddetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Model.CommentModel;
import phamthuc.android.eatitclient.Model.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveDataFood;
    private MutableLiveData<CommentModel> mutableLiveDataComment;

    public void setCommentModel(CommentModel commentModel){
        if(mutableLiveDataComment != null)
            mutableLiveDataComment.setValue( commentModel );
    }

    public FoodDetailViewModel() {
        mutableLiveDataComment = new MutableLiveData<>(  );
    }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }

    public MutableLiveData<FoodModel> getMutableLiveDataFood() {
        if(mutableLiveDataFood == null)
            mutableLiveDataFood = new MutableLiveData<>(  );
        mutableLiveDataFood.setValue( Common.selectedFood );
        return mutableLiveDataFood;
    }

    public void setFoodModel(FoodModel foodModel) {
        if(mutableLiveDataFood != null)
            mutableLiveDataFood.setValue( foodModel );
    }
}