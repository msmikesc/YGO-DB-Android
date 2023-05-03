package ygodb.windows.connection;

public class WindowsUtil {

    private static SQLiteConnectionWindows dbInstance = null;

    public static SQLiteConnectionWindows getDBInstance(){
        if (dbInstance == null){
            dbInstance = new SQLiteConnectionWindows();
        }

        return dbInstance;
    }
}
