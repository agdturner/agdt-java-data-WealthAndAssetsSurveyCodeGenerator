/*
 * Copyright 2018 Andy Turner, CCG, University of Leeds.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.data.waascg.core;

import java.io.IOException;
import java.nio.file.Path;
import uk.ac.leeds.ccg.data.core.Data_Environment;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.data.waascg.io.WaASCG_Files;
import uk.ac.leeds.ccg.generic.io.Generic_IO;

/**
 * WaASCG_Environment
 * 
 * @author Andy Turner
 * @version 1.0.0
 */
public class WaASCG_Environment {

    public transient final Data_Environment de;
    public transient final WaASCG_Files files;
    public transient final Generic_Environment env;
    public transient final Generic_IO io;
    
    public transient final String EOL = System.getProperty("line.separator");
    public transient final byte W1 = 1;
    public transient final byte W2 = 2;
    public transient final byte W3 = 3;
    public transient final byte W4 = 4;
    public transient final byte W5 = 5;
    
    /**
     * Stores the number of waves in the WaAS
     */
    public transient final byte NWAVES = 5;

    public WaASCG_Environment(Data_Environment e, Path dataDir) throws IOException {
        de = e;
        env = e.env;
        io = env.io;
        files = new WaASCG_Files(dataDir);
    }
}
