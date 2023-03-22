package com.app.scrumble.model;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteDatabaseConnection {

    private Connection connection;

    private static final int TIMEOUT = 10;

    /**
     * Use to execute QUERY operations on the remote database
     * @param tableName The table on which the query should be performed
     * @param columnNames The names of the columns that should be queried. You may pass an empty array or null to query all columns in the table (equivalent to SELECT * FROM...).
     * @param whereClause An optional where clause using placeholders in place of actual values (do not include the WHERE keyword in this clause). For example, you could provide the following WHERE clause: "column1 = ? AND column2 = ?"
     * @param params The parameters to replace the '?' characters with in your WHERE clause, in the order they should appear. For example, if your where clause was 'column1 = ? AND column2 = ?' you could pass the values "1" and "red" to indicate that
     *               column1 must equal the value "1" and column 2 must equal the value "red".
     * @return A {@link List} of {@link HashMap} objects. Each {@link HashMap} object represents a row in the table and contains a mapping from column names to their values.
     * @throws DatabaseException
     */
    public List<Map<String,Object>> executeQuery(String tableName, String[] columnNames, String whereClause, Object[] params) throws DatabaseException{
        if(connection == null){
            setUpConnection();
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        if(columnNames == null || columnNames.length == 0){
            queryBuilder.append(" * ");
        }else{
            for (int i = 0; i < columnNames.length; i++) {
                queryBuilder.append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    queryBuilder.append(", ");
                }
            }
        }

        queryBuilder.append(" FROM ");
        queryBuilder.append(tableName);

        if(params != null && params.length > 0){
            queryBuilder.append(" WHERE ");
            queryBuilder.append(whereClause);

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
                // Set parameter values
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                preparedStatement.setQueryTimeout(TIMEOUT);

                return convertResultSetToMaps(preparedStatement.executeQuery());
            }catch (Exception e){
                throw new DatabaseException("Could not query Database! Details: " + e.getMessage());
            }
        }else{
            try {
                Statement statement = connection.createStatement();
                return convertResultSetToMaps(statement.executeQuery(queryBuilder.toString()));
            }catch (Exception e){
                throw new DatabaseException("Could not query Database! Details: " + e.getMessage());
            }
        }

    }

    private List<Map<String, Object>> convertResultSetToMaps(ResultSet resultSet) throws DatabaseException{
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                resultList.add(row);
            }
            return resultList;
        }catch (Exception e){
            throw new DatabaseException("Error converting results. Details: " + e.getMessage());
        }
    }

    /**
     *
     * @param tableName the name of the table on which this UPDATE operation will be performed.
     * @param columns the list of columns that to be updated
     * @param values The list of values that the columns should be updated to. Note that the number of elements in this array must match the number of column names provided and mus be in the order that matches their associated columns.
     * @param whereClause a required WHERE clause using placeholders instead of actual values. (You should not include the WHERE keyword in this WHERE clause). For example, you could provide the following WHERE clause: "column1 = ? AND column2 = ?"
     * @param params The parameters that will replace the '?' placeholders in the final statement. you must provide the same number of parameters as placeholder characters in your WHERE clause and they must be in the appropriate order. For example, if you have provided
     *               the following where clause: "column1 = ? AND column2 = ?" passing the following Object array: {1, "red"} would result in the statement "column1 = 1 AND column2 = 'red'" being executed.
     * @return The number of rows affected by this UPDATE statement.
     * @throws DatabaseException
     */
    public int executeUpdate(String tableName, @NotNull String[] columns, @NotNull Object[] values,  @NonNull String whereClause, @NotNull Object[] params) throws DatabaseException{
        if (connection == null){
            setUpConnection();
        }
        if(whereClause == null || params == null || params.length == 0){
            throw new IllegalArgumentException("You must provide a where clause and associated parameters to avoid updating every row in the table :(");
        }
        if(columns == null || columns.length == 0){
            throw new IllegalArgumentException("You must provide an array of columns to be updated :(");
        }
        if(values == null || values.length == 0){
            throw new IllegalArgumentException("You must provide an array of values with which the provided columns can be updated :(");
        }
        if(columns.length != values.length){
            throw new IllegalArgumentException("The number of columns to be updated and the number of values with which to update them must be the same :(");
        }

        int newSize = values.length + params.length;
        Object[] combinedArray = Arrays.copyOf(values, newSize);
        System.arraycopy(params, 0, combinedArray, values.length, params.length);

        String sql = "INSERT INTO " + tableName + "(" + String.join(", ", columns) + ") VALUES (";
        for(int x = 0; x < values.length; x++) {
            if(x == values.length - 1) {
                sql = sql + "?)";
            }else {
                sql = sql + "?, ";
            }
        }

        sql = sql + " WHERE " + whereClause;

        try (PreparedStatement statement = connection.prepareStatement(sql);){
            for(int i = 0 ; i < combinedArray.length ; i++){
                statement.setObject(i + 1, combinedArray[i]);
            }
            return statement.executeUpdate();
        }catch (Exception e){
            throw new DatabaseException("Could not update database! Details: " + e.getMessage());
        }
    }


    /**
     *
     * @param tableName the name of the table on which this DELETE operation will be performed.
     * @param whereClause a required WHERE clause using placeholders instead of actual values. (You should not include the WHERE keyword in this WHERE clause). For example, you could provide the following WHERE clause: "column1 = ? AND column2 = ?"
     * @param params The parameters that will replace the '?' placeholders in the final statement. you must provide the same number of parameters as placeholder characters in your WHERE clause and they must be in the appropriate order. For example, if you have provided
     *               the following where clause: "column1 = ? AND column2 = ?" passing the following Object array: {1, "red"} would result in the statement "column1 = 1 AND column2 = 'red'" being executed.
     * @return The number of rows that were deleted.
     * @throws DatabaseException
     */
    public int executeDelete(String tableName, String whereClause, Object[] params) throws DatabaseException {
        if(connection == null){
            setUpConnection();
        }

        if(whereClause == null || params == null || params.length == 0){
            throw new IllegalArgumentException("You must provide a where clause and associated parameters to avoid deleting every row in the table :(");
        }
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            // Set parameter values of whereClause
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            // Execute statement and return number of rows affected
            return preparedStatement.executeUpdate();
        }catch (Exception e){
            throw new DatabaseException("Could not delete from database! Details: " + e.getMessage());
        }
    }

    /**
     * Executes an INSERT operation on the remote database.
     * @param tableName The table on which the operation should be performed.
     * @param columnNames The column names for which values will be provided.
     * @param values The values for for each column that was provided. Note that the number of column names and the number of values provided must be the same and must both be greater than 0.
     * @return An {@link InsertResult} object that details whether the INSERT was successful and, if so, the unique ID that was generated for the row (if one was in fact generated).
     * @throws DatabaseException
     */
    public InsertResult executeInsert(String tableName, String[] columnNames, Object[] values) throws DatabaseException{
        if(connection == null){
            setUpConnection();
        }

        if(columnNames == null || columnNames.length == 0 || values == null || values.length == 0) {
            throw new IllegalArgumentException("you must provide some data for the new row");
        }else if(columnNames.length != values.length) {
            throw new IllegalArgumentException("you must provide the same number of parameters as you have provided column names");
        }
        String sql = "INSERT INTO " + tableName + "(" + String.join(", ", columnNames) + ") VALUES (";
        for(int i = 0; i < values.length; i++) {
            if(i == values.length - 1) {
                sql = sql + "?)";
            }else {
                sql = sql + "?, ";
            }
        }

	    try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	        for (int i = 0; i < values.length; i++) {
	            preparedStatement.setObject(i + 1, values[i]);
	        }
	        boolean successful = preparedStatement.executeUpdate() == 1;
            Integer generatedID = null;
            if(successful){
                ResultSet generatedIDs = preparedStatement.getGeneratedKeys();
                if (generatedIDs.next()){
                    generatedID = generatedIDs.getInt(1);
                }
            }
            return new InsertResult(successful, generatedID);
	    }catch (Exception e){
            throw new DatabaseException("Could not create new entry in the table " + tableName + ". Details: " + e.getMessage());
        }
    }

    private void setUpConnection() throws DatabaseException {

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection
            String url = "jdbc:mysql://scrumble.cvqntnx7bdmt.eu-north-1.rds.amazonaws.com:3306/Scrumble";
            String username = "user";
            String password = "Scrumble123";
            connection = DriverManager.getConnection(url, username, password);

        }catch (Exception e){
            throw new DatabaseException("Could not connect to database! Details: " + e.getMessage());
        }
    }

    public static final class DatabaseException extends RuntimeException{

        public DatabaseException(String message){
            super(message);
        }

    }

    public final class InsertResult{

        private final boolean successful;
        private final Integer generatedID;

        private InsertResult(boolean successful, Integer generatedID){
            this.successful = successful;
            this.generatedID = generatedID;
        }

        /**
         * @return whether or not the INSERT statement ran correctly
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * @return The unique ID generated for the row created after the INSERT statement was run. May be null if no such uniqueID was generated.
         */
        public long getGeneratedID() {
            return generatedID;
        }
    }

}
