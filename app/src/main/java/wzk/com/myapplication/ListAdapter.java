package wzk.com.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangZhengkui on 2015-12-15 18:47
 */
public class ListAdapter extends BaseAdapter {
    Context mContext;
    List<String> date = new ArrayList<>(0);
    public ListAdapter(Context mContext) {
        this.mContext = mContext;
        createDate();
    }

    //这里的数据创建应该放在Activity中，这里为了方便，就直接在这些写了。不要在意这些细节
    public void createDate() {
        for (int i = 0; i < 30; i++) {
            date.add("item"+i);

        }
    }
    @Override
    public int getCount() {
        return date.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            Log.i("ListAdapter", "convertView == null");
            convertView = new SlideView(mContext);
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            ((SlideView)convertView).setContentView(view);
            holder = new Holder();
            holder.contentTv = (TextView) view.findViewById(R.id.contentTv);
            holder.testButtonTv = (TextView) view.findViewById(R.id.testButtonTv);
            convertView.setTag(holder);

            //这里不建议在getView中每次都加载一些view,这是跟listView的复用思想冲突的，而且如果加载的view较复杂，会影响滑动的效果；
            //可以根据项目的实际需要进行复用
            TextView deleteTv = (TextView) View.inflate(mContext, R.layout.slide_bottom_item1, null);
            TextView buttonTv = (TextView) View.inflate(mContext,R.layout.slide_bottom_item2,null);
            ((SlideView)convertView).setBottomView(deleteTv, buttonTv);

            holder.deleteTv = deleteTv;
            holder.buttonTv = buttonTv;
        } else {
            holder = (Holder) convertView.getTag();
            Log.i("ListAdapter", "convertView != null");
        }
        holder.position = position;
        holder.contentTv.setText(date.get(position));
        holder.convertView = convertView;
        holder.contentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "测试按钮" + date.get(position), Toast.LENGTH_SHORT).show();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "click" + date.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteTv.setTag(convertView);
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "删除", Toast.LENGTH_SHORT).show();
                View view = (View) v.getTag();
                ((SlideView)view).performDismiss(((Holder) view.getTag()).position, new SlideView.OnDismissCallback() {
                    @Override
                    public void onDismiss(int dismissPosition) {
                        //移除item
                        date.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
        });

        holder.buttonTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "按钮", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    class Holder {
        View convertView;
        TextView contentTv;
        TextView testButtonTv;
        TextView deleteTv;
        TextView buttonTv;

        int position;
    }
}
