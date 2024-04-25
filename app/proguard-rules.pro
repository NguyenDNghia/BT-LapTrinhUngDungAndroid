# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Giữ không đổi tên các lớp chính
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Giữ các phương thức trong các lớp Activity, v.v.
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Giữ các enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Giữ getter/setter
-keepclassmembers class * {
    void set*(***);
    *** get*();
}

# Giữ các thành viên của các lớp được sử dụng bởi Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Giữ các lớp ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Giữ các views và setters của chúng được sử dụng trong các layout XML.
-keepclassmembers public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
}

# Giữ các liệt kê được sử dụng trong các giao tiếp được tuần tự hóa.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Giữ Parcelable Creator
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Các lớp ViewModel không nên bị obfuscate để đảm bảo chúng có thể được khởi tạo một cách chính xác.
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Các trường LiveData không nên bị đổi tên hoặc xóa để đảm bảo chức năng chính xác.
-keep class androidx.lifecycle.LiveData {
    *;
}

# Giữ tất cả các lớp runtime của Compose
-keep class androidx.compose.runtime.** { *; }

# Giữ các hàm và lớp Composable
-keepclasseswithmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Giữ các lớp với lambda được tạo bởi Compose
-keepclassmembers class * {
    void lambda$*(...);
}
