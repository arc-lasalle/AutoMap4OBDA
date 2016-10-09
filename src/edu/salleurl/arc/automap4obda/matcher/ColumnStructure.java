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

public class ColumnStructure {
	public String name = "";
	public String type = "";
	public String foreigntable = "";
	public String foreignkey = "";
	public boolean isPrimarykey = false;
	public boolean isForeignkey = false;
	
	public ColumnStructure (String sname, String stype){
		
		name = sname;
		type = setType(stype);
	}
	
	public ColumnStructure (String sname, String stype, String sforeigntable, String sforeignkey, 
			boolean bisPrimarykey, boolean bisForeignkey){
		
		name = sname;
		type = setType(stype);
		foreigntable = sforeigntable;
		foreignkey = sforeignkey;
		isPrimarykey = bisPrimarykey;
		isForeignkey = bisForeignkey;
	}
	
	public String getNormalizedName(){
		return name;
		//return DatabaseStructure.getNormalizedName(name);
	}
	

	public String getNormalizedForeigntable() {
		return foreigntable;
		/*
		String out, tmp = foreigntable.toLowerCase();

		out = tmp.substring(0, 1).toUpperCase() + tmp.substring(1, tmp.length());
		
		out = out.replace(' ', '_');
		out = out.replace('&', '_');
		
		return out;
		*/
	}
	
	public String setType(String sTypeFromDB){
		if(sTypeFromDB.equalsIgnoreCase("VARCHAR") || sTypeFromDB.equalsIgnoreCase("TEXT") || sTypeFromDB.toUpperCase().contains("CHAR") || sTypeFromDB.equalsIgnoreCase("LONGTEXT")) {
			type = "string";
		} else if(sTypeFromDB.equalsIgnoreCase("DATE") || sTypeFromDB.equalsIgnoreCase("DATETIME") || sTypeFromDB.equalsIgnoreCase("TIMESTAMP")) {
			type = "date";
		} else if(sTypeFromDB.equalsIgnoreCase("INT") || sTypeFromDB.equalsIgnoreCase("SMALLINT") || sTypeFromDB.equalsIgnoreCase("BIGINT") || sTypeFromDB.toUpperCase().contains("INT")) {
			type = "int";
		} else if(sTypeFromDB.equalsIgnoreCase("REAL") || sTypeFromDB.equalsIgnoreCase("FLOAT") || sTypeFromDB.equalsIgnoreCase("DECIMAL")||  sTypeFromDB.toUpperCase().contains("DOUBLE")) {
			type = "float";
		} else if(sTypeFromDB.equalsIgnoreCase("BOOLEAN") || sTypeFromDB.equalsIgnoreCase("BOOL")  ) {
			type = "boolean";
		} else if(sTypeFromDB.toUpperCase().contains("BINARY") ) {
			type = "binary";
		} else {
			type = "";		
		}
		
		return type;
	}

}
