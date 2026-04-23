/*
 *  Copyright (C) 2013 Joos Kiener
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.

 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.kienerj.sdf_reader;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds a single entry of an SD-File. This includes the molfile(Ctab) and the
 * data properties.
 *
 * @author Joos Kiener <Joos.Kiener@gmail.com>
 */
@EqualsAndHashCode(callSuper = false, of = {"index"})
public class SdfRecord {

    @Getter
    private final int index;
    @Getter
    @Setter
    private String molfileName;
    @Getter
    @Setter
    private String molfile;
    @Getter
    @Setter
    private Map<String,String> properties;

    public SdfRecord(int index){

        this.properties = new HashMap<>();
        this.molfileName = "";
        this.index = index;
    }

    public void addProperty(String name, String value){
        properties.put(name, value);
    }

    public String getProperty(String key){
        return properties.get(key);
    }
}
