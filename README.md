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

## Miscellaneous

The application creates an .index file for each sd-file you load. This file is
created only once and loading the same file again later will be much quicker.
You may safely delete it. A future version will have the option to auto-remove
this index file when closing the application.

For rendering the chemical structures the [Indigo toolkit][1] is used.

[1]: http://ggasoftware.com/opensource/indigo