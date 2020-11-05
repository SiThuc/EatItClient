package phamthuc.android.eatitclient.ui.comments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitclient.Adapter.MyCommentAdapter;
import phamthuc.android.eatitclient.Callback.ICommentCallbackListener;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Model.CommentModel;
import phamthuc.android.eatitclient.R;

public class CommentFragment extends BottomSheetDialogFragment implements ICommentCallbackListener {
    private CommentViewModel commentViewModel;
    private Unbinder unbinder;

    @BindView( R.id.recycler_comment )
    RecyclerView recycler_comment;

    AlertDialog dialog;
    ICommentCallbackListener listener;

    public CommentFragment() {
        listener = this;
    }

    private static CommentFragment instance;

    public static CommentFragment getInstance(){
        if(instance == null)
            instance = new CommentFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = LayoutInflater.from( getContext() )
                .inflate( R.layout.bottom_sheet_comment_fragment, container, false );
        unbinder = ButterKnife.bind( this, itemView );
        initViews();
        loadCommentsFromFirebase();
        commentViewModel.getMutableLiveDataCommentList().observe( this, commentModels -> {
            MyCommentAdapter adapter = new MyCommentAdapter(getContext(), commentModels);
            recycler_comment.setAdapter( adapter );
        } );
        return itemView;

    }

    private void loadCommentsFromFirebase() {
        dialog.show();
        List<CommentModel> commentModels = new ArrayList<>(  );
        FirebaseDatabase.getInstance().getReference( Common.COMMENT_REF )
                .child( Common.selectedFood.getId() )
                .orderByChild( "commentTimestamp" )
                .limitToLast( 100 )
                .addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot commentSnapShot:snapshot.getChildren()){
                            CommentModel commentModel = commentSnapShot.getValue(CommentModel.class);
                            commentModels.add( commentModel );
                        }
                        Log.d("SOLUONG", String.valueOf( commentModels.size() ) );
                        listener.onCommentLoadSuccess( commentModels );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCommentLoadFailed( error.getMessage() );

                    }
                } );
    }

    private void initViews() {
        commentViewModel = ViewModelProviders.of( this ).get( CommentViewModel.class );
        dialog = new SpotsDialog.Builder().setContext( getContext() ).setCancelable( false ).build();

        recycler_comment.setHasFixedSize( true );
        LinearLayoutManager layoutManager = new LinearLayoutManager( getContext(), RecyclerView.VERTICAL, true );
        recycler_comment.setLayoutManager( layoutManager );
        recycler_comment.addItemDecoration( new DividerItemDecoration( getContext(), layoutManager.getOrientation() ) );
    }

    @Override
    public void onCommentLoadSuccess(List<CommentModel> commentModels) {
        dialog.dismiss();
        commentViewModel.setCommentList( commentModels );

    }

    @Override
    public void onCommentLoadFailed(String message) {
        dialog.dismiss();
        Toast.makeText( getContext(), message, Toast.LENGTH_SHORT ).show();

    }
}
