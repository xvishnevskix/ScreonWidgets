[Setup]
AppName=screon
AppVersion=1.0.0
PrivilegesRequired=lowest
DefaultDirName={userappdata}\screon
DisableDirPage=no
DefaultGroupName=screon
UninstallDisplayIcon={app}\screon.exe
Compression=lzma2
SolidCompression=yes
OutputDir=.
OutputBaseFilename=screon_installer
CreateAppDir=yes


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"

[Code]
function InitializeSetup(): Boolean;
begin
  // Убираем явное задание языка, так как Inno сам определит
  Result := True;
end;

[Setup]
LanguageDetectionMethod=uilanguage
ShowLanguageDialog=auto



[Files]
Source: "composeApp\build\compose\binaries\main\app\screon\*"; \
  DestDir: "{app}"; \
  Flags: ignoreversion recursesubdirs createallsubdirs
  
  

[Registry]
Root: HKCU; Subkey: "Software\JavaSoft\Prefs"; ValueName: "screen/Id"; Flags: deletevalue uninsdeletevalue
Root: HKCU; Subkey: "Software\JavaSoft\Prefs"; ValueName: "screenId"; Flags: deletevalue uninsdeletevalue

[Registry]
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
    ValueType: string; ValueName: "screon"; ValueData: """{app}\screon.exe"""; \
    Flags: uninsdeletevalue; Tasks: autorun

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
Name: "autorun"; Description: "Запускать при старте Windows"; GroupDescription: "Дополнительно"

[Registry]
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
    ValueType: string; ValueName: "screon"; ValueData: """{app}\screon.exe"""; \
    Flags: uninsdeletevalue; Tasks: autorun
    

[Run]
Filename: "{app}\screon.exe"; WorkingDir: "{app}"; \
  Description: "{cm:LaunchProgram,screon}"; \
  Flags: nowait postinstall runascurrentuser skipifsilent





