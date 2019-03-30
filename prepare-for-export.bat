::Cleans an Android Studio project folder to prepare for archiving or sharing
::Based on the work of Daniel Fowler: 
::	http://tekeye.uk/android/export-android-studio-project
:: Save as "prepare-for-export.bat" and put the script in root of your project. 
:: Execute by double-clicking on the bat-file.
@ECHO off
@for /f %%a in ('2^>nul dir "%FOLDER%" /a-d/b/-o/-p/s^|find /v /c ""') do set n=%%a
ECHO %n% files in project folder. Cleaning...

::Removing Gradle code, it's added back in on import
rmdir .gradle /s /q
::Removing IDE files
rmdir .idea /s /q
@del *.iml /f /s
@del local.properties
::Removing build folders, will be recreated
rmdir build /s /q
rmdir app\build /s /q
::Removing Gradle Wrapper, will be added back in on import
rmdir gradle /s /q
::Removing git ignore files
::del .gitignore /f /s
::Removing other Gradle files
@del gradle.properties
@del gradle?.*
::Remove libs folder
::rmdir app\libs /s /q
::Remove ProGuard rules
::del app\proguard-rules.pro /f
::Remove test code
rmdir app\src\androidTest /s /q
rmdir app\src\test /s /q
::Clear Read-only attributes
attrib -R *.* /s


@for /f %%a in ('2^>nul dir "%FOLDER%" /a-d/b/-o/-p/s^|find /v /c ""') do set n=%%a
ECHO Done!
ECHO %n% files remaining.
PAUSE