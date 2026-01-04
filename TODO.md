### 1. Read about Replay Attacks - PASS 
    - Replay attaks can happen but we only care in some cases:
    #### Case 1 (we DON'T CARE)
        - if there is a log in packet that the middle men snifes and sends again we don t care becauce he does't have
        the key for decrypting the packet so he can't get the information to log in he can't get the jwt token from the 
        payload 

    #### Case 2 (we Care)
        - if there is some request like send person A 5$ the middle man can coppy the packet and resend it. In this case the 
        attecker doesn't care that he can't see any information he only cares that the transaction will be made by the server
        and the user A will take another 5$ with out the consent of the original sender.

    Only sensitive endpoints should be equipt with CSRF tokens otherwise we the attacker can't do much. 
    We don't do this for every request becauce there will be more load on ther server and the server should save data
    for every request.

### 2. JWT token resend - NOT PASS
    - When th JWT token expires a new one has to be send to the user 

### 3. JWT token set as cookie - PASS
    - I think the token must be made a cookie not just send then I can see it in the browser (not sure what else is the diff)
    For mobile there are no cookies so we will stay with the current architecture the jwt token will be saved where the clients wants.

### 4. Auto createing tables 
    - Should be turned off when in production

### 5. Should switch to cookies - PASS 
    - The jwt token should be stored in cookie and send to the client
    - CSRF token should be added as well when using the cookies
FROM the 3 

### 6. Biscuits for storing session on mobile - PASS
    - how sessions mobile are stored
    Not done like this

### 7. Cross site attacks 
    - if the jwt token is saved in the local storage some one can read the local storage and see the jwt token
