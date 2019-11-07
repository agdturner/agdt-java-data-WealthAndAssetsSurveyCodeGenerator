/*
 * Copyright 2018 geoagdt.
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
package uk.ac.leeds.ccg.andyt.generic.data.waas.core;

import java.io.Serializable;
import uk.ac.leeds.ccg.andyt.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_IO;

/**
 * @author Andy Turner
 */
public abstract class WaASCJ_Object implements Serializable {

    public transient final WaASCJ_Environment we;
    
    /**
     * For convenience
     */
    public transient final Generic_Environment env;
    public transient final Generic_IO io;

    public WaASCJ_Object(WaASCJ_Environment e) {
        this.we = e;
        env = e.env;
        io = env.io;
    }
}
