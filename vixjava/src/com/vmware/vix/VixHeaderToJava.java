/*******************************************************************************
 * Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All
 * other rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of VMware, Inc. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.vix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the vix.h header file for VIX data types and enum values.
 * <p/>
 * EXPERIMENTAL.
 */
public class VixHeaderToJava {

   private final String VIX_CONSTANTS_CLASS = "VixConstants";

   private final String mHeaderPath;
   private final String mOutputPath;

   private final HashMap<String, HashMap<String, String>> mTypesMap;
   private final HashMap<String, String> mConstantsMap;

   public static final String copyrightHeader =
         "/*******************************************************************************\n"
               + "* Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All\n"
               + "* other rights reserved.\n"
               + "*\n"
               + "* Redistribution and use in source and binary forms, with or without\n"
               + "* modification, are permitted provided that the following conditions are met:\n"
               + "*\n"
               + "* - Redistributions of source code must retain the above copyright notice, this\n"
               + "* list of conditions and the following disclaimer.\n"
               + "*\n"
               + "* - Redistributions in binary form must reproduce the above copyright notice,\n"
               + "* this list of conditions and the following disclaimer in the documentation\n"
               + "* and/or other materials provided with the distribution.\n"
               + "*\n"
               + "* - Neither the name of VMware, Inc. nor the names of its contributors may be\n"
               + "* used to endorse or promote products derived from this software without\n"
               + "* specific prior written permission.\n"
               + "*\n"
               + "* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\n"
               + "* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n"
               + "* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\n"
               + "* ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR\n"
               + "* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n"
               + "* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n"
               + "* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER"
               + "* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,\n"
               + "* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"
               + "* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n"
               + "******************************************************************************/\n\n";

   /**
    * Constructor.
    *
    * @param headerPath
    *           Full path to the vix.h header file
    * @param outputPath
    *           Directory to put generated .java files in
    */
   public VixHeaderToJava(String headerPath, String outputPath) {
      mHeaderPath = headerPath;
      mOutputPath = outputPath;
      mTypesMap = new HashMap<String, HashMap<String, String>>();
      mConstantsMap = new HashMap<String, String>();
   }

   /**
    * Read the specified header file, and find constants. If constants are in
    * an enum preceded by a typedef, place those constants under the specified
    * type.
    */
   public void parseConstantsAndTypes() {
      try {
         FileReader reader = new FileReader(mHeaderPath);
         BufferedReader in = new BufferedReader(reader);
         String typeName;

         try {
            String line = null;
            while ((line = in.readLine()) != null) {
               /*
                * Skip comments
                */
               if (isSkippable(line)) {
                  continue;
               }

               /*
                * Check if this is a typedef, which should be followed by an
                * enum. Skip method typedef declarations.
                */
               Pattern typedef = Pattern.compile("^typedef\\s+(\\w+)\\s+(\\w+);");
               Matcher m = typedef.matcher(line);
               if (m.matches()) {
                  typeName = m.group(2);

                  if (mTypesMap.containsKey(typeName)) {
                     continue;
                  }

                  System.out.println("Found new type " + typeName + ".");
                  mTypesMap.put(typeName, new HashMap<String, String>());

                  parseEnum(in, mTypesMap.get(typeName));
               } else if (line.startsWith("enum")) {
                  parseEnum(in, mConstantsMap);
               }
            }
         } finally {
            in.close();
         }
      } catch (FileNotFoundException notFound) {
         System.err.println(notFound);
         notFound.printStackTrace();
      } catch (IOException e) {
         System.err.println(e);
         e.printStackTrace();
      }
   }

   /**
    * @TODO make this actually work
    */
   public void parseFunctionDeclarations() {
      try {
         FileReader reader = new FileReader(mHeaderPath);
         BufferedReader in = new BufferedReader(reader);
         try {
            String line = null;
            String returnType;
            String name;
            Vector<String> params = new Vector<String>();
            Pattern fnDecl = Pattern.compile("^(\\w+)\\s+(\\w+)[(](.*)");
            while ((line = in.readLine()) != null) {
               Matcher m = fnDecl.matcher(line);
               if (m.matches()) {
                  //System.out.println(line);

                  returnType = checkType(m.group(1));
                  name = m.group(2);
                  System.out.println(returnType + " " + name + "(");
                  //System.out.println(m.group(3));
                  Scanner s = new Scanner(in);
                  s.useDelimiter("(,\\s+|[\\n\\r])");

                  String str;// = s.findWithinHorizon("(\\w+\\s+\\w+)(,\\s?)", 0);
                  while (s.hasNext()) {
                     str = s.next();
                     System.out.println(str);
                     if (str.contains(");")) {
                        System.out.println("End func declaration");
                        break;
                     }
                  }
               }
            }
         } finally {
            in.close();
         }
      } catch (FileNotFoundException notFound) {

      } catch (IOException e) {

      }
   }

