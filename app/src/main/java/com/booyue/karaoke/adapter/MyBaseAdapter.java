package com.booyue.karaoke.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 自定义基适配器
 * 原则上所有的ListView的自定义适配器都应该继承自该适配器
 * 适配器的基类
 * @author pjy
 *
 * @param <T>
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter {
	public   final String TAG = this.getClass().getSimpleName();
	public List<T> ts;
	public Context context;
	public LayoutInflater inflater;
	
	public MyBaseAdapter(Context context, List<T> ts) {
		this.ts = ts;
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return ts.size();
	}

	@Override
	public T getItem(int position) {
		return ts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getItemView(position,convertView,parent);
	}
	/**
	 * 抽象方法，需要继承者实现，用来返回ListView中条目的布局
	 */
	public abstract View getItemView(int position, View convertView, ViewGroup parent);
	
	/**
	 * 用来刷新ListView中数据的工具方法
	 */
	public void addAll(List<T> list, boolean isClearDatasource){
		if(isClearDatasource){
			ts.clear();
		}
		ts.addAll(list);
		notifyDataSetChanged();
		
	}
	
	/**
	 * 返回Adapter中使用的数据源
	 */
	public List<T> getDatasource(){
		return ts;
	}
	/**
	 * 返回Adapter中使用的上下文
	 */
	public Context getContext(){
		return context;
	}
	/**
	 * 返回Adapter中使用的LayoutInflater对象
	 */
	public LayoutInflater getInflater(){
		return inflater;
	}
	
}

