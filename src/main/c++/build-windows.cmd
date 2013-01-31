msbuild /p:platform=x64 /p:configuration=release /t:rebuild
copy /y ..\..\..\target\drnglib-x64.dll ..\resources\net\nullschool\util\windows\
msbuild /p:platform=win32 /p:configuration=release /t:rebuild
copy /y ..\..\..\target\drnglib-x86.dll ..\resources\net\nullschool\util\windows\
