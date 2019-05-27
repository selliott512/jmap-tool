jmap-tool
=========

A very simple tool that uses [JMapProjLib](https://github.com/OSUCartography/JMapProjLib) to create map of a chosen projection given an equirectangular map as input.

Usage:
```txt
JMapTool [-tv] projection-name in-eqrect-image out-image
    -t Test each point by projecting it and comparing.
    -v Verbose. Show exterme map points and statistics.
```
Examples:

Use the [Equal Earth](http://equal-earth.com/) projection to create "/tmp/equalearth.jpg" based on "images/Equirectangular_projection_SW-cropped.jpg" (adjust the classpath as need be):
```txt
java -cp bin:JMapProjLib/dist/JMapProjLib.jar org.selliott.jmaptool.JMapTool -tv equalearth images/Equirectangular_projection_SW-cropped.jpg /tmp/equalearth.jpg
```
Output:
```txt
Extreme points (lot, lat -> x, y):
-3.1415926535897930, -1.5707963267948966 -> -1.6035886482840516, -1.3173627591574133
 0.0000000000000000, -1.5707963267948966 ->  0.0000000000000000, -1.3173627591574133
 3.1415926535897930, -1.5707963267948966 ->  1.6035886482840516, -1.3173627591574133
-3.1415926535897930,  0.0000000000000000 -> -2.7066299836960748,  0.0000000000000000
 0.0000000000000000,  0.0000000000000000 ->  0.0000000000000000,  0.0000000000000000
 3.1415926535897930,  0.0000000000000000 ->  2.7066299836960748,  0.0000000000000000
-3.1415926535897930,  1.5707963267948966 -> -1.6035886482840516,  1.3173627591574133
 0.0000000000000000,  1.5707963267948966 ->  0.0000000000000000,  1.3173627591574133
 3.1415926535897930,  1.5707963267948966 ->  1.6035886482840516,  1.3173627591574133

Width     :  5.4132599673921495
Height    :  2.6347255183148266
Ratio     :  2.0545821300028537
Pole/Eq   :  0.5924668898015567
Asymmetry :  0.0000000000000000

Converted "images/Equirectangular_projection_SW-cropped.jpg" to "/tmp/equalearth.jpg" in 1940 millis.
```

The "patches" directory contains patches for "JMapProjLib" that may be helpful.

Image "images/Equirectangular_projection_SW-cropped.jpg" is based on the image from Wikipedia page "Equirectangular projection". The image was uploaded on 22:26, 15 August 2011 by Daniel R. Strebe. The license is CC BY-SA 3.0, which can be found here:
    https://creativecommons.org/licenses/by-sa/3.0/
The original image was cropped slightly to remove a border, and resized slightly to make it so that it has precisely a 2 (width) to 1 (height) ratio.
