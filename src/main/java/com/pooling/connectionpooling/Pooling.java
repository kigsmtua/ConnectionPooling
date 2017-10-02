/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pooling.connectionpooling;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *
 * @author john kiagu
 * @TODO make this more generic one
 */
public class Pooling {
    /**
     * The MySQL connection pool name.
     */
    private final transient String poolName;

    /**
     * Constructor.
     *
     * @param host the MySQL host machine
     * @param port the port to use on the MySQL host machine
     * @param database the MySQL database name
     * @param user the MySQL user
     * @param password the MySQL password
     * @param dbPoolName the data pool name
     * @param maxConnections the maximum number of MySQL connections
     *
     * @throws ClassNotFoundException if the MySQL driver cannot be found
     * @throws InstantiationException if the MySQL driver cannot initialized
     * @throws IllegalAccessException if there are insufficient permissions to
     * access the MySQL driver
     * @throws SQLException if the MySQL connection pool cannot be set up
     */
    public Pooling(final String host, final String port, final String database,
            final String user, final String password, final String dbPoolName,
            final int maxConnections)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SQLException {
        //@TODO make this generic
      
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        String connection = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?user=" + user + "&password=" + password
                + "&autoReconnect=true&characterEncoding=UTF-8";

        this.poolName = dbPoolName;

        setupDriver(connection, maxConnections);
    }
    /**
     * Sets up the connection pool driver.
     *
     * @param connectURI the connection string
     * @param maxConnections the maximum number of MySQL connections
     *
     * @throws ClassNotFoundException on error
     * @throws SQLException on error
     */
    private void setupDriver(final String connectURI, final int maxConnections)
            throws ClassNotFoundException, SQLException {
        /*
         * First, we'll need an ObjectPool that serves as the actual pool of
         * connections.
         *
         * We'll use a GenericObjectPool instance, although any ObjectPool
         * implementation will suffice.
         *
         * Parameters here are:
         *
         * factory - the (possibly null) PoolableObjectFactory to use to create,
         * validate and destroy objects maxActive - the maximum number of
         * objects that can be borrowed at one time (see setMaxActive(int))
         * whenExhaustedAction - the action to take when the pool is exhausted
         * maxWait - the maximum amount of time to wait (in ms) for an idle
         * object when the pool is exhausted and whenExhaustedAction is
         * WHEN_EXHAUSTED_BLOCK (otherwise ignored) testOnBorrow - whether or
         * not to validate objects before they are returned by the
         * borrowObject() method testOnReturn - whether or not to validate
         * objects after they are returned to the returnObject(java.lang.Object)
         * method
         */
        ObjectPool<PoolableConnectionFactory> connectionPool = 
                new GenericObjectPool<>(null,
                maxConnections, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 30000,
                true, true);

        /*
         * Next, we'll create a ConnectionFactory that the pool will use to
         * create Connections. We'll use the DriverManagerConnectionFactory,
         * using the connect string passed in.
         */
        ConnectionFactory connectionFactory = 
                new DriverManagerConnectionFactory(connectURI, null);

        /*
         * Now we'll create the PoolableConnectionFactory, which wraps the
         * "real" Connections created by the ConnectionFactory with the classes
         * that implement the pooling functionality.
         *
         * The parameters used here are:
         *
         * connFactory - the ConnectionFactory from which to obtain base
         * Connections pool - the ObjectPool in which to pool those Connections
         * stmtPoolFactory - the KeyedObjectPoolFactory to use to create
         * KeyedObjectPools for pooling PreparedStatements, or null to disable
         * PreparedStatement pooling validationQuery - a query to use to
         * validate Connections. Should return at least one row. Using null
         * turns off validation validationQueryTimeout - the number of seconds
         * that validation queries will wait for database response before
         * failing. Use a value less than or equal to 0 for no timeout
         * connectionInitSqls - a Collection of SQL statements to initialize
         * Connections. Using null turns off initialization defaultReadOnly -
         * the default "read only" setting for borrowed Connections
         * defaultAutoCommit - the default "auto commit" setting for returned
         * Connections
         */
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
                connectionPool, null, "SELECT 1", 5, null, false, true);

        // Finally, we create the PoolingDriver itself...
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

        // ...and register our pool with it
        driver.registerPool(poolName, connectionPool);

        /*
         * Now we can just use the connect string
         * "jdbc:apache:commons:dbcp:poolName" to access our pool of
         * Connections.
         */
    }

    /**
     * Shuts down the connection pool.
     *
     * @throws SQLException on error
     */
    public void shutdownDriver() throws SQLException {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.closePool(poolName);
    }

    /**
     * <p> Gets a  database connection. Will throw an SQLException if there
     * is an error. The connection uses UTF-8 character encoding. </p>
     *
     * <p> The connection is obtained using the connect string:
     * jdbc:apache:commons:dbcp:poolName </p>
     *
     * @return a MySQL connection object, null on error
     *
     * @throws SQLException if unable to get a connection from the connection
     * pool
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + poolName);

        return conn;
    }

}
