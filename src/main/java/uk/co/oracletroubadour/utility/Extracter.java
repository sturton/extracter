package uk.co.oracletroubadour.utility;

/*
	 * @author	  Stuart Turton
	 * @version ${project.version}
	 *
	 * Derived from an OTN sample 
	 *
	 */



// Package for JDBC classes 
import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.sql.SQLException;
import java.sql.Connection;


import oracle.jdbc.pool.OracleDataSource;

// Java Utility Classes
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.regex.*;
import java.util.zip.ZipOutputStream;


import java.text.SimpleDateFormat;

// Package for using Streams
import java.io.IOException;

//import java.net.URL;

import java.io.InputStream;

import java.io.OutputStream;

import java.io.PrintStream;


import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleResultSetMetaData;
//import oracle.sql.DATE;

import oracle.jdbc.OracleStatement;

import gnu.getopt.Getopt;

import gnu.getopt.LongOpt;

import java.sql.Date;
import java.sql.Timestamp;

import java.util.Calendar;

import java.util.MissingResourceException;
import java.util.TimeZone;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;

import oracle.jdbc.OracleTypes;

import oracle.sql.DATE;
import oracle.sql.Datum;
import oracle.sql.StructDescriptor;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPTZ;


/**
 * This class extracts data from a SELECT statement and outputs it to a stream,
 * optionally adding field separators and delimiters and record separators.
 * 
 */
public class

