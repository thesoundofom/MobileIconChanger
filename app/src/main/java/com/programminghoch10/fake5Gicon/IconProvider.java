package com.programminghoch10.fake5Gicon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IconProvider {
	private static final Map<String, Icon> icons = new HashMap<>();
	private static final String TAG = "IconProvider";
	// a list of replaceable system icons, to be overwritten with actual list by xposed
	static Map<String, Icon> systemIcons = new HashMap<>();
	
	static void collectSystemIcons(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("systemIcons", Context.MODE_PRIVATE);
		systemIcons.clear();
		for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
			if (!entry.getValue().getClass().equals(String.class)) continue;
			Icon icon = new Icon();
			icon.key = entry.getKey();
			icon.name = entry.getKey().replace("ic_", "").replace("_mobiledata", "").replace("_", " ").toUpperCase();
			String drawableString = (String) entry.getValue();
			Log.d(TAG, "collectSystemIcons: string=" + drawableString);
			Bitmap bitmap = StringToBitMap((String) entry.getValue());
			icon.drawable = new BitmapDrawable(context.getResources(), bitmap);
			
			Log.d(TAG, "collectSystemIcons: drawable " + icon.key + "=" + icon.drawable);
			systemIcons.put(icon.key, icon);
		}
	}
	
	static Map<String, Icon> getIcons() {
		return new HashMap<>(icons);
	}
	
	static void collectIcons(Context context) {
		List<Field> iconFields = Arrays.asList(R.drawable.class.getFields());
		iconFields = iconFields.stream().filter(s -> s.getName().startsWith("ic_") && s.getName().endsWith("_mobiledata")).collect(Collectors.toList());
		for (Field iconField : iconFields) {
			Log.d(TAG, "collectIcons: Field=" + iconField + " name=" + iconField.getName() + " type=" + iconField.getType());
			int fieldValue = Resources.ID_NULL;
			try {
				Log.d(TAG, "collectIcons: value=" + iconField.getInt(context.getResources()));
				fieldValue = iconField.getInt(context.getResources());
			} catch (Exception ignored) {
			}
			if (fieldValue != Resources.ID_NULL) {
				Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), fieldValue, context.getTheme());
				if (drawable != null) {
					Icon icon = new Icon();
					icon.key = iconField.getName();
					icon.name = normalizeIconName(icon.key);
					icon.drawable = drawable;
					icons.put(icon.name, icon);
				}
			}
		}
	}
	
	private static String normalizeIconName(String name) {
		return name.replace("ic_", "").replace("_mobiledata", "").replace("_", " ");
	}
	
	public static String BitMapToString(Bitmap bitmap) {
		if (bitmap == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);
	}
	
	public static Bitmap StringToBitMap(String encodedString) {
		if (encodedString == null) return null;
		try {
			byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
		} catch (Exception e) {
			return null;
		}
	}
	
	static class Icon {
		String key;
		String name;
		Drawable drawable;
	}
	
}
