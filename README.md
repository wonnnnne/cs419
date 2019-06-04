# cs419
Computer Security 



                        CRYPTR: Secure File Sharing over the World Wide Web


  Securely sharing information over a public network has always been a problem. Most people who use the
internet to send files and messages typically don’t want outsiders to be able to hijack their messages and
steal any personal information such as names, addresses, credit card information.
SSL/TLS protocols can used to set up secure network connections and securely transfer information
over the internet. The issue here is that this requires both parties at each end of the network to be online
for the whole duration of the message transmission. This is okay for people who are online all the time,
but most people aren’t. In the most common case, in order to pass messages and files in an asynchronous
manner, people typically use online services such as email, Facebook, Google Drive, or Dropbox. In this
case, users upload and download information to the hosting service over a secure network connection and
trust that their information is secure on the services’ databases.
In recent times, it has been shown that many of these online services have been found to be insecure,
resulting in personal user files and messages leaked. To combat this, these services have taken extra
precautions such as encrypting files internally in order to reassure their users that their information is
safe. Despite all these efforts, more external attacks, such as social engineering to steal usernames and
passwords, can still be used to bypass these internal security mechanisms.
The main problem at hand is, "How can you share a file and get it from point A to point B ensuring
the file can be only be read at Point B?". To ensure the utmost amount of security over the untrusted
internet, we would have to ensure any files or messages can not be decrypted and read until it reaches
it destination. In this assignment we’ll play around with what we know about modern cryptography and
implement our own way to share files over the untrusted internet, allowing us share files and messages
on any service we want by hiding our information in plain sight.


The crypt program will support the following functions:
1. Generating a secret key
2. Encrypting a file using a secret key
3. Decrypting a file using a secret key
4. Encrypting a secret key using a public key
5. Decrypting a secret key using a private key



