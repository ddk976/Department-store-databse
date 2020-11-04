import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Year;

import java.util.Properties;

import org.newsclub.net.unix.AFUNIXSocketFactory;

public class FootballRecordsSystem {

    public static void main(String[] args) throws SQLException {

        // You should only need to fetch the connection details once
        Connection conn = getConnection();

        // Display simple menu
        System.out.println("Select a function:");
        System.out.println("1. Delete data and re-import schema");
        System.out.println("2. Batch load data from CSV files (incomplete)");
        System.out.println("3. List all leagues");
        System.out.println("q. quit");

        // Wait for user input
        String selectedOption = readEntry("Enter your option: ");

        // Act on selected option
        if (selectedOption.charAt(0) == '1') {
            processSchema(conn);
        } else if (selectedOption.charAt(0) == '2') {
            importData(conn);
        } else if (selectedOption.charAt(0) == '3') {
            showAllLeagues(conn);
        } else {
            System.out.println("No valid option given...exiting.");
        }

        // Always close statements, result sets and connections after use
        // Otherwise you run out of available open cursors!
        conn.close();

    }

    /**
    * @param conn An open database connection 
    */
    public static void processSchema(Connection conn) {

        // TODO: This is an example schema that works with the format of the input data

        String playerTableCreate = "CREATE TABLE PLAYER"
            + "("
            + " PlayerID INTEGER PRIMARY KEY,"
            + " Name VARCHAR(30) NOT NULL,"
            + " Age INTEGER NOT NULL,"
            + " Height DECIMAL(5,2) NOT NULL,"
            + " Weight DECIMAL(5,2) NOT NULL,"
            + " Nationality VARCHAR(30) NOT NULL,"
            + " Rating INTEGER NOT NULL,"
            + " PreferredFoot CHAR(1) NOT NULL,"
            + " Position CHAR(3) NOT NULL,"
            + " ShirtNumber INT(4) NOT NULL"
            + " ContractExpiry DATE NOT NULL"
            + ")";

        String teamTableCreate = "CREATE TABLE TEAM"
            + "("
            + " TeamID INTEGER PRIMARY KEY,"
            + " Name VARCHAR(60) NOT NULL,"
            + " StadiumName VARCHAR(60) NOT NULL,"
            + " StadiumCapacity INTEGER NOT NULL,"
            + " City VARCHAR(60) NOT NULL"
            + ")";

        String teamPlayerTableCreate = "CREATE TABLE TEAM_PLAYER"
            + "("
            + " TeamID INTEGER NOT NULL,"
            + " PlayerID INTEGER UNIQUE,"
            + " PRIMARY KEY (TeamID，PlayerID),"
            + " FOREIGN KEY (TeamID) reference TEAM(TeamID) ,"
            + " FOREIGN KEY (PlayerID) reference PLAYER(PlayerID),"
            + ")";

        String leagueTableCreate = "CREATE TABLE LEAGUE"
            + "("
            + " LeagueID INTEGER PRIMARY KEY,"
            + " Name VARCHAR(60) NOT NULL,"
            + " Country VARCHAR(60) NOT NULL,"
            + " DateEstablished DATE NOT NULL"
            + ")";


        String seasonTableCreate = "CREATE TABLE SEASON"
            + "("
            + " SeasonID INTEGER PRIMARY KEY,"
            + " LeagueID INTEGER NOT NULL,"
            + " SeasonStartDate DATE NOT NULL,"
            + " SeasonEndDate DATE NOT NULL,"
            + " FOREIGN KEY (LeagueID) REFERENCES LEAGUE(LeagueID)"
            + ")";

        String seasonTeamTableCreate = "CREATE TABLE SEASON_TEAM"
            + "("
            + " SeasonID INTEGER NOT NULL,"
            + " TeamID INTEGER NOT NULL ,"
            + " PRIMARY KEY (SeasonID, TeamID),"
            + " FOREIGN KEY (SeasonID) REFERENCES SEASON(SeasonID),"
            + " FOREIGN KEY (TeamID) REFERENCES TEAM(TeamID)"
            + ")";

        String matchTableCreate = "CREATE TABLE MATCH"
            + "("
            + " MatchID INTEGER PRIMARY KEY,"
            + " SeasonID INTEGER NOT NULL,"
            + " HomeTeamID INTEGER NOT NULL,"
            + " AwayTeamID INTEGER NOT NULL,"
            + " DatePlayed DATE NOT NULL,"
            + " FOREIGN KEY (SeasonID) REFERENCES SEASON(SeasonID),"
            + " FOREIGN KEY (HomeTeamID) REFERENCE TEAM(TeamID),"
            + " FOREIGN KEY (AwayTeamID) REFERENCE TEAM(TeamID)"
            + ")";

        String goalTableCreate = "CREATE TABLE GOAL"
            + "("
            + " GoalID INTEGER PRIMARY KEY,"
            + " MatchID INTEGER NOT NULL,"
            + " PlayerID INTEGER NOT NULL ,"
            + " TeamID INTEGER NOT NULL,"
            + " MinuteScored  INTEGER NOT NULL ,"
            + " FOREIGN KEY (MatchID) REFERENCES MATCH(MatchID),"
            + " FOREIGN KEY (PlayerID) REFERENCES PLAYER(PlayerID),"
            + " FOREIGN KEY (TeamID) REFERENCES TEAM(TeamID),"
            + " CHECK(MinuteScored>0 ANG MinuteScore<= 90),"
            + ")";


        //The below code looks very repetitive, 
        //but if one of the tables fails to be created, we'd like to make sure the others are attempted

        recreateTable(conn,"LEAGUE",leagueTableCreate);
         recreateTable(conn,"PLAYER", playerTableCreate);
         recreateTable(conn,"TEAM", teamTableCreate);
         recreateTable(conn,"TEAM_PLAYER",teamPlayerTableCreate);
         recreateTable(conn,"SEASON",seasonTableCreate);
         recreateTable(conn,"SEASON_TEAM",seasonTeamTableCreate);
         recreateTable(conn,"MATCH",matchTableCreate);
         recreateTable(conn,"GOAL",goalTableCreate);

    }
    
