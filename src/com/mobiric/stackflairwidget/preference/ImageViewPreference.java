package com.mobiric.stackflairwidget.preference;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.mobiric.stackflairwidget.R;

/**
 * Custom {@link Preference} that displays an {@link ImageView}. </p> This class is a bit of a HACK
 * in the way it handles its layout, and could be improved.
 */
// TODO make custom Preference properly
public class ImageViewPreference extends Preference
{
	private ImageView imageView;
	private int height;
	private int width;

	private Context context;
	private AttributeSet attrs;

	public ImageViewPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		this.attrs = attrs;

		// this.setWidgetLayoutResource(R.layout.custom_pref_flair);
		// if (mPhoto == null)
		// {
		// mPhoto = BitmapFactory.decodeResource(getContext().getResources(),
		// R.drawable.ic_launcher);
		// }
	}

	/**
	 * Returns the view to show. </p> This is a HACK that returns a basic {@link ImageView}
	 * independent of the layout files used elsewhere. This is the only way I could get a custom
	 * {@link Preference} layout to work.
	 */
	@Override
	public View getView(View convertView, ViewGroup parent)
	{
		if (imageView == null)
		{
			imageView = new ImageView(context, attrs);
			imageView.setImageResource(R.drawable.flair_383414);
			LayoutParams layoutParams =
					new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			imageView.setLayoutParams(layoutParams);
			imageView.setScaleType(ScaleType.FIT_CENTER);
		}

		return imageView;
	}

	/**
	 * Sets a new image to display.
	 */
	public void setBitmap(final Bitmap bitmap)
	{
		// null check
		if (bitmap == null)
		{
			return;
		}

		// lazy initialise dimensions
		if (width == 0)
		{
			height = imageView.getHeight();
			width = imageView.getWidth();
		}

		// scale bitmap correctly
		final Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, width, height, true);

		imageView.post(new Runnable()
		{
			public void run()
			{
				imageView.setImageBitmap(bitmapResized);
				imageView.invalidate();
			}
		});
	}
}