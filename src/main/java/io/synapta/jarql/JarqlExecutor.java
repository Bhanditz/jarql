/*
 * The MIT License
 *
 * Copyright 2017 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.synapta.jarql;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.InputStream;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import static io.synapta.jarql.JarqlParser.parse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author user
 */
public class JarqlExecutor {

    /**
     * @param args the command line arguments
     * @throws IOException 
     */

    @SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
    	final String fileType = args[0];
        final String jsonFileName = args[1];
        final File jsonFile = new File(jsonFileName);
        if (!jsonFile.exists()) {
            System.err.println("File " + jsonFileName + " does not exist");
            System.exit(-1);
        }
        FileInputStream jsonIn = new FileInputStream(jsonFile);
        final String queryFileName = args[2];
        final File queryFile = new File(queryFileName);
        if (!queryFile.exists()) {
            System.err.println("File " + queryFileName + " does not exist");
            System.exit(-1);
        }
        String jsonPrettyPrintString="";
        FileInputStream fin = new FileInputStream(jsonFile);
        byte[] xmlData = new byte[(int) jsonFile.length()];
        fin.read(xmlData);
        fin.close();
        String TEST_XML_STRING = new String(xmlData, "UTF-8");
        if(fileType.equals("xml")){
            JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
            jsonPrettyPrintString = xmlJSONObj.toString(4);
            File newTextFile = new File("temp.json");
            FileWriter temp = new FileWriter(newTextFile);
            temp.write(jsonPrettyPrintString);
            temp.close();
            jsonIn = new FileInputStream(newTextFile);

        }
        final FileInputStream queryIn = new FileInputStream(queryFile);
        String queryString = new BufferedReader(new InputStreamReader(queryIn)).lines().collect(Collectors.joining("\n"));
        Graph graph = execute(jsonIn, queryString);
        Serializer.getInstance().serialize(System.out, graph, "text/turtle");
    }
    
    
    static public Graph execute(final InputStream in, final String queryString) {
        Graph JsonGraph = new SimpleGraph();
        JarqlParser.parse(in, JsonGraph);
        JenaGraph jg = new JenaGraph(JsonGraph);
        Model model = ModelFactory.createModelForGraph(jg);
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            Model results = qexec.execConstruct();
            return new JenaGraphAdaptor(results.getGraph());
        }

    }
}
