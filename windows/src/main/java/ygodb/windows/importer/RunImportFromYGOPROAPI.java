package ygodb.windows.importer;

import java.io.IOException;
import java.sql.SQLException;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.connection.WindowsUtil;
import ygodb.commonLibrary.importer.ImportFromYGOPROAPI;

public class RunImportFromYGOPROAPI {

    public static void main(String[] args) throws SQLException, IOException {

        String setName = "Speed Duel GX: Duelists of Shadows";

        ImportFromYGOPROAPI mainObj = new ImportFromYGOPROAPI();

        SQLiteConnection db = WindowsUtil.getDBInstance();

        mainObj.run(db, setName);
        db.closeInstance();
        System.out.println("Import Finished");
    }

}
