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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
public class SdfFileFilter extends FileFilter {

    /**
     * Whether the given file is accepted by this filter.
     */
    @Override
    public boolean accept(File f) {

        if (f.isDirectory()) {
            return true;
        }

        String fileName = f.getName();
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension.equals("sdf");
    }

    /**
     * The description of this filter. For example: "JPG and GIF Images"
     *
     * @see FileView#getName
     */
    public String getDescription() {
        return "SD-files: *.sdf";
    }
}
