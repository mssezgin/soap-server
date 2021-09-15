const URL = "http://localhost:8080/ws/";
const NAMESPACE_URI = "http://sezgin.com/MyWebServer/namespace";

// these will get assigned after login
let SessionID;
let UserID;
let commonHeader = "";
var commonResponse = undefined;

function commonRequest(request, callBackFunction, async = true) {

    let fullRequest =
        '<?xml version="1.0" encoding="utf-8"?>' +
        '<Envelope xmlns="http://schemas.xmlsoap.org/soap/envelope/" ' +
                  'xmlns:tns="' + NAMESPACE_URI + '">' +
            '<Header>' + commonHeader + '</Header>' +
            '<Body>' + request + '</Body>' +
        '</Envelope>';

    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4) {
            if (xmlHttp.status === 200) {
                if (callBackFunction !== undefined) {
                    callBackFunction(this);
                } else {
                    commonResponse = this;
                }
            } else if (xmlHttp.status === 500) {
                commonError(this);
            }
        }
    };
    xmlHttp.open("POST", URL, async);
    xmlHttp.setRequestHeader("Content-Type", "text/xml");
    xmlHttp.setRequestHeader("SOAPAction", URL);
    xmlHttp.send(fullRequest);
}

function commonError(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let errorMessage =
        xmlDoc.getElementsByTagName("faultcode")[0].innerHTML + ": " +
        xmlDoc.getElementsByTagName("faultstring")[0].innerHTML;
        alert(errorMessage);
}

function loginRequest() {
    let request =
        "<tns:loginRequest>" +
            "<tns:username>" + document.getElementById("login-username").value + "</tns:username>" +
            "<tns:password>" + document.getElementById("login-password").value + "</tns:password>" +
        "</tns:loginRequest>";
    commonRequest(request, loginResponse);
}

function loginResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    SessionID = xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "sessionID")[0].innerHTML;
    UserID = xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid")[0].innerHTML;
    commonHeader =
        "<tns:clientSoapHeaders>" +
            "<tns:client>" +
                "<tns:sessionID>" + SessionID + "</tns:sessionID>" +
                "<tns:userid>" + UserID + "</tns:userid>" +
            "</tns:client>" +
        "</tns:clientSoapHeaders>";

    getUserByUserIDRequest(UserID, true);
    let elements = document.getElementsByClassName("user-box");
    for (let i = 0; i < elements.length; ++i) {
        elements[i].style.display = "block";
    }
    document.getElementsByClassName("login")[0].style.display = "none";
}

function initializeProfile(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let elements = ["userid", "admin", "username", "password", "email", "firstname", "lastname", "gender", "birth"];
    for (let i = 0; i < elements.length; ++i) {
        document.getElementById(elements[i] + "-info").innerHTML =
            xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, elements[i])[0].innerHTML;
    }
    if (xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "admin")[0].innerHTML === "true") {
        let elements = document.getElementsByClassName("admin-box");
        for (let i = 0; i < elements.length; ++i) {
            elements[i].style.display = "block";
        }
    }
}

function logoutRequest() {
    let request =
        "<tns:logoutRequest>" +
            "<tns:userid>" + UserID + "</tns:userid>" +
        "</tns:logoutRequest>";
    commonRequest(request, logoutResponse);
}

function logoutResponse(xmlHttp) {
    location.reload();
    document.getElementById("login-title").innerHTML = "Logged out";
}

function listUserIDsRequest(limit = -1) {
    let request =
        "<tns:listUserIDsRequest>" +
            "<tns:limit>" + limit + "</tns:limit>" +
        "</tns:listUserIDsRequest>";
    commonRequest(request, listUserIDsResponse);
}

function listUserIDsResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let userids = xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid");
    let userList = "";
    for (let i = 0; i < userids.length; ++i) {
        userList += '<div onclick="getUserByUserIDRequest(' + userids[i].innerHTML + ');">' + userids[i].innerHTML + '</div>';
    }
    document.getElementById("user-list").innerHTML = userList;
}

function getUserIDByUsernameRequest(username) {
    let request =
        "<tns:getUserIDByUsernameRequest>" +
            "<tns:username>" + username + "</tns:username>" +
        "</tns:getUserIDByUsernameRequest>";
    commonRequest(request, undefined, false);
    let userid = getUserIDByUsernameResponse(commonResponse);
    commonResponse = undefined;
    return userid;
}

function getUserIDByUsernameResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let userid;
    try {
        userid = xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid")[0].innerHTML;
    } catch (e) {
        userid = 0;
    }
    return userid;
}

function getUsernameByUserIDRequest(fromOrTo, userid = UserID) {
    if (fromOrTo !== undefined) {
        userid = document.getElementById(fromOrTo).innerHTML;
    }
    let request =
        "<tns:getUsernameByUserIDRequest>" +
            "<tns:userid>" + userid + "</tns:userid>" +
        "</tns:getUsernameByUserIDRequest>";
    commonRequest(request, undefined, false);
    getUsernameByUserIDResponse(commonResponse, fromOrTo);
    commonResponse = undefined;
}

