import { getUserSessionData, getTokenSessionDate } from "../utils/session";
import Sidebar from "./SideBar";
import { RedirectUrl } from "./Router.js";
import Navbar from "./Navbar.js";
import { API_URL, ALERT_BOX } from "../utils/server.js";
import { user_me } from "..";

const STATES = [ "ER", "M", "EV", "O", "V", "EL", "L", "AE", "E", "R", "RE" ];

let page = document.querySelector("#page");

const ModificationFurniturePage = () => {    
    Sidebar(true, false);

    const user = getUserSessionData();
    if (!user || !user.isBoss || !user_me.furnitureId) {
        Navbar();
        RedirectUrl("/");
    } else {
        // Fetch for get all informations of a specifique Furniture.
        fetch(API_URL + "furnitures/infosUpdate/" + user_me.furnitureId, {
            method: "GET", 
            headers: {
              "Content-Type": "application/json",
            },
        })
        .then((response) => {
            if (!response.ok) {
            return response.text().then((err) => onError(err));
            }
            else
            return response.json().then((data) => onPageCreate(data));
        });
}
};

const onPageCreate = (data) => {
    let furniture = data.furniture;
    let types = data.types;
    let users = data.users;
    let photos = data.photos;
    let photosFurnitures = data.photosFurnitures;
    let option = data.option;

    //console.log(photos, photosFurnitures);

    // Modification of all Timestamp into Date.
    // Furniture date collection.
    if(furniture.furnitureDateCollection){
        furniture.furnitureDateCollection = createTimeStamp(furniture.furnitureDateCollection);
    }
    // Deposit date.
    if(furniture.depositDate){
        furniture.depositDate = createTimeStamp(furniture.depositDate);
    }
    // Date of sale.
    if(furniture.dateOfSale){
        furniture.dateOfSale = createTimeStamp(furniture.dateOfSale);
    }
    // Sale withdrawal date.
    if(furniture.saleWithdrawalDate){
        furniture.saleWithdrawalDate = createTimeStamp(furniture.saleWithdrawalDate);
    }
    // Pick-up date.
    if(furniture.pickUpDate){
        furniture.pickUpDate = createTimeStamp(furniture.pickUpDate);
    }
    
    let modifPage = `
    <form id="update" class="form-inline pb-5" enctype="multipart/form-data">
        <input id="furnitureId" value="${furniture.furnitureId}" hidden>
        <div class="row">
            <div class="col-sm-6 bg-info">
                <label for="title">Titre: </label>
                <input type="text" class="form-control" id="title" value="${furniture.furnitureTitle}" placeholder="Enter title" name="title" required`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="state">Etat: </label>
                <div class="form-group">
                    <select class="form-control" id="state" name="state"`;
                    if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                    modifPage += `>`;
                    if(furniture.state == "ER"){
                        modifPage += `<option value="ER"`;
                        if(furniture.state == "ER") modifPage += ` selected`;
                        modifPage += `>En restauration</option>`;
                    }
                    if(furniture.state == "ER" || furniture.state == "M"){
                        modifPage += `<option value="M"`;
                        if(furniture.state == "M") modifPage += ` selected`;
                        modifPage += `>En magasin</option>`;
                    }
                    if(furniture.state == "M" || furniture.state == "EV" || furniture.state == "O"){
                        modifPage += `<option value="EV"`;
                        if(furniture.state == "EV") modifPage += ` selected`;
                        modifPage += `>En vente</option>`;
                    }
                    if(/*furniture.state == "EV" || */furniture.state == "O"){
                        modifPage += `<option value="O"`;
                        if(furniture.state == "O") modifPage += ` selected`;
                        modifPage += `>Sous option</option>`;
                    }
                    if(furniture.state == "ER" || furniture.state == "M" || furniture.state == "EV" 
                    || furniture.state == "O" || furniture.state == "V"){
                        modifPage += `<option value="V"`;
                        if(furniture.state == "V") modifPage += ` selected`;
                        modifPage += `>Vendu</option>`;
                    }
                    /*if(furniture.state == "EL"){
                        modifPage += `<option value="EL"`;
                        if(furniture.state == "EL") modifPage += ` selected`;
                        modifPage += `>En livraison</option>`;
                    }*/
                    /*if(furniture.state == "L"){
                        modifPage += `<option value="L"`;
                        if(furniture.state == "L") modifPage += ` selected`;
                        modifPage += `>Livré</option>`;
                    }*/
                    /*if(furniture.state == "AE"){
                        modifPage += `<option value="AE"`;
                        if(furniture.state == "AE") modifPage += ` selected`;
                        modifPage += `>a emporté</option>`;
                    }*/
                    /*if(furniture.state == "E"){
                        modifPage += `<option value="E"`;
                        if(furniture.state == "E") modifPage += ` selected`;
                        modifPage += `>Emporté</option>`;
                    }*/
                    /*if(furniture.state == "R"){
                        modifPage += `<option value="R"`;
                        if(furniture.state == "R") modifPage += ` selected`;
                        modifPage += `>Reservé</option>`;
                    }*/
                    if(furniture.state == "EV" || furniture.state == "RE"){
                        modifPage += `<option value="RE"`;
                        if(furniture.state == "RE") modifPage += ` selected`;
                        modifPage += `>Retiré</option>`;
                    }
                    modifPage += `</select>
                </div>`;
                if(furniture.state == "O" && option){
                    modifPage += `<p><u>Option from:</u> `;
                    users.forEach(user => { 
                        if(user.id == option.customer) modifPage += `<strong>${user.username}</strong>`; 
                    });
                    modifPage += `</p>`;
                }
                modifPage += `<label for="pickUpDate">Pick-up date: </label>`;
                modifPage += `<input type="datetime-local" class="form-control" id="pickUpDate"`;
                if(furniture.pickUpDate) modifPage += ` value="${furniture.pickUpDate}"`;
                modifPage += ` name="pickUpDate" step="1" required`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="seller">Vendeur: </label>
                <div class="form-group">
                    <select class="form-control" id="seller" name="seller"`;
                    if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                    modifPage += `>`;
                    users.forEach(user => {
                        modifPage += `<option value="${user.id}"`;
                        if(furniture.seller == user.id) modifPage += ` selected`;
                        modifPage += `>${user.username}</option>`;
                    });
                    modifPage += `
                    </select>
                </div>
                <label for="type">Type: </label>
                <div class="form-group">
                    <select class="form-control" id="type" name="type"`;
                    if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                    modifPage += `>`;
                        types.forEach(type => {
                            modifPage += `<option value="${type.typeId}"`;
                            if(furniture.type == type.typeId) modifPage += ` selected`;
                            modifPage += `>${type.name}</option>`;
                        });
                        modifPage += `
                    </select>
                </div>
                <br>
                <input type="file" id="files" name="files" multiple`;
                modifPage += `>
                <p class="text-muted">* Veuillez selectionner toutes les photos en une seule fois.</p>
                <div id="showImg"></div>`;
                    modifPage += `<button type="submit" name="submitPhoto" class="btn btn-primary mb-2"><i class="fas fa-save"></i></button>`;
                modifPage += `<div class="card-columns">`;
                for (let i = 0; i < photos.length; i++) {
                    //console.log(photos[i]);
                    modifPage += `<div class="card" style="width: 90px">
                        <input id="photoId" value="${photos[i].id}" hidden>
                        <img class="card-img-top max_width" src="` + photos[i].picture + `" alt="` + photos[i].name +`" />`;
                  
                        modifPage += `<div class="card-body">
                                <button id="${photos[i].id}" type="submit" name="delettePhoto" class="btn btn-danger">
                                    <i class="material-icons">delete</i>
                                </button>
                                <button id="${photos[i].id}" type="submit" name="favoritePhoto" class="btn btn-light">
                                    <i class="`;
                                    if(photosFurnitures[i].favourite) modifPage += `fas fa-heart`;
                                    else modifPage += `far fa-heart`;
                                modifPage += `" style="color:red"></i>
                                </button>
                                <button id="${photos[i].id}" type="submit" name="visiblePhoto" class="btn btn-light">
                                    <i class="fas fa-eye`;
                                    if(!photosFurnitures[i].visible) modifPage += `-slash`;
                                modifPage += `"></i>
                                </button>
                            </div>`;
                    
                    modifPage += `</div>`;
                };
                modifPage += `</div>
            </div>
            <div class="col-sm-6 bg-warning">`;
                modifPage += `<label for="depositDate">Date de dépot: </label>
                <div class="input-group"><input type="datetime-local" class="form-control" id="depositDate" `;
                if(furniture.depositDate) modifPage += ` value="${furniture.depositDate}"`;
                modifPage += `" name="depositDate" step="1"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `><button id="depositDateButton"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `><i class="material-icons">delete</i></button>
                </div>
                <!--<label for="furnitureDateCollection">Date emporter: </label>-->
                <input type="datetime-local" class="form-control" id="furnitureDateCollection"`;
                if(furniture.furnitureDateCollection) modifPage += ` value="${furniture.furnitureDateCollection}"`;
                modifPage += ` name="furnitureDateCollection" step="1" hidden`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="dateOfSale">Date de vente: </label>
                <div class="input-group"><input type="datetime-local" class="form-control" id="dateOfSale"`;
                if(furniture.dateOfSale) modifPage += ` value="${furniture.dateOfSale}"`;
                modifPage += ` name="dateOfSale" step="1"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `><button id="dateOfSaleButton"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `><i class="material-icons">delete</i></button>
                </div>`;
                if(furniture.state == "EV" || furniture.state == "RE") modifPage += `<label for="saleWithdrawalDate">Date de retrait: </label>
                <div class="input-group">`;
                modifPage += `<input type="datetime-local" class="form-control" id="saleWithdrawalDate"`;
                if(furniture.saleWithdrawalDate) modifPage += ` value="${furniture.saleWithdrawalDate}"`;
                modifPage += ` name="saleWithdrawalDate" step="1"`;
                if(furniture.state != "EV" && furniture.state != "RE") modifPage += ` hidden`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>`;
                if(furniture.state == "EV") modifPage += `<button id="saleWithdrawalDateButton"><i class="material-icons">delete</i></button>
                </div>`;
                modifPage += `<label for="purchasePrice">Prix d'achat: </label>
                <input type="number" class="form-control" id="purchasePrice" value="${furniture.purchasePrice}" placeholder="Enter purchase Price" name="purchasePrice" required`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="sellingPrice">Prix de vente: </label>
                <input type="number" class="form-control" id="sellingPrice" value="${furniture.sellingPrice}" placeholder="Enter selling Price" name="sellingPrice"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="specialSalePrice">Prix antiquaire: </label>
                <input type="number" class="form-control" id="specialSalePrice" value="${furniture.specialSalePrice}" placeholder="Enter special Sale Price" name="specialSalePrice"`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>
                <label for="buyer">Acheteur: </label>
                <div class="form-group">
                    <select class="form-control" id="buyer" name="buyer"`;
                    if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                    modifPage += `>
                        <option value="0">Nobody</option>`;
                        users.forEach(user => {
                            modifPage += `<option value="${user.id}"`;
                            if(furniture.buyer == user.id) modifPage += ` selected`;
                            modifPage += `>${user.username}</option>`;
                        });
                        modifPage += `
                    </select>
                </div>
                <!--<label for="delivery">Livraison: </label>-->
                <input type="number" class="form-control" id="delivery" value="${furniture.delivery}" placeholder="Enter delivery" name="delivery" hidden`;
                if(furniture.state == "V" || furniture.state == "RE") modifPage += ` disabled`;
                modifPage += `>`;
                if(furniture.state != "V" && furniture.state != "RE") modifPage += `<input type="submit" name="submitUpdate" value="update" class="btn btn-lg btn-primary btn-block">
            </div>
        </div>
    </form><div>`;

    page.innerHTML = modifPage;
    let form = document.querySelector("#update");
    form.addEventListener("submit", onSubmit);
    let uploadImage = document.querySelector("#files");
    uploadImage.addEventListener("change", onUpload);

    // Set remove buttons.
    let depositDateButton = document.querySelector("#depositDateButton");
    depositDateButton.addEventListener("click", onRemoveDepositDate);
    let dateOfSaleButton = document.querySelector("#dateOfSaleButton");
    dateOfSaleButton.addEventListener("click", onRemoveDateOfSale);
    if(furniture.state == "EV") {
        let saleWithdrawalDateButton = document.querySelector("#saleWithdrawalDateButton");
        saleWithdrawalDateButton.addEventListener("click", onRemoveSaleWithdrawalDate);
    }
}

