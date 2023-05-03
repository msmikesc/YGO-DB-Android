package ygodb.windows.importer;

import java.io.IOException;
import java.sql.SQLException;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.connection.WindowsUtil;
import ygodb.commonLibrary.importer.ImportFromYGOPROAPI;

public class RunImportFromYGOPROAPI {

    public static void main(String[] args) throws SQLException, IOException {
        ImportFromYGOPROAPI mainObj = new ImportFromYGOPROAPI();

        SQLiteConnection db = WindowsUtil.getDBInstance();

        mainObj.run(db);
        db.closeInstance();
        System.out.println("Import Finished");
    }

}
