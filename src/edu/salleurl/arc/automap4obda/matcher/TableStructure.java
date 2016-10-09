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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class TableStructure {
	
	public String name = "";
	
	private HashMap<String, ColumnStructure> columns;
	
	public LinkedHashSet<String> primarykeys;
	
	public LinkedHashSet<String> foreignkeys;
	
	
	public TableStructure(String sname){
		name = sname;
		columns = new HashMap<String, ColumnStructure>();
		primarykeys = new LinkedHashSet<String>();
		foreignkeys = new LinkedHashSet<String>();
	}
	
	public String getNormalizedName(){
		return name;
		//return DatabaseStructure.getNormalizedName(name);

	}
	
	public void addColumn(ColumnStructure col) {
		if(!columns.containsKey(col.name))
			columns.put(col.name,  col);
		
		if(col.isPrimarykey) {
			if(!primarykeys.contains(col.name))
				primarykeys.add(col.name);
		}
		if(col.isForeignkey) {
			if(!foreignkeys.contains(col.name))
				foreignkeys.add(col.name);	
		}
	}
	
	public void updateColumn(ColumnStructure col) {
		
		columns.put(col.name,  col);
		
		if(col.isPrimarykey) {
			if(!primarykeys.contains(col.name))
				primarykeys.add(col.name);
		}
		if(col.isForeignkey) {
			if(!foreignkeys.contains(col.name))
				foreignkeys.add(col.name);	
		}
	}
	
	public boolean hasColumn(String sName){
		return columns.containsKey(sName);
	}
	
	public Collection<ColumnStructure> getColumns(){
		return columns.values();
	}
	
	public ColumnStructure getColumn(String sName) {
		
		if(columns.containsKey(sName))
			return columns.get(sName);
			
		return null;
	}

	public String getOnePrimaryKey() {

		if(primarykeys.isEmpty()) return columns.values().iterator().next().name;
		
		return primarykeys.iterator().next();
	}
	
	public LinkedHashSet<String> getPrimaryKeys() {
		
		return primarykeys;
	}
	
	
	public LinkedHashSet<String> getForeignKeys() {
		
		return foreignkeys;
	}
}
