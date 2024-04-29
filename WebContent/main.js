/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleGenreBrowseResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let genreBodyElement = jQuery("#genre_body");

    for (let i= 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<li><a href='movielist.html?genre=" + resultData[i]["genre_name"] + "'>" + resultData[i]["genre_name"] + "</a></li>";

        // Append the row created to the table body, which will refresh the page
        genreBodyElement.append(rowHTML);
    }

    let titleBodyElement = jQuery("#title_body");

    let titleRowHTML = "<li>";
    for (let i = 65; i <= 90; i++) {
        titleRowHTML += "<a class = 'browsing_elements' href='movielist.html?char=" + String.fromCharCode(i) + "'>" + String.fromCharCode(i) + "</a>";
    }
    titleRowHTML += "</li><li>";
    for (let i = 0; i <= 10; i++) {
        if (i === 10) {
            titleRowHTML += "<a class = 'browsing_elements' href = 'movielist.html?char=*'>*</a>";
        } else {
            titleRowHTML += "<a class = 'browsing_elements' href = 'movielist.html?char=" + i.toString() + "'>" + i.toString() + "</a>";
        }
    }
    titleRowHTML += "</li>";

    titleBodyElement.append(titleRowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/main", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreBrowseResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});