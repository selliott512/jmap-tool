// jmap-tool - A simple JMapProjLib client
// Copyright (C)2019 Steven Elliott
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// 02110-1301, USA.

package org.selliott.jmaptool;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * A very simple tool to exercise JMapProjLib.
 */
public class JMapTool
{
    /**
     * A bit bigger than some projections. Used for -t (test).
     */
    private static final double EPS = 1.0e-8;

    public static void main(String[] args) throws Exception
    {
        final long before = System.currentTimeMillis();
        if (args.length < 3)
        {
            System.err.println(
                    "JMapTool [-tv] projection-name in-eqrect-image out-image");
            System.err.println(
                    "    -t Test each point by projecting it and comparing.");
            System.err.println(
                    "    -v Verbose. Show exterme map points and statistics.");
            System.exit(1);
        }

        // Parse command line options. Must be first set of arguments.
        boolean test = false;
        boolean verbose = false;
        int i = 0; // Index to current argument being considered.
        while (args[i].startsWith("-"))
        {
            final String opts = args[i];
            for (int j = 1; j < opts.length(); j++)
            {
                final char optChar = opts.charAt(j);
                switch (optChar)
                {
                case 't':
                    test = true;
                    break;
                case 'v':
                    verbose = true;
                    break;
                default:
                    System.err.println("Unknown option character \"" + optChar + "\".");
                    System.exit(1);
                }
            }
            i++;
        }

        // Options must be first.
        for (int j = i; j < args.length; j++)
        {
            if (args[j].startsWith("-"))
            {
                System.err.println("Argument \"" + args[j] + "\" is an option "
                        + "argument after a non-option argument.");
                System.exit(1);
            }
        }

        // Parse remaining non-option arguments.
        final String projName = args[i++];
        final String inName = args[i++];
        final String outName = args[i++];

        final Projection proj = ProjectionFactory
                .getNamedPROJ4Projection(projName);
        if (proj == null)
        {
            System.err.println("Unknown projection \"" + projName + "\".");
            System.exit(1);
        }
        if (!proj.hasInverse())
        {
            // TODO: Get the inverse using Newton's method. Why doesn't
            // JMapProjLib use Newton's method by default when a particular
            // projection does not implement an inverse?
            System.err.println("Projection \"" + projName
                    + "\" does not have an inverse.");
            System.exit(1);
        }

        final Point2D.Double p = new Point2D.Double();
        final Point2D.Double p2 = new Point2D.Double();

        if (verbose)
        {
            double poleMinX = Double.MAX_VALUE;
            double poleMaxX = Double.MIN_VALUE;
            double eqMinX = Double.MAX_VALUE;
            double eqMaxX = Double.MIN_VALUE;
            double poleMinY = Double.MAX_VALUE;
            double poleMaxY = Double.MIN_VALUE;
            System.out.println("Extreme points (lot, lat -> x, y):");
            for (final double lat : new double[]{-Math.PI/2, 0, Math.PI/2})
            {
                for (final double lon : new double[]{-Math.PI, 0, Math.PI})
                {
                    proj.project(lon, lat, p);
                    if (lat == 0)
                    {
                        // Equator
                        eqMinX = Math.min(eqMinX, p.x);
                        eqMaxX = Math.max(eqMaxX, p.x);
                    }
                    else
                    {
                        // Poles
                        poleMinX = Math.min(poleMinX, p.x);
                        poleMaxX = Math.max(poleMaxX, p.x);
                        poleMinY = Math.min(poleMinY, p.y);
                        poleMaxY = Math.max(poleMaxY, p.y);
                    }
                    System.out.printf("%19.16f, %19.16f -> %19.16f, %19.16f\n",
                            lon, lat, p.x, p.y);
                }
            }
            final double eqWidth = eqMaxX - eqMinX;
            final double height = poleMaxY - poleMinY;
            System.out.printf("\nWidth     : %19.16f\nHeight    : %19.16f\n",
                    eqWidth, height);
            System.out.printf("Ratio     : %19.16f\n", eqWidth / height);
            final double poleWidth = poleMaxX - poleMinX;
            System.out.printf("Pole/Eq   : %19.16f\n", poleWidth / eqWidth);
            // A made up definition of asymmetry.
            final double asymmetry =
                    (Math.abs(eqMaxX) - Math.abs(eqMinX)) /
                    eqWidth +
                    (Math.abs(poleMaxY) - Math.abs(poleMinY)) /
                    (poleMaxY - poleMinY) +
                    (Math.abs(poleMaxX) - Math.abs(poleMinX)) /
                    (poleMaxX - poleMinX);
            System.out.printf("Asymmetry : %19.16f\n\n", asymmetry);
        }

        proj.project(Math.PI, 0, p);
        if (Double.isNaN(p.x) || Double.isNaN(p.y))
        {
            System.err.println("Could not determine maximum X");
            System.exit(1);
        }
        final double xMax = p.x;

        proj.project(0, Math.PI / 2, p);
        if (Double.isNaN(p.x) || Double.isNaN(p.y))
        {
            System.err.println("Could not determine maximum Y.");
            System.exit(1);
        }
        final double yMax = p.y;

        final BufferedImage inImg = ImageIO.read(new File(inName));
        final int height = inImg.getHeight();
        final int width = inImg.getWidth();
        final BufferedImage outImg = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int errors = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                final double xd = xMax
                        * (((x + 0.5) - (width / 2)) / (width / 2));
                final double yd = yMax
                        * (((y + 0.5) - (height / 2)) / (height / 2));
                proj.projectInverse(xd, yd, p);
                if (Double.isNaN(p.x) || Double.isNaN(p.y)
                        || Math.abs(p.x) >= Math.PI
                        || Math.abs(p.y) >= Math.PI / 2)
                {
                    continue;
                }
                if (test)
                {
                    proj.project(p.x, p.y, p2);
                    if (Math.abs(p2.x - xd) + Math.abs(p2.y - yd) > EPS)
                    {
                        if (errors++ == 0)
                        {
                            System.err.printf("Input pixel at %d, %d could not "
                                    + "be projected accurately. Not showing "
                                    + "additional errors.\n", x, y);
                        }
                    }
                }
                final int inX = (int) ((p.x + 3 * Math.PI)
                        * (width / (2 * Math.PI))) % width;
                final int inY = (int) ((p.y + 3 * Math.PI / 2)
                        * (height / (Math.PI))) % height;
                final int rgb = inImg.getRGB(inX, inY);
                outImg.setRGB(x, y, rgb);
            }
        }
        final String outType;
        int d = outName.lastIndexOf('.');
        if (d != -1)
        {
            outType = outName.substring(d + 1, outName.length()).toLowerCase();
        } else
        {
            outType = "JPG";
        }
        ImageIO.write(outImg, outType, new File(outName));
        System.out.printf("Converted \"%s\" to \"%s\" in %d millis.\n", inName,
                outName, System.currentTimeMillis() - before);
        if (errors > 0)
        {
            System.err.printf("%d projection errors.\n", errors);
            System.exit(1);
        }
    }
}
