function setting_parameters() {

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/metadata", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMetadataResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

function handleMetadataResult(resultData) {
    console.log("handleResult: populating metadata tables...");

    let metadataTables = jQuery("#metadata_tables");

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";

        rowHTML += "<div><h3 class=\"table_title\">"+resultData[i]["TableName"]+"</h3><table>";

        rowHTML += "<tr><th>Attribute</th><th>Type</th></tr>";

        let fieldArray = resultData[i]["Fields"].split(",");
        let typeArray = resultData[i]["Types"].split(",");

        for (let j = 0; j < fieldArray.length - 1; j++) {
            rowHTML += "<tr><th>"+fieldArray[j]+"</th><th>"+typeArray[j]+"</th></tr>"
        }

        rowHTML += "</table></div>";

        metadataTables.append(rowHTML);
    }
}

setting_parameters();