function getUsernameByUserIDResponse(xmlHttp, fromOrTo) {
    let xmlDoc = xmlHttp.responseXML;
    document.getElementById(fromOrTo).innerHTML =
        xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "username")[0].innerHTML;
}

function getUserByUserIDRequest(userid = UserID, loginAction = false) {
    let request =
        "<tns:getUserByUserIDRequest>" +
            "<tns:userid>" + userid + "</tns:userid>" +
        "</tns:getUserByUserIDRequest>";
    if (loginAction) {
        commonRequest(request, initializeProfile);
    } else {
        commonRequest(request, getUserByUserIDResponse);
    }
}

function getUserByUserIDResponse(xmlHttp) {
    document.getElementById("admin-info-table").style.display = "block";
    let xmlDoc = xmlHttp.responseXML;
    let elements = ["userid", "admin", "username", "password", "email", "firstname", "lastname", "gender", "birth"];
    for (let i = 0; i < elements.length; ++i) {
        document.getElementById("shown-" + elements[i] + "-info").innerHTML =
            xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, elements[i])[0].innerHTML;
    }
    document.getElementById("admin-input-table").style.display = "none";
}

function updateButton() {
    document.getElementById("admin-info-table").style.display = "none";
    document.getElementById("admin-input-table").style.display = "block";
    document.getElementById("newuser-create-update").value = "Update";
    document.getElementById("newuser-create-update")
        .setAttribute("onclick", "updateUserRequest();");
    let elements = ["userid", "username", "password", "email", "firstname", "lastname", "gender", "birth"];
    for (let i = 0; i < elements.length; ++i) {
        document.getElementById("newuser-" + elements[i]).value =
            document.getElementById("shown-" + elements[i] + "-info").innerHTML;
    }
    document.getElementById("newuser-admin").checked =
        (document.getElementById("shown-admin-info").innerHTML === "true");
}

function newUserButton() {
    document.getElementById("admin-info-table").style.display = "none";
    document.getElementById("admin-input-table").style.display = "block";
    document.getElementById("newuser-create-update").value = "Create";
    document.getElementById("newuser-create-update")
        .setAttribute("onclick", "createUserRequest();");
    let elements = ["userid", "username", "password", "email", "firstname", "lastname", "gender", "birth"];
    for (let i = 0; i < elements.length; ++i) {
        document.getElementById("newuser-" + elements[i]).value = "";
    }
    document.getElementById("newuser-admin").checked = false;
}

function createUserRequest() {
    let request =
        "<tns:createUserRequest>" +
            "<tns:user>" +
                "<tns:userid/>" +
                "<tns:admin>" + document.getElementById("newuser-admin").checked + "</tns:admin>" +
                "<tns:username>" + document.getElementById("newuser-username").value + "</tns:username>" +
                "<tns:password>" + document.getElementById("newuser-password").value + "</tns:password>" +
                "<tns:email>" + document.getElementById("newuser-email").value + "</tns:email>" +
                "<tns:firstname>" + document.getElementById("newuser-firstname").value + "</tns:firstname>" +
                "<tns:lastname>" + document.getElementById("newuser-lastname").value + "</tns:lastname>" +
                "<tns:gender>" + document.getElementById("newuser-gender").value + "</tns:gender>" +
                "<tns:birth>" + document.getElementById("newuser-birth").value + "</tns:birth>" +
                "<tns:inbox/>" +
                "<tns:sent/>" +
            "</tns:user>" +
        "</tns:createUserRequest>";
    commonRequest(request, createUserResponse);
}

function createUserResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    alert("User was created.\nUserID: " + xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid")[0].innerHTML);
    document.getElementById("admin-input-table").style.display = "none";
}

function updateUserRequest() {
    let request =
        "<tns:updateUserRequest>" +
            "<tns:userid>" + document.getElementById("newuser-userid").value + "</tns:userid>" +
            "<tns:user>" +
                "<tns:userid/>" +
                "<tns:admin>" + document.getElementById("newuser-admin").checked + "</tns:admin>" +
                "<tns:username>" + document.getElementById("newuser-username").value + "</tns:username>" +
                "<tns:password>" + document.getElementById("newuser-password").value + "</tns:password>" +
                "<tns:email>" + document.getElementById("newuser-email").value + "</tns:email>" +
                "<tns:firstname>" + document.getElementById("newuser-firstname").value + "</tns:firstname>" +
                "<tns:lastname>" + document.getElementById("newuser-lastname").value + "</tns:lastname>" +
                "<tns:gender>" + document.getElementById("newuser-gender").value + "</tns:gender>" +
                "<tns:birth>" + document.getElementById("newuser-birth").value + "</tns:birth>" +
                "<tns:inbox/>" +
                "<tns:sent/>" +
            "</tns:user>" +
        "</tns:updateUserRequest>";
    if (confirm("Are you sure?")) {
        commonRequest(request, updateUserResponse);
    }
}

function updateUserResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    alert("User was updated.\nUserID: " + xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid")[0].innerHTML);
    document.getElementById("admin-input-table").style.display = "none";
}

function deleteUserByUserIDRequest() {
    let request =
        "<tns:deleteUserByUserIDRequest>" +
            "<tns:userid>" + document.getElementById("shown-userid-info").innerHTML + "</tns:userid>" +
        "</tns:deleteUserByUserIDRequest>";
    if (confirm("Are you sure?")) {
        commonRequest(request, deleteUserByUserIDResponse);
    }
}

function deleteUserByUserIDResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    alert("User was deleted.\nUserID: " + xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "userid")[0].innerHTML);
}

function listInboxSentMessageIDsRequest(inboxOrSent = "Inbox") {
    let request =
        "<tns:list" + inboxOrSent + "MessageIDsRequest>" +
            "<tns:userid>" + UserID + "</tns:userid>" +
        "</tns:list" + inboxOrSent + "MessageIDsRequest>";
    commonRequest(request, listInboxSentMessageIDsResponse);
}

function listInboxSentMessageIDsResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let msgids = xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "msgid");
    let messageList = "";
    for (let i = 0; i < msgids.length; ++i) {
        messageList += '<div onclick="getMessageByMsgIDRequest(' + msgids[i].innerHTML + ');">' + msgids[i].innerHTML + '</div>';
    }
    document.getElementById("message-list").innerHTML = messageList;
}

function getMessageByMsgIDRequest(msgid) {
    let request =
        "<tns:getMessageByMsgIDRequest>" +
            "<tns:msgid>" + msgid + "</tns:msgid>" +
        "</tns:getMessageByMsgIDRequest>";
    commonRequest(request, getMessageByMsgIDResponse);
}

function getMessageByMsgIDResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    let elements = ["msgid", "when", "from", "to", "subject", "body"];
    for (let i = 0; i < elements.length; ++i) {
        document.getElementById(elements[i] + "-info").innerHTML =
            xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, elements[i])[0].innerHTML;
    }
    document.getElementById("input-table").style.display = "none";
}

function replyButton() {
    newMessageButton();
    document.getElementById("newmsg-to").value =
        document.getElementById("from-info").innerHTML;
    document.getElementById("newmsg-subject").value =
        "Re: " + document.getElementById("subject-info").innerHTML;
    document.getElementById("newmsg-body").value = "";
}

function forwardButton() {
    newMessageButton();
    document.getElementById("newmsg-to").value = "";
    document.getElementById("newmsg-subject").value =
        "Fwd: " + document.getElementById("subject-info").innerHTML;
    document.getElementById("newmsg-body").value =
        document.getElementById("body-info").innerHTML;
}

function newMessageButton() {
    document.getElementById("input-table").style.display = "block";
    document.getElementById("newmsg-from").value = UserID;
    document.getElementById("newmsg-to").value = "";
    document.getElementById("newmsg-subject").value = "";
    document.getElementById("newmsg-body").value = "";
}

function sendMessageRequest() {
    let to = getUserIDByUsernameRequest(document.getElementById("newmsg-to").value);
    let request =
        "<tns:sendMessageRequest>" +
            "<tns:message>" +
                "<tns:msgid/>" +
                "<tns:when/>" +
                "<tns:from>" + UserID + "</tns:from>" +
                "<tns:to>" + to + "</tns:to>" +
                "<tns:subject>" + document.getElementById("newmsg-subject").value + "</tns:subject>" +
                "<tns:body>" + document.getElementById("newmsg-body").value + "</tns:body>" +
            "</tns:message>" +
        "</tns:sendMessageRequest>";
    commonRequest(request, sendMessageResponse);
}

function sendMessageResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    alert("Message was sent.\nMessageID: " + xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "msgid")[0].innerHTML);
    document.getElementById("input-table").style.display = "none";
}

function deleteMessageByMsgIDRequest() {
    let request =
        "<tns:deleteMessageByMsgIDRequest>" +
            "<tns:msgid>" + document.getElementById("msgid-info").innerHTML + "</tns:msgid>" +
        "</tns:deleteMessageByMsgIDRequest>";
    if (confirm("Are you sure?")) {
        commonRequest(request, deleteMessageByMsgIDResponse);
    }
}

function deleteMessageByMsgIDResponse(xmlHttp) {
    let xmlDoc = xmlHttp.responseXML;
    alert("Message was deleted.\nMessageID: " + xmlDoc.getElementsByTagNameNS(NAMESPACE_URI, "msgid")[0].innerHTML);
}