   public String checkType(String type) {
      if (type.equalsIgnoreCase("VixEventProc")) {
         return "Callback";
      }
      if (type.contains("const char")) {
         return "String";
      } else if (type.equalsIgnoreCase("int32")) {
         return "int";
      } else if (type.equalsIgnoreCase("uint64")) {
         return "long";
      } else if (type.contains("*")) {
         return "Pointer";
      } else {
         return type;
      }
   }

   /**
    * Parses an enum declaration in a C header file until the end of the
    * declaration is reached.
    *
    * @param reader
    *           Reader that is pointing to an enum
    * @param map
    *           Map to add found values to.
    * @throws IOException
    */
   public void parseEnum(BufferedReader reader, HashMap<String, String> map)
         throws IOException {
      String line = null;
      while ((line = reader.readLine()) != null) {
         if (line.contains("};")) {
            break;
         }
         Pattern enumValue =
               Pattern.compile("\\s+(\\w+)\\s+=\\s+([a-zA-Z_\\-0-9]+).*?$");
         Matcher matcher = enumValue.matcher(line);
         if (matcher.matches()) {
            String name = matcher.group(1);
            String val = matcher.group(2);
            map.put(name, val);
         }
      }
   }

   public void outputTypes() {

      String lineSeparator = System.getProperty("line.separator");

      String boilerPlate =
            "/* Auto-generated by VixHeaderToJava from the vix.h header file */";
      String header =
            boilerPlate + lineSeparator + "package com.vmware.vix;"
                  + lineSeparator + lineSeparator
                  + "@SuppressWarnings(\"serial\")" + lineSeparator;
      String indent = "   ";

      StringBuilder constantsContent = new StringBuilder();
      constantsContent.append(header);
      constantsContent.append("public class " + VIX_CONSTANTS_CLASS + " {"
            + lineSeparator);
      /*
       * Output the generic constants that aren't grouped by a certain type
       */
      Set<String> constants = mConstantsMap.keySet();
      for (String key : constants) {
         constantsContent.append(indent + "public static final int " + key + " = "
               + mConstantsMap.get(key) + ";" + lineSeparator);
      }
      // End class
      constantsContent.append("}");
      writeFile(constantsContent, mOutputPath + "\\" + VIX_CONSTANTS_CLASS + ".java");

      String innerIndent = indent + indent;
      Set<String> types = mTypesMap.keySet();
      for (String type : types) {
         StringBuilder typeContent = new StringBuilder();

         /*
          * Print out a header that indicates this file was auto-generated.
          */
         typeContent.append(header);
         typeContent.append("public class " + type + " extends VixType {"
               + lineSeparator + lineSeparator);

         /*
          * Print out constant values.
          */
         Set<String> values = mTypesMap.get(type).keySet();
         for (String val : values) {
            typeContent.append(indent + "public static final " + type + " " + val
                  + " = new " + type + "(" + mTypesMap.get(type).get(val) + ");" + lineSeparator);
         }
         typeContent.append(lineSeparator);

         /*
          * Provide some constructors.
          */
         typeContent.append(indent + "public " + type + "(long val) {"
               + lineSeparator);
         typeContent.append(innerIndent + "super(val);" + lineSeparator);
         typeContent.append(indent + "}" + lineSeparator);
         typeContent.append(lineSeparator);
         typeContent.append(indent + "public " + type + "() {" + lineSeparator);
         typeContent.append(innerIndent + "super();" + lineSeparator);
         typeContent.append(indent + "}");
         typeContent.append(lineSeparator);


         // End class
         typeContent.append("}" + lineSeparator);
         writeFile(typeContent, mOutputPath + "\\" + type + ".java");
      }
   }

   private void writeFile(StringBuilder content, String fileName) {
      try {
         FileWriter writer = new FileWriter(fileName);
         BufferedWriter output = new BufferedWriter(writer);
         try {
            output.append(content);
         } finally {
            output.close();
         }
      } catch (IOException e) {

      }
   }

   public void outputConstants() {
      Set<String> keys = mConstantsMap.keySet();
      for (String key : keys) {
         System.out.println(key + " => " + mConstantsMap.get(key));
      }
   }

   public boolean isSkippable(String line) {
      return line.startsWith("#")
            || line.contains("/*");
            //|| line.matches("^\\s?\\*.+")
            //|| line.matches("\\*\\/$");
            //|| line.matches("/\\*[^*]*\\*+([^/*][^*]*\\*+)*/|(\"(\\.|[^\"\\])*\"|'(\\.|[^'\\])*'|.[^/\"'\\]*))");
   }

   public static void main(String[] args) throws FileNotFoundException {
      String header = "N:\\srm\\builds\\VIX\\vix.h";
      String output = "c:\\testware\\srm-messina\\tests\\SRM\\Java\\com\\vmware\\vix";

      VixHeaderToJava v = new VixHeaderToJava(header, output);
      v.parseConstantsAndTypes();
      //v.parseFunctionDeclarations();
      v.outputTypes();
      //v.outputConstants();
   }
}