const onUpload = (e) => {
    let files = e.target.files;
    
    // Reset visuel
    document.getElementById('showImg').innerHTML = "";

    // Add visuel and 
    for(let i = 0; i < files.length; i++){
        let reader = new FileReader();
        reader.onloadend = function() {
            document.getElementById('showImg').innerHTML += `<img id="blah" src="` 
            + reader.result + `" style="width: 100px" alt="` + files[i].name.substr(0, files[i].name.length - 4) + `" />`;
        }
        reader.readAsDataURL(files[i]);
    }
}

const onRemoveDepositDate = () => {
    document.querySelector("#depositDate").value = "";
}

const onRemoveDateOfSale = () => {
    document.querySelector("#dateOfSale").value = "";
}

const onRemoveSaleWithdrawalDate = () => {
    document.querySelector("#saleWithdrawalDate").value = "";
}

const onSubmit = (e) => {
    e.preventDefault();

    let activeElement = document.activeElement;
    if(activeElement.name == "submitPhoto") onSubmitPhoto();
    else if(activeElement.name == "delettePhoto") onDelettePhoto(activeElement.id);
    else if(activeElement.name == "favoritePhoto") onFavoritePhoto(activeElement.id, activeElement);
    else if(activeElement.name == "visiblePhoto") onVisiblePhoto(activeElement.id, activeElement);
    else if(activeElement.name == "submitUpdate") onSubmitUpdate();
}

