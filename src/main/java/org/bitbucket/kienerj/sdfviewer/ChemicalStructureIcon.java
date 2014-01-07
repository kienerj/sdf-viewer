/*
 * Copyright (C) 2014 Joos Kiener <Joos.Kiener@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bitbucket.kienerj.sdfviewer;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.ggasoftware.indigo.IndigoRenderer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;

/**
 * <p>
 * <code>ChemicalStructureIcon</code> allows the rendering of a chemical
 * structure as an image in a swing component. The image automatically resizes
 * itself to the size of the container. </p>
 *
 * <p> This code was inspired by the <a
 * href="http://tips4java.wordpress.com/2012/03/31/stretch-icon/"><code>StretchIcon</code></a>.
 * </p>
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class ChemicalStructureIcon extends ImageIcon {

    private final Indigo indigo;
    private final IndigoRenderer renderer;
    private final String structureData;
    private final String smiles;
    private int width;
    private int height;

    /**
     * <p> Creates a
     * <code>ChemicalStructureIcon</code> using the passed in parameters for
     * rendering the image. </p>
     *
     * @param structureData
     * @param indigo
     * @param renderer
     * @param width
     * @param height
     */
    public ChemicalStructureIcon(String structureData, Indigo indigo,
            IndigoRenderer renderer, int width, int height) {
        super();
        this.indigo = indigo;
        this.renderer = renderer;
        this.structureData = structureData;
        this.width = width;
        this.height = height;
        indigo.setOption("render-image-size", width, height);
        IndigoObject mol = indigo.loadMolecule(structureData);
        this.smiles = mol.canonicalSmiles();
        setDescription(smiles);
        Image image = renderImage();
        setImage(image);
    }

    /**
     * <p> Paints the icon. The image is (re-)rendered to fit the component to
     * which it is painted. </p> <p> If this icon has no image observer,this
     * method uses the
     * <code>c</code> component as the observer. </p>
     *
     * @param c the component to which the Icon is painted. This is used as the
     * observer if this icon has no image observer
     * @param g the graphics context
     * @param x not used.
     * @param y not used.
     *
     * @see ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        Image image = getImage();
        if (image == null) {
            return;
        }
        Insets insets = ((Container) c).getInsets();
        x = insets.left;
        y = insets.top;

        int w = c.getWidth() - x - insets.right;
        int h = c.getHeight() - y - insets.bottom;

        if (w != width || h != height) {
            if (w < 16 || h < 16) {
                // 16 pixels is minsize supported by indigo
                return;
            }
            width = w;
            height = h;
            indigo.setOption("render-image-size", w, h);
            image = renderImage();
            setImage(image);
        }

        ImageObserver io = getImageObserver();
        g.drawImage(image, x, y, w, h, io == null ? c : io);
    }

    private Image renderImage() {
        Image image = null;
        if (structureData != null) {
            IndigoObject mol = indigo.loadMolecule(structureData);
            if (mol != null) {
                mol.layout();
                byte[] imageData = renderer.renderToBuffer(mol);
                // see ImageIcon constructor
                image = Toolkit.getDefaultToolkit().createImage(imageData);
                if (image == null) {
                    return null;
                }
            }
        }
        return image;
    }

    /**
     * Overridden to return 0. The size of this Icon is determined by the size
     * of the component.
     *
     * @return 0
     */
    @Override
    public int getIconWidth() {
        return 0;
    }

    /**
     * Overridden to return 0. The size of this Icon is determined by the size
     * of the component.
     *
     * @return 0
     */
    @Override
    public int getIconHeight() {
        return 0;
    }
}
