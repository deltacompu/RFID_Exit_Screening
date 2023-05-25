package org.zebra.rfidScanEmb2;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;

public class MysqlCon{  
	Connection connection = null;
	Statement statement = null;
	ResultSet resultSet = null;

	public Boolean insertData(String id_epc, String type_asset, String model,String image) throws ClassNotFoundException, SQLException, IOException, PropertyVetoException{  
		try {
            connection = DatabaseUtility.getInstance().getConnection();
            statement = connection.createStatement();
			String sql = "insert into tags_seen "
			+"(id, id_epc, type_asset, model, image, date_time)"
			+ " values (default,'"+id_epc+"','"+type_asset+"','"+model+"','"+image+"',now())";
			statement.executeUpdate(sql);
			System.out.println("Insert completed.");
			return true;
		}catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
            if (statement != null) try { statement.close(); } catch (SQLException e) {e.printStackTrace();}
            if (connection != null) try { connection.close(); } catch (SQLException e) {e.printStackTrace();}
        }
		return false;
	}

	public  boolean validateFiveMinutes(String epc) throws ClassNotFoundException, SQLException, IOException, PropertyVetoException{
		
		connection = DatabaseUtility.getInstance().getConnection();
        statement = connection.createStatement();
			
		//3. Execute Query
		String sql = "select id_epc from tags_seen where id_epc='"+epc+"' and date_time > now() - interval 5 minute;" 	;
		ResultSet rs = statement.executeQuery(sql);
		return rs.next();
	}

	
	
}  