Extracter {

    /**
     * Classname utility string for use in logging. 
     */
    private final static String CLASS_NAME = Extracter.class.getCanonicalName();

    public static final String VERSION = "${project.version}"; 
    //public static final String DEFAULT_DATE_FORMAT = "YYYYMMDD"; // Oracle DATE format

    /**
     * Default format to display DATE columns/data
     */
    public static final String DEFAULT_DATE_FORMAT = 
        "yyyyMMddHHmmss"; //Java SimpleDateFormat
        
    public static final String DEFAULT_TIMESTAMP_FORMAT = 
         "yyyyMMddHHmmss.sssss"; //Java SimpleDateFormat
         
    public static final String DEFAULT_TIMESTAMP_TZ_FORMAT = 
           "yyyyMMddHHmmss.sssss zzz"; //Java SimpleDateFormat
           

    public static final String DEFAULT_ORACLE_DATE_FORMAT = 
         "YYYYMMDDHH24MISS"; //Oracle DATE Format

    public static final String DEFAULT_ORACLE_TIMESTAMP_FORMAT = 
        "YYYYMMDDHH24MISS.FF5"; //Oracle Timestamp format

    public static final String DEFAULT_ORACLE_TIMESTAMP_TZ_FORMAT = 
         "YYYYMMDDHH24MISS.FF5 TZD"; //Oracle Timestamp format
          //"YYYYMMDDHH24MISS.FF5 TZR"; //Oracle Timestamp format
          //"YYYYMMDDHH24MISS.FF5 TZH:TZM"; //Oracle Timestamp format

    public static final String DEFAULT_CONFIGURATION_FILE_NAME = 
          Extracter.class.getCanonicalName(); 
    

    /**
     * Default batch size for retrieving rows 
     */
    public static final int DEFAULT_ARRAY_SIZE = 100;

    /*
     * name of the resource file to 
     */
    String configurationFileName = DEFAULT_CONFIGURATION_FILE_NAME;
    
    /** 
     * Database Connection Object 
     * */
    Connection connection = null;

    TimeZone timeZone = TimeZone.getDefault(); //TimeZone neede for any TIMESTAMP WITH LOCAL TIME ZONE rows retrieved

    /**
     * 
     */

    String dateFormat = DEFAULT_DATE_FORMAT; 
    String timestampFormat = DEFAULT_TIMESTAMP_FORMAT; 
    String timestampTZFormat = DEFAULT_TIMESTAMP_TZ_FORMAT; 

    String oracleDateFormat = DEFAULT_ORACLE_DATE_FORMAT; 
    String oracleTimestampFormat = DEFAULT_ORACLE_TIMESTAMP_FORMAT; 
    String oracleTimestampTZFormat = DEFAULT_ORACLE_TIMESTAMP_TZ_FORMAT; 

    /**
     * JDBC driver type for Oracle connection
     */
    String driverType = "thin";

    /**
     * Oracle host 
     */
    String hostName;

    /**
     * Oracle Service
     */
    String ServiceName;

    /**
     * Oracle TNS port 
     */
    String portName;

    /**
     * Oracle Account
     */
    String userName;

    /**
     * Oracle Password
     */
    String password;

    /**
     * JDBC URL - generated from parameters or passed in as a string
     */
    String connectionURL;

    /**
     * File Header - defaults to column names
     */
    String header ;

    /**
     * File Trailer - defaults to nothing 
     */
    String trailer ;

    /**
     * String surrounding fields - default is quote ('"')
     * 
     * Each occurrence of the delimiter within a field is replaced by 2 occurences,
     * i.e. it is the default Oracle method of encoding string delimiters
     */
    String delimiter = "\"";

    /**
     * String between adjacent fields - default is tab character ('\t')
     */
    String separator = "\t";

    /**
     * String between adjacent records - default is carriage return + line feed  ('\r\n')
     */
    String recordSeparator = "\r\n";


    /**
     * Format to output metadata information (SQL*Loader,DATA)- default is none (use the standard Unix convention of "-" to write to standard out)
     */
    String controlFormat = "DATA" ; //Data format 


    /**
     * SQL*Loader Record format as (STRream/FIXED/VARiable)
     */
    String loaderRecordFormat = "STR" ; //Loader Method
     

    
   /**
    * SQL*Loader Method to write control file format as (APPEND/INSERT/TRUNCATE/REPLACE)
    */
    String loaderMethod = "APPEND" ; //Loader Method
    

    /**
     * path to input SQL statement - default is standard input (using the standard Unix convention of "-")
     */
    String input = "-";

    /**
     * path to write output - default is standard output (using the standard Unix convention of "-")
     */
    String output = "-";

    /**
     * path to write output format information (SQL*Loader,DATA, etc.)- default is none (use the standard Unix convention of "-" to write to standard out)
     */
    String control = null ;

    /**
     * path to write errors/processing information - default is standard error (using the standard Unix convention of "-")
     */

    String error = "-";

    //In case we have to generate our own SQL statement 

    /**
     * Allow top-level record set to be derived from SQL (default), PLSQL function returning REFCURSOR --possibly java method
     */
    
    String recordSetSource = "SQL";
    
    /**
     * optional table name - for generating SELECT statements from 
     * "SELECT " 
     * + columnNames 
     * + " FROM "
     * + " tableName "
     * + "WHERE "
     * + whereClause
     */
    String tableName = ""; //recordSetSource = "SQL"

    /**
     * optional column list - for generating SELECT statements from 
     * "SELECT " 
     * + columnNames 
     * + " FROM "
     * + " tableName "
     * + "WHERE "
     * + whereClause
     */
    String columnNames = "*"; //recordSetSource = "SQL"

    /**
     * optional WHERE / GROUP BY / HAVING / ORDER BY text - for generating SELECT statements from 
     * "SELECT " 
     * + columnNames 
     * + " FROM "
     * + " tableName "
     * + "WHERE "
     * + whereClause
     */
    String whereClause = ""; //recordSetSource = "SQL"

    String functionName = "";//recordSetSource = "P"

    /**
     * JDBC prefetch value - always equals DEFAULT_ARRAY_SIZE 
     */
    int prefetchValue = DEFAULT_ARRAY_SIZE;

    /**
     * whether to Zip output - a hook for later enhancement
     */
    boolean zipOutput;
    
    /**
     * ZipOutputStream
     */
    ZipOutputStream zipOutputStream = null ;

    /**
     * Output enhanced logging - set using the "v" (verbose) command line flag
     */
    boolean debug;

    /**
     * Stream for logging messages/warnings /errors
     */
    PrintStream errorStream;


    private String twoDelimiters = "";
    private SimpleDateFormat dateSimpleDateFormat;
    private SimpleDateFormat timestampSimpleDateFormat;
    private SimpleDateFormat timestampTZSimpleDateFormat;
    


    public static void printUsage() {
        System.err.println("Usage: java ... " + CLASS_NAME + " [ option , ... ] " + 
                           "\n\nOptions:\n" + 
                           "\n    A <array-size>       - array-fetchsize default \"" + DEFAULT_ARRAY_SIZE + "\"" + 
                           "\n    v                    - verbose/debug on " + 
                           "\n\n(optional) input/output stream specification:" + 
                           "\n    i <input-pattern>    - file name(s) to get SQL from \"-\"  means take statement from standard input " + 
                           "\n    o <output-pattern>   - file name(s) to output results to \"-\"  means take output to standard output " + 
                           "\n    e <error-pattern>    - file name(s) to output errors and warnings to \"-\"  means take output to standard error " + 
                           "\n    C <control-pattern>  - file name(s) to output meta-data information to \"-\"  means take output to standard error " + 
                           "\n    F <control-format>   - output format for meta-data information " + 
                           "\n\n(optional) output formatting:" + 
                           "\n    D <date-format>      - dateFormat in Java SimpleDateFormat form default \"" + 
                           DEFAULT_DATE_FORMAT + "\"" + 
                           "\n    s <separator>        - field separator default =TAB" + 
                           "\n    d <delimiter>        - field separator default ='\"'  " + 
                           "\n    r <record-separator> - record separator default ='\\r\\n' (CRNL)" + 
                           "\n\n(optional)  header and trailer:" + 
                           "\n    b <header>       - before/header string " + 
                           "\n    a <trailer>      - after/trailer string " + 

                           "\n\n(optional) command-line specification of extract details:" + 
                           "\n    c <column-list>       - list of columns/expressions to extract " + 
                           "\n    t <table-name>        - table/view/inline view to extract from " + 
                           "\n    w <where-clause>      - WHERE clause, GROUP BY , ORDER BY text  " + 
                           "\n    f <function-name>     - PLSQL FUNCTION returning REF CURSOR" + 

                           "\n\n(optional- overrides Connection.properties) connection details:" + 
                           "\n    T <driver-type>      - JDBC driver type (thin|oci|oci8) etc. default Connection.properties " + 
                           "\n    H <host-name>        - JDBC host name (localhost) etc. default Connection.properties " + 
                           "\n    P <listener-port>    - JDBC listener port (1521) etc. default Connection.properties " + 
                           "\n    S <ServiceName>      - JDBC ServiceName Name/SID  (ORCL) etc. default Connection.properties " + 
                           "\n    U <user>             - JDBC user name (btdonline|testonline) etc. default Connection.properties " + 
                           "\n    X <password>         - user password (?) default Connection.properties " + 
                           "\n    u <JDBC-URL>         - URL:" + 
                           "\n                              e.g. \"jdbc:oracle:oci8:${username}/${password}@\" (bequeath)" + 
                           "\n                                   \"jdbc:oracle:oci8:${username}/${password}@${host}\" (OCI)" + 
                           "\n                                   \"jdbc:oracle:thin:${username}/${password}@${host}:${port}/${ServiceName}\" (thin)" + 
                           "\n                                   \"jdbc:oracle:${drivertype}:${username}/${password}@${host}:${port}/${ServiceName}\" (generic)"
                           );
    }

    /**
     *	  Main entry point for the class. 
     *     
     *     Instantiates the Extracter class using the specified command line arguments 
     *	  and sets up the database connection.
     */
    public static void main(String[] args) {
        Extracter Extracter = new Extracter();

        try {

            LongOpt[] longopts = new LongOpt[38];
            longopts[0] = new LongOpt("username", LongOpt.REQUIRED_ARGUMENT, null, 'U'); 
            longopts[1] = new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'X'); 
            longopts[2] = new LongOpt("SID", LongOpt.REQUIRED_ARGUMENT, null, 'S'); 
            longopts[3] = new LongOpt("Host", LongOpt.REQUIRED_ARGUMENT, null, 'H'); 
            longopts[4] = new LongOpt("driverType", LongOpt.REQUIRED_ARGUMENT, null, 'T'); 
            longopts[5] = new LongOpt("Service/SID", LongOpt.REQUIRED_ARGUMENT, null, 'S'); 
            longopts[6] = new LongOpt("Port", LongOpt.REQUIRED_ARGUMENT, null, 'P'); 
            longopts[7] = new LongOpt("dateFormat", LongOpt.REQUIRED_ARGUMENT, null, 'D'); 
            longopts[8] = new LongOpt("arraySize", LongOpt.REQUIRED_ARGUMENT, null, 'A'); 
            longopts[9] = new LongOpt("columnList", LongOpt.REQUIRED_ARGUMENT, null, 'c'); 
            longopts[10] = new LongOpt("tableName", LongOpt.REQUIRED_ARGUMENT, null, 't'); 
            longopts[11] = new LongOpt("whereClause", LongOpt.REQUIRED_ARGUMENT, null, 'w'); 
            longopts[12] = new LongOpt("inputPath", LongOpt.REQUIRED_ARGUMENT, null, 'i'); 
            longopts[13] = new LongOpt("outputPath", LongOpt.REQUIRED_ARGUMENT, null, 'o'); 
            longopts[14] = new LongOpt("errorPath", LongOpt.REQUIRED_ARGUMENT, null, 'e'); 
            longopts[15] = new LongOpt("fieldSeparator", LongOpt.REQUIRED_ARGUMENT, null, 's'); 
            longopts[16] = new LongOpt("fieldDelimiter", LongOpt.REQUIRED_ARGUMENT, null, 'd'); 
            longopts[17] = new LongOpt("recordSeparator", LongOpt.REQUIRED_ARGUMENT, null, 'r'); 
            longopts[18] = new LongOpt("URL", LongOpt.REQUIRED_ARGUMENT, null, 'u'); 
            longopts[19] = new LongOpt("ZIPOutput", LongOpt.REQUIRED_ARGUMENT, null, 'z'); 
            longopts[22] = new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v');
            longopts[23] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
            longopts[20] = new LongOpt("header", LongOpt.REQUIRED_ARGUMENT, null, 'b'); 
            longopts[21] = new LongOpt("trailer", LongOpt.REQUIRED_ARGUMENT, null, 'a'); 
            longopts[24] = new LongOpt("controlFilePath", LongOpt.REQUIRED_ARGUMENT, null, 'C'); 
            longopts[25] = new LongOpt("controlFormat", LongOpt.REQUIRED_ARGUMENT, null, 'F'); 
            longopts[26] = new LongOpt("loaderMethod", LongOpt.REQUIRED_ARGUMENT, null, 'M');
            longopts[27] = new LongOpt("loaderFormat", LongOpt.REQUIRED_ARGUMENT, null, 'L');
            
            longopts[28] = new LongOpt("oracleDateFormat", LongOpt.REQUIRED_ARGUMENT, null, 28); 
            longopts[29] = new LongOpt("oracleTimestampFormat", LongOpt.REQUIRED_ARGUMENT, null, 29); 
            longopts[30] = new LongOpt("oracleTimestampTZFormat", LongOpt.REQUIRED_ARGUMENT, null, 30); 
            longopts[31] = new LongOpt("DateFormat", LongOpt.REQUIRED_ARGUMENT, null, 31); 
            longopts[32] = new LongOpt("TimestampFormat", LongOpt.REQUIRED_ARGUMENT, null, 32); 
            longopts[33] = new LongOpt("TimestampTZFormat", LongOpt.REQUIRED_ARGUMENT, null, 33); 
            longopts[34] = new LongOpt("TimeZone", LongOpt.REQUIRED_ARGUMENT, null, 34); 
            longopts[35] = new LongOpt("ResultSetSource", LongOpt.REQUIRED_ARGUMENT, null, 'R'); 
            longopts[36] = new LongOpt("PLSQLFunctionName", LongOpt.REQUIRED_ARGUMENT, null, 'f'); 
            longopts[37] = new LongOpt("ConfigurationFileName", LongOpt.REQUIRED_ARGUMENT, null, 37); 

/*
 
 Long options are defined by LongOpt objects. 
 These objects are created with a contructor that takes four params: 
 a String representing the object name
 a integer specifying what arguments the option takes (the value is one of LongOpt.NO_ARGUMENT, LongOpt.REQUIRED_ARGUMENT, or LongOpt.OPTIONAL_ARGUMENT)
 a StringBuffer flag object (described below)
 an integer value (described below).

 To enable long option parsing, create an array of LongOpt's representing the legal options and pass it to the Getopt() constructor. WARNING: If all elements of the array are not populated with LongOpt objects, the getopt() method will throw a NullPointerException.
 *  When getopt() is called and a long option is encountered, one of two things can be returned. 
 *  If the flag field in the LongOpt object representing the long option is non-null, 
 *  then the integer value field is stored there and an integer 0 is returned to the caller. 
 *  The val field can then be retrieved from the flag field. 
 *  Note that since the flag field is a StringBuffer, 
 *  the appropriate String to integer converions must be performed in order to get the actual int value stored there. 
 *  If the flag field in the LongOpt object is null, then the value field of the LongOpt is returned. 
 *  This can be the character of a short option. 
 *  This allows an app to have both a long and short option sequence (say, "-h" and "--help") that do the exact same thing.
 *  With long options, there is an alternative method of determining which option was selected. 
 *  The method getLongind() will return the the index in the long option array (NOT argv) of the long option found. So if multiple long options are configured to return the same value, the application can use getLongind() to distinguish between them. 
 */

            final Getopt getopt = 
                new Getopt("Extracter", args, "F:C:T:H:P:S:U:X:D:A:c:t:w:i:o:e:s:d:r:u:vhzb:a:L:M:R:f:W;",longopts);

            int c;
            while ((c = getopt.getopt()) != -1) {
                switch (c) {

                    case 28:
                        Extracter.setOracleDateFormat(getopt.getOptarg());
                        break;
                    case 29:
                        Extracter.setOracleTimestampFormat(getopt.getOptarg());
                        break;
                    case 30:
                        Extracter.setOracleTimestampTZFormat(getopt.getOptarg());
                        break;
                    case 31:
                        Extracter.setDateFormat(getopt.getOptarg());
                        break;
                    case 32:
                        Extracter.setTimestampFormat(getopt.getOptarg());
                        break;
                    case 33:
                        Extracter.setTimestampTZFormat(getopt.getOptarg());
                        break;

                    case 34:
                        Extracter.setTimeZone(TimeZone.getTimeZone(getopt.getOptarg()));
                        break;

                    case 37:
                        Extracter.setConfigurationFileName(getopt.getOptarg());
                        break;

                 
                 case 1:
                   System.out.println("I see you have return in order set and that " +
                                      "a non-option argv element was just found " +
                                      "with the value '" + getopt.getOptarg() + "'");
                   break;
                       //
                case 'T':
                    Extracter.setDriverType(getopt.getOptarg());
                    break;

                case 'H':
                    Extracter.setHostName(getopt.getOptarg());
                    break;

                case 'P':
                    Extracter.setPortName(getopt.getOptarg());
                    break;

                case 'S':
                    Extracter.setServiceName(getopt.getOptarg());
                    break;

                case 'U':
                    Extracter.setUserName(getopt.getOptarg());
                    break;

                case 'X':
                    Extracter.setPassword(getopt.getOptarg());
                    break;

                case 'D':
                    Extracter.setDateFormat(getopt.getOptarg());
                    break;

                case 'A':
                    Extracter.setPrefetchValue(Integer.parseInt(getopt.getOptarg()));
                    break;

                case 'v':
                    Extracter.setDebug(true);
                    break;

                case 'i':
                    Extracter.setInput(getopt.getOptarg());
                    break;

                case 'o':
                    Extracter.setOutput(getopt.getOptarg());
                    break;

                case 'C':
                        Extracter.setControl(getopt.getOptarg());
                        break;

                case 'F':
                        Extracter.setControlFormat(getopt.getOptarg());
                        break;

                case 'M':
                        Extracter.setLoaderMethod(getopt.getOptarg());
                        break;

                case 'L':
                        Extracter.setLoaderRecordFormat(getopt.getOptarg());
                        break;

                case 'e':
                    Extracter.setError(getopt.getOptarg());
                    break;

                case 's':
                    Extracter.setSeparator(unescape(getopt.getOptarg()));
                    break;
                case 'd':
                    Extracter.setDelimiter(unescape(getopt.getOptarg()));
                    break;

                case 'r':
                    Extracter.setRecordSeparator(unescape(getopt.getOptarg()));
                    break;

                case 'c':
                    Extracter.setColumnNames(getopt.getOptarg());
                    break;

                case 't':
                    Extracter.setTableName(getopt.getOptarg());
                    break;

                case 'w':
                    Extracter.setWhereClause(getopt.getOptarg());
                    break;

                case 'u':
                    Extracter.setConnectionURL(getopt.getOptarg());
                    break;

                case 'b':
                    Extracter.setHeader(getopt.getOptarg());
                    break;

                case 'a':
                    Extracter.setTrailer(getopt.getOptarg());
                    break;

                case 'z':
                    Extracter.setZipOutput(true);
                    break;

                case 'R':
                    Extracter.setRecordSetSource(getopt.getOptarg());
                    break;

                case 'f':
                    Extracter.setFunctionName(getopt.getOptarg());
                    break;

                case 'W':
                  throw new Exception("Hmmm. You tried a -W with an incorrect long " +
                                     "option name");
                  //break;

                case ':':
                  throw new Exception("Doh! You need an argument for option " +
                                     (char) c);
                  //break;
                case '?': //getOptArg() has alread printed an error
                    throw new Exception("Parameter validation error");
                    
                default:
                    printUsage();
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Validation problem:" + e.getMessage());
            printUsage();
            System.exit(1);
        }

        //Passed validation - Perform the extract
        try {
            try {
                if (Extracter.error.equals("-")) {
                    Extracter.setErrorStream(System.err);
                } else {
                    Extracter.setErrorStream(new PrintStream(new FileOutputStream(Extracter.error)));
                }
            } catch (IOException io) {
                System.err.println("Failed to open error output \"" + 
                                   Extracter.error + 
                                   "\" (defaulting to standard error): " + 
                                   io.getMessage());
                Extracter.setErrorStream(System.err);
            }
            Extracter.dbConnection(); // Make JDBC Connection

            if (Extracter.connection != null) {
                Extracter.selectRecords();
                Extracter.exitApplication();
            }
        } catch (Exception e) {

            ((null == Extracter.errorStream) ? System.err : 
             Extracter.errorStream).println("Execution problem:" + 
                                           e.getMessage() + "\nStack Trace:");
            e.printStackTrace((null == Extracter.errorStream) ? System.err : 
                              Extracter.errorStream);
            System.exit(1);
        }

    }

    /**
     * This method reads a properties file which is passed as
     * the parameter to it and load it into a java Properties
     * object and returns it.
     */
    private static Properties loadParams(String file) throws IOException {
        // Loads a ResourceBundle and creates Properties from it
        Properties prop = new Properties();
        ResourceBundle bundle = ResourceBundle.getBundle(file);
        Enumeration enumeration = bundle.getKeys();
        String key = null;
        while (enumeration.hasMoreElements()) {
            key = (String)enumeration.nextElement();
            prop.put(key, bundle.getObject(key));
        }
        return prop;
    }


  /** Convert String as Hexadecimal encoded equivalent
   * @param s - incoming string 
   * @return hexadecimal encoded string 
   */
  private String StringToHexString(String s)
  {
    char c;
    StringBuffer sb = new StringBuffer();
    String hex;
    
    for (int i = 0 ; i < s.length() ; i++) 
    {
      c = s.charAt(i);
      
      hex = Integer.toString(c,16);
      
      if (hex.length() == 1)
      hex = '0' + hex;
      
      sb.append(hex); 
    } 
    
    return sb.toString();
  }
  
    /**
     * Creates a database connection object using DataSource object. Please
     * substitute the database connection parameters with appropriate values in
     * Connection.properties file
     */
    private void dbConnection() throws Exception {
        OracleDataSource ods = null;
        try {
            //gui.putStatus("Trying to connect to the Database");
            if (this.debug)
                System.err.println("Trying to connect to the Database");

            // Set the V$SESSION columns
            Properties sessionProperties = new Properties();
            sessionProperties.put("v$session.program", this.getClass().getName());
            sessionProperties.put("V$SESSION.PROGRAM", this.getClass().getName());
            
            // Load the properties file to get the connection information
            Properties prop = null;
            try // Loading 
            {
              if (debug) System.err.println("Reading confguration file \"" 
                                            + configurationFileName
                                            + "\""
                                            ); 
              prop = this.loadParams(configurationFileName);
            }
            catch (MissingResourceException mre)
            {
              System.err.println("ERR: Could not find specified configuration file \""
                                                  + configurationFileName
                                                  + "\""
                                                  );
              if (!configurationFileName.equals(DEFAULT_CONFIGURATION_FILE_NAME))
              {
                throw new Exception("Could not find specified configuration file \""
                                    + configurationFileName
                                    + "\""
                                    );                
              }
              else
              {
                System.err.println("WARN: Could not find default configuration file \""
                                  + configurationFileName
                                  + "\", reverting to fallback generic configuration file");                 
                try 
                {
                  prop = this.loadParams("Connection");                
                }
                catch (Exception e)
                {
                  System.err.println("Could not find specified configuration file \""
                                    + "Connection"
                                    + "\"");                 
                }
              }
            }

            // Create a OracleDataSource instance
            ods = new OracleDataSource();

            if (null != connectionURL && !connectionURL.equals("")) {
                ods.setURL(connectionURL);
            } else {
                // Sets the driver type
                ods.setDriverType((null != driverType && 
                                   !driverType.equals("")) ? driverType : 
                                  (String)prop.get("DriverType"));

                // Sets the database server name
                ods.setServerName((null != hostName && !hostName.equals("")) ? 
                                  hostName : (String)prop.get("HostName"));

                // Sets the database name
                ods.setDatabaseName((null != ServiceName && 
                                     !ServiceName.equals("")) ? ServiceName : 
                                    (String)prop.get("ServiceName"));

                // Sets the port number
                ods.setPortNumber(new Integer((null != portName && 
                                               !portName.equals("")) ? 
                                              portName : 
                                              (String)prop.get("Port")).intValue());

                // Sets the user name
                ods.setUser((null != userName && !userName.equals("")) ? 
                            userName : (String)prop.get("UserName"));

                // Sets the password
                ods.setPassword((null != password && !password.equals("")) ? 
                                password : (String)prop.get("Password"));

            }

            // Create a connection	  object
            connection = ods.getConnection();

            // Sets the auto-commit property for the connection to be false. 
            connection.setAutoCommit(false);

            if (this.debug)
                System.err.println(" Connected to (TNSEntry/Database/URL) (" + 
                                   ods.getTNSEntryName() + "/" + 
                                   ods.getDatabaseName() + "/" + ods.getURL() + 
                                   ") Database as " + ods.getUser());


        } catch (SQLException ex) { // Trap SQL errors
             throw new Exception("Error in Connecting to (TNSEntry/Database/URL) (" + 
                                ods.getTNSEntryName() + "/" + 
                                ods.getDatabaseName() + "/" + ods.getURL(), 
                                ex);
        } catch (IOException ex) { // Trap I/O errors 
            throw new Exception("Error in reading the properties file ", ex);
        }
    }

    /**
     * Retrieves all the tables present in the user's schema 
     */

    void showMetaData(OracleResultSetMetaData rsmd, 
                      PrintStream printStream
                      , char outputFormat 
                      , int level
                      ) throws java.sql.SQLException {

        int numberOfColumns = rsmd.getColumnCount();

        switch (outputFormat)
        {
        
            case 'V' : // Verbose
            
             printStream.print("\nColumnName:");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? '\n' : ','));
                 printStream.print(rsmd.getColumnName(i));
             }

             printStream.print("\nColumnType:");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? '\n' : ','));
                 printStream.print(rsmd.getColumnType(i));
             }

             printStream.print("\nColumnTypeName:");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? '\n' : ','));
                 printStream.print(rsmd.getColumnTypeName(i));
             }

             printStream.print("\nColumnClassName:");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? '\n' : ','));
                 printStream.print(rsmd.getColumnClassName(i));
             }

             printStream.print("\nDisplaySize:");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? '\n' : ','));
                 printStream.print(rsmd.getColumnDisplaySize(i));
             }

            break;
             
            case 'L' : // SQL*Loader
            
            
            if (level == 1) 
            {
               printStream.print("\n--SQL*Loader Description:");
               printStream.print("\nLOAD DATA\n");
              if (null != control && output.equals(control)) 
              {
                  printStream.print("\nINFILE *" );
              }
              else {
                  printStream.print("\nINFILE \"" + output + "\"" );
              }
               
              // print out Record Format 
              printStream.print( " \"" + loaderRecordFormat + " X'" + StringToHexString(recordSeparator) +  "'\" \n");
  
              printStream.print("\n-- Java File Encoding= " + System.getProperty("file.encoding") + "\n" );
  
              printStream.print("\n"+loaderMethod +  "\n");
              printStream.print("\nINTO TABLE " + tableName + "\n" );
              
              printStream.print("FIELDS");
              
              if (null != separator && !separator.equals("")) 
              {
                  printStream.print(" TERMINATED BY '" + 
                                    (separator.equals("'") 
                                      ? (separator + separator)
                                      : separator 
                                    )
                                    + "'" );
              }
  
              if (null != delimiter && !delimiter.equals("")) 
              {
                  printStream.print(" OPTIONALLY ENCLOSED BY '" + 
                                    (delimiter.equals("'") 
                                      ? (delimiter + delimiter)
                                      : delimiter 
                                    )
                                    + "'" );
              }
              
              printStream.print(" TRAILING NULLCOLS" );
              
  
               for (int i = 1; i < numberOfColumns + 1; i++) {
                   printStream.print((i == 1 ? "\n\n(" : "\n,"));
                   printStream.print(rsmd.getColumnName(i));
                   printStream.print(( " " ));
  
                   if (rsmd.getColumnTypeName(i).equals("DATE"))
                  {
  
                       printStream.print(rsmd.getColumnTypeName(i));
                       printStream.print(" \"" + oracleDateFormat + "\"" );
                  }
                  else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMPTZ")) 
                   {
                       printStream.print("TIMESTAMP WITH TIME ZONE");
                       printStream.print(" \"" + oracleTimestampTZFormat + "\"" );
                   }
                   else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMPLTZ")) 
                    {
                        printStream.print("TIMESTAMP WITH LOCAL TIME ZONE");
                        printStream.print(" \"" + oracleTimestampFormat + "\"" );
                    }
                   else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMP")) 
                    {
                        printStream.print(rsmd.getColumnTypeName(i));
                        printStream.print(" \"" + oracleTimestampFormat + "\"" );
                    }
                   else if (rsmd.getColumnClassName(i).equals("oracle.sql.STRUCT")) 
                    {
                      StructDescriptor structDescriptor = 
                          new StructDescriptor(rsmd.getColumnTypeName(i)
                                          ,connection
                                          );
                      printStream.print(" COLUMN OBJECT TREAT AS " + rsmd.getColumnTypeName(i) + "\n(" );
                      showMetaData((OracleResultSetMetaData) structDescriptor.getMetaData(), 
                                             printStream
                                            ,outputFormat 
                                            ,(level +1)
                                            );
                      printStream.print(")" );
                    }
                  else
                  {
                      //printStream.print(rsmd.getColumnTypeName(i));
                      printStream.print("CHAR");
                      printStream.print("(" + rsmd.getColumnDisplaySize(i) + ")" );
                  }
                   
               }
  
              printStream.print( "\n)");
              
              if (null != control && output.equals(control)) 
              {
                  printStream.print("\nBEGINDATA");
              }
            }
            else
            {
              
              for (int i = 1; i < numberOfColumns + 1; i++) {
                  printStream.print((i == 1 ? "\n " : "\n,"));
                  printStream.print(rsmd.getColumnName(i));
                  printStream.print(( " " ));
              
                  if (rsmd.getColumnTypeName(i).equals("DATE"))
                 {
              
                      printStream.print(rsmd.getColumnTypeName(i));
                      printStream.print(" \"" + oracleDateFormat + "\"" );
                 }
                 else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMPTZ")) 
                  {
                      printStream.print("TIMESTAMP WITH TIME ZONE");
                      printStream.print(" \"" + oracleTimestampTZFormat + "\"" );
                  }
                  else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMPLTZ")) 
                   {
                       printStream.print("TIMESTAMP WITH LOCAL TIME ZONE");
                       printStream.print(" \"" + oracleTimestampFormat + "\"" );
                   }
                  else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMP")) 
                   {
                       printStream.print(rsmd.getColumnTypeName(i));
                       printStream.print(" \"" + oracleTimestampFormat + "\"" );
                   }
                  else if (rsmd.getColumnClassName(i).equals("oracle.sql.STRUCT")) 
                   {
                       StructDescriptor structDescriptor = 
                           new StructDescriptor(rsmd.getColumnTypeName(i)
                                           ,connection
                                           );
                       
 
                       printStream.print(" COLUMN OBJECT TREAT AS " + rsmd.getColumnTypeName(i) +  "\n(" );
                       showMetaData((OracleResultSetMetaData) structDescriptor.getMetaData(), 
                                              printStream
                                             ,outputFormat 
                                             ,(level +1)
                                             );
                       printStream.print(")" );
                       
                   }
                 else
                 {
                     //printStream.print(rsmd.getColumnTypeName(i));
                     printStream.print("CHAR");
                     printStream.print("(" + rsmd.getColumnDisplaySize(i) + ")" );
                 }
                  
              }
              
              
            }
          
            break;

            case 'D' : // Data
            
             printStream.print("\nTableName:"+ tableName);
             printStream.print("\nColumnName");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                printStream.print((i == 1 ? ":" : separator ));
                printStream.print(rsmd.getColumnName(i));
             }

             printStream.print("\nColumnType");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                    printStream.print((i == 1 ? ":" : separator));
                    printStream.print(rsmd.getColumnType(i));
             }

             printStream.print("\nColumnTypeName");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? ":" : separator));
                 printStream.print(rsmd.getColumnTypeName(i));
             }

            printStream.print("\nColumnClassName");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? ":" : separator ));
                 printStream.print(rsmd.getColumnClassName(i));
             }

            printStream.print("\nDisplaySize");
             for (int i = 1; i < numberOfColumns + 1; i++) {
                 printStream.print((i == 1 ? ":" : separator ));
                 printStream.print(rsmd.getColumnDisplaySize(i));
             }
            break;
            
            case 'd' : // Data

             for (int i = 1; i < numberOfColumns + 1; i++) {
                printStream.print((i == 1 ? "" : separator ));

               String className = rsmd.getColumnClassName(i);
               
               if (rsmd.getColumnClassName(i).equals("oracle.sql.STRUCT")) 
                {
                    StructDescriptor structDescriptor = 
                        new StructDescriptor(rsmd.getColumnTypeName(i)
                                        ,connection
                                        );
                    
  
                    showMetaData((OracleResultSetMetaData) structDescriptor.getMetaData(), 
                                           printStream
                                          ,outputFormat 
                                          ,(level +1)
                                          );
                    
                }
                else
                {
               
                 if (null != delimiter) printStream.print(delimiter);
                  printStream.print(rsmd.getColumnName(i).replaceAll(delimiter,delimiter+delimiter));
                 if (null != delimiter) printStream.print(delimiter);
                }
             }
             
             if (1 == level)  printStream.print(recordSeparator);

            break;
             
            default:
            System.err.println("Invalid metadata output format (" + outputFormat + ")");
        }
        
        if (1 == level && null != control && output.equals(control))
        {
            if (outputFormat != 'D' && outputFormat != 'd') printStream.println("");
        }
 
    }

    /**
     * This method creates the SQL query to be executed based on the selections
     * by the user, and displays the results in the Results JTable
     */
    private void selectRecords() throws Exception {


        InputStream inputStream;
        PrintStream outputStream;
        PrintStream controlStream;
        StringBuffer query;
        //String twoDelimiters = "";
        //SimpleDateFormat dateSimpleDateFormat;
        //SimpleDateFormat timestampSimpleDateFormat;
        //SimpleDateFormat timestampTZSimpleDateFormat;


        if (recordSetSource.equalsIgnoreCase("SQL")) {
            if (null == tableName || tableName.equals("")) {
                byte[] inputBuffer = new byte[1024];
                query = new StringBuffer(1024);
    
                try {
                    if (input.equals("-")) {
                        if (debug)
                            errorStream.println("Building query from Standard Input");
                        inputStream = System.in;
                    } else {
                        if (debug)
                            errorStream.println("Building query from \"" + input + 
                                                "\"");
                        inputStream = new FileInputStream(input);
                    }
    
                    int bytesRead;
                    while (inputStream.available() > 0) {
                        bytesRead = inputStream.read(inputBuffer);
                        query.append(new String(inputBuffer, 0, bytesRead));
                    }
                    if (debug)
                        errorStream.println("Query derived from inputStream: \"" + 
                                            query + "\"");
                } catch (IOException io) {
                    errorStream.println("Failed to read input \"" + input + 
                                        "\": " + io.getMessage());
    
                }
                //Look in standard input  
            } else { // Base query on table Name 
                // Get the concatenated string of column names for the columns
                // to be retrieved
                // Form the dynamic Query
                if (debug)
                    errorStream.println("Building query from instance variables");
    
                query = new StringBuffer("SELECT "); // Start the SQL statement
                query.append(columnNames + 
                             " FROM "); // Add columns to be retrieved 
                query.append(tableName); // Add table to retrieve
    
                // Add WHERE clause if any
                if (!whereClause.equals(""))
                    //query.append(" WHERE " + whereClause);
                     query.append("\n" + whereClause);
    
                if (debug)
                    errorStream.println("Query derived from tableName,ColumnNames,whereClause: \"" + 
                                        query + "\"");
    
            }
        }
        else if (recordSetSource.equalsIgnoreCase("PLSQL")) {
                if (null == functionName || functionName.equals("")) {
                    byte[] inputBuffer = new byte[1024];
                    query = new StringBuffer(1024);
            
                    try {
                        if (input.equals("-")) {
                            if (debug)
                                errorStream.println("Building assignment from Standard Input");
                            inputStream = System.in;
                        } else {
                            if (debug)
                                errorStream.println("Building assignment from \"" + input + 
                                                    "\"");
                            inputStream = new FileInputStream(input);
                        }
            
                        int bytesRead;
                        while (inputStream.available() > 0) {
                            bytesRead = inputStream.read(inputBuffer);
                            query.append(new String(inputBuffer, 0, bytesRead));
                        }
                        if (debug)
                            errorStream.println("Assignment derived from inputStream: \"" + 
                                                query + "\"");
                    } catch (IOException io) {
                        errorStream.println("Failed to read input \"" + input + 
                                            "\": " + io.getMessage());
            
                    }
                    //Look in standard input  
                } else { // Base query on table Name 
                    // Get the concatenated string of column names for the columns
                    // to be retrieved
                    // Form the dynamic Query
                    if (debug)
                        errorStream.println("Building Assignment from instance variables");
            
                    query = new StringBuffer("BEGIN "); // Start the PLSQL block
                    query.append(" ? := " + functionName + ";  END; "); // Add functions to be executed 
            
                    if (debug)
                        errorStream.println("Assignment derived from functionName: \"" + 
                                            query + "\"");
            
                }
            }
        else {
            throw new Exception("ResourceSetSource must be one of SQL or PLSQL: please use default or use the \"f\" option.");
        }
            

        //try {
        if (dateFormat == null) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        dateSimpleDateFormat = new SimpleDateFormat(dateFormat);
        timestampSimpleDateFormat = new SimpleDateFormat(timestampFormat);
        timestampTZSimpleDateFormat = new SimpleDateFormat(timestampTZFormat);
        /*try  } catch( Exception ex ) {
            errorStream.println("Exception thrown from format " +
                                         "method of Utilities class of given "+
                                             "status : " + ex.getMessage()

                              );
            throw ex;
           }
        */


        ZipEntry outputZipEntry = null; 
        
        //Statement stmt = null;
        OracleStatement stmt = null;
        try {
            if (debug)
                errorStream.println("Selecting Records ...");

            // Create a SQL statement context to execute the Query 
            stmt = (OracleStatement)connection.createStatement();
            stmt.setFetchSize(prefetchValue);
            stmt.execute("ALTER SESSION SET NLS_DATE_FORMAT='" + oracleDateFormat + "'");
            stmt.execute("ALTER SESSION SET NLS_TIMESTAMP_FORMAT='" + oracleTimestampFormat + "'");
            stmt.execute("ALTER SESSION SET NLS_TIMESTAMP_TZ_FORMAT='" + oracleTimestampTZFormat + "'");
            //stmt.execute("ALTER SESSION SET TIME_ZONE='" + connection.g+ "'");
             
            ((OracleConnection)connection).setSessionTimeZone(timeZone.getID()); ; // Required for TIMESTAMP WITH LOCAL TIME ZONE 

            // Execute the formed query and obtain the ResultSet
            OracleResultSet resultSet;

            if (recordSetSource.equals("SQL")) 
            {
		if (debug)
		   errorStream.println("Using SQL RecordSet");
                 resultSet = 
                    (OracleResultSet)stmt.executeQuery(new String(query));
                
            }
            else 
            if (recordSetSource.equals("PLSQL")) 
            {
		if (debug)
		   errorStream.println("Using PLSQL RecordSet");
                /*
                 * Assume a callable statement with 
                 * no input parameters 
                 * and 
                 * 1 output parameter comprising a resultset
                 */
                //prepareCall () chokes on carriage returns (or at least a terminating Carriage Return/Line Feed
                //removing the Carrriage Returns works around this problem.
                OracleCallableStatement callableStatement =(OracleCallableStatement) connection.prepareCall(query.toString().replaceAll("\r",""));

                // Bind the result set 
                callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
                // Execute the result set 
                callableStatement.execute();
                //Assign the output parameter to the extract result set
                resultSet = (OracleResultSet)callableStatement.getObject(1);
            }
            else {
                throw new Exception("ResourceSetSource must be one of SQL or PLSQl: please use default or use the \"f\" option.");
            }
            


            OracleResultSetMetaData rsmd = 
                (OracleResultSetMetaData)resultSet.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();

            if (debug) showMetaData(rsmd, errorStream, 'V', 1 );
            

            try {
                if (output.equals("-")) {
                  if (zipOutput)
                  {
                    zipOutputStream = new ZipOutputStream(System.out);
                    outputStream = new PrintStream(zipOutputStream );}
                  else
                  {
                    outputStream = System.out;
                  }
                } else {
                  if (zipOutput)
                  {
                    zipOutputStream = new ZipOutputStream(new FileOutputStream(output+".zip"));
                    outputStream = new PrintStream(zipOutputStream );}
                  else
                  {
                    outputStream = new PrintStream(new FileOutputStream(output));
                  }                                           
                }

                /*
                 * Output any control file data specified
                 */
                if (null != control && null != controlFormat) 
                {
                  
                     ZipEntry controlZipEntry = null; 

                     if (control.equals(output)) 
                     {
                        controlStream =  outputStream;
                       if (null!= zipOutputStream)
                       {
                         controlZipEntry = new ZipEntry(!control.equals("-")
                                                                     ? control
                                                                     : (tableName.equals("") 
                                                                       ? "data" 
                                                                        : tableName  
                                                                       )+".metadata"                                                                                               
                                                                  );
                         zipOutputStream.putNextEntry(controlZipEntry);
                       }
                                           }
                     else if (control.equals(error)) 
                    {
                       controlStream =  errorStream;
                    }
                    else if (control.equals("-")) 
                    {
                     controlStream = System.out;
                    }
                    else
                    {
                       controlStream = new PrintStream(new FileOutputStream(control));
                    }
                    
                  
                    showMetaData(rsmd, controlStream, controlFormat.charAt(0), 1);
                    
                    if (null != controlZipEntry) 
                    {
                      zipOutputStream.closeEntry(); 
                    }
                }

            
                if (null!= zipOutputStream)
                {
                   outputZipEntry = new ZipEntry(!output.equals("-")
                                                              ? output
                                                              : (tableName.equals("") 
                                                                ? "data" 
                                                                 : tableName  
                                                                )+".dat"
                                                );
                                                            
                   zipOutputStream.putNextEntry(outputZipEntry);
                }          

                if (null != header && !header.equals("") )
                {
                    outputStream.print(header);
                    outputStream.print(recordSeparator);
                }
                /* Defaults */
                else if ( (null == control || control.equals("") ) /* Control undefined */
                         && controlFormat.toUpperCase().equals("DATA") /* Data format */
                        )
                {

                  showMetaData(rsmd
                               ,outputStream
                               ,controlFormat.charAt(0) 
                               ,1
                               );
                  
                    for (int i = 1; i < numberOfColumns + 1; i++) {

                        //outputStream.print((i == 1 ? recordSeparator : separator ));
                        outputStream.print((i == 1 ? "" : separator));
                        //Column names are not delimited, otherwise DARTmail fails to load the file 
                        outputStream.print(rsmd.getColumnLabel(i));

                      
                    }
                    outputStream.print(recordSeparator);
                }

                String[] columnValues = new String[numberOfColumns];

                //Used to escape delimiters in data 
                if (null != delimiter) {
                    twoDelimiters = delimiter + delimiter;
                }

                 //outputResultSet(ResultSet, outputStream, errorStream); 
                // Populate from the resultset 
                long rowCount = 0L;
                while (resultSet.next()) { // Point result set to next row
                    // Retrieve column values for this row
                    for (int i = 1; i < numberOfColumns + 1; i++) {

                        outputStream.print((i == 1 ? (rowCount>0 ? recordSeparator : "" ) : 
                                            separator));
                           
                        String columnType =  rsmd.getColumnTypeName(i);
                                    
                        
                        if (columnType.equals("REFCURSOR")) 
                        {
                          outputResultSet((OracleResultSet)resultSet.getCursor(i),outputStream);
                        }
                        else
                        {
                          outputDatum(resultSet.getOracleObject(i),outputStream);
                        }
                        
                    }

                    //Increment rowCount
                    rowCount ++;
                }
                outputStream.print(recordSeparator);

                //Output Trailer if defined, replacing rowCountplaceholder if necessary
                if (null != trailer && !  trailer.equals("")) {
                    outputStream.print(trailer.replaceAll("@rowCount", new Long(rowCount).toString()  ));
                    outputStream.print(recordSeparator);
                }
                
                outputStream.flush();
            } catch (IOException io) {
                errorStream.println("Failed to write output \"" + output + 
                                    "\": " + io.getMessage());
                throw new Exception("Failed to write output \"" + output + 
                                    "\": " + io.getMessage(), io);
            }


            if (debug)
                errorStream.println("Selection Complete");

          if (null != outputZipEntry) 
          {
            zipOutputStream.closeEntry(); 
          }
        } catch (SQLException ex) { // Trap SQL errors
            throw new Exception("Error in querying the database " + '\n' + 
                                ex.toString() + "\n SQL follows:\n\"" + query + 
                                "\"", ex);
        } finally {
            try {
                stmt.close(); // Close statement which also closes open result sets 
            } catch (SQLException ex) {
                errorStream.println("Ignoring this error whilst closing the statement: " + '\n' + 
                                ex.toString() + "\n");
            }
        }
    }
    

  /**
   * 
   */
  private void outputDatum(oracle.sql.Datum datum,
                               PrintStream  outputStream 
                               ) throws SQLException
  {
    String columnValue = "";
    
    if (null != datum)
    {
      String datumType;
      datumType = datum.getClass().getCanonicalName();
      //if (debug) System.err.println("Datum class is " + datumType); 
      if (datumType.equals("oracle.sql.DATE") ) {
  
          Timestamp dateTime = datum.timestampValue();
          
          columnValue = 
                  (null != dateTime) ? dateSimpleDateFormat.format(dateTime) : 
                  "";
                  
          //DATE oracleDate = new DATE(dateTime);
          //columnValue = oracleDate.toText(oracleDateFormat,"'UNITED KINGDOM.ENGLISH");
          
      } else if (datumType.equals("oracle.sql.TIMESTAMPTZ")) {
  
          Timestamp dateTime = datum.timestampValue();
          columnValue = 
                  (null != dateTime) ? timestampTZSimpleDateFormat.format(dateTime) : 
                  "";
         
          //TIMESTAMPTZ oracleTimestamp = resultSet.getTIMESTAMPTZ(i);
          //columnValue = oracleTimestamp.stringValue(connection);
          
          //columnValue = resultSet.getString(i); // .getTIMESTAMPTZ(i);
          //columnValue = oracleTimestamp.toText(oracleTimestampFormat,"'UNITED KINGDOM.ENGLISH");
          
           //columnValue = TIMESTAMPTZ.toString(connection,resultSet.getTIMESTAMPTZ(i).toBytes());
           
            } else if (datumType.equals("oracle.sql.TIMESTAMP")) {
  
                Timestamp dateTime = datum.timestampValue();
                columnValue = 
                        (null != dateTime) ? timestampSimpleDateFormat.format(dateTime) : 
                        "";
  
      } 
      else if (datumType.equals("oracle.sql.STRUCT"))
      {
        oracle.sql.STRUCT oracleSTRUCT = (oracle.sql.STRUCT) datum;
        oracle.sql.Datum[] attrs = oracleSTRUCT.getOracleAttributes();
        if (debug)
        {
          System.err.println("Oracle TYPE.." + oracleSTRUCT.getSQLTypeName());
          System.err.println("STRUCT DUMP " + oracleSTRUCT.dump());
          System.err.println("Number of attributes.." + attrs.length);
        }
        for (int j = 0; j < attrs.length ; j++)
        {
           
          //if (debug) System.err.println("Attribute class is " + attrs[j].getClass().getCanonicalName());
          if (j>0) outputStream.print(separator);
          outputDatum(attrs[j],outputStream);
        }
        return;
      }
      else if (datumType.equals("OracleResultSet"))
      {
          outputResultSet((OracleResultSet) datum,outputStream);
          return;
      }
      else {
          columnValue = datum.stringValue();
      }
    }

    /*
         * This fails
        columnValue =  resultSet.getDATE(i).toText("YYYYMMDD",null) ;

         java.sql.SQLException: Invalid column type: getDATE not implemented for class oracle.jdbc.driver.T4CNumberAccessor
    */

    //Output delimited column if necessary
    if (null != delimiter) {
        //Output start delimiter 
        outputStream.print(delimiter);
        if (null != columnValue) {
            outputStream.print(columnValue.replaceAll(delimiter, 
                                                      twoDelimiters));
        }
        //Output end delimiter 
        outputStream.print(delimiter);
    } else if (null != columnValue) {
        outputStream.print(columnValue);
    }
  }
   
    /**
     * 
     */
    private void outputResultSet(OracleResultSet resultSet,
                                 PrintStream  outputStream 
                                 ) throws SQLException
    {
        

      OracleResultSetMetaData rsmd = 
          (OracleResultSetMetaData)resultSet.getMetaData();
      int numberOfColumns = rsmd.getColumnCount();

        // Populate from the resultset
        long rowCount = 0L;
        while (resultSet.next()) { // Point result set to next row
           // Retrieve column values for this row
           for (int i = 1; i < numberOfColumns + 1; i++) {

             if (i > 1 ) outputStream.print( separator );
             String columnValue = "";
               if (rsmd.getColumnTypeName(i).equals("DATE") ) {

                   Timestamp dateTime = resultSet.getTimestamp(i);
                   
                   columnValue = 
                           (null != dateTime) ? dateSimpleDateFormat.format(dateTime) : 
                           "";
                           
                   //DATE oracleDate = new DATE(dateTime);
                   //columnValue = oracleDate.toText(oracleDateFormat,"'UNITED KINGDOM.ENGLISH");
                   
               } else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMPTZ")) {

                   Timestamp dateTime = resultSet.getTimestamp(i);
                   columnValue = 
                           (null != dateTime) ? timestampTZSimpleDateFormat.format(dateTime) : 
                           "";
                    
                     } else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMP")) {

                         Timestamp dateTime = resultSet.getTimestamp(i);
                         columnValue = 
                                 (null != dateTime) ? timestampSimpleDateFormat.format(dateTime) : 
                                 "";

               } 
               else if (rsmd.getColumnClassName(i).equals("oracle.sql.STRUCT"))
               {
                 //outputSTRUCT(STRUCT) 
                 oracle.sql.STRUCT oracleSTRUCT=((OracleResultSet)resultSet).getSTRUCT(i);
                 oracle.sql.Datum[] attrs = oracleSTRUCT.getOracleAttributes();
                 if (debug)
                 {
                   System.err.println("Oracle TYPE.." + oracleSTRUCT.getSQLTypeName());
                   System.err.println("STRUCT DUMP " + oracleSTRUCT.dump());
                   System.err.println("Number of attributes.." + attrs.length);
                   for (int j = 0; j < attrs.length ; j++)
                   {
                      
                     System.err.println("Attribute class is " + attrs[j].getClass().getCanonicalName());
                   }
                 }
                 
               }
               else {
                   columnValue = resultSet.getString(i);
               }
               /*
                    * This fails
                   columnValue =  resultSet.getDATE(i).toText("YYYYMMDD",null) ;

                    java.sql.SQLException: Invalid column type: getDATE not implemented for class oracle.jdbc.driver.T4CNumberAccessor
                   */

               //Output delimited column if necessary
               if (null != delimiter) {
                   //Output start delimiter 
                   outputStream.print(delimiter);
                   if (null != columnValue) {
                       outputStream.print(columnValue.replaceAll(delimiter, 
                                                                 twoDelimiters));
                   }
                   //Output end delimiter 
                   outputStream.print(delimiter);
               } else if (null != columnValue) {
                   outputStream.print(columnValue);
               }

               //Replace any delimiters if necessary

           }
           //Increment rowCount
           rowCount ++;
        }
