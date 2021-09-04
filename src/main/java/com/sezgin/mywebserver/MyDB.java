package com.sezgin.mywebserver;

import com.sezgin.mywebserver.namespace.MessageIDList;
import com.sezgin.mywebserver.namespace.MessageObject;
import com.sezgin.mywebserver.namespace.UserIDList;
import com.sezgin.mywebserver.namespace.UserObject;
import org.springframework.util.Assert;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.sql.*;
import java.util.List;

public class MyDB {

    private final String URL = "jdbc:postgresql://localhost:5432/mydb";
    private final String USER = "postgres";
    private final String PASSWORD = "safak";
    private Connection con = null;

    // database
    public void connectDB() throws SQLException {

        con = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connected to database.");

        try {
            Statement st = con.createStatement();
            st.executeQuery("SELECT * FROM Users LIMIT 1;");
            st.executeQuery("SELECT * FROM Messages LIMIT 1;");
        } catch (SQLException e) {

            Statement st = con.createStatement();
            String query = "CREATE TABLE Users (" +
                    "userid serial NOT NULL, " +
                    "admin boolean NOT NULL, " +
                    "username varchar(32) NOT NULL, " +
                    "password varchar(8) NOT NULL, " +
                    "email varchar(64), " +
                    "firstname varchar(64), " +
                    "lastname varchar(64), " +
                    "gender varchar(6), " +
                    "birth date," +
                    "inbox integer[] DEFAULT '{}', " +
                    "sent integer[] DEFAULT '{}'" +
                    ");";
            st.executeUpdate(query);
            query = "ALTER TABLE Users ADD CONSTRAINT primary_userid PRIMARY KEY (userid);";
            st.executeUpdate(query);
            query = "ALTER TABLE Users ADD CONSTRAINT unique_username UNIQUE (username);";
            st.executeUpdate(query);
            query = "ALTER TABLE Users ADD CONSTRAINT unique_email UNIQUE (email);";
            st.executeUpdate(query);
            query = "INSERT INTO Users VALUES (DEFAULT, TRUE, 'superuser', '0000', " +
                    "'superuser@sezgin.com', 'Super', 'User', '', '0001-01-01', DEFAULT, DEFAULT);";
            st.executeUpdate(query);
            query = "CREATE TABLE Messages (" +
                    "msgid serial NOT NULL, " +
                    "\"when\" timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP(0), " +
                    "\"from\" integer NOT NULL, " +
                    "\"to\" integer NOT NULL, " +
                    "subject text, " +
                    "body text" +
                    ");";
            st.executeUpdate(query);
            query = "ALTER TABLE Messages ADD CONSTRAINT primary_msgid PRIMARY KEY (msgid);";
            st.executeUpdate(query);
            st.close();
            System.out.println("Users and Messages tables do not exist. Created new ones.");
            // e.printStackTrace();
        }
    }

    public void closeDB() throws SQLException {
        if (con != null)
            con.close();
    }

