package com.sezgin.mywebserver;

import com.sezgin.mywebserver.namespace.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@Endpoint
public class MyWebServerEndpoint {

    private static final String NAMESPACE_URI = "http://sezgin.com/MyWebServer/namespace";
    private UserMessageRepository repository;

    @Autowired
    public MyWebServerEndpoint(UserMessageRepository repository) {
        this.repository = repository;
    }

    private ClientSoapHeaders getClientSoapHeaders(SoapHeaderElement header) {
        ClientSoapHeaders clientSoapHeaders = null;
        try {
            JAXBContext context = JAXBContext.newInstance(ClientSoapHeaders.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            clientSoapHeaders = (ClientSoapHeaders) unmarshaller.unmarshal(header.getSource());
        } catch (Exception e) {
            clientSoapHeaders = new ClientSoapHeaders();
        }
        return clientSoapHeaders;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "loginRequest")
    @ResponsePayload
    public LoginResponse login(@RequestPayload LoginRequest request) {
        LoginResponse response = new LoginResponse();
        response.setClient(repository.login(request.getUsername(), request.getPassword()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "logoutRequest")
    @ResponsePayload
    public LogoutResponse logout(@RequestPayload LogoutRequest request,
                                 @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        LogoutResponse response = new LogoutResponse();
        response.setUserid(repository.logout(this.getClientSoapHeaders(header).getClient(), request.getUserid()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listUserIDsRequest")
    @ResponsePayload
    public ListUserIDsResponse listUserIDs(@RequestPayload ListUserIDsRequest request,
                                           @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        ListUserIDsResponse response = new ListUserIDsResponse();
        response.setUserids(repository.listUserIDs(this.getClientSoapHeaders(header).getClient(), request.getLimit()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserIDByUsernameRequest")
    @ResponsePayload
    public GetUserIDByUsernameResponse getUserIDByUsername(@RequestPayload GetUserIDByUsernameRequest request,
                                                           @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        GetUserIDByUsernameResponse response = new GetUserIDByUsernameResponse();
        response.setUserid(repository.findUser(this.getClientSoapHeaders(header).getClient(), request.getUsername()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUsernameByUserIDRequest")
    @ResponsePayload
    public GetUsernameByUserIDResponse getUsernameByUserID(@RequestPayload GetUsernameByUserIDRequest request,
                                                           @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        GetUsernameByUserIDResponse response = new GetUsernameByUserIDResponse();
        response.setUsername(repository.findUser(this.getClientSoapHeaders(header).getClient(), request.getUserid()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserByUserIDRequest")
    @ResponsePayload
    public GetUserByUserIDResponse getUserByUserID(@RequestPayload GetUserByUserIDRequest request,
                                                   @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        GetUserByUserIDResponse response = new GetUserByUserIDResponse();
        response.setUser(repository.showUser(this.getClientSoapHeaders(header).getClient(), request.getUserid()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createUserRequest")
    @ResponsePayload
    public CreateUserResponse createUser(@RequestPayload CreateUserRequest request,
                                         @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        CreateUserResponse response = new CreateUserResponse();
        response.setUserid(repository.addUser(this.getClientSoapHeaders(header).getClient(), request.getUser()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserRequest")
    @ResponsePayload
    public UpdateUserResponse updateUser(@RequestPayload UpdateUserRequest request,
                                         @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        UpdateUserResponse response = new UpdateUserResponse();
        response.setUserid(repository.changeUser(this.getClientSoapHeaders(header).getClient(), request.getUserid(), request.getUser()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserByUserIDRequest")
    @ResponsePayload
    public DeleteUserByUserIDResponse deleteUserByUserID(@RequestPayload DeleteUserByUserIDRequest request,
                                                         @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        DeleteUserByUserIDResponse response = new DeleteUserByUserIDResponse();
        response.setUserid(repository.removeUser(this.getClientSoapHeaders(header).getClient(), request.getUserid()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listInboxMessageIDsRequest")
    @ResponsePayload
    public ListInboxMessageIDsResponse listInboxMessageIDs(@RequestPayload ListInboxMessageIDsRequest request,
                                                           @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        ListInboxMessageIDsResponse response = new ListInboxMessageIDsResponse();
        response.setInbox(repository.listMessageIDs(this.getClientSoapHeaders(header).getClient(), request.getUserid(), "inbox"));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listSentMessageIDsRequest")
    @ResponsePayload
    public ListSentMessageIDsResponse listSentMessageIDs(@RequestPayload ListSentMessageIDsRequest request,
                                                         @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        ListSentMessageIDsResponse response = new ListSentMessageIDsResponse();
        response.setSent(repository.listMessageIDs(this.getClientSoapHeaders(header).getClient(), request.getUserid(), "sent"));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getMessageByMsgIDRequest")
    @ResponsePayload
    public GetMessageByMsgIDResponse getMessageByMsgID(@RequestPayload GetMessageByMsgIDRequest request,
                                                       @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        GetMessageByMsgIDResponse response = new GetMessageByMsgIDResponse();
        response.setMessage(repository.showMessage(this.getClientSoapHeaders(header).getClient(), request.getMsgid()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendMessageRequest")
    @ResponsePayload
    public SendMessageResponse sendMessage(@RequestPayload SendMessageRequest request,
                                           @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        SendMessageResponse response = new SendMessageResponse();
        response.setMsgid(repository.conveyMessage(this.getClientSoapHeaders(header).getClient(), request.getMessage()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteMessageByMsgIDRequest")
    @ResponsePayload
    public DeleteMessageByMsgIDResponse deleteMessageByMsgID(@RequestPayload DeleteMessageByMsgIDRequest request,
                                                             @SoapHeader(value = "{" + NAMESPACE_URI + "}clientSoapHeaders") SoapHeaderElement header) {
        DeleteMessageByMsgIDResponse response = new DeleteMessageByMsgIDResponse();
        response.setMsgid(repository.removeMessage(this.getClientSoapHeaders(header).getClient(), request.getMsgid()));
        return response;
    }
}
