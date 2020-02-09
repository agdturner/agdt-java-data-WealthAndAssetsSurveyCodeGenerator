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
package uk.ac.leeds.ccg.data.waascg.process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.data.core.Data_Environment;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.data.waascg.core.WaASCG_Environment;
import uk.ac.leeds.ccg.data.waascg.core.WaASCG_Object;
import uk.ac.leeds.ccg.data.waascg.core.WaASCG_Strings;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.math.Math_Byte;
import uk.ac.leeds.ccg.math.Math_Double;
import uk.ac.leeds.ccg.math.Math_Integer;
import uk.ac.leeds.ccg.math.Math_Short;

/**
 * This class produces source code for loading the Wealth and Assets Survey
 * (WaAS) data. The source code is written to the output data directory. It
 * should then be copied to the agdt-java-generic-data-WealthAndAssetsSurvey
 * code base. Source code classes written in order to load the WaAS household
 * data are written to uk.ac.leeds.ccg.data.waas.data.hhold. Source code classes
 * written in order to load the WaAS person data are written to
 * uk.ac.leeds.ccg.data.waas.data.person.
 *
 * As the WaAS data contains many variables, it was thought best to write some
 * code that wrote some code to load these data and provide access to the
 * variables. Most variables are loaded as Double types. Some such as dates have
 * been loaded as String types. There are documents:
 * data/input/WaAS/UKDA-7215-tab/mrdoc/pdf/7215_was_questionnaire_wave_1.pdf
 * data/input/WaAS/UKDA-7215-tab/mrdoc/pdf/7215_was_questionnaire_wave_2.pdf
 * data/input/WaAS/UKDA-7215-tab/mrdoc/pdf/7215_was_questionnaire_wave_3.pdf
 * data/input/WaAS/UKDA-7215-tab/mrdoc/pdf/7215_was_questionnaire_wave_4.pdf
 * data/input/WaAS/UKDA-7215-tab/mrdoc/pdf/7215_was_questionnaire_wave_5.pdf
 * that detail what values are expected from what variables. Another way to
 * create the data loading classes would be to parse these documents. A thorough
 * job of exploring these data would check the data values to make sure that
 * they conformed to these schemas. This would also allow the variables to be
 * stored in the most appropriate way (e.g. as an integer, double, String, date
 * etc.).
 *
 * @author Andy Turner
 * @version 1.0.0
 */
public class WaASCG_Main extends WaASCG_Object {

    public final HashMap<Integer, String> indents;
    public final String indent;

//    protected WaASCG_Main() {
//        super();
//    }
    public WaASCG_Main(WaASCG_Environment env) {
        super(env);
        indents = new HashMap<>();
        indent = "    ";
    }