    // utility
    private XMLGregorianCalendar convertDate(java.sql.Date sqlDate) {

        XMLGregorianCalendar xmlDate = null;
        if (sqlDate != null) {
            try {
                xmlDate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(sqlDate.toLocalDate().toString());
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        return xmlDate;
    }

    private XMLGregorianCalendar convertTimestamp(java.sql.Timestamp sqlTimestamp) {

        XMLGregorianCalendar xmlTimestamp = null;
        if (sqlTimestamp != null) {
            try {
                xmlTimestamp = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(sqlTimestamp.toLocalDateTime().toString());
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        return xmlTimestamp;
    }

    private MessageIDList convertInboxSent(Array sqlArray) {

        MessageIDList messageIDList = new MessageIDList();
        try {
            List<BigInteger> list = messageIDList.getMsgid();
            ResultSet rs = sqlArray.getResultSet();
            while (rs.next()) {
                list.add(BigInteger.valueOf(rs.getInt(2)));
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return messageIDList;
    }

    // user
    public UserIDList selectUserIDs(int limit) {

        try {
            String query = "SELECT userid FROM Users ORDER BY userid" + ((limit < 0) ? "" : " LIMIT " + limit) + ";";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            UserIDList userids = new UserIDList();
            List<BigInteger> list = userids.getUserid();
            while (rs.next()) {
                list.add(BigInteger.valueOf(rs.getInt("userid")));
            }
            return userids;
        } catch (Exception e) {
            Assert.notNull(null, "Exception in selectUserIDs.");
            return null;
        }
    }

    public BigInteger searchUser(String username) {

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT userid FROM Users WHERE username='" + username.trim() + "';");
            if (rs.next()) {
                return BigInteger.valueOf(rs.getInt("userid"));
            } else {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in searchUser.");
            return null;
        }
    }

    public String searchUser(BigInteger userid) {

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT username FROM Users WHERE userid='" + userid + "';");
            if (rs.next()) {
                return rs.getString("username");
            } else {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in searchUser.");
            return null;
        }
    }

    public UserObject selectUser(BigInteger userid) {

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Users WHERE userid=" + userid + ";");
            if (rs.next()) {
                UserObject user = new UserObject();
                user.setUserid(BigInteger.valueOf(rs.getInt("userid")));
                user.setAdmin(rs.getBoolean("admin"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setGender(rs.getString("gender"));
                user.setBirth(this.convertDate(rs.getDate("birth")));
                user.setInbox(this.convertInboxSent(rs.getArray("inbox")));
                user.setSent(this.convertInboxSent(rs.getArray("sent")));
                return user;
            } else {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in selectUser.");
            return null;
        }
    }

    public BigInteger insertUser(UserObject user) {

        // TODO: use NULL or DEFAULT for empty values
        // TODO: handle null exception when date is invalid
        String values = "DEFAULT, ";
        values += "'" + user.isAdmin()             + "', ";
        values += "'" + user.getUsername().trim()  + "', ";
        values += "'" + user.getPassword()         + "', ";
        values += "'" + user.getEmail().trim()     + "', ";
        values += "'" + user.getFirstname().trim() + "', ";
        values += "'" + user.getLastname().trim()  + "', ";
        values += "'" + user.getGender().trim()    + "', ";
        values += "'" + user.getBirth().toString() + "', ";
        values += "DEFAULT, DEFAULT";

        String query = "INSERT INTO Users VALUES (" + values  + ") RETURNING userid;";
        try {
            Statement st = con.createStatement();
            try {
                ResultSet rs = st.executeQuery(query);
                if (rs.next()) {
                    return BigInteger.valueOf(rs.getInt("userid"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in insertUser.");
            return null;
        }
    }

    public BigInteger updateUser(UserObject user, UserObject newUser) {
        // TODO: improve this
        String columns = "";
        if (newUser.isAdmin() != user.isAdmin())
            columns += "admin='" + newUser.isAdmin() + "'|";
        if (newUser.getUsername() != null && !newUser.getUsername().isEmpty())
            columns += "username='" + newUser.getUsername() + "'|";
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty())
            columns += "password='" + newUser.getPassword() + "'|";
        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty())
            columns += "email='" + newUser.getEmail() + "'|";
        if (newUser.getFirstname() != null && !newUser.getFirstname().isEmpty())
            columns += "firstname='" + newUser.getFirstname() + "'|";
        if (newUser.getLastname() != null && !newUser.getLastname().isEmpty())
            columns += "lastname='" + newUser.getLastname() + "'|";
        if (newUser.getGender() != null && !newUser.getGender().isEmpty())
            columns += "gender='" + newUser.getGender() + "'|";
        if (newUser.getBirth() != null)
            columns += "birth='" + newUser.getBirth() + "'|";

        String query = "UPDATE Users SET " + String.join(", ", columns.split("\\|")) +
                " WHERE userid=" + user.getUserid() + ";";
        try {
            Statement st = con.createStatement();
            try {
                st.executeUpdate(query);
                // TODO: improve return
                return user.getUserid();
            } catch (SQLException e) {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in updateUser.");
            return null;
        }
    }

    public BigInteger deleteUser(UserObject user) {

        try {
            Statement st = con.createStatement();
            try {
                st.executeUpdate("DELETE FROM Users WHERE userid=" + user.getUserid() + ";");
                return user.getUserid();
            } catch (SQLException e) {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in deleteUser.");
            return null;
        }
    }

    // message
    public MessageObject selectMessage(BigInteger msgid) {

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Messages WHERE msgid=" + msgid + ";");
            if (rs.next()) {
                MessageObject msg = new MessageObject();
                msg.setMsgid(BigInteger.valueOf(rs.getInt("msgid")));
                msg.setWhen(this.convertTimestamp(rs.getTimestamp("when")));
                msg.setFrom(BigInteger.valueOf(rs.getInt("from")));
                msg.setTo(BigInteger.valueOf(rs.getInt("to")));
                msg.setSubject(rs.getString("subject"));
                msg.setBody(rs.getString("body"));
                return msg;
            } else {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in selectMessage.");
            return null;
        }
    }

    public BigInteger insertMessage(MessageObject msg) {

        String values = "DEFAULT, DEFAULT, ";
        values += "" + msg.getFrom() + ", ";
        values += "" + msg.getTo()   + ", ";
        values += "'" + String.join("''", msg.getSubject().split("'", -1)) + "', ";
        values += "'" + String.join("''", msg.getBody().split("'", -1))    + "'";

        String query = "INSERT INTO Messages VALUES (" + values  + ") RETURNING msgid;";
        try {
            Statement st = con.createStatement();
            try {
                ResultSet rs = st.executeQuery(query);
                if (rs.next()) {
                    return BigInteger.valueOf(rs.getInt("msgid"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in insertMessage.");
            return null;
        }
    }

    public boolean updateInboxSent(UserObject from, UserObject to, BigInteger msgid, String appendOrRemove) {
        // TODO: improve this
        try {
            Statement st = con.createStatement();
            try {
                st.executeUpdate("UPDATE Users SET sent=ARRAY_"  + appendOrRemove + "(sent, "  + msgid + ") " +
                        "WHERE userid=" + from.getUserid() + ";");
                st.executeUpdate("UPDATE Users SET inbox=ARRAY_" + appendOrRemove + "(inbox, " + msgid + ") " +
                        "WHERE userid=" + to.getUserid()   + ";");
                return true;
            } catch (SQLException e) {
                return false;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in updateInboxSent.");
            return false;
        }
    }

    public BigInteger deleteMessage(MessageObject msg) {

        try {
            Statement st = con.createStatement();
            try {
                st.executeUpdate("DELETE FROM Messages WHERE msgid=" + msg.getMsgid() + ";");
                return msg.getMsgid();
            } catch (SQLException e) {
                return null;
            }
        } catch (Exception e) {
            Assert.notNull(null, "Exception in deleteMessage.");
            return null;
        }
    }
}
