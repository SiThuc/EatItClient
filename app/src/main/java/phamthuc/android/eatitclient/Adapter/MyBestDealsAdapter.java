package phamthuc.android.eatitclient.Adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitclient.EventBus.BestDealItemClick;
import phamthuc.android.eatitclient.Model.BestDealModel;
import phamthuc.android.eatitclient.R;


public class MyBestDealsAdapter extends LoopingPagerAdapter<BestDealModel> {
    @BindView( R.id.img_best_deal )
    ImageView img_best_deal;
    @BindView( R.id.txt_best_deal )
    TextView txt_best_deal;

    Unbinder unbinder;


    public MyBestDealsAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super( context, itemList, isInfinite );
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from( context ).inflate( R.layout.layout_best_deal_item, container, false );
    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {
        unbinder = ButterKnife.bind( this, convertView );
        //Set data
        Glide.with( convertView ).load( itemList.get( listPosition ).getImage() ).into(img_best_deal);
        Glide.with( convertView ).load( itemList.get( listPosition ).getName() );
        txt_best_deal.setText( itemList.get( listPosition ).getName() );

        convertView.setOnClickListener( v -> {
            EventBus.getDefault().postSticky( new BestDealItemClick(itemList.get( listPosition )) );
        } );


    }
}
