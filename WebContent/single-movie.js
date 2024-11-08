/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

function handleCartInfo(movieId) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    // cartEvent.preventDefault();

    $.ajax("api/cart", {
        method: "POST",
        data: {"movieId": movieId},
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            // handleCartArray(resultDataJson["previousItems"]);
        }
    });
    document.getElementById("overlay").style.display = "block";
    document.getElementById("popupDialog").style.display = "block";
}

function closeFn() {
    document.getElementById("overlay").style.display = "none";
    document.getElementById("popupDialog").style.display = "none";
}


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    let movieTitleElement = jQuery("#single-movie-title");
    movieTitleElement.append(resultData[0]["movie_title"]);
    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>" + resultData[0]["movie_title"] + " (" + resultData[0]["movie_year"] + ")</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        const list_of_genres = resultData[i]["movie_genres"].split(",");

        rowHTML += "<th>";
        for (let j = 0; j < list_of_genres.length; j++) {
            rowHTML += '<a href="movielist.html?genre=' + list_of_genres[j] + '">'
                + list_of_genres[j] + '</a>';
            if (j !== (list_of_genres.length - 1)) {
                rowHTML += ", "
            }
        }
        rowHTML += "</th>";

        const list_of_stars = resultData[i]["movie_stars"].split(",");
        const list_of_stars_ids = resultData[i]["stars_ids"].split(",");
        rowHTML += "<th>"
        for (let j = 0; j < list_of_stars.length; j++) {
            rowHTML += '<a href="single-star.html?id=' + list_of_stars_ids[j] + '">'
                + list_of_stars[j] + '</a>';
            if (j !== (list_of_stars.length - 1)) {
                rowHTML += ", "
            }
        }
        rowHTML += "</th>";


        let movie_rating = "N/A";
        if (resultData[i]["movie_rating"] !== null) {
            movie_rating = resultData[i]["movie_rating"]
        }
        rowHTML += "<th>" + movie_rating + "</th>";

        rowHTML += "<th>"+ "<input type='button' value='Add' onclick='handleCartInfo(\" " + resultData[i]["movie_id"] + " \");' />" + "</th>"

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});