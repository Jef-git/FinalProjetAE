import { RedirectUrl } from "./Router.js";
import { getTokenSessionDate } from "../utils/session";
import { API_URL } from "../utils/server.js";
import Sidebar from "./SideBar.js";

let page = document.querySelector("#page");

const VisitListPage = () => {
  Sidebar(true);

  let list = `
  <div class="containerForm">
<div class="d-flex justify-content-center h-100 mt-4">
  <div class="card">
    <div class="card-header">
  <div class="col-sm-3" id="list"> </div>
  </div>
  </div>
  <div class="col-sm-3"  id="confirmVisitDesc"></div>
  <div id="messageBoardForm"></div>
  </div>
  </div>
  `;
  let id = getTokenSessionDate();
  page.innerHTML = list;
  fetch(API_URL + "visits/confirmed", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: id,
    },
  }).then((response) => {
    if (!response.ok) {
      return response.text().then((err) => onError(err));
    } else return response.json().then((data) => onVisitList(data));
  });
};

const onVisitList = (data) => {
  if (!data) return;
  showVisitList(data.users, data.visits);
};

const showVisitList = (users, visits) => {
  let visitList = document.querySelector("#list");
  let table = `
    <div class="input-group rounded" id="search_visit_list">
  <input type="search" class="form-control rounded" placeholder="Search" aria-label="Search"
    aria-describedby="search-addon" />
  <span class="input-group-text border-0" id="search-addon">
    <i class="fas fa-search"></i>
  </span>
  </div>
  <nav id="nav_user">
  <ul class="list-group">`;
  data.list.forEach((element) => {
    table += `
    <li id="${element.id}" class="list-group-item" data-toggle="collapse"
    href="#collapse${element.id}" role="button"
    aria-expanded="false" aria-controls="collapse${element.id}">
      <div class="row" id="${element.id}" >
        <div class="col-sm-">
          <p>
            <h5>${element.username}</h5>
           
          </p>
        </div>
      </div>
    </li>`;
  });

  table += `  
        </ul>
      </nav>
`;
  userList.innerHTML = table;

  const viewUsers = document.querySelectorAll("li");
  viewUsers.forEach((elem) => {
    elem.addEventListener("click", onClick);
  });
};

const onClick = (e) => {
  e.preventDefault();
  const visit = e.target.parentElement.parentElement.id;
  if (visit == "nav_user") return;

  if (visit == null) return;

  let id = getTokenSessionDate();
  fetch(API_URL + "visits/" + visit, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: id,
    },
  }).then((response) => {
    if (!response.ok) {
      return response.text().then((err) => onError(err));
    } else
      return response.json().then((data) => onConfirmVisitDescription(data));
  });
};

const onConfirmVisitDescription = (data) => {
  let id = getTokenSessionDate();
  fetch(API_URL + "users/" + data.visit.userId, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: id,
    },
  }).then((response) => {
    if (!response.ok) {
      return response.text().then((err) => onError(err));
    } else
      return response.json().then((obj) => visitDescription(obj.user, data));
  });
};
const visitDescription = (user, data) => {
  data.visit.requestDate = createTimeStamp(data.visit.requestDate);
  let info = document.querySelector("#confirmVisitDesc");
  let description = `
  <a href="#" class="previous">&laquo; Previous</a>
  <table id="description_user" class="table table-striped table-bordered" style="width:100%" >
  <thead>
            <tr>
                <th>Username</th>
                <th>Lastname</th>
                <th>Firstname</th>
                <th>Email</th> 
                <th>Request Date</th>  
                <th>Time slot</th> 
                <th>Explanatory note</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>${data.user.username}</td>
                <td>${data.user.lastName}</td>
                <td>${data.user.firstName}</td>
                <td>${data.user.email}</td>
                <td>${data.visit.requestDate}</td>
                <td>${data.visit.timeSlot}</td>
                <td>${data.visit.explanatoryNote}</td>
          
            </tr>
       </tbody>
  </table>
    `;

  info.innerHTML = description;
};

const onError = (err) => {
  let messageBoard = document.querySelector("#messageBoardForm");
  messageBoard.innerHTML = err;
};

const createTimeStamp = (dateString) => {
  let Timestamp = new Date(dateString);
  let timeSplit = Timestamp.toLocaleString().split("/");
  return (
    timeSplit[2].substr(0, 4) +
    "-" +
    timeSplit[1] +
    "-" +
    timeSplit[0] +
    " " +
    Timestamp.toLocaleTimeString()
  );
};

export default VisitListPage;
