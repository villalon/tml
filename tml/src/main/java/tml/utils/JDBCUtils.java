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

public class JDBCUtils {

	private Connection m_conn;
	private Statement m_stmt;

	public JDBCUtils(String driver, String url, String username, String password) throws Exception
	{
		try {
			Class.forName(driver);
			m_conn = DriverManager.getConnection(url, username, password);
			m_stmt = m_conn.createStatement();
		} catch (Exception e) {
			throw e;
		}
	}
	public ResultSet sendQuery(String sql) {
		try {
			ResultSet m_rs = m_stmt.executeQuery(sql);
			return m_rs;
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




}
