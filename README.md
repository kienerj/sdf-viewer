# FreeSDFViewer

FreeSDFViewer is a free and open-source desktop viewer for sd-files. It is targeted at users that quickly want to look and scan 
through any type of sd-file including very large ones (>100k of entries).

## SD-files

SD-files is a widely used format for storing and sharing chemical structures and 
properties of these structures. These files are often use to exchange chemical 
structure databases. Such files can contain thousands to millions of 
chemical structures.

## Features

* tabular display of records in the file including all properties
* resizeable chemical structure image column
* row index column
* variable row heights
* Support for very large sd-files with smooth scrolling

## Miscellaneous

The application can create an .index file for each sd-file you load. This is set
in the settings menu. By default the index is not saved. This file is created 
only once and loading the same file (must be unchanged!!!) again later will be 
much quicker. You may safely delete it. 

For rendering the chemical structures the [Indigo toolkit][1] is used.

## Trouble-shooting

When opening very large sd-files (million+ molecules) the file might not load.
In that case you need to close the application and start it from the command-line
so that you can increase the memory available to FreeSDFViewer. On the 
command-line enter

`javaw -Xmx512m -jar "C:\Program Files\FreeSDFViewer.jar"`

You must add the full path. Above assumes Windows and that you put the jar file
into the Program Files folder. If it still does not load increase the number in
`-Xmx512m` further. It denotes amount of memory to use in megabytes.

[1]: https://lifescience.opensource.epam.com/indigo/