const onSubmitPhoto = () => {
    const input = document.getElementById('files');

    const formData = new FormData();
    for(let i = 0; i < input.files.length; i++){
        //console.log(input.files[i]);
        formData.append("photo"+i, input.files[i]);
    }
    //console.log("Formdata: ", formData, "get('photo0'): ", formData.get("photo0"));

    let furnitureId = document.getElementById("furnitureId").value;
    let id = getTokenSessionDate();
    fetch(API_URL + "photos/uploadPhotosFurniture", {
        method: "POST", 
        body: formData, 
        headers: {
            "Authorization": id,
            "furnitureId": furnitureId,
        },
    })
    .then((response) => {
        if (!response.ok) {
          return response.text().then((err) => onError(err));
        }
        else
          return response.json().then((data) => RedirectUrl("/updateFurniture"));
    });
}

const onDelettePhoto = (photoId) => {
    //console.log("Delette Photo",photoId);
    let furnitureId = document.getElementById("furnitureId").value;
    let id = getTokenSessionDate();
    fetch(API_URL + "photos/" + photoId, {
        method: "DELETE",
        headers: {
            "Authorization": id,
            "furnitureId": furnitureId,
        },
    })
    .then((response) => {
        if (!response.ok) {
          return response.text().then((err) => onError(err));
        }
        else
          return response.json().then((data) => RedirectUrl("/updateFurniture"));
    });
}

