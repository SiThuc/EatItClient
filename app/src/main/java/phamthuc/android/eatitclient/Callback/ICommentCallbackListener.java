package phamthuc.android.eatitclient.Callback;

import java.util.List;

import phamthuc.android.eatitclient.Model.CommentModel;

public interface ICommentCallbackListener  {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
