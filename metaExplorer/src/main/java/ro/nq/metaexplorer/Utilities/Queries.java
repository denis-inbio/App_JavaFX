package ro.nq.metaexplorer.Utilities;

import ro.nq.metaexplorer.DataEncapsulations.FileMetadata;

import java.sql.*;
import java.util.LinkedList;

public class Queries {
    public static final String tableName = "files";
    public static final String createTable_CompleteQuery = "CREATE TABLE IF NOT EXISTS \"" + Queries.tableName + "\" (" +
            "\"id\" INTEGER NOT NULL UNIQUE," +
            "\"fileName\" TEXT NOT NULL," +
            "\"fileType\" TEXT NOT NULL," +
            "\"fileSize\" INTEGER NOT NULL," +
            "\"tags\" TEXT," +
            "\"content\" BLOB NOT NULL UNIQUE," +
            "PRIMARY KEY(\"id\" AUTOINCREMENT)" +
        ");";
    public static final String selectAllFiles_CompleteQuery = "SELECT \"id\", \"fileName\", \"fileType\", \"fileSize\", \"tags\", \"content\" FROM \"" + Queries.tableName + "\";";
    public static final String selectFile_ById = "SELECT \"id\", \"fileName\", \"fileType\", \"fileSize\", \"tags\", \"content\" FROM \"" + Queries.tableName + "\" WHERE id=?;";
    public static final String conditionalSelectFile_QueryPrefix = "SELECT \"id\", \"fileName\", \"fileType\", \"fileSize\", \"tags\", \"content\" FROM \"" + Queries.tableName + "\"";
    public static final String insertFile_CompleteQuery = "INSERT INTO \"" + Queries.tableName + "\" (\"fileName\", \"fileType\", \"fileSize\", \"tags\", \"content\") VALUES (?, ?, ?, ?, ?);";
    public static final String updateFile_CompleteQuery = "UPDATE \"" + Queries.tableName + "\" SET \"fileName\"=?, \"fileType\"=?, \"fileSize\"=?, \"tags\"=? WHERE \"id\"=?;";
    public static final String removeAllFiles_CompleteQuery = "DELETE FROM \"" + Queries.tableName + "\";";
    public static final String removeFile_ById = "DELETE FROM \"" + Queries.tableName + "\n WHERE id=?;";
    public static final String countFiles_CompleteQuery = "SELECT COUNT(*) as \"count\" FROM \"" + Queries.tableName + "\";";
// ---- [methods]
    public static boolean evaluateCreateTableIfNotExists (Connection connection) {
        try {
            boolean exists = false;

            DatabaseMetaData databaseMetaData = connection.getMetaData();
//                databaseMetaData.
            // modify ::exists if table is found
            // exists = true;

            PreparedStatement preparedStatement = connection.prepareStatement(Queries.createTable_CompleteQuery);

            // <TODO> this is NOT the way to determine if the table was or wasn't created (*); instead, query the Sqlite database for its Metadata
            //  and search for the table there
            preparedStatement.execute();
            return !exists;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateSelectAllFilesStatement()} [SQLException] (Connection.prepareStatement() | PreparedStatement.executeQuery()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public static LinkedList<FileMetadata> evaluateSelectAllFilesStatement (Connection connection) {
        try {
            LinkedList<FileMetadata> files = new LinkedList<>();

            PreparedStatement preparedStatement = connection.prepareStatement(Queries.selectAllFiles_CompleteQuery);
                ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        long id = resultSet.getLong(1);
                        String fileName = resultSet.getString(2);
                        String fileType = resultSet.getString(3);
                        long fileSize = resultSet.getLong(4);
                        String tags = resultSet.getString(5);
                        byte[] content = resultSet.getBytes(6);

                        files.add(new FileMetadata(connection, id, fileName, fileType, fileSize, tags, content));
                    }

            return files;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateSelectAllFilesStatement()} [SQLException] (Connection.prepareStatement() | PreparedStatement.executeQuery()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
    public static PreparedStatement prepareSelectByIdStatement (Connection connection, long id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.selectFile_ById);
                preparedStatement.setLong(1, id);

            return preparedStatement;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.prepareSelectByIdStatement} [SQLException] (Connection.prepareStatement()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
    public static LinkedList<FileMetadata> evaluateSelectByIdStatement (Connection connection, PreparedStatement preparedStatement) {
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet == null) {
                    return null;
                }
            try {
                LinkedList<FileMetadata> result = new LinkedList<>();
                    while (resultSet.next()) {
                        long id = resultSet.getLong(1);
                        String fileName = resultSet.getString(2);
                        String fileType = resultSet.getString(3);
                        long fileSize = resultSet.getLong(4);
                        String tags = resultSet.getString(5);
                        byte[] content = resultSet.getBytes(6);

                        if (fileName != null && fileType != null && tags != null && content != null && content.length > 0) {
                            FileMetadata completeFileDatabase = new FileMetadata(connection, id, fileName, fileType, fileSize, tags, content);
                                result.add(completeFileDatabase);
                            System.out.println("Returned file with id: " + id + " and size: " + fileSize + " Bytes");
                        }
                    }

                return result;
            }
            catch (SQLException exception) {
                System.out.println("{Queries.evaluateSelectByIdStatement} [SQLException] (ResultSet.next()) - " + exception.getMessage() + "\n" + exception.getCause());
                return null;
            }

        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateSelectByIdStatement} [SQLException] (PreparedStatement.executeQuery()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
    public static String generateConditionalSelectQuery(boolean fileName, boolean fileType, boolean fileSizeMinimum, boolean fileSizeMaximum, int tags, int limit) {
        String query = Queries.conditionalSelectFile_QueryPrefix;
        String wherePrefix = " WHERE";
        boolean whereInserted = false;

        if (fileName) {
            if (!whereInserted) {
                query += wherePrefix;
                whereInserted = true;
            }

            query += " \"fileName\" LIKE ?";
        }

        if (fileType) {
            if (!whereInserted) {
                query += wherePrefix;
                whereInserted = true;
            }

            if (fileName) {
                query += " AND";
                    fileName = false;
            }
            query += " \"fileType\" LIKE ?";
        }

        if (fileSizeMinimum) {
            if (!whereInserted) {
                query += wherePrefix;
                whereInserted = true;
            }

            if (fileName || fileType) {
                query += " AND";
                    fileName = false;
                    fileType = false;
            }
            query += " ? <= \"fileSize\"";
        }

        if (fileSizeMaximum) {
            if (!whereInserted) {
                query += wherePrefix;
                whereInserted = true;
            }

            if (fileName || fileType || fileSizeMinimum) {
                query += " AND";
                fileName = false;
                fileType = false;
                fileSizeMinimum = false;
            }
            query += " \"fileSize\" <= ?";
        }

        if (tags > 0) {
            if (!whereInserted) {
                query += wherePrefix;
                whereInserted = true;
            }

            if (fileName || fileType || fileSizeMinimum || fileSizeMaximum) {
                query += " AND (";
            }
            for (int index = 0; index < tags; index++) {
                if (index > 0) {
                    query += " OR";
                }
                query += " \"tags\" LIKE ?";
            }

            if (fileName || fileType || fileSizeMinimum || fileSizeMaximum) {
                query += " )";
                fileName = false;
                fileType = false;
                fileSizeMinimum = false;
                fileSizeMaximum = false;
            }
        }

        if (limit > 0) {
            query += " LIMIT " + String.valueOf(limit);
        }

        query += ";";

        return query;
    }
    public static LinkedList<FileMetadata> evaluateConditionalSelectQuery (Connection connection, PreparedStatement preparedStatement) {
        try {
            LinkedList<FileMetadata> files = new LinkedList<>();

            ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    long id = resultSet.getLong(1);
                    String fileName = resultSet.getString(2);
                    String fileType = resultSet.getString(3);
                    long fileSize = resultSet.getLong(4);
                    String tags = resultSet.getString(5);
                    byte[] content = resultSet.getBytes(6);

                    files.add(new FileMetadata(connection, id, fileName, fileType, fileSize, tags, content));
                }

            return files;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateConditionalSelectQuery()} [SQLException] (Connection.prepareStatement() | PreparedStatement.executeQuery()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }

    public static PreparedStatement createInsertFileStatement (Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.insertFile_CompleteQuery, Statement.RETURN_GENERATED_KEYS);
                return preparedStatement;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.createInsertFileStatement()} [SQLException] (Connection.prepareStatement()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
    public static boolean prepareInsertFileStatement (PreparedStatement preparedStatement, String fileName, String fileType, long fileSize, String tags, byte[] content) {
//        "INSERT INTO \"" + tableName + "\" (\"fileName\", \"fileType\", \"fileSize\", \"tags\", \"content\") VALUES (?, ?, ?, ?, ?);";
        try {
            preparedStatement.setString(1, fileName);
            preparedStatement.setString(2, fileType);
            preparedStatement.setLong(3, fileSize);
            preparedStatement.setString(4, tags);
            preparedStatement.setBytes(5, content);

            return true;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.prepareInsertFileStatement()} [SQLException] (PreparedStatement.setString()|.setLong()|.setBytes()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public static long evaluateInsertFileStatement (PreparedStatement preparedStatement) {
        try {
            int insertedEntry = preparedStatement.executeUpdate();
            long generatedKey = -1;

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedKey = generatedKeys.getLong(1);
                }

            return generatedKey;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateInsertFileStatement()} [SQLException] (PreparedStatement.executeUpdate()|.getGeneratedKeys() | ResultSet.getLong()) - " + exception.getMessage() + "\n" + exception.getCause());
            return -1;
        }
    }

    public static PreparedStatement createUpdateFileStatement (Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.updateFile_CompleteQuery);
            return preparedStatement;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.createUpdateFileStatement()} [SQLException] (Connection.prepareStatement()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
    public static boolean prepareUpdateFileStatement (PreparedStatement preparedStatement, long id, String fileName, String fileType, long fileSize, String tags) {
//        "UPDATE \"" + tableName + "\" SET \"fileName\"=?, \"fileType\"=?, \"fileSize\"=?, \"tags\"=? WHERE \"id\"=?";
        try {
            preparedStatement.setString(1, fileName);
            preparedStatement.setString(2, fileType);
            preparedStatement.setLong(3, fileSize);
            preparedStatement.setString(4, tags);
            preparedStatement.setLong(5, id);

            return true;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.prepareUpdateFileStatement()} [SQLException] (PreparedStatement.setString()|.setLong()|.setLong()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public static boolean evaluateUpdateFileStatement (PreparedStatement preparedStatement) {
        try {
            int affectedEntriesByUpdate = preparedStatement.executeUpdate();
                return affectedEntriesByUpdate >= 1;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateUpdateFileStatement()} [SQLException] (PreparedStatement.executeUpdate()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }

    public static long countFilesStatement (Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.countFiles_CompleteQuery);

            long count = -1;
            ResultSet queryResult = preparedStatement.executeQuery();
                if (queryResult.next()) {
                    count = queryResult.getLong(1);
                }

            return count;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.evaluateCountFilesStatement()} [SQLException] (Connection.prepareStatement() | PreparedStatement.executeQuery() | ResultSet.getLong()) - " + exception.getMessage() + "\n" + exception.getCause());
            return -1;
        }
    }
    public static PreparedStatement prepareRemoveByIdStatement (Connection connection, long id) {
        // "DELETE FROM \"" + Queries.tableName + "\n WHERE id=?;";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.removeFile_ById);
            preparedStatement.setLong(1, id);

            return preparedStatement;
        }
        catch (SQLException exception) {
            System.out.println("{Queries.prepareRemoveByIdStatement} [SQLException] (Connection.prepareStatement()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
// ---- [unknown placement]
    public static LinkedList<String> normalizedTagsList (String tags) {
        String[] splitTags = tags.split(",");

        LinkedList<String> validTags = new LinkedList<>();
            for (int index = 0; index < splitTags.length; index++) {
                String trimmedTag = splitTags[index].trim();
                if (trimmedTag.length() > 0) {
                    validTags.add(trimmedTag);
                }
            }

        return validTags;
    }
    public static String normalizedTagsMerged (String tags) {
        LinkedList<String> tagsList = normalizedTagsList(tags);

        StringBuilder tagsMerged = new StringBuilder();
            for (int index = 0; index < tagsList.size(); index++) {
                if (index > 0) {
                    tagsMerged.append(",");
                }
                tagsMerged.append(tagsList.get(index));
            }

        return tagsMerged.toString();
    }
}