    public static void main(String[] args) {
        try {
            Path dataDir = Paths.get(System.getProperty("user.home"),
                    WaASCG_Strings.s_data, WaASCG_Strings.s_data,
                    WaASCG_Strings.s_WaAS);
            Data_Environment de = new Data_Environment(new Generic_Environment(
                    new Generic_Defaults(dataDir)));
            WaASCG_Environment e = new WaASCG_Environment(de, de.files.getDir());
            WaASCG_Main p = new WaASCG_Main(e);
            String type;
            Path outdir;
//            // hhold
//            type = WaASCG_Strings.s_hhold;
//            Object[]  hholdTypes = p.getFieldTypes(type);
//            outdir = p.run(type, hholdTypes);
//            de.env.log("Generated code was written to " + outdir.toString());
            // person
            type = WaASCG_Strings.s_person;
            Object[] personTypes = p.getFieldTypes(type);
            p.run(type, personTypes);
            outdir = p.run(type, personTypes);
            de.env.log("Generated code was written to " + outdir.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Pass through the collections and works out what numeric type is best to
     * store each field in the collections.
     *
     * @param type
     * @return keys are standardised field names, value is: 0 if field is to be
     * represented by a String; 1 if field is to be represented by a double; 2
     * if field is to be represented by a int; 3 if field is to be represented
     * by a short; 4 if field is to be represented by a byte; 5 if field is to
     * be represented by a boolean.
     * @throws java.io.IOException If one is encountered.
     */
    protected Object[] getFieldTypes(String type) throws IOException {
        int nwaves = we.NWAVES;
        Object[] r = new Object[4];
        Path indir = we.files.getInputDir();
        Path generateddir = we.files.getGeneratedDir();
        Path outdir = Paths.get(generateddir.toString(), WaASCG_Strings.s_Subsets);
        Files.createDirectories(outdir);
        HashMap<String, Integer>[] allFieldTypes = new HashMap[nwaves];
        String[][] headers = new String[nwaves][];
        HashMap<String, Byte>[] v0ms = new HashMap[nwaves];
        HashMap<String, Byte>[] v1ms = new HashMap[nwaves];
        for (int w = 0; w < nwaves; w++) {
            Object[] t = loadTest(w + 1, type, indir);
            HashMap<String, Integer> fieldTypes = new HashMap<>();
            allFieldTypes[w] = fieldTypes;
            String[] fields = (String[]) t[0];
            headers[w] = fields;
            boolean[] strings = (boolean[]) t[1];
            boolean[] doubles = (boolean[]) t[2];
            boolean[] ints = (boolean[]) t[3];
            boolean[] shorts = (boolean[]) t[4];
            boolean[] bytes = (boolean[]) t[5];
            boolean[] booleans = (boolean[]) t[6];
            HashMap<String, Byte> v0m = (HashMap<String, Byte>) t[7];
            HashMap<String, Byte> v1m = (HashMap<String, Byte>) t[8];
            v0ms[w] = v0m;
            v1ms[w] = v1m;
            for (int i = 0; i < strings.length; i++) {
                String field = fields[i];
                if (strings[i]) {
                    System.out.println("" + i + " " + "String");
                    fieldTypes.put(field, 0);
                } else {
                    if (doubles[i]) {
                        System.out.println("" + i + " " + "double");
                        fieldTypes.put(field, 1);
                    } else {
                        if (ints[i]) {
                            System.out.println("" + i + " " + "int");
                            fieldTypes.put(field, 2);
                        } else {
                            if (shorts[i]) {
                                System.out.println("" + i + " " + "short");
                                fieldTypes.put(field, 3);
                            } else {
                                if (bytes[i]) {
                                    System.out.println("" + i + " " + "byte");
                                    fieldTypes.put(field, 4);
                                } else {
                                    if (booleans[i]) {
                                        System.out.println("" + i + " " + "boolean");
                                        fieldTypes.put(field, 5);
                                    } else {
                                        try {
                                            throw new Exception("unrecognised type");
                                        } catch (Exception ex) {
                                            ex.printStackTrace(System.err);
                                            Logger.getLogger(WaASCG_Main.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        HashMap<String, Integer> consolidatedFieldTypes = new HashMap<>();
        consolidatedFieldTypes.putAll(allFieldTypes[0]);
        for (int w = 1; w < nwaves; w++) {
            HashMap<String, Integer> fieldTypes = allFieldTypes[w];
            Iterator<String> ite = fieldTypes.keySet().iterator();
            while (ite.hasNext()) {
                String field = ite.next();
                int fieldType = fieldTypes.get(field);
                if (consolidatedFieldTypes.containsKey(field)) {
                    int consolidatedFieldType = consolidatedFieldTypes.get(field);
                    if (fieldType != consolidatedFieldType) {
                        consolidatedFieldTypes.put(field,
                                Math.min(fieldType, consolidatedFieldType));
                    }
                } else {
                    consolidatedFieldTypes.put(field, fieldType);
                }
            }
        }
        r[0] = consolidatedFieldTypes;
        r[1] = headers;
        r[2] = v0ms;
        r[3] = v1ms;
        return r;
    }

    /**
     *
     * @param wave
     * @param TYPE
     * @param indir
     * @return
     * @throws java.io.FileNotFoundException If one is encountered.
     */
    public Object[] loadTest(int wave, String TYPE, Path indir)
            throws FileNotFoundException, IOException {
        String m = "loadTest(wave=" + wave + ", Type=" + TYPE + ", indir="
                + indir.toString() + ")";
        env.logStartTag(m);
        Object[] r = new Object[9];
        HashMap<String, Byte> v0m = new HashMap<>();
        HashMap<String, Byte> v1m = new HashMap<>();
        //Path f = getInputFile(wave, TYPE, indir);
        Path f = we.files.getInputFile((byte) wave, TYPE);
        String line;
        try (BufferedReader br = Generic_IO.getBufferedReader(f)) {
            line = br.lines().findFirst().get();
        }
        String[] fields = parseHeader(line, wave);
        int n = fields.length;
        boolean[] strings = new boolean[n];
        boolean[] doubles = new boolean[n];
        boolean[] ints = new boolean[n];
        boolean[] shorts = new boolean[n];
        boolean[] bytes = new boolean[n];
        boolean[] booleans = new boolean[n];
        byte[] v0 = new byte[n];
        byte[] v1 = new byte[n];
        for (int i = 0; i < n; i++) {
            strings[i] = false;
            doubles[i] = false;
            ints[i] = false;
            shorts[i] = false;
            //bytes[i] = true;
            bytes[i] = false;
            booleans[i] = true;
            v0[i] = Byte.MIN_VALUE;
            v1[i] = Byte.MIN_VALUE;
        }
        try (BufferedReader br = Generic_IO.getBufferedReader(f)) {
            br.readLine(); // Skip header.
            line = br.readLine();
            while (line != null) {
                String[] split = line.split("\t");
                for (int i = 0; i < n; i++) {
                    parse(split[i], fields[i], i, strings, doubles, ints, shorts,
                            bytes, booleans, v0, v1, v0m, v1m);
                }
                line = br.readLine();
            }
        }
        /**
         * Order v0m and v1m so that v0m always has the smaller value and v1m
         * the larger.
         */
        Iterator<String> ite = v0m.keySet().iterator();
        while (ite.hasNext()) {
            String s = ite.next();
            byte v00 = v0m.get(s);
            if (v1m.containsKey(s)) {
                byte v11 = v1m.get(s);
                if (v00 > v11) {
                    v0m.put(s, v11);
                    v1m.put(s, v00);
                }
            }
        }
        r[0] = fields;
        r[1] = strings;
        r[2] = doubles;
        r[3] = ints;
        r[4] = shorts;
        r[5] = bytes;
        r[6] = booleans;
        r[7] = v0m;
        r[8] = v1m;
        env.logEndTag(m);
        return r;
    }

    /**
     * If s can be represented as a byte reserving Byte.Min_Value for a
     * noDataValue,
     *
     * @param s
     * @param field
     * @param index
     * @param strings
     * @param doubles
     * @param ints
     * @param shorts
     * @param bytes
     * @param booleans
     * @param v0
     * @param v1
     * @param v0m
     * @param v1m
     */
    public void parse(String s, String field, int index, boolean[] strings,
            boolean[] doubles, boolean[] ints, boolean[] shorts,
            boolean[] bytes, boolean[] booleans, byte[] v0, byte[] v1,
            HashMap<String, Byte> v0m, HashMap<String, Byte> v1m) {
        if (!s.trim().isEmpty()) {
            if (!strings[index]) {
                if (doubles[index]) {
                    doDouble(s, index, strings, doubles);
                } else {
                    if (ints[index]) {
                        doInt(s, index, strings, doubles, ints);
                    } else {
                        if (shorts[index]) {
                            doShort(s, index, strings, doubles, ints, shorts);
                        } else {
                            if (bytes[index]) {
                                doByte(s, index, strings, doubles, ints,
                                        shorts, bytes);
                            } else {
                                if (booleans[index]) {
                                    if (Math_Byte.isByte(s)) {
                                        byte b = Byte.valueOf(s);
                                        if (v0[index] > Byte.MIN_VALUE) {
                                            if (!(b == v0[index])) {
                                                if (v1[index] > Byte.MIN_VALUE) {
                                                    if (!(b == v1[index])) {
                                                        booleans[index] = false;
                                                        bytes[index] = true;
                                                    }
                                                } else {
                                                    v1[index] = b;
                                                    v1m.put(field, b);
                                                }
                                            }
                                        } else {
                                            v0[index] = b;
                                            v0m.put(field, b);
                                        }
                                    } else {
                                        booleans[index] = false;
                                        shorts[index] = true;
                                        doShort(s, index, strings, doubles, ints,
                                                shorts);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void doByte(String s, int index, boolean[] strings,
            boolean[] doubles, boolean[] ints, boolean[] shorts,
            boolean[] bytes) {
        if (!Math_Byte.isByte(s)) {
            bytes[index] = false;
            shorts[index] = true;
            doShort(s, index, strings, doubles, ints, shorts);
        }
    }

    protected void doShort(String s, int index, boolean[] strings,
            boolean[] doubles, boolean[] ints, boolean[] shorts) {
        if (!Math_Short.isShort(s)) {
            shorts[index] = false;
            ints[index] = true;
            doInt(s, index, strings, doubles, ints);
        }
    }

    protected void doInt(String s, int index, boolean[] strings,
            boolean[] doubles, boolean[] ints) {
        if (!Math_Integer.isInt(s)) {
            ints[index] = false;
            doubles[index] = true;
            doDouble(s, index, strings, doubles);
        }
    }

    protected void doDouble(String s, int index, boolean[] strings,
            boolean[] doubles) {
        if (!Math_Double.isDouble(s)) {
            doubles[index] = false;
            strings[index] = true;
        }
    }

    /**
     *
     * @param type
     * @param types
     * @return The output directory path.
     * @throws IOException
     */
    public Path run(String type, Object[] types) throws IOException {
        int nwaves = we.NWAVES;
        HashMap<String, Integer> fieldTypes = (HashMap<String, Integer>) types[0];
        String[][] headers = (String[][]) types[1];
        HashMap<String, Byte>[] v0ms = (HashMap<String, Byte>[]) types[2];
        HashMap<String, Byte>[] v1ms = (HashMap<String, Byte>[]) types[3];
        TreeSet<String>[] fields = getFields(headers);
        HashMap<String, Byte> v0m0 = setCommonBooleanMaps(v0ms, v1ms, fields, fieldTypes);
        Path outdir = Paths.get(we.files.getOutputDir().toString(),
                WaASCG_Strings.s_src, WaASCG_Strings.s_main,
                WaASCG_Strings.s_java, WaASCG_Strings.s_uk, WaASCG_Strings.s_ac,
                WaASCG_Strings.s_leeds, WaASCG_Strings.s_ccg,
                WaASCG_Strings.s_data, WaASCG_Strings.s_waas,
                WaASCG_Strings.s_data, type);
        Files.createDirectories(outdir);
        String packageName = "uk.ac.leeds.ccg.data.waas.data." + type;
        String extendedClassName;
        String prepend = WaASCG_Strings.s_WaAS + WaASCG_Strings.symbol_underscore;
        type = type.toUpperCase().substring(0, 1);

        for (int w = 0; w <= nwaves + 3; w++) {
            if (w < nwaves) {
                // Classes
                int wave = w + 1;
                HashMap<String, Byte> v0m = v0ms[w];
                String  className = prepend + "W" + wave + type + "Record";
                Path fout = Paths.get(outdir.toString(), className + ".java");
                try (PrintWriter pw = Generic_IO.getPrintWriter(fout, false)) {
                    writeHeaderPackageAndImports(pw, packageName, getImports0());
                    boolean isAbstract = false;
                    switch (w) {
                        case 0:
                            extendedClassName = prepend + "W1W2" + type + "Record";
                            break;
                        case 1:
                            extendedClassName = prepend + "W1W2" + type + "Record";
                            break;
                        case 2:
                            extendedClassName = prepend + "W3W4W5" + type + "Record";
                            break;
                        case 3:
                            extendedClassName = prepend + "W4W5" + type + "Record";
                            break;
                        case 4:
                            extendedClassName = prepend + "W4W5" + type + "Record";
                            break;
                        default:
                            extendedClassName = "";
                            break;
                    }
                    printClassDeclarationSerialVersionUID(pw, packageName,
                            className, isAbstract, "", extendedClassName);
                    // Print Field Declarations Inits And Getters
                    printFieldDeclarationsInitsAndGetters(pw, fields[w], fieldTypes,
                            v0m);
                    // Constructor
                    printConstructor(pw, className, headers, w);
                }
            } else {
                // Abstract classes
                boolean isAbstract = true;
                if (w == nwaves) {
                    String className = prepend + "W1W2W3W4W5" + type + "Record";
                    Path fout = Paths.get(outdir.toString(), className + ".java");
                    try (PrintWriter pw = Generic_IO.getPrintWriter(fout, false)) {
                        writeHeaderPackageAndImports(pw, packageName, getImports1());
                        //String implementations = "Serializable";
                        String implementations = "";
                        printClassDeclarationSerialVersionUID(pw, packageName,
                                className, isAbstract, implementations, "Data_Record");
                        pw.println();
                        pw.println(getIndent(1) + "protected String[] s;");
                        printConstructor(pw, className);
                        // Print Field Declarations Inits And Getters
                        printFieldDeclarationsInitsAndGetters(pw, fields[w], fieldTypes, v0m0);
                        pw.println("}");
                    }
                } else if (w == (nwaves + 1)) {
                    String className = prepend + "W1W2" + type + "Record";
                    Path fout = Paths.get(outdir.toString(), className + ".java");
                    try (PrintWriter pw = Generic_IO.getPrintWriter(fout, false)) {
                        writeHeaderPackageAndImports(pw, packageName, getImports0());
                        extendedClassName = prepend + "W1W2W3W4W5" + type + "Record";
                        printClassDeclarationSerialVersionUID(pw, packageName,
                                className, isAbstract, "", extendedClassName);
                        printConstructor(pw, className);
                        // Print Field Declarations Inits And Getters
                        printFieldDeclarationsInitsAndGetters(pw, fields[w], fieldTypes, v0m0);
                        pw.println("}");
                    }
                } else if (w == (nwaves + 2)) {
                    String className = prepend + "W3W4W5" + type + "Record";
                    Path fout = Paths.get(outdir.toString(), className + ".java");
                    try (PrintWriter pw = Generic_IO.getPrintWriter(fout, false)) {
                        writeHeaderPackageAndImports(pw, packageName, getImports0());
                        extendedClassName = prepend + "W1W2W3W4W5" + type + "Record";
                        printClassDeclarationSerialVersionUID(pw, packageName,
                                className, isAbstract, "", extendedClassName);
                        printConstructor(pw, className);
                        // Print Field Declarations Inits And Getters
                        printFieldDeclarationsInitsAndGetters(pw, fields[w], fieldTypes, v0m0);
                        pw.println("}");
                    }
                } else if (w == (nwaves + 3)) {
                    String className = prepend + "W4W5" + type + "Record";
                    Path fout = Paths.get(outdir.toString(), className + ".java");
                    try (PrintWriter pw = Generic_IO.getPrintWriter(fout, false)) {
                        writeHeaderPackageAndImports(pw, packageName, getImports0());
                        extendedClassName = prepend + "W3W4W5" + type + "Record";
                        printClassDeclarationSerialVersionUID(pw, packageName,
                                className, isAbstract, "", extendedClassName);
                        printConstructor(pw, className);
                        // Print Field Declarations Inits And Getters
                        printFieldDeclarationsInitsAndGetters(pw, fields[w], fieldTypes, v0m0);
                        pw.println("}");
                    }
                }
            }
        }
        return outdir;
    }

    public void printGetID(PrintWriter pw) {
        pw.println();
        pw.println(getIndent(1) + "@Override");
        pw.println(getIndent(1) + "public WaAS_RecordID getID() {");
        pw.println(getIndent(2) + "return (WaAS_RecordID) id;");
        pw.println(getIndent(1) + "}");
    }

    public String getIndent(int i) {
        if (!indents.containsKey(i)) {
            String s = "";
            for (int j = 0; j < i; j++) {
                s += indent;
            }
            indents.put(i, s);
            return s;
        }
        return indents.get(i);
    }

    /**
     *
     * @param pw
     * @param className
     */
    public void printConstructor(PrintWriter pw, String className) {
        pw.println();
        pw.println(getIndent(1) + className + "(WaAS_RecordID i){");
        pw.println(getIndent(2) + "super(i);");
        pw.println(getIndent(1) + "}");
    }

    /**
     *
     * @param pw
     * @param className
     * @param headers
     * @param w
     */
    public void printConstructor(PrintWriter pw, String className, String[][] headers, int w) {
        pw.println();
        pw.println(getIndent(1) + "public " + className + "(WaAS_RecordID i, String line) throws Exception {");
        pw.println(getIndent(2) + "super(i);");
        pw.println(getIndent(2) + "s = line.split(\"\\t\");");
        for (int j = 0; j < headers[w].length; j++) {
            pw.println(getIndent(2) + "init" + headers[w][j] + "(s[" + j + "]);");
        }
        pw.println(getIndent(1) + "}");
        printGetID(pw);
        pw.println("}");
    }

    private ArrayList<String> imports0;

    private ArrayList<String> getImports0() {
        if (imports0 == null) {
            imports0 = new ArrayList<>();
            imports0.add("uk.ac.leeds.ccg.data.waas.data.id.WaAS_RecordID");
        }
        return imports0;
    }

    private ArrayList<String> imports1;

    private ArrayList<String> getImports1() {
        if (imports1 == null) {
            imports1 = new ArrayList<>();
            imports1.addAll(getImports0());
            imports1.add("uk.ac.leeds.ccg.data.Data_Record");
        }
        return imports1;
    }

    /**
     *
     * @param pw
     * @param packageName
     * @param imports
     */
    public void writeHeaderPackageAndImports(PrintWriter pw,
            String packageName, ArrayList<String> imports) {
        pw.println("/**");
        pw.println(" * Source code generated by " + this.getClass().getName());
        pw.println(" */");
        pw.println();
        pw.println("package " + packageName + ";");
        if (imports != null) {
            imports.stream().forEach(i -> pw.println("import " + i + ";"));
        }
        pw.flush();
    }

    /**
     *
     * @param pw
     * @param packageName
     * @param className
     * @param isAbstract If true then the class is declared abstract.
     * @param implementations
     * @param extendedClassName
     */
    public void printClassDeclarationSerialVersionUID(PrintWriter pw,
            String packageName, String className, boolean isAbstract,
            String implementations, String extendedClassName) {
        pw.println();
        pw.print("public ");
        if (isAbstract) {
            pw.print("abstract ");
        }
        pw.print("class " + className);
        if (!extendedClassName.isEmpty()) {
            pw.print(" extends " + extendedClassName);
        }
        if (!implementations.isEmpty()) {
            pw.print(" implements " + implementations);
        }
        pw.println(" {");
        pw.println();
        /**
         * This is not included for performance reasons.
         */
        /**
         * pw.println("private static final long serialVersionUID = " +
         * serialVersionUID + ";");
         */
        //pw.flush();
    }

    /**
     * @param pw
     * @param fields
     * @param fieldTypes
     * @param v0
     */
    public void printFieldDeclarationsInitsAndGetters(PrintWriter pw,
            TreeSet<String> fields, HashMap<String, Integer> fieldTypes,
            HashMap<String, Byte> v0) {
        // Field declarations
        printFieldDeclarations(pw, fields, fieldTypes);
        // Field init
        printFieldInits(pw, fields, fieldTypes, v0);
        // Field getters
        printFieldGetters(pw, fields, fieldTypes);
    }

    /**
     * @param pw
     * @param fields
     * @param fieldTypes
     */
    public void printFieldDeclarations(PrintWriter pw, TreeSet<String> fields,
            HashMap<String, Integer> fieldTypes) {
        Iterator<String> ite = fields.iterator();
        while (ite.hasNext()) {
            String field = ite.next();
            int fieldType = fieldTypes.get(field);
            pw.print(getIndent(1));
            switch (fieldType) {
                case 0:
                    pw.println("protected String " + field + ";");
                    break;
                case 1:
                    pw.println("protected double " + field + ";");
                    break;
                case 2:
                    pw.println("protected int " + field + ";");
                    break;
                case 3:
                    pw.println("protected short " + field + ";");
                    break;
                case 4:
                    pw.println("protected byte " + field + ";");
                    break;
                default:
                    pw.println("protected boolean " + field + ";");
                    break;
            }
            pw.println();
        }
        //pw.flush();
    }

    /**
     *
     * @param pw
     * @param fields
     * @param fieldTypes
     */
    public void printFieldGetters(PrintWriter pw, TreeSet<String> fields,
            HashMap<String, Integer> fieldTypes) {
        Iterator<String> ite = fields.iterator();
        while (ite.hasNext()) {
            String field = ite.next();
            int fieldType = fieldTypes.get(field);
            pw.print(getIndent(1));
            switch (fieldType) {
                case 0:
                    pw.println("public String get" + field + "() {");
                    break;
                case 1:
                    pw.println("public double get" + field + "() {");
                    break;
                case 2:
                    pw.println("public int get" + field + "() {");
                    break;
                case 3:
                    pw.println("public short get" + field + "() {");
                    break;
                case 4:
                    pw.println("public byte get" + field + "() {");
                    break;
                default:
                    pw.println("public boolean get" + field + "() {");
                    break;
            }
            pw.println(getIndent(2) + "return " + field + ";");
            pw.println(getIndent(1) + "}");
            pw.println();
        }
        pw.flush();
    }

    /**
     *
     * @param pw
     * @param fields
     * @param fieldTypes
     * @param v0
     */
    public void printFieldInits(PrintWriter pw, TreeSet<String> fields,
            HashMap<String, Integer> fieldTypes, HashMap<String, Byte> v0) {
        Iterator<String> ite = fields.iterator();
        while (ite.hasNext()) {
            String field = ite.next();
            int fieldType = fieldTypes.get(field);
            pw.print(getIndent(1));
            switch (fieldType) {
                case 0:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + field + " = s;");
                    break;
                case 1:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + field + " = Double.parseDouble(s);");
                    pw.println(getIndent(2) + "} else {");
                    pw.println(getIndent(3) + field + " = Double.NaN;");
                    break;
                case 2:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + field + " = Integer.parseInt(s);");
                    pw.println(getIndent(2) + "} else {");
                    pw.println(getIndent(3) + field + " = Integer.MIN_VALUE;");
                    break;
                case 3:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + field + " = Short.parseShort(s);");
                    pw.println(getIndent(2) + "} else {");
                    pw.println(getIndent(3) + field + " = Short.MIN_VALUE;");
                    break;
                case 4:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + field + " = Byte.parseByte(s);");
                    pw.println(getIndent(2) + "} else {");
                    pw.println(getIndent(3) + field + " = Byte.MIN_VALUE;");
                    break;
                default:
                    pw.println("protected final void init" + field + "(String s) {");
                    pw.println(getIndent(2) + "if (!s.trim().isEmpty()) {");
                    pw.println(getIndent(3) + "byte b = Byte.parseByte(s);");
                    if (v0.get(field) == null) {
                        pw.println(getIndent(3) + field + " = false;");
                    } else {
                        pw.println(getIndent(3) + "if (b == " + v0.get(field) + ") {");
                        pw.println(getIndent(4) + field + " = false;");
                        pw.println(getIndent(3) + "} else {");
                        pw.println(getIndent(4) + field + " = true;");
                        pw.println(getIndent(3) + "}");
                    }
                    break;
            }
            pw.println(getIndent(2) + "}");
            pw.println(getIndent(1) + "}");
            pw.println();
        }
        //pw.flush();
    }

    /**
     * Thinking to returns a lists of IDs...
     *
     * @param header
     * @param wave
     * @return
     */
    public String[] parseHeader(String header, int wave) {
        String[] r;
        String ws = "W" + wave;
        String keyIdentifier1 = "CASE" + ws;
        String keyIdentifier2 = "PERSON" + ws;
        String uniqueString1 = "uniqueString1";
        String uniqueString2 = "uniqueString2";
        String h1 = header.toUpperCase();
        try {
            if (h1.contains(uniqueString1)) {
                throw new Exception(uniqueString1 + " is not unique!");
            }
            if (h1.contains(uniqueString2)) {
                throw new Exception(uniqueString2 + " is not unique!");
            }
        } catch (Exception ex) {
            Logger.getLogger(WaASCG_Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        h1 = h1.replaceAll("\t", " ,");
        h1 = h1 + " ";
        h1 = h1.replaceAll(keyIdentifier1, uniqueString1);
        h1 = h1.replaceAll(keyIdentifier2, uniqueString2);
        h1 = h1.replaceAll(ws + " ", " ");
        h1 = h1.replaceAll(" " + ws, " ");
        h1 = h1.replaceAll(ws + "_", "_");
        h1 = h1.replaceAll("_" + ws, "_");
        h1 = h1.replaceAll(ws + " ", "___" + ws + " ");
        h1 = h1.trim();
        h1 = h1.replaceAll(" ,", "\t");
        h1 = h1.replaceAll(uniqueString1, keyIdentifier1);
        h1 = h1.replaceAll(uniqueString2, keyIdentifier2);
        r = h1.split("\t");
        return r;
    }

    protected HashMap<String, Byte> setCommonBooleanMaps(
            HashMap<String, Byte>[] v0ms, HashMap<String, Byte>[] v1ms,
            TreeSet<String>[] allFields, HashMap<String, Integer> fieldTypes) {
        TreeSet<String> fields = allFields[5];
        HashMap<String, Byte> v0m1 = new HashMap<>();
        HashMap<String, Byte> v1m1 = new HashMap<>();
        Iterator<String> ites0 = fields.iterator();
        while (ites0.hasNext()) {
            String field0 = ites0.next();
            if (fieldTypes.get(field0) == 5) {
                for (int w = 0; w < v0ms.length; w++) {
                    HashMap<String, Byte> v0m = v0ms[w];
                    HashMap<String, Byte> v1m = v1ms[w];
                    Iterator<String> ites1 = v0m.keySet().iterator();
                    while (ites1.hasNext()) {
                        String field1 = ites1.next();
                        if (field0.equalsIgnoreCase(field1)) {
                            byte v0 = v0m.get(field1);
                            Byte v1;
                            if (v1m == null) {
                                v1 = Byte.MIN_VALUE;
                            } else {
                                //System.out.println("field1 " + field1);
                                //System.out.println("field1 " + field1);
                                v1 = v1m.get(field1);
                                if (v1 == null) {
                                    v1 = Byte.MIN_VALUE;
                                }
                            }
                            Byte v01 = v0m1.get(field1);
                            Byte v11 = v1m1.get(field1);
                            if (v01 == null) {
                                v0m1.put(field1, v0);
                            } else {
                                if (v01 != v0) {
                                    // Field better stored as a byte than boolean.
                                    fieldTypes.put(field1, 4);
                                }
                                if (v11 == null) {
                                    v1m1.put(field1, v1);
                                } else {
                                    if (v1 != v11.byteValue()) {
                                        // Field better stored as a byte than boolean.
                                        fieldTypes.put(field1, 4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return v0m1;
    }

    /**
     * Finds and returns r where.
     * <ul>
     * <li>r[0] are the fields in common with all waves.</li>
     * <li>r[1] are the fields in common with all waves.</li>
     * <li>r[2] are the fields in common with all waves.</li>
     * <li>r[3] are the fields in common with all waves.</li>
     * <li>r[4] are the fields in common with all waves.</li>
     * <li>r[5] fields common to waves 1, 2, 3, 4 and 5 (12345)</li>
     * <li>r[6] fields other than 12345 that are common to waves 1 and 2
     * (12).</li>
     * <li>r[7] fields other than 12345 that are in common to waves 3, 4 and 5
     * (345)</li>
     * <li>r[8] fields other than 345 that are in common to waves 4 and 5
     * (45)</li>
     * </ul>
     *
     * @param headers
     * @return
     */
    public TreeSet<String>[] getFields(String[][] headers) {
        TreeSet<String>[] r;
        int size = headers.length;
        r = new TreeSet[(size * 2) - 1];
        for (int i = 0; i < 5; i++) {
            r[i] = getFields(headers[i]);
        }
        // Get fields common to waves 1, 2, 3, 4 and 5 (12345)
        r[5] = getFieldsInCommon(r[0], r[1], r[2], r[3], r[4]);
        System.out.println("Number of fields common to waves 1, 2, 3, 4 and 5"
                + " (12345) " + r[5].size());
        // Get fields other than 12345 that are common to waves 1 and 2 (12)
        r[6] = getFieldsInCommon(r[0], r[1], null, null, null);
        r[6].removeAll(r[5]);
        System.out.println("Number of fields other than 12345 that are common"
                + " to waves 1 and 2 (12) " + r[6].size());
        // Get fields other than 12345 that are in common to waves 3, 4 and 5 (345)
        r[7] = getFieldsInCommon(r[2], r[3], r[4], null, null);
        r[7].removeAll(r[5]);
        System.out.println("Number of fields other than 12345 that are in "
                + "common to waves 3, 4 and 5 (345) " + r[7].size());
        // Get fields other than 345 that are in common to waves 4 and 5 (45)
        r[8] = getFieldsInCommon(r[3], r[4], null, null, null);
        r[8].removeAll(r[5]);
        r[8].removeAll(r[7]);
        System.out.println("Number of fields other than 345 that are in common "
                + "to waves 4 and 5 (45) " + r[8].size());
        r[0].removeAll(r[5]);
        r[0].removeAll(r[6]);
        r[1].removeAll(r[5]);
        r[1].removeAll(r[6]);
        r[2].removeAll(r[5]);
        r[2].removeAll(r[7]);
        r[3].removeAll(r[5]);
        r[3].removeAll(r[7]);
        r[3].removeAll(r[8]);
        r[4].removeAll(r[5]);
        r[4].removeAll(r[7]);
        r[4].removeAll(r[8]);
        return r;
    }

    /**
     * Finds and returns those fields that are in common and those fields .
     * result[0] are the fields in common with all.
     *
     * @param headers
     * @return
     */
    public ArrayList<String>[] getFieldsList(ArrayList<String> headers) {
        ArrayList<String>[] r;
        int size = headers.size();
        r = new ArrayList[size];
        Iterator<String> ite = headers.iterator();
        int i = 0;
        while (ite.hasNext()) {
            r[i] = getFieldsList(ite.next());
            i++;
        }
        return r;
    }

    /**
     *
     * @param fields
     * @return
     */
    public TreeSet<String> getFields(String[] fields) {
        TreeSet<String> r = new TreeSet<>();
        r.addAll(Arrays.asList(fields));
        return r;
    }

    /**
     *
     * @param s
     * @return
     */
    public ArrayList<String> getFieldsList(String s) {
        ArrayList<String> r = new ArrayList<>();
        String[] split = s.split("\t");
        r.addAll(Arrays.asList(split));
        return r;
    }

    /**
     * Returns all the values common to s1, s2, s3, s4 and s5 and removes all
     * these common fields from s1, s2, s3, s4 and s5.
     *
     * @param s1
     * @param s2
     * @param s3 May be null.
     * @param s4 May be null.
     * @param s5 May be null.
     * @return
     * @Todo generalise
     */
    public TreeSet<String> getFieldsInCommon(TreeSet<String> s1,
            TreeSet<String> s2, TreeSet<String> s3, TreeSet<String> s4,
            TreeSet<String> s5) {
        TreeSet<String> r = new TreeSet<>();
        r.addAll(s1);
        r.retainAll(s2);
        if (s3 != null) {
            r.retainAll(s3);
        }
        if (s4 != null) {
            r.retainAll(s4);
        }
        if (s5 != null) {
            r.retainAll(s5);
        }
        return r;
    }
}
