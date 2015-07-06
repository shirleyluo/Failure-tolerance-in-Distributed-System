I plan to implement two main functions in project3

1. Support user accounts and authentication

    Implementing a separate API for creating new users. The interface showing in terminal like:
    1. register
    2. login
    Users can only do POST, GET and DELETE when login. However, user can do get no matter login or not. 
    

2. Implement eventual consistency

3. DELETE
    
    a) delete all tweets
        DELETE /tweets
    b) delete by hashtag
        DELETE /tweets?hashtag=hello
    
    DELETE is also strong consistency. Only after all secondaries have received the update will the primary reply to the client. 
    

Demo Requirements:

1. Launch two backend and two frontend nodes
2. Launch two client nodes
3. Register new user, then login old user
4. Execute several POST, GET and DELETE request from each client
    Execute GET from both primary and secondary nodes
5. Kill one of the backend nodes
6. Execute several more POST, GET and DELETE requests from the frontends
7. Demonstrate that if a frontend contact a backend with a GET and has a higher timestamp the backend will not reply with older data