const onFavoritePhoto = (photoId, e) => {
    //console.log("Favorite Photo",photoId);
    let furnitureId = document.getElementById("furnitureId").value;

    // Change in is favourite or in is not favourite.
    let favoriteButton = e.querySelector("i");
    //console.log(favoriteButton);
    let isFavorite = false;
    if (favoriteButton.className == "fas fa-heart") {
        favoriteButton.className = "far fa-heart";
        isFavorite = false;
        //console.log("not anymore favorite");
    } else if (favoriteButton.className == "far fa-heart") {
        favoriteButton.className = "fas fa-heart";
        isFavorite = true;
        //console.log("now is favorite");
    }

    let photo = {
        "photoId": photoId,
        "furnitureId": furnitureId,
        "isFavorite": isFavorite,
    };
    //console.log(photo);

    let id = getTokenSessionDate();
    fetch(API_URL + "photos/favorite", {
        method: "PUT",
        body: JSON.stringify(photo),
        headers: {
            "Content-Type": "application/json",
            "Authorization": id,
        },
    })
    .then((response) => {
        //console.log(response);
        if (!response.ok) {
          return response.text().then((err) => onError(err));
        }
        else
          return response.json().then((data) => RedirectUrl("/updateFurniture"));
    });
}

const onVisiblePhoto = (photoId, e) => {
    //console.log("Visible Photo",photoId);
    let furnitureId = document.getElementById("furnitureId").value;

    // Change in is visible or in is not visible.
    let visibilityButton = e.querySelector("i");
    //console.log(visibilityButton);
    let isVisible = false;
    if (visibilityButton.className == "fas fa-eye") {
        visibilityButton.className = "fas fa-eye-slash";
        isVisible = false;
        //console.log("not anymore visible");
    } else if (visibilityButton.className == "fas fa-eye-slash") {
        visibilityButton.className = "fas fa-eye";
        isVisible = true;
        //console.log("now is visible");
    }

    let photo = {
        "photoId": photoId,
        "furnitureId": furnitureId,
        "isVisible": isVisible,
    };
    //console.log(photo);

    let id = getTokenSessionDate();
    fetch(API_URL + "photos/visible", {
        method: "PUT",
        body: JSON.stringify(photo),
        headers: {
            "Content-Type": "application/json",
            "Authorization": id,
        },
    })
    .then((response) => {
        if (!response.ok) {
          return response.text().then((err) => onError(err));
        }
        else
          return response.json().then((data) => RedirectUrl("/updateFurniture"));
    });
}

