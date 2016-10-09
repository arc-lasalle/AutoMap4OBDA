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

public class DataPropertyMatch {
	public String putativename = "";
	public String domainname = "";
	public String table = "";
	public String column = "";
	public String type = "";
	public boolean isMatched = false;
	
	public DataPropertyMatch(){
		
	}
	public DataPropertyMatch(String sputativename, String stable, String scolumn, String stype){
		putativename = sputativename;
		table = stable;
		column = scolumn;
		type = stype;
	}
}
