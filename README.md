# SlideListView

完全仿QQ列表滑动。优化各种细节。支持点击事件、添加删除动画
目前点击事件仅支持item的整体点击，不支持item里面的子VIew的点击事件。

用法：
在Adapter的getView(final int position, View convertView, ViewGroup parent)的方法中如下代码

        Holder holder = null;
        if (convertView == null) {
            Log.i("ListAdapter", "convertView == null");
            //这里要将SlideView作为listView的子Item
            convertView = new SlideView(mContext);
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            ((SlideView)convertView).setContentView(view);
            holder = new Holder();
            holder.contentTv = (TextView) view.findViewById(R.id.contentTv);
            holder.testButtonTv = (TextView) view.findViewById(R.id.testButtonTv);
            convertView.setTag(holder);

            //这里根据自己的需要，添加view
            TextView deleteTv = (TextView) View.inflate(mContext, R.layout.slide_bottom_item1, null);
            TextView buttonTv = (TextView) View.inflate(mContext,R.layout.slide_bottom_item2,null);
            ((SlideView)convertView).setBottomView(deleteTv, buttonTv);

            holder.deleteTv = deleteTv;
            holder.buttonTv = buttonTv;
        } else {
            holder = (Holder) convertView.getTag();
            Log.i("ListAdapter", "convertView != null");
        }
