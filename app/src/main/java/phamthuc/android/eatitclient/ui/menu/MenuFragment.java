package phamthuc.android.eatitclient.ui.menu;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitclient.Adapter.MyCategoriesAdapter;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Common.SpacesItemDecoration;
import phamthuc.android.eatitclient.EventBus.MenuItemBack;
import phamthuc.android.eatitclient.R;

public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;
    Unbinder unbinder;
    @BindView( R.id.recycler_menu )
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                ViewModelProviders.of( this ).get( MenuViewModel.class );
        View root = inflater.inflate( R.layout.fragment_menu, container, false );
        unbinder = ButterKnife.bind( this, root );
        initViews();
        menuViewModel.getMessageError().observe( getViewLifecycleOwner(), s -> {
            Toast.makeText( getContext(), ""+s, Toast.LENGTH_SHORT ).show();
            dialog.dismiss();
        });
        menuViewModel.getCategoryListMutable().observe( getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            adapter = new MyCategoriesAdapter( getContext(), categoryModelList );
            recycler_menu.setAdapter( adapter );
            recycler_menu.setLayoutAnimation( layoutAnimationController );
        } );
        return root;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext( getContext() ).setCancelable( false ).build();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation( getContext(), R.anim.layout_item_from_left );
        GridLayoutManager layoutManager = new GridLayoutManager( getContext(), 2 );
        layoutManager.setOrientation( RecyclerView.VERTICAL );
        layoutManager.setSpanSizeLookup( new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter != null){
                    switch (adapter.getItemViewType( position )){
                        case Common.DEFAULT_COLUMN_COUNT: return 1;
                        case Common.FULL_WIDTH_COLUMN: return 2;
                        default: return -1;
                    }
                }
                return -1;
            }
        } );
        recycler_menu.setLayoutManager( layoutManager );
        recycler_menu.addItemDecoration( new SpacesItemDecoration(8) );
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky( new MenuItemBack() );
        super.onDestroy();
    }
}