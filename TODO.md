### 1. Read about Replay Attacks, 
    - JWT partiuly mitigats them but not sure how much and if it works.

    - One more token can be added to the packet the user sends this token is 
      valid for only one request if the packet is copied and send again the the token is not valid

### 2. JWT token resend
    - When th JWT token expires a new one has to be send to the user 

### 3. JWT token set as cookie
    - I think the token must be made a cookie not just send then I can see it in the browser (not sure what else is the diff)
