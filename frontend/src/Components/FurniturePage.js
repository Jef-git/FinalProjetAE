import { RedirectUrl } from "./Router.js";
import Navbar from "./Navbar.js";
import { setUserSessionData } from "../utils/session.js";
import { API_URL } from "../utils/server.js";
import Sidebar from "./SideBar.js";

let page = document.querySelector("#content");

const FurniturePage = async () => {
  Sidebar(true);

  fetch(API_URL + "furnitures/allFurnitures", {
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
        return response.json().then((data) => onFurnitureList(data));
    })
};

const onFurnitureList = (data) => {
  if (!data) return;

  var table = `
          <div class="input-group rounded" id="search_furniture_list">
            <input type="search" class="form-control rounded" placeholder="Search" aria-label="Search"
              aria-describedby="search-addon" />
            <span class="input-group-text border-0" id="search-addon">
              <i class="fas fa-search"></i>
            </span>
          </div>
          <nav id="nav_furniture">
            <ul class="list-group">`;
  data.list.forEach(element => {
    table += `
        <li id="${element.furnitureId}" class="list-group-item" data-toggle="collapse"
              href="#collapse${element.furnitureId}" role="button"
              aria-expanded="false" aria-controls="collapse${element.furnitureId}">
                <div class="row" id="${element.furnitureId}" >
                  <div class="col-sm-4" id="${element.furnitureId}">
                    <img src="assets/Images/Bureau_1.png" class="rounded" style="width:100%;"/>
                  </div>
                  <div class="col-sm-">
                    <p>
                      <h5>${element.furnitureTitle}</h5>
                      Type : ${element.type}
                    </p>
                  </div>
                </div>
        </li>`;
  });

  table += `  
        </ul>
      </nav>
           
      
`;
      
  page.innerHTML = table

  
  const viewFurnitures = document.querySelectorAll("li");
  viewFurnitures.forEach((elem) =>{
    elem.addEventListener("click", onClick);
  })
}
 
const onClick = (e) => {
    console.log("onClick");
    console.log(e.target.parentElement.parentElement.id);
    const furnitureId = e.target.parentElement.parentElement.id;
  
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
          return response.json().then((data) => onFurnitureDescription(data));
      })

      
};

const onFurnitureDescription = (data) => {
  console.log(data.furniture);
  let table = `
  <div id="description_furniture">
    <h4>${data.furniture.furnitureTitle}</h4>
    <img src="assets/Images/Bureau_1.png" style="width:25%;"/>
    <p>Type : ${data.furniture.type} </br>
       State : ${data.furniture.state}
         </p>
  </div>`;

  page.innerHTML += table;
};


const onError = (err) => {
  let messageBoard = document.querySelector("#messageBoardForm");
  messageBoard.innerHTML = err;
};

export default FurniturePage;