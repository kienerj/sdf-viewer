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
package io.github.kienerj.sdf_viewer;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;

import java.awt.*;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;
import java.awt.geom.AffineTransform;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

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

    private static final XLogger logger = XLoggerFactory.getXLogger("ChemicalStructureIcon");
    private final Indigo indigo;
    private final IndigoRenderer renderer;
    private final String structureData;
    private final IndigoObject mol;
    private int width;
    private int height;

    /**
     * <p> Creates a
     * <code>ChemicalStructureIcon</code> using the passed in parameters for
     * rendering the image. </p>
     *
     * @param structureData the chemical structure (mol block) for this row
     * @param indigo the indigo instance to use for rendering
     * @param renderer the indigo renderer to use for rendering
     * @param width initial width of the image
     * @param height initial height of the image
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
        indigo.setOption("image-resolution", 96);
        mol = indigo.loadMolecule(structureData);
        String smiles = getSmiles(mol);
        setDescription(smiles);
        Image image = renderImage();
        setImage(image);
    }

    private String getSmiles(IndigoObject mol) {
        String smiles = mol.canonicalSmiles();
        /*
         * Indigo may add SMILES extensions after the smiles string separated by
         * a space.
         * As example molfiles with stereo bonds but without chiral flag
         * get converted to smiles that have additional stereo info at end of
         * string:
         *
         * "CC[C@@H](N)[C@@H](O)CC |&1:2,4|"
         *
         * see http://www.chemaxon.com/marvin/help/formats/cxsmiles-doc.html
         *      - AND stereo group type:
         *          &<group>:<atomindex>,< atomindex>...
         *
         * This happens for non-chiral molecules with up/down bonds.
         *
         * if chiral flag is set, absolute stereo chemistry is assumed
         * and no SMILES extension is set.
         * Chiral flag is on molfile line 4, the 5th option (the 1):
         *      8  8  0  0  1  0  0  0  0  0999 V2000
         *
         * Below we optionally remove the extensions as they do not belong to
         * the SMILES specification. In above case they are from chemaxon.
         */
        int firstSpaceIndex = smiles.indexOf(" ");
        if (firstSpaceIndex != -1) {
            smiles = smiles.substring(0, firstSpaceIndex);
        }
        return smiles;
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

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform transform = g2d.getTransform();
        //logger.debug(transform.toString());

        // Dimensions for table cell size for Graphics2d
        int wg = c.getWidth() - x - insets.right;
        int hg = c.getHeight() - y - insets.bottom;
        // Dimensions for indigo renderer taking into account dpi scaling
        // This is required so that images look sharp and are not getting manipulated by awt
        int w = (int) (wg * transform.getScaleX());
        int h = (int) (hg * transform.getScaleY());
        //logger.debug("Rendering Chemical Structure image: W: {} H: {} scaleX: {} scaleY: {}",
                //w, h, transform.getScaleX(), transform.getScaleY());

        if (w != width || h != height) {
            if (w < 16 || h < 16) {
                // 16 pixels is minimum size supported by indigo
                return;
            }
            width = w;
            height = h;
        }

        indigo.setOption("render-image-size", w, h);
        image = renderImage();
        setImage(image);
        ImageObserver io = getImageObserver();
        g.drawImage(image, x, y, wg, hg, io == null ? c : io);
    }

    private Image renderImage()  {

        Image image = null;
        if (structureData != null) {
            if (mol != null) {
                if (!mol.hasCoord()) {
                    mol.layout();
                }
                byte[] imageData = renderer.renderToBuffer(mol);
                image = Toolkit.getDefaultToolkit().createImage(imageData);
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