//        outputStream.print(recordSeparator);
           
    }


    /**
     *	  Closes the connection and exits from the program when exit button is
     *	  pressed
     */
    private void exitApplication() {
        if (connection != null) {
            try {
                connection.close(); // Close the connection while exiting
            } catch (Exception ex) {
                errorStream.println(" Error in closing the connection: ");
            }
        }
        System.exit(0);
    }

    //Regular Expression solution
    //private static final Pattern p = Pattern.compile("\\\\u([0-9A-F]{4})");

    /**
     * Pattern representing a Unicode escape pattern
     */
    private static final Pattern unicode_escaped_character = 
        Pattern.compile("\\\\[uU]([0-9A-Fa-f]{4})");

    /**
     * Halfheartedly convert some escape patterns in this string to their character equivalents
     * @param escapedString - string potentialy containing escape patterns (e.g. "\n")
     * @return converted string
     */
    private static String unescape(String escapedString) {

        //J2SE 1.5 return String.format(escapedString);
        //J2SE 1.4 Poor man's unescape -- Use replaceAll 
        //As the parameters to replaceAll are regular expressions, we end up with multiple levels of escaping
        //e.g. requiring a Record Separator of a single '\'
        // '\' 
        //     -> "\\" to get slash character in String 
        //             -> "\\\\" to represent '\' as an escaped regular expression

        //return escapedString.replaceAll("\\\\n","\n").replaceAll("\\\\r","\r").replaceAll("\\\\t","\t").replaceAll("\\\\f","\f").replaceAll("\\\\\"","\"").replaceAll("\\\\'","\'").replaceAll("\\\\b","\b").replaceAll("\\\\\\\\","\\\\") ;

        String res = 
            escapedString.replaceAll("\\\\n", "\n").replaceAll("\\\\r", 
                                                               "\r").replaceAll("\\\\t", 
                                                                                "\t").replaceAll("\\\\f", 
                                                                                                 "\f").replaceAll("\\\\\"", 
                                                                                                                  "\"").replaceAll("\\\\'", 
                                                                                                                                   "\'").replaceAll("\\\\b", 
                                                                                                                                                    "\b").replaceAll("\\\\\\\\", 
                                                                                                                                                                     "\\\\");

        //System.err.println("Unescape Before: \"" + res + "\"" );
        Matcher m = unicode_escaped_character.matcher(res);
        while (m.find()) {
            //System.err.println("Unescape matched: (G0,G1)=(" +m.group(0) + "," + m.group(1) + ") \"" + res + "\"" );
            //int unicode_codepoint = Integer.parseInt(m.group( 1), 16);
            //System.err.println("\n... character code point is " + unicode_codepoint );
            //char unicode_character = (char) unicode_codepoint;
            //System.err.println("\n... character is \"" + unicode_character +"\" with value " 
            //                      + Character.getNumericValue(unicode_character) );
            //Character.toString(unicode_character)
                res = 
                    res.replaceAll("\\" + m.group(0), Character.toString((char)Integer.parseInt(m.group(1), 
                                                                                                16)));
            //System.err.println("Unescape after replaceAll: (G0,G1)=(" +m.group(0) + "," + m.group(1) + ") \"" + res + "\"" );
        }
        //System.err.println("Unescape After: \"" + res + "\"" );

        return res;
    }


    /**
     * Set Oracle hostname /IP address
     * 
     * @param hostName - hostname /IP address
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * 
     * @return Oracle hostname /IP address
     */
    public String getHostName() {
        return hostName;
    }

    public void setServiceName(String sID) {
        this.ServiceName = sID;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getPortName() {
        return portName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getSeparator() {
        return separator;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public void setZipOutput(boolean zipOutput) {
        this.zipOutput = zipOutput;
    }

    public boolean isZipOutput() {
        return zipOutput;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public String getDriverType() {
        return driverType;
    }

    public void setRecordSeparator(String recordSeparator) {

        //this.recordSeparator = unescape(recordSeparator);
        this.recordSeparator = recordSeparator;

    }

    public String getRecordSeparator() {
        return recordSeparator;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    public PrintStream getErrorStream() {
        return errorStream;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setPrefetchValue(int prefetchValue) {
        this.prefetchValue = prefetchValue;
    }

    public int getPrefetchValue() {
        return prefetchValue;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getControl() {
        return control;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setOracleDateFormat(String oracleDateFormat) {
        this.oracleDateFormat = oracleDateFormat;
    }

    public String getOracleDateFormat() {
        return oracleDateFormat;
    }

    public void setOracleTimestampFormat(String oracleTimestampFormat) {
        this.oracleTimestampFormat = oracleTimestampFormat;
    }

    public String getOracleTimestampFormat() {
        return oracleTimestampFormat;
    }

    public void setControlFormat(String controlFormat) {
        this.controlFormat = controlFormat;
    }

    public String getControlFormat() {
        return controlFormat;
    }

    public void setOracleTimestampTZFormat(String oracleTimestampTZFormat) {
        this.oracleTimestampTZFormat = oracleTimestampTZFormat;
    }

    public String getOracleTimestampTZFormat() {
        return oracleTimestampTZFormat;
    }

    public void setTimestampTZFormat(String timestampTZFormat) {
        this.timestampTZFormat = timestampTZFormat;
    }

    public String getTimestampTZFormat() {
        return timestampTZFormat;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setRecordSetSource(String param) {
        this.recordSetSource = param;
    }

    public String getRecordSetSource() {
        return recordSetSource;
    }

    public void setFunctionName(String param) {
        this.functionName = param;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setLoaderMethod(String param) {
        this.loaderMethod = param;
    }

    public String getLoaderMethod() {
        return loaderMethod;
    }

  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }

  public Connection getConnection()
  {
    return connection;
  }

  public void setLoaderRecordFormat(String loaderRecordFormat)
  {
    this.loaderRecordFormat = loaderRecordFormat;
  }

  public String getLoaderRecordFormat()
  {
    return loaderRecordFormat;
  }

  public void setConfigurationFileName(String configurationFileName)
  {
    this.configurationFileName = configurationFileName;
  }

  public String getConfigurationFileName()
  {
    return configurationFileName;
  }
}


