# UMengPushSdkHelper
友盟推送封装
## 如何导入？

* Android Studio
		
		allprojects {
			repositories {
			  ...
			  maven { url 'https://jitpack.io' }
			}
		}
		  
		dependencies {
	        classpath 'com.android.tools.build:gradle:2.3.3'
	        //下面这一行～
	        classpath "io.realm:realm-gradle-plugin:3.3.2"
	        // NOTE: Do not place your application dependencies here; they belong
	        // in the individual module build.gradle files
	    }
		dependencies {
			compile 'com.github.shuhaoSCode:UMengPushSdkHelper:1.0.0'
		}

* eclipse。。。请自行copy class。
