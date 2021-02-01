# ZoomView
ZoomView for Bitmap Image based on [android-zoom-view](https://github.com/Polidea/android-zoom-view)
You can control movement with touch or other ways

If you use this for smart glasses 
Recommend use with [TiltScroller](https://github.com/yc-park/TiltScroller)


## Support
- minSdkVersion 16
- targetSdkVersion 30

## Setup
Implementation (Latest Release : [![](https://jitpack.io/v/yc-park/ZoomView.svg)](https://jitpack.io/#yc-park/ZoomView) )
```javascript
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.yc-park:ZoomView:[Latest Release]'
}
```


## How To Use
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.ycpark.zoomview.ZoomView
        android:id="@+id/zoom_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:image_in_minimap="true"
        app:minimap_enabled="true"
        app:minimap_height="120dp"
        app:minimap_margin="20dp"
        app:touchable="true"     
        app:minimap_in_border_color="@color/colorAccent"
        app:minimap_in_border_size="3dp"
        app:minimap_out_border_color="@color/colorPrimary"
        app:minimap_out_border_size="3dp"
        ·
        ·
        ·>

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:src="@drawable/sample" />
        </FrameLayout>
    </com.ycpark.zoomview.ZoomView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

```java
zoomView.smoothZoomTo(float zoomLevel);

// For custom movement
zoomView.move(int coordX, int coordY);
```

![example](https://user-images.githubusercontent.com/58277725/101324013-e1dfc100-38ac-11eb-9daf-9b231e0de6a4.jpg)
