# Free SDF Viewer

Free SDF Viewer is a free and open-source desktop viewer for the chemical 
structure file format sdf. It has good performance and also works with very 
large SD-files. It is targeted at users that quickly want to look and scan 
through any type of sd-file.

## SD-files

SD-files is a widely used format for storing and sharing chemical structures and 
properties of these structures. These files are often use to exchange chemical 
structure databases. Such files can contain thousands to millions of 
chemical structures.

## Features

* tabular display of records in the file including all properties
* resizeable chemical structure image column
* row index column
* selected rows can be copy & pasted into Excel (Structure as SMILES)

## Screenshots

### Properties and row height

Below images shows all the properties in the SD-file and the last row is higher
to better fit the larger molecule:

![variable row height](https://googledrive.com/host/0B1KuZlTEmhZNam93R1FRSFBWX28/propertiesAndRowHeight.png)

### Viewing large SD-Files

This screenshot is an example of a rather large file. See the row number on the
left side of the table:

![large file example](https://googledrive.com/host/0B1KuZlTEmhZNam93R1FRSFBWX28/largeFileExample.png)

Scrolling remains smooth with very reasonable memory usage.

## Miscellaneous

The application can create an .index file for each sd-file you load. This is set
in the settings menu. By default the index is not saved. This file is created 
only once and loading the same file (must be unchanged!!!) again later will be 
much quicker. You may safely delete it. 

For rendering the chemical structures the [Indigo toolkit][1] is used.

[1]: http://ggasoftware.com/opensource/indigo