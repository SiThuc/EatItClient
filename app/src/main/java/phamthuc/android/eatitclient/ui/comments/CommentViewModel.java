package phamthuc.android.eatitclient.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import phamthuc.android.eatitclient.Model.CommentModel;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataCommentList;

    public CommentViewModel() {
        mutableLiveDataCommentList = new MutableLiveData<>(  );
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataCommentList() {
        return mutableLiveDataCommentList;
    }

    public void setCommentList(List<CommentModel> commentList){
        mutableLiveDataCommentList.setValue( commentList );
    }
}
