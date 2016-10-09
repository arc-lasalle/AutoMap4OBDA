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

package edu.salleurl.arc.automap4obda.matcher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/**
 * @author asicilia
 *
 */
public class DatabaseStructure {
	private HashMap<String, TableStructure> tables;
	public LinkedHashSet<String> columns;


	DirectedGraph<String, DefaultEdge> g;
	List<GraphPath<String, DefaultEdge>> allPaths;
	HashMap<String, List<GraphPath<String, DefaultEdge>>> pathsOfVertex;
	
	HashMap<String, List<GraphPath<String, DefaultEdge> >> pathsFound = new HashMap<>();
	
	
	public DatabaseStructure()
	{
		tables = new HashMap<String, TableStructure>();
		columns = new LinkedHashSet<String>();
	}


	public void extractDatabaseStructure(Connection conn, String schema) {
		ResultSet rs = null;
        
        try{       
            java.sql.DatabaseMetaData dbmd = (java.sql.DatabaseMetaData) conn.getMetaData();
            
            ResultSet res = dbmd.getTables(null, schema, null, new String[] {"TABLE"});
 
            while (res.next()) {
            	
            	//if(res.getString("TABLE_NAME").toLowerCase().compareTo("wellbore_exploration_all") != 0) continue;
            	
            	TableStructure tab = new TableStructure(res.getString("TABLE_NAME"));
            	

 			    //Store all columns
 			    rs = dbmd.getColumns(null, schema, res.getString("TABLE_NAME"), null);
			    while (rs.next()) {			    	
			    	ColumnStructure col = new ColumnStructure(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
			    	tab.addColumn(col);
			    }
			
			    //String sPrimaryKey = "";
			    rs = dbmd.getPrimaryKeys(null, schema, res.getString("TABLE_NAME"));
			    while (rs.next()) {    	
			    	ColumnStructure col = tab.getColumn(rs.getString("COLUMN_NAME"));
			    	col.isPrimarykey = true;
			    	tab.updateColumn(col);
			    }
			   
			    rs = dbmd.getImportedKeys(conn.getCatalog(), schema, res.getString("TABLE_NAME"));
			    while (rs.next()) {
			    	ColumnStructure col = tab.getColumn(rs.getString("FKCOLUMN_NAME"));
			    	
			    	col.isForeignkey = true;
			    	col.foreignkey= rs.getString("PKCOLUMN_NAME");
			    	col.foreigntable= rs.getString("PKTABLE_NAME");
			    	
			    	tab.updateColumn(col);
			    }
			
	           	this.addTable(tab);
			}
			res.close();
        }
        catch (SQLException ex) {
	        System.out.println("SQLException: " + ex.getMessage()); 
	        System.out.println("SQLState: " + ex.getSQLState()); 
	        System.out.println("VendorError: " + ex.getErrorCode()); 
	        ex.printStackTrace();
	    }
		
	}
	
	public void addTable(TableStructure tab) {
		if(!tables.containsKey(tab.name))
			tables.put(tab.name,  tab);
		
		for(ColumnStructure col : tab.getColumns()) {
			if(!columns.contains(col.name))
				columns.add(col.name);
		}		
	}
	
	public void addColumnToTable(String sTableName, ColumnStructure col) {
		if(tables.containsKey(sTableName)){
			tables.get(sTableName).addColumn(col);
			
			if(!columns.contains(col.name))
				columns.add(col.name);
		}
	}
	
	public Collection<TableStructure> getTables(){
		return tables.values();
	}
	
	public TableStructure getTable(String sName) {
		
		if(tables.containsKey(sName))
			return tables.get(sName);
			
		return null;
	}


	/**
	 * Checks if the columns of the foreign table are a subset of the columns of the table
	 * @param table name of the table 
	 * @param foreigntable name of the foreign table
	 * @return
	 */
	public boolean CheckAttributeSubset(String table, String foreigntable) {

		TableStructure foreign = this.getTable(foreigntable);
		
		if(foreign != null) {
			for(ColumnStructure col : this.getTable(table).getColumns()) {
				
				if(!foreign.hasColumn(col.name))
					return false;
			}
		}

		return true;
	}


	
	public boolean hasTable(String target) {
 
		return tables.containsKey(target);
	}
	
	public void print() {
		
		System.out.println("");
		System.out.println("Database Structure");
		System.out.println("------------------");
		System.out.println("");
		for(TableStructure tab: tables.values()) {
			
			System.out.println("Table: " + tab.name);
			for(ColumnStructure col : tab.getColumns()){
				System.out.println("  - " + col.name + " | " + col.type + (col.isPrimarykey ?  " | PK ":"") + (col.isForeignkey ?  " | FK: " +col.foreigntable+"."+col.foreignkey :""));
			}
			System.out.println("");
		}
	}
	
	public static String getNormalizedName(String name){
		String out, tmp = name.toLowerCase();

		out = tmp.substring(0, 1).toUpperCase() + tmp.substring(1, tmp.length());
		
		out = out.replace(' ', '_');
		out = out.replace('&', '_');
		
		return out;
	}
	
	
	/**
	 * Initializes the graph to be used to find paths between tables
	 */
	public void initSearch() {
		g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

		//1. We add tables as vertex of the graph
		for(TableStructure tab: tables.values()) {
			g.addVertex(tab.name);
		}
		
		for(TableStructure tab: tables.values()) {
			
			//2. We add foreign keys as vertex of the graph and we connect them using edges

			for(ColumnStructure col : tab.getColumns()){
				if(col.isForeignkey) {

					g.addVertex(tab.name+"."+col.name);
					if(!g.containsVertex(col.foreigntable+"."+col.foreignkey))
						g.addVertex(col.foreigntable+"."+col.foreignkey);
					
					g.addEdge(tab.name, tab.name+"."+col.name);
					g.addEdge(tab.name+"."+col.name, col.foreigntable+"."+col.foreignkey);
					g.addEdge(col.foreigntable+"."+col.foreignkey, col.foreigntable);
					
					
					g.addEdge(col.foreigntable, col.foreigntable+"."+col.foreignkey);
					g.addEdge(col.foreigntable+"."+col.foreignkey, tab.name+"."+col.name);
					g.addEdge(tab.name+"."+col.name, tab.name);						
				}
			}
		}
	}
	
	public List<GraphPath<String, DefaultEdge> > findPathBetween(String sourceTable, String targetTable)
	{	
		//lets see if the path has been already calculated
		if(pathsFound.containsKey(sourceTable+"."+targetTable)) {
			return pathsFound.get(sourceTable+"."+targetTable);
		}
		
		List<GraphPath<String, DefaultEdge>> path = new ArrayList<GraphPath<String, DefaultEdge>>();
		
		Set<String> sources = new HashSet<>();
		Set<String> targets = new HashSet<>();

		sources.add(sourceTable);
		targets.add(targetTable);
		

        DijkstraShortestPath<String, DefaultEdge> fastpath = new DijkstraShortestPath<String, DefaultEdge>(
                g,
                sourceTable,
                targetTable,
                Double.POSITIVE_INFINITY);

        if(fastpath == null) return path;
        if(fastpath.getPathEdgeList() == null) return path;
        
        
        int iMinSizes = fastpath.getPathEdgeList().size();
		
		AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<String, DefaultEdge>(g);

		allPaths = pathFindingAlg.getAllPaths(sources, targets, true, iMinSizes);
		
		Iterator<GraphPath<String, DefaultEdge>> iter = allPaths.iterator();
				
		iter = allPaths.iterator();
		
		while(iter.hasNext())
		{
			GraphPath<String, DefaultEdge> gpath = iter.next();
			
			if(gpath.getEdgeList().size() == iMinSizes) {
				path.add(gpath);
			}
		}
		
		if(!pathsFound.containsKey(sourceTable+"."+targetTable)) {
			pathsFound.put(sourceTable+"."+targetTable, path);
		}
		
		return path;
	}
}
