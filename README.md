# Issue Viewer
Issue Viewer is an Android application which displays a paginated list of Google's GitHub repositories.  Upon touching a repository, a similar Activity is displayed which displays a paginated scrollable list of the issues associated with that repository (including the ability to filtr by all, open, or closes issues).

# Dependencies
This application uses two external libraries.  The first is The GitHub Java API (https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core).  The pre-built .JAR may be found, here: https://search.maven.org/search?q=a:org.eclipse.egit.github.core.  This library in turn references Google's GSon (https://github.com/google/gson).  The pre-built .JAR may be found, here: https://search.maven.org/artifact/com.google.code.gson/gson/2.8.6/jar.  Both of these .JARs are included in this Android project at master/app/libs (https://github.com/sgussmanbusiness/issuesviewer/tree/master/app/libs).

# Legal
The unchanged GitHub Java API .JAR then is still re-distributed here under the Eclipse Public License - V1.0 (https://www.eclipse.org/legal/epl-v10.html).  Both .JARs re-distributed here as a part of this project retain any and all of their original licenses.

# Running the application
This project may be downloaded and built in an IDE (it was developed in Android Studio), or alternatively, the side-loadable APK maay be downloaded, here: https://drive.google.com/file/d/1dlkDv2pD5wymxM7HVBCyS_2oavkdogTy/view?usp=sharing.
To build in Android Studio, this repo should be downloaded and the project opened in Android Studio.  Then, Build>Run App (with either an Android emulator or a real Android device connected by ADB).
