Project 1:
https://youtu.be/hxcgVetsnco

Project 2:
https://youtu.be/_0b5ynjj2tw

Credits: All Me

LIKE/ILIKE USAGE:
for none concatenated columns, typically like "where title like '%term%'"
or for char "where title like 'A%'"

for concatenated columns (i.e genre and stars)
"having genre_names like '%Action%'"
or "having stars like '%tom%'"


Project 3:
https://youtu.be/yai91yO90k8

Credits: All Me

Brief, optimization report:
I used the "load data" command in mysql while directly putting the files into the
"secure_file_priv" folder location found using "show variables like 'secure_file_priv'".

I also used hashing with previous entries that have been added in the .xml files we are
parsing so as to avoid duplicate entries. I also didn't have to load all of the data
of the original table into memory since the ids looked extremely different and were in
various different forms. If the ids had a chance of looking similar, then I would've
loaded them into memory to check for duplicates.

Pre-optimization would take around 10 seconds in total on local machine.
Post-optimization would be around 4 seconds in total.

Incosistency reports:
casts_error_log.txt
mains_error_log.txt

Prepared Statement Files:
LoginServlet
EmployeeLoginServlet
MoviesServlet
SingleMovieServlet
SingleStarServlet
AddMovieServlet

Project 4:
https://youtu.be/_MaczAkGIFM

Credits: All Me

Task 2:
Had to edit WebContent/META-INF/context.xml
<Resource> tags with name:
jdbc/moviedb
and with url:
"jdbc:mysql://<ip>:3306/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"
where <ip> is localhost
and with factory:
"org.apache.tomcat.jdbc.pool.DataSourceFactory"
and with attributes:
maxTotal="100" maxIdle="30" maxWaitMillis="10000"
to my <Resource> tags on top of the original attributes
also added <resource-ref> tags to web.xml to include both master and slave

PreparedStatements:
AddMovieServlet
AddStarServlet
EmployeeLoginServlet
LoginServlet
MoviesServlet
SearchServlet
SingleMovieServlet
SingleStarServlet

Task 4, Sub-task 1:
Same thing as task 2 except added added jdbc/master and jdbc/slave
with same attributes where <ip> for jdbc/master is my private ipv4 address for my master instance (instance 2)
and <ip> for jdbc/slave is localhost
Had to add two other <resource-ref> tags in web.xml to account for jdbc/master and jdbc/slave