LiveData transformations test

 Test for flow: user sets id, observed by repo loader which sets data. Data is observed to provide name and email.

     user changes: [id] -switchmap-> repo loads: [data] --map-> [name]
                                                       \-map-> [email]
