### 1. Read about Replay Attacks, 
    * JWT partiuly mitigats them but not sure how much and if it works.
    * One more token can be added to the packet the user sends this token is 
      valid for only one request if the packet is copied and send again the the token is not valid
    * Adding salt to the password hash storing the salt in the database with the userid as a key(one salt per user).
