import { getTokenSessionDate, getUserSessionData } from "../utils/session.js";
import { RedirectUrl } from "./Router.js";
import { user_me } from "../index.js";
import { API_URL, ALERT_BOX} from "../utils/server.js";
let sideBar = document.querySelector("#sideBar");
let movingRow = document.querySelector("#movingRow");

const Sidebar = (needed,secondNeeded) => {
  fetch(API_URL + "types/", {
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
      return response.json().then((data) => AfficherSidebar(needed,secondNeeded,data));
  });

} 

const AfficherSidebar = (needed, secondNeeded,data) => {
  if(!needed) {
      // Remove padding.
      movingRow.className = "row justify-content-center mt-5";
      return (sideBar.innerHTML = "");
  }

  // Add padding.
  //movingRow.className.replace("noPaddingForSideBar", "smoothTransition");
  movingRow.className = "row justify-content-center mt-5 smoothTransition";

  let sidebar = "";

  // Add new navbar on the left if is boss
  let user = getUserSessionData();
  if(user && user.isBoss){
    sidebar += `
    <div class="onLeft">
      <div class="navbar navbar-nav ml-auto mr-auto pt-3">
        <a id="/confirmUser" class="btn btn-info mb-1 samebutton" href="#">List of inscriptions</a>
        <a class="btn btn-info mb-1 samebutton" href="#" id="/userList">List of clients</a>
        <a class="btn btn-info mb-1 samebutton" href="#" id="/addFurniture">Add furniture</a>
        <a class="btn btn-info mb-1 samebutton" href="#" id="/confirmVisits">Confirm visits</a>
        <a class="btn btn-info mb-1 samebutton" href="#" id="/visitListPage">List of visits</a>
      </div>
    </div>`;
  }

  if(secondNeeded){
    // SideBar Content.
    sidebar += `<!-- SideBar -->
    <div id="mySidenav" class="`;
    //if(user && user.isBoss) sidebar += `sidenav`;
    //else sidebar += `sidenavBoss`;
    sidebar += `sidenav`;
    sidebar += `">
      <form class="mb-5 pb-4">
        <input type="search" class="form-control rounded" placeholder="Search" id="search" />
          <div class="form-check">
            <input type="radio" name="choice" id="-1" class="radio" checked> 
            <label for="type" class="form-check-label">Tout</label>
          </div>`;
      data.types.forEach(element => {
        sidebar+=`<div class="form-check">
            <input type="radio" name="choice" id="${element.typeId}" class="radio"> 
            <label for="type" class="form-check-label">
            ${element.name}
            </label>
        </div>`;
      });
     
     sidebar +=`
     <label for="min">Min Price</label>
     <input type="number" id="minPrice" name="min" value="0">
     <label for="quantity">Max Price</label>
     <input type="number" id="maxPrice" name="max" value="1000">
    `;

    sidebar +=`
    <button type="submit" id="rechercher" class="btn btn-primary">Rechercher</button>
    </form></div>
    `;
    // Button for SideBar.
    sidebar += `
    <!-- Button for SideBar -->
    <div id="mySidenavButton" class="transitionButton">
      <a class="closebtn" onclick='
        let i = document.getElementById("mySidenav").style.width;
        if(!i) i = "0px";
        if(i != "0px"){
            //Close
            document.getElementById("mySidenav").style.width = "0px";
            document.getElementById("mySidenavButton").style.marginLeft = "0px";
            i--;
        }else{
            // Open
            document.getElementById("mySidenav").style.width = "300px";
            document.getElementById("mySidenavButton").style.marginLeft = "300px";
            i++;
        }
    '>&#9776;</a>
    </div><div class="pb-4"></div>`;
  }

  sideBar.innerHTML = sidebar;
  
  // Change position of sidebar if is boss
  if(secondNeeded && user && user.isBoss){
    let mySidenav = document.querySelector("#mySidenav");
    let mySidenavButton = document.querySelector("#mySidenavButton");

    mySidenav.className += " patron";
    mySidenavButton.className += " patron";
  }

  // Create listener.
  if(user && user.isBoss){
    document.getElementById("/confirmUser").addEventListener("click", onTest);
    document.getElementById("/userList").addEventListener("click", onTest);
    document.getElementById("/addFurniture").addEventListener("click", onTest);
    document.getElementById("/confirmVisits").addEventListener("click", onTest);
    document.getElementById("/visitListPage").addEventListener("click", onTest);
  }
  if(secondNeeded){
    document.getElementById("rechercher").addEventListener("click", function(e){
      e.preventDefault();
      getListMeuble(data.types)});
  }
};

