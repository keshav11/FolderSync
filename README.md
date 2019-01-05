

[![Build Status](https://travis-ci.org/keshav11/FolderSync.svg?branch=master)](https://travis-ci.org/keshav11/FolderSync)      
# FolderSync

FolderSync is a service written in java that keeps the contents of folders in sync. The user can provide any number of folders to be kept in sync while starting the service and FolderSync would keep them in sync.
## Running FolderSync
Use the following steps to clone and run FolderSync

```
git clone https://github.com/keshav11/FolderSync.git
cd FolderSync
ant compile
cd out
java foldersync.FolderSync path_to_folder1 path_to_folder2
```

## License 
[MIT License](https://github.com/keshav11/FolderSync/blob/master/LICENSE)
