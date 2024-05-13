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

Brief, optimization report:
I used the "load data" command in mysql while directly putting the files into the
"secure_file_priv" folder location found using "show variables like 'secure_file_priv'".

I also used hashing with previous entries that have been added in the .xml files we are
parsing so as to avoid duplicate entries. I also didn't have to load all of the data
of the original table into memory since the ids looked extremely different and were in
various different forms. If the ids had a chance of looking similar, then I would've
loaded them into memory to check for duplicates.