const getListMeuble = (types) =>{
  let search =document.getElementById("search").value;
  let min = document.getElementById("minPrice").value;
  let max = document.getElementById("maxPrice").value;
  let type = -1;
  let checked;
  for(let i =0;i<types.length;i++){
    checked = document.getElementById(types[i].typeId).checked;
    if(checked){
      type = types[i].typeId;
      break;
    }
  };

  let specifications ={
    "minPrice" : min,
    "maxPrice" : max,
    "type" : type,
    "searchBar" : search
  }
  let id = getTokenSessionDate();
  if(id){
    fetch(API_URL + "furnitures/searchFurniture", {
      method: "POST",
      body: JSON.stringify(specifications),
      headers: {
        "Content-Type": "application/json",
        "Authorization": id
      },
    })
    .then((response) => {
      if (!response.ok) {
        return response.text().then((err) => onError(err));
      }
      else
        return response.json().then((data) => onFurnitureList(data, types));
    });
  }else{
    fetch(API_URL + "furnitures/searchFurniture", {
      method: "POST",
      body: JSON.stringify(specifications),
      headers: {
        "Content-Type": "application/json",
      },
    })
    .then((response) => {
      if (!response.ok) {
        return response.text().then((err) => onError(err));
      }
      else
        return response.json().then((data) => onFurnitureList(data, types));
    });
  }
}
  
const onFurnitureList = (data, types) => {
  let furnitureList = document.querySelector("#list"); 
  if (!data) return;
  if (data.list.length == 0) {
    list.innerHTML = "<h3> There aren't any furnitures with these specifications </h3>";
    return;
  }
  let table = `
  <nav id="nav_furniture">
  <ul class="list-group">`;
  let furnitures = data.list;
  let photos = data.photos;
  for (let i = 0; i < furnitures.length; i++) {
    table += `
      <li id="${furnitures[i].furnitureId}" class="list-group-item" data-toggle="collapse"
      href="#collapse${furnitures[i].furnitureId}" role="button"
      aria-expanded="false" aria-controls="collapse${furnitures[i].furnitureId}">
        <div class="row" id="${furnitures[i].furnitureId}" >
          <div class="col-sm-4" id="${furnitures[i].furnitureId}">`;
          if(photos[i]) table += `<img src="${photos[i].picture}" alt="${photos[i].name}" class="rounded" style="width:100%;"/>`;
          table += `</div>
          <div class="col-sm-">
            <p>
              <h5>${furnitures[i].furnitureTitle}</h5>
              Type : `;
              types.forEach(type => {
                if(type.typeId == furnitures[i].type) table += `${type.name}`;
              });
            table += `</p>
          </div>
        </div>
      </li>`;
  }

  table += `  
        </ul>
      </nav>
    `;
  furnitureList.innerHTML = table;

  const viewFurnitures = document.querySelectorAll("li");
  viewFurnitures.forEach((elem) => {
    elem.addEventListener("click", function (e) { onClick(e, types); });
  });
}

const onClick = (e, types) => {
  let furnitureId = -1;
    if(e.target.id){
      furnitureId = e.target.id;
    }else {
      furnitureId = e.target.parentElement.parentElement.id;
    }

    if(furnitureId == 'nav_furniture') return;
    if(furnitureId == null ) return;
  
    fetch(API_URL + "furnitures/" + furnitureId, {
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
        return response.json().then((data) => onFurnitureDescription(data, types));
    });
};

