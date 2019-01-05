rm -rf out
ant compile
mkdir out/f1 out/f2
cd out
java foldersync.FolderSync f1 f2
