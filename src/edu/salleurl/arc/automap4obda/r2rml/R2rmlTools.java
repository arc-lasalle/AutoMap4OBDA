/*
 *  Copyright (C) 2016 ARC La Salle Barcelona Campus, Ramon Llull University.
 *
 *  for comments please contact Alvaro Sicilia (ascilia@salleurl.edu)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.salleurl.arc.automap4obda.r2rml;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import edu.salleurl.arc.automap4obda.matcher.ClassMatch;
import edu.salleurl.arc.automap4obda.matcher.ColumnStructure;
import edu.salleurl.arc.automap4obda.matcher.DataPropertyMatch;
import edu.salleurl.arc.automap4obda.matcher.DatabaseStructure;
import edu.salleurl.arc.automap4obda.matcher.RDB2OntMatcher;
import edu.salleurl.arc.automap4obda.matcher.TableStructure;
import edu.salleurl.arc.automap4obda.ontology.OntologyTools;


public class R2rmlTools {

	private int indexMapping = 0;
	private OntologyTools ont;
	
	RDB2OntMatcher matcher;
	
	DatabaseStructure db;
	
	String schema = "";
	HashMap<String, Boolean> testedQueries = new HashMap<>();
	HashMap<String, Boolean> checkedTripleMaps = new HashMap<>();
	
    
    public R2rmlTools( 	OntologyTools aontDomain,
    		RDB2OntMatcher amatcher, String aschema)
    {
    	ont = aontDomain;
    	matcher = amatcher;
    	schema = aschema.length() > 0 ? aschema+"." : aschema;
    	db = matcher.getDatabaseStructure();
    }
    
	public void WriteR2RMLHeader(PrintWriter out) {
		out.println();
		out.println("@prefix rr: <http://www.w3.org/ns/r2rml#> .");
		out.println("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
		out.println("@prefix : <http://www.benchmark.com/quest/> .");
		out.println("@base <http://example.com/base/> .");
		out.println();
		out.println();
	}
	
	public void WriteR2RMLTripleMap(PrintWriter out, ClassMatch cl, 
			ClassMatch clRange, String oObjectProperty, Connection conn)
	{
		if(!cl.isMatched) return;
		
		String sqlquery = getSQLQueryFromClass(cl, clRange, cl.dataproperties.values());  
		String uri = getTemplateFromClass(cl, cl.domainname);
		
		//If the query is empty the triple map cannot be instantiated
		if(sqlquery.isEmpty()) return;

		//If the triple map has already been introduced we exit
		if(checkTripleMapAlreadyExists(uri, cl, sqlquery, clRange, oObjectProperty)) return;
		
		//If the query do not return data the triple map cannot be instantiated !!!
		if(!testQuery(conn, sqlquery)) return;
				
		out.println();
		out.println("###########################################");
		out.println("# TripleMap for " + indexMapping + ": " + cl.domainname);
		out.println("<mapping1_"+indexMapping+"> a rr:TriplesMap;");
		out.println("	rr:logicalTable [ rr:sqlQuery \""+sqlquery+"\" ];");
		out.println("	rr:subjectMap [	rr:template \""+uri+"\";");
		out.println("		rr:class <"+cl.domainname+">");
		out.println("	];");
		
		for (DataPropertyMatch dp : cl.dataproperties.values()) {
			if(dp.isMatched) {
				out.println();
				out.println("	rr:predicateObjectMap [ ");
				out.println("		rr:predicate 	<" + dp.domainname + "> ; ");
				out.println("		rr:objectMap [ rr:column \""+dp.table.toLowerCase()+dp.column.toLowerCase()+"\" ] ");
				out.println("  	]; ");
			}
		}
		
		out.println();
		
		if(clRange != null) {
			String template= getTemplateFromClass(clRange, clRange.domainname);
			
			out.println();
			out.println("	rr:predicateObjectMap [ ");
			out.println("		rr:predicate 	<" + oObjectProperty + "> ; ");
			out.println("		rr:objectMap [ rr:template \""+template+"\" ] ");
			out.println("  	]; ");
		}
			
		out.println(".");
		out.println();
		
		indexMapping++;
	}
	
	private boolean checkTripleMapAlreadyExists(String uri, ClassMatch cl,
			String sqlquery, ClassMatch clRange, String oObjectProperty) {

		String hash = uri+cl.domainname+cl.table+cl.column+sqlquery;
		
		if(clRange != null) {
			hash += oObjectProperty + clRange.domainname;
		}
		
		hash = hash.replace("\\", "").replace("\"","");
		
		if(checkedTripleMaps.containsKey(hash)) return true;
		
		checkedTripleMaps.put(hash, true);
		
		return false;
	}

	/**
	 * Method for testing if the query returns results or not.
	 * @param conn
	 * @param sqlquery
	 * @return
	 */
	private boolean testQuery(Connection conn, String sqlquery) {

		//System.out.println("Testing: " + sqlquery);
		
		if(testedQueries.containsKey(sqlquery))
			return testedQueries.get(sqlquery);
		
		
		boolean ret = false;
		
		try {
			Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery(sqlquery.replace("\\", "")+" LIMIT 1");
			
			if(rs.next()) {
				ret = true;
			}
			
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		testedQueries.put(sqlquery, ret);
		
		return ret;
	}

	public String getTemplateFromClass(ClassMatch cl, String domain){
				
		String classname = ont.getClassFragment(domain).toLowerCase();
		
		String uri = "http://www.automated-examples.com/resource/"+classname+"/{\\\""+cl.table.toLowerCase()+cl.column.toLowerCase()+"\\\"}";
		
		return uri;
		
	}
	
	
	public String getSQLQueryFromClass(ClassMatch cl, 
			ClassMatch clRange, Collection<DataPropertyMatch> oDataproperties){
		
		String table = cl.table;
		String column = cl.column;
		
		String sql = "SELECT ";
		HashMap<String, Integer> selects = new HashMap<String, Integer>();
		HashMap<String, String> tables = new HashMap<String, String>();
		HashMap<String, Integer> joins = new HashMap<String, Integer>();
		
		StringBuilder joinClause = new StringBuilder();
		
		selects.put("\\\""+table + "\\\".\\\""+ column+"\\\"", 1);
		
		if(clRange != null) {
			
			//String path = getPathBetweenTables(table, column, range.table, range.column);
			List<GraphPath<String, DefaultEdge>> path = db.findPathBetween(table, clRange.table);
						
			if(path.size() > 0)
				getSQLComponents(table, column, clRange.table, clRange.column, path, selects, tables, joins, joinClause);
			else{
				// Mark that the object property cannot be instantiated
				return "";
			}
		}
		
		for (DataPropertyMatch dp : oDataproperties) {
			if(dp.isMatched && !selects.containsKey("\\\""+dp.table + "\\\".\\\""+ dp.column+"\\\""))
				selects.put("\\\""+dp.table + "\\\".\\\""+ dp.column+"\\\"", 1);
		}
		
		String[] selectskeys = selects.keySet().toArray(new String[0]);
		for(int i = 0; i < selectskeys.length; i++) {
			sql = sql + selectskeys[i] + " AS \\\"" +selectskeys[i].replace(".",  "").replace("\\", "").replace("\"","").toLowerCase()+"\\\"";
			if(i < selectskeys.length -1) sql += ", ";
		}

		sql += " FROM " + schema+"\\\""+table+"\\\"";

		sql += joinClause.toString();
		
		if(!cl.where.isEmpty())
		{
			sql += " WHERE " + cl.where;
		}
		
		return sql;
	}

	// To get the components of the SELECT clause
	private void getSQLComponents(String table, String column, String rangetable, String rangecolumn,
			List<GraphPath<String, DefaultEdge>> paths, HashMap<String, Integer> selects,
			HashMap<String, String> tables, HashMap<String, Integer> joins, StringBuilder joinClause) {
				
		if(paths.size() >= 1){

			GraphPath<String, DefaultEdge> dbpath = paths.get(0);
			
			for(int i = 1; i < dbpath.getEdgeList().size()-1; i++) {
				
				String sLeft = dbpath.getGraph().getEdgeSource(dbpath.getEdgeList().get(i));
				String sRight = dbpath.getGraph().getEdgeTarget(dbpath.getEdgeList().get(i));

				//both left and right have to be like: <table>.<column>
				if(sLeft.split("\\.").length != 2 || sRight.split("\\.").length != 2) continue;
					
				if(!tables.containsKey(sRight.split("\\.")[0])) tables.put(sRight.split("\\.")[0], sLeft + " = " + sRight);

				
				if(joinClause.indexOf(" INNER JOIN " + schema+"\\\""+sRight.split("\\.")[0]+"\\\"" + " ON ") == -1)
					joinClause.append(" INNER JOIN " + schema+"\\\""+sRight.split("\\.")[0]+"\\\"" + " ON \\\"" + 
							sLeft.replace(".", "\\\".\\\"") + "\\\" = \\\"" + sRight.replace(".", "\\\".\\\"")+"\\\"");
			}
		} 
		
		
		if(!selects.containsKey("\\\""+table + "\\\".\\\""+ column+"\\\""))				selects.put("\\\""+table + "\\\".\\\""+ column+"\\\"", 1);
		if(!selects.containsKey("\\\""+rangetable + "\\\".\\\""+ rangecolumn+"\\\""))	selects.put("\\\""+rangetable + "\\\".\\\""+ rangecolumn+"\\\"", 1);
	}
	

	// To get the path between two tables and columns
	private String getPathBetweenTables(String table, String column, String rangetable, String rangecolumn) {

		HashMap<String, Integer> path = new HashMap<String, Integer>();
		
		HashMap<String, Integer> visited = new HashMap<String, Integer>();
		
		String ret = recursivePathDFS(table, rangetable, visited, path);
		
		if(!path.containsKey(rangetable))
			path.put(rangetable, 1);

		
		return ret;
	}

	//////////////////
	// Recursive method to find the connection between two tables.
	private String recursivePathDFS(String domain, String target, HashMap<String, Integer> visited, HashMap<String, Integer> path) {
		
		visited.put(target, 1);
		String ret = "";
		
		if(domain.compareTo(target) == 0) {
			//we found it
		
			return ">>>";
		}
		
		HashMap<String, String> nextTables = new HashMap<String, String>();
		
		//getting the tables where we have to look
		getNextTableFromForeignKeys (target, nextTables);
		
		for (String nexttarget : nextTables.keySet()) {

			
			String[] arr = nexttarget.split("\\.");
			
			if(visited.containsKey(arr[0]) && ret.compareTo("") == 0) {
			} else {
				String retRec = recursivePathDFS(domain, arr[0], visited, path);
				
				if( retRec.compareTo("") != 0) {
					if( retRec.compareTo(">>>") != 0) 
						ret = retRec + ">>>" + nexttarget + "=" + nextTables.get(nexttarget);
					else
						ret = nexttarget + "=" + nextTables.get(nexttarget);
						
					if(!path.containsKey(arr[0]))
						path.put(arr[0], 1);
				}
			}
		}		
		
		return ret;
	}

	private void getNextTableFromForeignKeys(String target, HashMap<String, String> nextTables) {
		
		if(!db.hasTable(target)) return;
		
		String targetPrimarykey = db.getTable(target).getOnePrimaryKey();
		
		for(ColumnStructure col : db.getTable(target).getColumns())
		{	
			if(col.isForeignkey) {
				if(!nextTables.containsKey(col.foreigntable+"."+col.foreignkey))
					nextTables.put(col.foreigntable+"."+col.foreignkey, target+"."+targetPrimarykey);
			}
			
			if(col.isPrimarykey) {

				for(TableStructure table : db.getTables()){
					for(ColumnStructure col2 : table.getColumns())
					{
						if(col2.foreigntable.compareTo(target) == 0) {
						
							if(!nextTables.containsKey(table.name+"."+col2.name))
								nextTables.put(table.name+"."+col2.name, target+"."+col.name);
						}
					}
				}
			}
		}
	}
}