const onSubmitUpdate = () => {
    let furnitureId = document.getElementById("furnitureId").value;
    let title = document.getElementById("title").value;
    let type = document.getElementById("type").value;
    let buyer = document.getElementById("buyer").value;
    let purchasePrice = document.getElementById("purchasePrice").value;
    let furnitureDateCollection = document.getElementById("furnitureDateCollection").value;
    let sellingPrice = document.getElementById("sellingPrice").value;
    let specialSalePrice = document.getElementById("specialSalePrice").value;
    let delivery = document.getElementById("delivery").value;
    let state = document.getElementById("state").value;
    let depositDate = document.getElementById("depositDate").value;
    let dateOfSale = document.getElementById("dateOfSale").value;
    let saleWithdrawalDate = document.getElementById("saleWithdrawalDate").value;
    let seller = document.getElementById("seller").value;
    let pickUpDate = document.getElementById("pickUpDate").value;

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Verifier qu'il ne peux pas revenir en arriere genre de en vente vers en restauration.
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // Required field.
    if(!furnitureId || !title || !state || !purchasePrice || !seller 
        || !pickUpDate || !type) {
        let err = { message: 'Something required is missing.' };
        return onError(err);
    }

    // Check if state is correct.
    if(!STATES.find(e => e == state)) {
        let err = { message: "The state is incorrect." };
        return onError(err);
    }

    // Check about depositDate.
    if(depositDate && state == "ER") {
        let err = { message: "You cant have a deposit date if the state is en restauration." };
        return onError(err);
    }
    if(state != "ER" && !depositDate) {
        let err = { message: "You need a deposit date if the furniture is not anymore en restauration." };
        return onError(err);
    }

    // Check about put up for sale.
    if(sellingPrice && sellingPrice != "0" && (state == "ER" || state == "M")) {
        let err = { message: "You cant have a selling price if the state is en restauration or en magasin." };
        return onError(err);
    }
    if((!sellingPrice || sellingPrice == "0") && state != "ER" && state != "M") {
        let err = { message: "You need a selling price if the furniture is not anymore en restauration or en magasin." };
        return onError(err);
    }
    if(sellingPrice < 0) {
        let err = { message: "You cant have a negative selling price." };
        return onError(err);
    }

    // Check for buyer.
    if((!buyer || buyer == "0") && (state == "V" || state == "EL" || state == "L" || state == "AE" || state == "E" 
    || state == "R")) {
        let err = { message: "You need a buyer if the state is sold, on delivery, "
        + "delivered, to go, taken away or reserved." };
        return onError(err);
    }
    if(buyer && buyer != "0" && state != "V" && state != "EL" && state != "L" && state != "AE" 
    && state != "E" && state != "R") {
        let err = { message: ("You cant have a buyer if the state is not vendu or "
        + "not en livraison or not livre or not emporte or not reserve.") };
        return onError(err);
    }
    if(buyer && buyer != "0" && !dateOfSale) {
        let err = { message: "You need a date of sale if a buyer is specify." };
        return onError(err);
    }

    // Check for delivery
    if(delivery && delivery != "0" && (!buyer|| buyer == "0")) {
        let err = { message: "You need a buyer if the delivery is specify." };
        return onError(err);
    }
    if((!delivery || delivery == "0") && (state == "EL" || state == "L")) {
        let err = { message: "Delivery is needed if the state is on delivery or delivered." };
        return onError(err);
    }

    // Check for take away.
    if((state == "AE" || state == "E") && !furnitureDateCollection) {
        let err = { message: "Furniture date collection is needed if the state is"
        + " to go or take away." };
        return onError(err);
    }

    // Check about special sale.
    if(specialSalePrice && specialSalePrice != "0" && (state == "ER" || state == "M" 
    || state == "EV" || state == "O" || state == "RE")) {
        let err = { message: ("You cant have a special sale price if the state is "+
        "on restoration, in shop, on sale, under option or withdraw.") };
        return onError(err);
    }
    if(specialSalePrice && specialSalePrice != "0" && (!buyer || buyer == "0")) {
        let err = { message: "You need a buyer if the special price is specify." };
        return onError(err);
    }
    if(specialSalePrice < 0) {
        let err = { message: "You cant have a negative special sale price." };
        return onError(err);
    }

    // Check for withdraw.
    if(!saleWithdrawalDate && state == "RE") {
        let err = { message: "Sale Withdrawal Date is needed if the state is withdraw." };
        return onError(err);
    }
    if(saleWithdrawalDate && state != "RE") {
        let err = { message: "The state need to be withdraw if a sale withdrawal date is specify." };
        return onError(err);
    }

    let furniture = {
      "furnitureId": furnitureId,
      "title": title,
      "type": type,
      "buyer": buyer,
      "purchasePrice": purchasePrice,
      "furnitureDateCollection": furnitureDateCollection,
      "sellingPrice": sellingPrice,
      "specialSalePrice": specialSalePrice,
      "delivery": delivery,
      "state": state,
      "depositDate": depositDate,
      "dateOfSale": dateOfSale,
      "saleWithdrawalDate": saleWithdrawalDate,
      "seller": seller,
      "pickUpDate": pickUpDate,
    };
    //console.log(furniture);
  
    let id = getTokenSessionDate();
    fetch(API_URL + "furnitures", {
      method: "PUT", 
      body: JSON.stringify(furniture), 
      headers: {
        "Content-Type": "application/json",
        "Authorization": id,
      },
    })
      .then((response) => {
        if (!response.ok) {
          return response.text().then((err) => onError(err));
        }
        else
          return response.json().then((data) => onFurnitureUpdate(data));
      });
};
  
const onFurnitureUpdate = (furnitureData) => {
    RedirectUrl("/");
};
  
const onError = (err) => {
    let messageBoard = document.querySelector("#messageBoard");
    if(err.message) ALERT_BOX(messageBoard, err.message);
    else ALERT_BOX(messageBoard, err);
};

const createTimeStamp = (dateString) => {
    let Timestamp = new Date(dateString);
    let timeSplit = Timestamp.toLocaleString().split("/");
    return timeSplit[2].substr(0, 4) + "-" + timeSplit[1] + "-" + timeSplit[0] + "T" + Timestamp.toLocaleTimeString();
}
 
export default ModificationFurniturePage;