    /**
    * @param conn An open database connection 
    * @param tableName The name of the table to be recreated
    * @param sqlCreateTable String containing the relevant CREATE TABLE statement
    */
    public static void recreateTable(Connection conn, String tableName, String sqlCreateTable) {        
        Statement statement;
        try {
            dropTable(conn, tableName);
            statement = conn.createStatement();
            statement.execute(sqlCreateTable);
            statement.close();

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s\n\n", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * @param conn An open database connection 
    * @param tableName The name of the table to be dropped
    */
    public static void dropTable(Connection conn, String tableName) {
        
        String tableDropStatement = "DROP TABLE "+ tableName +" CASCADE";
        
        try {

            Statement statement = conn.createStatement();
            statement.execute(tableDropStatement);
            System.out.println("Dropped table " + tableName);
            statement.close();

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s\n\n", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /**
    * @param conn An open database connection 
    */
    public static void importData(Connection conn) {

        // This might be useful for the season start and end dates!
        DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // And for the match dates!
        DateTimeFormatter alternativeShortDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String[][] leagues = loadDataFromCSV("leagues.csv");

        System.out.print("Populating the LEAGUE table...");

        String insertStatement = "INSERT INTO LEAGUE"
            + "(LeagueID, Name, Country, DateEstablished)"
            + " VALUES"
            + "(?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;

        for (String[] league : leagues) {

            try {

                preparedStatement = conn.prepareStatement(insertStatement);

                preparedStatement.setInt(1, Integer.parseInt(league[0]));
                preparedStatement.setString(2, league[1]);
                preparedStatement.setString(3, league[2]);
                preparedStatement.setDate(4, java.sql.Date.valueOf(Year.parse(league[3]).atDay(1)));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

       String[][] players = loadDataFromCSV("players.csv");

        System.out.print("Populating the PLAYER table...");

        String insertPlayerStatement = "INSERT INTO PLAYER"
                   +"(ID,Name,Age,Height,Weight,Nationality,Rating,Preferred,Position,ShirtNumber,ContractExpirty)";

        for (String[] player : players) {

            try {

                preparedStatement = conn.prepareStatement(insertPlayerStatement);

                preparedStatement.setDate(11, java.sql.Date.valueOf(Year.parse(player[10]).atDay(1)));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");



        String[][] teams = loadDataFromCSV("teams.csv");

        System.out.print("Populating the TEAM table...");

        String insertTeamStatement = "INSERT INTO TEAM"
            + "(TeamID, Name, StadiumName, StadiumCapacity, City)"
            + " VALUES"
            + "(?, ?, ?, ?, ?)";

        for (String[] team : teams) {

            try {

                preparedStatement = conn.prepareStatement(insertTeamStatement);

                preparedStatement.setInt(1, Integer.parseInt(team[0]));
                preparedStatement.setString(2, team[1]);
                preparedStatement.setString(3, team[2]);
                preparedStatement.setInt(4, Integer.parseInt(team[3]));
                preparedStatement.setString(5, team[4]);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

        String[][] seasons = loadDataFromCSV("seasons.csv");

        System.out.print("Populating the SEASON table...");

        String insertSeasonStatement = "INSERT INTO SEASON"
            + "(SeasonID, LeagueID, SeasonStartDate, SeasonEndDate)"
            + " VALUES"
            + "(?, ?, ?, ?)";

        for (String[] season : seasons) {

            try {

                preparedStatement = conn.prepareStatement(insertSeasonStatement);

                preparedStatement.setInt(1, Integer.parseInt(season[0]));
                preparedStatement.setInt(2, Integer.parseInt(season[1]));
                preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.parse(season[2], shortDateFormat)));
                preparedStatement.setDate(4, java.sql.Date.valueOf(LocalDate.parse(season[3], shortDateFormat)));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

        String[][] seasonteams = loadDataFromCSV("seasonteam.csv");

        System.out.print("Populating the SEASON_TEAM table...");

        String insertSeasonTeamStatement = "INSERT INTO SEASON_TEAM"
            + "(SeasonID, TeamID)"
            + " VALUES"
            + "(?, ?)";

        for (String[] seasonteam : seasonteams) {

            try {

                preparedStatement = conn.prepareStatement(insertSeasonTeamStatement);

                preparedStatement.setInt(1, Integer.parseInt(seasonteam[0]));
                preparedStatement.setInt(2, Integer.parseInt(seasonteam[1]));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

        String[][] teamplayers = loadDataFromCSV("teamplayer.csv");

        System.out.print("Populating the TEAM_PLAYER table...");

        String insertTeamPlayerStatement = "INSERT INTO TEAM_PLAYER"
            + "(TeamID, PlayerID)"
            + " VALUES"
            + "(?, ?)";

        for (String[] teamplayer : teamplayers) {

            try {

                preparedStatement = conn.prepareStatement(insertTeamPlayerStatement);

                preparedStatement.setInt(1, Integer.parseInt(teamplayer[0]));
                preparedStatement.setInt(2, Integer.parseInt(teamplayer[1]));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

        String[][] matches = loadDataFromCSV("matches.csv");

        System.out.print("Populating the MATCH table...");

        String insertMatchStatement = "INSERT INTO MATCH"
            + "(MatchID, SeasonID, HomeTeamID, AwayTeamID, DatePlayed)"
            + " VALUES"
            + "(?, ?, ?, ?, ?)";

        for (String[] match : matches) {

            try {

                preparedStatement = conn.prepareStatement(insertMatchStatement);

                preparedStatement.setInt(1, Integer.parseInt(match[0]));
                preparedStatement.setInt(2, Integer.parseInt(match[1]));
                preparedStatement.setInt(3, Integer.parseInt(match[2]));
                preparedStatement.setInt(4, Integer.parseInt(match[3]));
                preparedStatement.setDate(5, java.sql.Date.valueOf(LocalDate.parse(match[4], alternativeShortDateFormat)));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

        String[][] goals = loadDataFromCSV("goals.csv");

        System.out.print("Populating the GOAL table...");

        String insertGoalStatement = "INSERT INTO GOAL"
            + "(GoalID, MatchID, PlayerID, TeamID, MinuteScored)"
            + " VALUES"
            + "(?, ?, ?, ?, ?)";

        for (String[] goal : goals) {

            try {

                preparedStatement = conn.prepareStatement(insertGoalStatement);

                preparedStatement.setInt(1, Integer.parseInt(goal[0]));
                preparedStatement.setInt(2, Integer.parseInt(goal[1]));
                preparedStatement.setInt(3, Integer.parseInt(goal[2]));
                preparedStatement.setInt(4, Integer.parseInt(goal[3]));
                preparedStatement.setInt(5, Integer.parseInt(goal[4]));

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                }
            }

        }

        System.out.println("done!");

    }

    public static void showAllLeagues(Connection conn) {

        String selectLeaguesStatement = "SELECT LeagueID, Name, DateEstablished FROM LEAGUE";

        try {

            PreparedStatement preparedStatement = conn.prepareStatement(selectLeaguesStatement);

            ResultSet leagues = preparedStatement.executeQuery();

            while (leagues.next()) {
                System.out.println(leagues.getInt(1) + " | " + leagues.getString(2) + " | " + leagues.getDate(3));
            }

            // Always close statements, result sets and connections after use
            // Otherwise you run out of available open cursors!
            preparedStatement.close();
            leagues.close();

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
    * @param conn An open database connection 
    * @param filename The filename for the csv that you are loading
    */
    public static String[][] loadDataFromCSV(String filename) {

        //You do not need to modify this method - just know that it returns a 2D array

        String[][] loadedData = new String[0][0];

        try {

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
            
            String row;

            
            int lineCount = 0;

            int attributeCount = 0;

            boolean firstLine = true;
            
            while ((row = csvReader.readLine()) != null) {
                lineCount++;
                if (firstLine) {
                    String[] data = row.split(",");
                    attributeCount = data.length;
                    firstLine = false;
                }
            }

            csvReader.close();

            loadedData = new String[lineCount - 1][attributeCount];

            csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));

            // Skips the header
            csvReader.readLine();

            int loadedCount = 0;

            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");

                int j = loadedCount++;

                for (int i = 0; i < attributeCount; i ++) {
                    loadedData[j][i] = data[i];
                }

            }
            csvReader.close();

            System.out.println("Successful import of " + filename + " with " + (lineCount-1) + " records");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedData;

    }

    private static String readEntry(String prompt) {
        
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }

    }

    public static Connection getConnection(){
        Properties props = new Properties();
        props.setProperty("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");

        props.setProperty("socketFactoryArg",System.getenv("PGHOST") + "/.s.PGSQL.5432");
        Connection conn;
        try{
          conn = DriverManager.getConnection("jdbc:postgresql://localhost/football", props);
          return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() {
        //This version of getConnection uses ports to connect to the server rather than sockets
        //If you use this method, you should comment out the above getConnection method, and comment out lines 19 and 21
        String user = "me";
        String passwrd = "mypassword";
        Connection conn;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:15432/mydb?user="+ user +"&password=" + passwrd);

            return conn;
        } catch(SQLException e) {
                e.printStackTrace();
            System.out.println("Error retrieving connection");
            return null;
        }

    }


}
