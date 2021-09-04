package com.sezgin.mywebserver;

import com.sezgin.mywebserver.namespace.*;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

@Component
public class UserMessageRepository {

    // TODO: create constants for error messages
    // TODO: delete unnecessary assertions that assert client is not null
    private static final MyDB db = new MyDB();
    private static final HashMap<BigInteger, BigInteger> onlineUsers = new HashMap<>();

    @PostConstruct
    public void initData() {
        try {
            db.connectDB();
        } catch (SQLException e) {
            Assert.notNull(null, "SQLException in initData");
        }
    }

    private BigInteger generateSessionID() {

        Random random = new Random();
        BigInteger sessionID;
        do {
            sessionID = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE));
        } while (onlineUsers.containsKey(sessionID));
        return sessionID;
    }

    private boolean validateSession(ClientObject client) {

        Assert.notNull(client, "Client cannot be null.");
        Assert.notNull(client.getSessionID(), "SessionID cannot be null.");
        Assert.notNull(client.getUserid(), "Userid cannot be null.");

        Assert.isTrue(onlineUsers.containsKey(client.getSessionID()), "Invalid sessionID.");
        return onlineUsers.get(client.getSessionID()).equals(client.getUserid());
    }

    // login logout // TODO: add sign up function
    public ClientObject login(String username, String password) {

        Assert.notNull(username, "Username cannot be null.");
        Assert.notNull(password, "Password cannot be null.");

        BigInteger userid = db.searchUser(username);
        Assert.notNull(userid, "Wrong username.");
        UserObject user = db.selectUser(userid);
        // Assert.notNull(user, "Could not show user.");
        Assert.isTrue(user.getPassword().equals(password), "Wrong password.");

        ClientObject client = new ClientObject();
        client.setSessionID(this.generateSessionID());
        client.setUserid(userid);
        onlineUsers.put(client.getSessionID(), client.getUserid());
        return client;
    }

    public BigInteger logout(ClientObject client, BigInteger userid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");
        Assert.isTrue(client.getUserid().equals(userid), "Invalid logout request.");
        return onlineUsers.remove(client.getSessionID());
    }

    // user // TODO: add list online clients function
    public UserIDList listUserIDs(ClientObject client, int limit) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        Assert.isTrue(user.isAdmin(), "You need to be an admin.");

        UserIDList userids = db.selectUserIDs(limit);
        Assert.notNull(userids, "Could not list userids.");
        return userids;
    }

    public BigInteger findUser(ClientObject client, String username) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(username, "Username cannot be null.");

        BigInteger userid = db.searchUser(username);
        Assert.notNull(userid, "Could not find user.");
        return userid;
    }

    public String findUser(ClientObject client, BigInteger userid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");

        String username = db.searchUser(userid);
        Assert.notNull(username, "Could not find user.");
        return username;
    }

    public UserObject showUser(ClientObject client, BigInteger userid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        // Assert.isTrue(user.getUserid().equals(userid) || user.isAdmin(), "You need to be an admin.");
        if (user.getUserid().equals(userid))
            return user;

        Assert.isTrue(user.isAdmin(), "You need to be an admin.");
        UserObject shownUser = db.selectUser(userid);
        Assert.notNull(shownUser, "Could not show user.");
        return shownUser;
        /* if (!user.getUserid().equals(userid)) {
            Assert.isTrue(user.isAdmin(), "You need to be an admin.");
            UserObject shownUser = db.selectUser(userid);
            Assert.notNull(shownUser, "Could not show user.");
            return shownUser;
        }
        return user; */
    }

    public BigInteger addUser(ClientObject client, UserObject addedUser) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(addedUser, "User cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        Assert.isTrue(user.isAdmin(), "You need to be an admin.");

        BigInteger userid = db.insertUser(addedUser);
        Assert.notNull(userid, "Could not add user.");
        return userid;
    }

    public BigInteger changeUser(ClientObject client, BigInteger userid, UserObject newUser) {

        // TODO: improve this
        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");
        Assert.notNull(newUser, "User cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        Assert.isTrue(user.isAdmin(), "You need to be an admin.");

        UserObject updatedUser = db.selectUser(userid);
        Assert.notNull(updatedUser, "User does not exist.");

        onlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(updatedUser.getUserid()));
        BigInteger updatedUserid = db.updateUser(updatedUser, newUser);
        Assert.notNull(updatedUserid, "Could not change user.");
        return updatedUserid;
    }

    public BigInteger removeUser(ClientObject client, BigInteger userid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        Assert.isTrue(user.isAdmin(), "You need to be an admin.");

        UserObject removedUser = db.selectUser(userid);
        Assert.notNull(removedUser, "User does not exist.");

        onlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(removedUser.getUserid()));
        BigInteger removedUserid = db.deleteUser(removedUser);
        Assert.notNull(removedUserid, "Could not remove user.");
        return removedUserid;
    }

    // message
    public MessageIDList listMessageIDs(ClientObject client, BigInteger userid, String inboxOrSent) {

        // TODO: userid may be redundant, admins can show user, no need to list others' inbox/sent
        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(userid, "Userid cannot be null.");
        Assert.isTrue(client.getUserid().equals(userid), "You need to be an admin.");
        // UserObject user = db.selectUser(userid);
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");

        MessageIDList msgids = null;
        switch (inboxOrSent) {
            case "inbox":
                msgids = user.getInbox();
                break;
            case "sent":
                msgids = user.getSent();
                break;
            default:
                // Assert.notNull(null, "Inbox or Sent should be specified.");
                break;
        }
        Assert.notNull(msgids, "Could not list messageIDs.");
        return msgids;
    }

    public MessageObject showMessage(ClientObject client, BigInteger msgid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(msgid, "Msgid cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(user, "User does not exist.");
        if (!user.getInbox().getMsgid().contains(msgid) && !user.getSent().getMsgid().contains(msgid)) {
            Assert.isTrue(user.isAdmin(), "You need to be an admin.");
        }

        // TODO: from and to are userids, make them usernames
        MessageObject msg = db.selectMessage(msgid);
        Assert.notNull(msg, "Could not show message.");
        return msg;
    }

    public BigInteger conveyMessage(ClientObject client, MessageObject msg) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(msg, "Message cannot be null.");
        Assert.isTrue(client.getUserid().equals(msg.getFrom()), "You need to be an admin.");

        UserObject from = db.selectUser(client.getUserid());
        // Assert.notNull(from, "User does not exist.");
        UserObject to = db.selectUser(msg.getTo());
        Assert.notNull(to, "User does not exist.");

        BigInteger msgid = db.insertMessage(msg);
        Assert.notNull(msgid, "Could not convey message.");
        boolean updated = db.updateInboxSent(from, to, msgid, "APPEND");
        Assert.isTrue(updated, "Could not update inbox and/or sent lists.");
        return msgid;
    }

    public BigInteger removeMessage(ClientObject client, BigInteger msgid) {

        Assert.isTrue(this.validateSession(client), "Invalid session.");
        Assert.notNull(msgid, "Msgid cannot be null.");
        UserObject user = db.selectUser(client.getUserid());
        // Assert.notNull(from, "User does not exist.");

        if (!user.getInbox().getMsgid().contains(msgid) && !user.getSent().getMsgid().contains(msgid)) {
            Assert.isTrue(user.isAdmin(), "You need to be an admin.");
            MessageObject msg = db.selectMessage(msgid);
            Assert.notNull(msg, "Message does not exist.");
            BigInteger removedMsgid = db.deleteMessage(msg);
            Assert.notNull(removedMsgid, "Could not remove message.");
            return removedMsgid;
        }

        boolean update = db.updateInboxSent(user, user, msgid, "REMOVE");
        Assert.isTrue(update, "Could not update inbox and/or sent lists.");
        return msgid;
    }
}
