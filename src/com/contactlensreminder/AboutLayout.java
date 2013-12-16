package com.contactlensreminder;

import java.util.ArrayList;
import java.util.List;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AboutLayout extends Activity{


	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_layout);
		ListView lv=(ListView) findViewById(R.id.aboutListview);
		List<String> listAbout=new ArrayList<String>();
		listAbout.add("App");
		listAbout.add("Facebook");
		listAbout.add("email");

		CustomListAdapter aboutAdapter=new CustomListAdapter(this,R.layout.about_custom,listAbout);
		lv.setAdapter(aboutAdapter);

		lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				if (pos==0){
					return;
				}
				else if (pos==1){
					final Intent facebookIntent=getOpenFacebookIntent(AboutLayout.this);
					if (facebookIntent!=null){
						AboutLayout.this.startActivity(facebookIntent);
					}
				}
				else if (pos==2){
					final Intent emailIntent=new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"anantauprety@gmail.com","escobarcristhian@utexas.edu"});

					AboutLayout.this.startActivity(Intent.createChooser(emailIntent,"Send mail..."));				
				}
			}


		});
	}

	
	public static Intent getOpenFacebookIntent(Context context) {

		   try {
		    context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
		    return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/156712151119657"));
		   } catch (Exception e) {
		    return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/pages/Contact-Lens-Reminder/156712151119657"));
		   }
		}


	public class CustomListAdapter extends ArrayAdapter<String> {

		private Context mContext;
		private int id;
		private List <String>items ;
		private final String version;
		private final Drawable mailIcon;
		private final Drawable facebookIcon;

		public CustomListAdapter(Context context, int textViewResourceId , List<String> list ) 
		{
			super(context, textViewResourceId, list);           
			mContext = context;
			id = textViewResourceId;
			items = list ;
			version=context.getResources().getString(R.string.version);
			mailIcon=context.getResources().getDrawable(R.drawable.gmail_icon);
			facebookIcon=context.getResources().getDrawable(R.drawable.facebook_icon);
		}

		@Override
		public View getView(int position, View v, ViewGroup parent)
		{
			View mView = v ;
			if(mView == null){
				LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mView = vi.inflate(id, null);
			}

			TextView linesText = (TextView) mView.findViewById(R.id.linesText);
			TextView headerText=(TextView) mView.findViewById(R.id.headerText);
			TextView imageText=(TextView)mView.findViewById(R.id.imageText);

			int color=Color.rgb(35,107,142);


			linesText.setTextColor(Color.WHITE);
			linesText.setBackgroundColor( color );
			headerText.setTextColor(Color.WHITE);
			headerText.setBackgroundColor(color);
			imageText.setTextColor(Color.WHITE);
			imageText.setBackgroundColor(color);

			if(items.get(position) != null )
			{
				if (position==0){
					headerText.setTextSize(20.0f);
					headerText.setText(mContext.getText(R.string.app_name));					
					linesText.setMaxLines(4);
					linesText.setLines(4);					
					linesText.setText("Version: "+version+"\n");
					linesText.append("Created by: \nAnanta Uprety \nCristhian Escobar");
					linesText.setVisibility(View.VISIBLE);
					imageText.setVisibility(View.GONE);
				}
				else if (position==1){					
					headerText.setTextSize(20.0f);
					headerText.setText("Connect via social media");
					linesText.setVisibility(View.GONE);
					imageText.setCompoundDrawablesWithIntrinsicBounds(facebookIcon,null,null,null);
					imageText.setTextSize(20.0f);
					imageText.setText("Like us on facebook");
					imageText.setVisibility(View.VISIBLE);
				}
				else if (position==2){					
					imageText.setTextSize(20.0f);
					headerText.setText("Help us improve");				
					linesText.setVisibility(View.GONE);
					imageText.setCompoundDrawablesWithIntrinsicBounds(mailIcon,null,null,null);
					imageText.setText("Comments? Suggestions? Bugs?");
					imageText.setVisibility(View.VISIBLE);
				}
			}

			return mView;
		}
	}
}
