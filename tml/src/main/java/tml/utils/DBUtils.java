/*******************************************************************************
 *  Copyright 2007, 2009 Jorge Villalon (jorge.villalon@uai.cl)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0 
 *  	
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 *******************************************************************************/
package tml.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBUtils {

	private Connection m_conn;
	private Statement m_stmt;
	private String url; 
	private  String username ;
	private String password;

	public DBUtils(String driver, String url, String username, String password) throws ClassNotFoundException
	{
		this.url=url;
		this.username=username;
		this.password=password;
		this.setDriver(driver);
	}
	public boolean setConnection() {
		try {                    	
			m_conn = DriverManager.getConnection(url, username, password);
			m_stmt = m_conn.createStatement();            
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean setDriver(String driver) throws ClassNotFoundException {
		Class.forName(driver);
		return true;
	}
	public ArrayList<String> sendQuery(String sql,String fieldname) {
		try {
			ArrayList<String> al = new  ArrayList<String>();
			ResultSet m_rs = m_stmt.executeQuery(sql);
			while (m_rs.next())   {
				al.add(m_rs.getString(fieldname));
			} 
			m_rs.getStatement().close(); 
			return al;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public int sendUpdate(String sql) {
		try {
			return m_stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	public void closeConnection()
	{
		try {
			m_conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}




}
