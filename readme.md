# Introduction
This is a project is for University of Auckland. This implements RFC913 simple file transfer protocol in java. [Link to Specification](https://tools.ietf.org/html/rfc913) 

All commands are implemented. 

Both Server and Client is started on "localhost". 
Port is set to "6666" at the moment. As the default "115" port sometimes might be reserved. 
*If the port and ip needs to be changed, these can be edited in Server/Server.java line 12-13 and Client/Client.java line 24-25*

Multiple instances of client can connect to the server as the server forks off a new thread each time. 
# Files
Files for the client and server transfer are stored by default in in the `Files` folder within respective directories. ie `Client/Files`, `Server/Files` 

# How to Set up
*This has been tested on ubuntu linux 20.04, but should work in windows too. JDK and JRE 14 is required. make sure java and javac has been added to your system path. Check google on how to do this* 

 - Open bash/terminal in the root folder `(../ming-rfc913) `
 - To compile : 
 `$ javac Client/Client.java `
 `$ javac Server/Server.java`
- To run: 
	Open two separate instances of terminal in the room folder `(../ming-rfc913)`
	Terminal one (Server)
	` $ java Server.Server`
	Terminal two (Client )
	` $ java Client.Client`
**(Can open as many clients terminals as you need)**
	
	

# Commands and Examples
All commands can be **entered ignoring case**. If format is entered incorrectly the client will prompt you the correct format. 
eg:
<pre><code>user adfsdf asdfds
USER command format : USER <user name>
list
LIST command format : LIST { F | V } <directory-path>. check README for specific directory path syntax
</code></pre>

## Logging in 
Most commands require the user to be loggedn first. User credentials are stored in 
`(../ming-rfc913/Server/Configs/userInfo.csv)`
Currently in file there are 4 users:
user1 and user2 requires all 3 parameters to be entered
User3 only needs username and password to be logged in, and admin only needs the user name. 
|user|account|password|
|--|--|--|
|user1|account1|pass1|
|user2|account2|pass2|
|user3||pass3|
|admin|||


### USER
 **format:** `USER user-id`
The user command is used to enter the username. 

- incorrect username example
<pre><code>USER wronguser
-Invalid user-id, try again </code></pre>

- correct username example
<pre><code>USER user1
+User-id valid, send account and password</code></pre> 

- admin acount only needs username
<pre><code>USER admin
!admin logged in</pre></code>

### ACCT
 **format:** `ACCT account`
Once a valid username is entered, you should then enter the account name. 
- Wrong account example
<pre><code>ACCT wrongaccount
-Invalid Account, try again</code></pre>

- Correct account example
<pre><code>ACCT account1 
+Account ok or not needed. Send your password next</code></pre>

### PASS
 **format:** `PASS password`
Once a valid username is entered, you can also enter the password. 

- Entering username, password then account
<pre><code>USER user1
+User-id valid, send account and password
PASS pass1
+Password ok but you haven't specified the account
ACCT account1 
!Account was ok or not needed. Skip the password.</code></pre>

- Enter username, account then password
<pre><code>USER user2
+User-id valid, send account and password
ACCT account2
+Account ok or not needed. Send your password next
PASS pass2
!Password is ok and you can begin file transfers.</code></pre> 

- user3 only needs username and pass
<pre><code>USER user3
+User-id valid, send account and password
PASS pass3
!Password is ok and you can begin file transfers.</code></pre>

- Wrong password
<pre><code>USER user1
+User-id valid, send account and password
PASS wrongpass
-Wrong password, try again</code></pre>


## Navigating Remote Server
Once you receive a message starting with "!", it means you're logged in and other commands are availabe. 
### CDIR
**Format:** `CDIR new-directory`
CDIR is used to change directories, `new-directory` syntax:
1. You can navigate to the default files folder using "~"
2. Specify absolute paths by prepending with "@"
3. Go up a folder using "..", can only go up a dir at a time
4. Otherwise it's relative to the current directory. 
*There are no restrictions in which folders you can navigate to;* 

- Examples of navigating around
<pre><code>CDIR ~
!Changed working dir to /home/ming/ming-rfc913/Server/Files
CDIR ..
!Changed working dir to /home/ming/ming-rfc913/Server
CDIR ..
!Changed working dir to /home/ming/ming-rfc913
CDIR Server
!Changed working dir to /home/ming/ming-rfc913/Server
CDIR @/home/ming
!Changed working dir to /home/ming
CDIR /home/ming/non_existant_folder
-Can't connect to directory because directory does not exist or you have no permission
CDIR ~
!Changed working dir to /home/ming/ming-rfc913/Server/Files
</code></pre>

CDIR also requires you to be logged in. If no username has been entered, you will be rejected. If username has been entered it'll prompt you to enter account and password
- Example of logging in then changing dir
<pre><code>CDIR ~
-Cannot use CDIR when not logged in
USER user1
+User-id valid, send account and password
CDIR ~
+directory ok, send account/password
ACCT account1
+account ok send password
PASS pass1
!Changed working dir to /home/ming/ming-rfc913/Server/Files</code></pre>

- Example of failure to log in. 
<pre><code>USER user2
+User-id valid, send account and password
CDIR Folder2
+directory ok, send account/password
ACCT account2
+account ok send password
PASS wrongpass
-invalid password</code></pre>

### LIST
**Format:** `LIST { F | V } directory-path`
List has two modes, V for a more detailed listing, and F just lists the file names. 
`directory-path` follows the same syntax as CDIR

<pre><code>LIST F ~
+/home/ming/ming-rfc913/Server/Files
| FileName                       |
----------------------------------
| longtext1.txt                  |
| cat.png                        |
| shorttxt.txt                   |
| Folder2/..                     |

LIST V Folder2
+/home/ming/ming-rfc913/Server/Files/Folder2
| FileName                       | ModifiedTime         | Size(B)    | Owner                | RWX |
---------------------------------------------------------------------------------------------------
| lenaa.tif                      | 01/09/2020 06:09     | 65734      | ming                 | RW- |
| longtext2.txt                  | 01/09/2020 06:09     | 3940       | ming                 | RW- |
| Folder3/..                     | 02/09/2020 18:15     | 4096       | ming                 | RWX |

LIST V wrongfolder
-Directory invalid
LIST F @/home/ming/ming-rfc913
+/home/ming/ming-rfc913
| FileName                       |
----------------------------------
| .git/..                        |
| Utils/..                       |
| Client/..                      |
| Server/..                      |
</code></pre>

### KILL
**format:** `KILL file-spec`
Kill is used to delete a file withing the current folder. then checking it is removed. 
`file-spec` is relative to the last active dir, either from LIST or CDIR. USE `LIST F` to check where you are. 
- Deleting a file
<pre><code>LIST F ~
+/home/ming/ming-rfc913/Server/Files
| FileName                       |
----------------------------------
| longtext1.txt                  |
| cat.png                        |
| shorttxt.txt                   |
| Folder2/..                     |

KILL shorttxt.txt
+shorttxt.txt deleted
LIST F 
+/home/ming/ming-rfc913/Server/Files
| FileName                       |
----------------------------------
| longtext1.txt                  |
| cat.png                        |
| Folder2/..                     |
</code></pre>

- Attempting to delete a non-existant file
<pre><code>KILL shorttxt.txt
-Not deleted because no file exists
</code></pre>

### NAME, TOBE
**Format:** `NAME old-file-spec`
**Format:** `TOBE new-file-spec`
`old-file-spec` is relative to current server directory. The file you want to change the name of.
`new-file-spec` new name you wish to change to.

Changes a file name. First checks if file exists, then send`TOBE` with new name, if any other commands is sent the renaming is cancelled
<pre><code>LIST F
+/home/ming/ming-rfc913/Server/Files
| FileName                       |
----------------------------------
| longtext1.txt                  |
| cat.png                        |
| Folder2/..                     |

NAME cat.png
+File Exists
TOBE cat1.png
+cat.png renamed to cat1.png
NAME cat2.png
-File wasn't renamed because cat2.png does not exist
NAME cat1.png
+File Exists
LIST F
-Name change aborted as you need to send TOBE with new file name
</pre></code>

### DONE
**Format:** `DONE`
Done is used to indicate the end of the session from the client. 

- Client Side display
<pre><code>DONE
+Thank you for using MCHE226 STFP service.
Closing Client Session
Client Session ended.</code></pre>
- Server Side display
<pre><code>Closing Server instance
Server instance terminated</code></pre>

## Transferring Files
### TYPE
 **format:** `TYPE { A | B | C }`
The rfc specification specifics different ways to send the file. A for Ascii, B for binary, C for continious.
<pre><code>TYPE A
+Using Ascii mode
TYPE B
+Using Binary mode
TYPE C
+Using Continuous mode
TYPE D
-Type not valid</code></pre>

### RETR,SEND,STOP
**format:**`RETR file-spec`
`file-spec` is relative to the last active dir, either from LIST or CDIR. USE `LIST F` to check where you are. 
**format:**`SEND`
**format:**`STOP`

Retrieves a file from the current folder from the Server to the remote. 
Aftering sending the RETR command,  if the file exsists, the server will reply with the size of the file. You  then have the option to SEND or STOP ,which continues or stops the transfer.

- Checking what's in the director
<pre><code>LIST F
+/home/ming/ming-rfc913/Server/Files
| FileName                       |
----------------------------------
| longtext1.txt                  |
| cat.png                        |
| Folder2/..                     |
</pre></code>
- Retriving an file
<pre><code>RETR cat.png
48387
SEND
+Sending, this might take a while depending on size
Ok file sent</code></pre>

- Rejecting a file after RETR command
<pre><code>RETR longtext1.txt
1176
STOP
+ok, RETR aborted
RETR nonexistant.file
-File doesn't exist
</code></pre>

### STOR
**format:**`STOR { NEW | OLD | APP } file-spec`
<pre><code>NEW: attempts to generate a new file in the server
OLD: overides old file
APP: appends to the old file.</code></pre>
`file-spec` name of file to store in the server. must be located in clients `Client/Files` folder.

Stores a file from the clients file folder to a servers current folder

- Changing to a different directory than default on the server
<pre><code>CDIR Folder2
!Changed working dir to /home/ming/ming-rfc913/Server/Files/Folder2
LIST F
+/home/ming/ming-rfc913/Server/Files/Folder2
| FileName                       |
----------------------------------
| lenaa.tif                      |
| longtext2.txt                  |
| Folder3/..                     |
</code></pre>

- Sending over a file containing
<pre><code>STOR new client1.txt
+File does not exists, will create a new file
+ok, waiting for file
+Saved client1.</code></pre>

- Sending same file over using New tag does not work, but Old will. 
<pre><code>STOR new client1.txt
-File Exists, but system doesn't support generations
STOR old client1.txt
+Will write over old file
+ok, waiting for file
+Saved client1.txt</code></pre>

- Appending to an existing file. The contents of client1.txt is appended to the existing file.
<pre><code>STOR app client1.txt
Will append to file
+ok, waiting for file
+Saved client1.txt</pre></code>
- Trying to save something to root directory of linux, no write permission
<pre><code>CDIR @/
!Changed working dir to /
STOR new client1.txt
+File does not exists, will create a new file
+ok, waiting for file
-Couldn't save because/client1.txt (Permission denied)
</code></pre>
