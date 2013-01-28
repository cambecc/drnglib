msbuild /p:platform=win32 /p:configuration=release /t:rebuild
copy /y ..\..\..\target\drnglib_x86.dll ..\resources\net\nullschool\util\
msbuild /p:platform=x64 /p:configuration=release /t:rebuild
copy /y ..\..\..\target\drnglib_x64.dll ..\resources\net\nullschool\util\
