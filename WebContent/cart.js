document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll('.quality-selector').forEach(selector => {
        const minusBtn = selector.querySelector('.minus-btn');
        const plusBtn = selector.querySelector('.plus-btn');
        const quantity = selector.querySelector('.quantity');

        let currentQuantity = parseInt(quantity.textContent);

        minusBtn.addEventListener('click', () => {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantity.textContent = currentQuantity.toString();
            }
            console.log("MINUS");
        });

        plusBtn.addEventListener('click', () => {
            currentQuantity++;
            quantity.textContent = currentQuantity.toString();
            console.log("PLUS!");
        });
    });
});


/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);
    // let item_list = $("#item_list");
    // // change it to html list
    // let res = "<ul>";
    // for (let i = 0; i < resultArray.length; i++) {
    //     // each item will be in a bullet point
    //     res += "<li>" + resultArray[i]["movie_id"] + "</li>";
    //     res += "<li>" + resultArray[i]["movie_title"] + "</li>";
    // }
    // res += "</ul>";

    // clear the old array and show the new array in the frontend
    // item_list.html("");
    // item_list.append(res);

    let shoppingCartElement = jQuery("#shopping_cart_body");

    for (let i = 0; i < resultArray.length; i++) {
        let rowHTML = "<tr id=\""+resultArray[i]["movie_id"]+"\">";

        rowHTML += "<th id=\"table_cell\"><a href='single-movie.html?id=" + resultArray[i]["movie_id"] + "'>" +
        resultArray[i]["movie_title"] + "</a></th>";

        rowHTML += "<th><div class=\"quality-selector\">\n" +
            "        <button class=\"minus-btn\" onclick=\"down(this)\">-</button>\n" +
            "        <span class=\"quantity\">"+resultArray[i]["count"]+"</span>\n" +
            "        <button class=\"plus-btn\" onclick=\"up(this)\">+</button>\n" +
            "      </div></th>";

        // rowHTML += "<th><input type=\"button\" value=\"Delete\"></th>";
        rowHTML += "<th><button onclick=\"deleteRow(this)\" type=\"button\" class=\"delete-button\">Delete</button></th>"

        rowHTML += "<th>$83.29</th>";

        rowHTML += "<th>$100.00</th>"

        rowHTML += "</tr>";
        shoppingCartElement.append(rowHTML);
    }
}

function deleteRow(evt) {
    var rowId = $(evt).closest('tr').attr('id');

    $.ajax("api/cart?movie_id=" + rowId.toString(), {
        method: "PUT",
    });
    const deleteIndex = evt.parentElement.parentElement.rowIndex;
    document.getElementById("cart_table").deleteRow(deleteIndex);
}

function minusRow(evt) {
    const deleteIndex = evt.parentElement.parentElement.parentElement.rowIndex; //this line is literally the only difference
    document.getElementById("cart_table").deleteRow(deleteIndex);
}

function down(evt) {
    var rowId = $(evt).closest('tr').attr('id');

    $.ajax("api/cart?movie_id=" + rowId.toString() + "&count=true", {
        method: "PUT",
    });

    var currentQuantity = document.getElementById(rowId).getElementsByTagName('th')[1].getElementsByClassName("quantity")[0].innerHTML;
    if (parseInt(currentQuantity) > 1) {
        document.getElementById(rowId).getElementsByTagName('th')[1].getElementsByClassName("quantity")[0].innerHTML = (parseInt(currentQuantity) - 1).toString();
    } else {
        minusRow(evt);
    }
}

function up(evt) {
    var rowId = $(evt).closest('tr').attr('id');
    var rowTitle = $(evt).closest('tr').html();

    $.ajax("api/cart", {
        method: "POST",
        data: {"movie_id": rowId.toString()},
    });

    var currentQuantity = document.getElementById(rowId).getElementsByTagName('th')[1].getElementsByClassName("quantity")[0].innerHTML;
    document.getElementById(rowId).getElementsByTagName('th')[1].getElementsByClassName("quantity")[0].innerHTML = (parseInt(currentQuantity) + 1).toString();
}

$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});