const onFurnitureDescription = (data, types) => {
  let info = document.querySelector("#furnitureDesc");

  let description = `
  <div id="description_furniture">
    <h4>${data.furniture.furnitureTitle}</h4>
    <div id="showImg"></div>
    <p>Type : ${types[data.furniture.type].name} </br>
       State : ${data.furniture.state}
         </p>
         <span id="optionform"></span>
         <span id="updateForm"></span>
     
  </div>`;

  info.innerHTML = description; 

  let photos = data.photos;
  let showImg = document.getElementById('showImg');
  photos.forEach(photo => {
    showImg.innerHTML += `<img class="width-15" src="` + photo.picture + `" alt="First slide" >`;
  });

  const user = getUserSessionData();
  if(user.isBoss){
    let updateFurniture = document.querySelector("#updateForm");
    updateFurniture.innerHTML += `<form class="btn" id="updateB">
    <input id="id" value="${data.furniture.furnitureId}" hidden>
    <input class="btn-primary" type="submit" value="Update">
   </form>`;
   let updateButton = document.querySelector("#updateB");
   updateButton.addEventListener("submit", onUpdate);
  }

  if(user && data.furniture.state === "EV") {
    let divOption = document.querySelector("#optionform");
    divOption.innerHTML+= `<form class="btn" id="option">
    <input id="idOption" value="${data.furniture.furnitureId}" hidden>
    <input class="btn-primary" type="submit" value="Introduce option">
    </form>`;
    let optionButton = document.querySelector("#option");
    optionButton.addEventListener("submit", onOption);
  }else if(user && data.furniture.state === "O"){
    let id = getTokenSessionDate();
    fetch(API_URL + "options/"+ data.furniture.furnitureId, {
    method: "GET",
    headers: {
        "Content-Type": "application/json",
        "Authorization":id
    },
    })
    .then((response) => {
    if (!response.ok) {
        return;
    }
    else
        return response.json().then((data) => showStopOptionButton(data));
    });
}
};

const onUpdate = (e) => {
  e.preventDefault();
  let furnitureId = document.getElementById("id").value;
  user_me.furnitureId = furnitureId;
  RedirectUrl(`/updateFurniture`);
}

const onTest = (e) => {
  e.preventDefault();
  RedirectUrl(document.activeElement.id);
}

const onOption = (e) => {
  e.preventDefault();
  let furnitureId = document.getElementById("idOption").value;
  user_me.furnitureId = furnitureId;
  RedirectUrl(`/introduceOption`);
};

const showStopOptionButton = (data) => {
  let divOption = document.querySelector("#optionform");
  divOption.innerHTML +=`<form class="btn" id="option">
  <input id="furnitureID" value="${data.option.furniture}" hidden>
  <input id="optionID" value="${data.option.id}" hidden>
  <input class="btn-primary" type="submit" value="Stop option">
  </form>`;
  let optionButton = document.querySelector("#option");
  optionButton.addEventListener("submit", stopOption);
};

const stopOption = (e) => {
  e.preventDefault();
  let id =getTokenSessionDate();
  let furnitureID = document.getElementById("furnitureID").value;
  let optionID = document.getElementById("optionID").value;
  let option ={
    "furnitureID":furnitureID,
    "optionID":optionID,
  }
  fetch(API_URL + "options/", {
    method: "PUT",
    body: JSON.stringify(option),
    headers: {
      "Content-Type": "application/json",
      "Authorization":id
    },
  })
  .then((response) => {
      if (!response.ok) {
          return response.text().then((err) => onError(err));
      }
      else{
          alert("The option on this furniture has been stopped.");
          RedirectUrl(`/furniture`);
      }
  });
};

const onError = (err) => {
  let messageBoard = document.querySelector("#messageBoard");
  if(err.message) ALERT_BOX(messageBoard, err.message);
  else ALERT_BOX(messageBoard, err);
};

export default Sidebar;