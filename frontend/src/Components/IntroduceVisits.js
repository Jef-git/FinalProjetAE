import { RedirectUrl } from "./Router.js";
import { API_URL } from "../utils/server.js";
import { getUserSessionData, getTokenSessionDate } from "../utils/session";
import Sidebar from "./SideBar.js";
import Navbar from "./Navbar.js";

let introduceVisits = `
<div class="containerForm">
	<div class="d-flex justify-content-center h-100">
		<div class="card">
			<div class="card-header">
				<h3>
					<center>Introduce a visit</center>
				</h3>
			</div>
			<div class="card-body">
				<div class="row" id = "flexbox">
					<div class="column">
						<h5>
							<center>Concerned funitures
								</center>
								</h5>
								<hr>
								<input type="file" id="files" name="files" multiple>
								<p class="text-muted">*Please select all photos at once.</p>
							  <div id="showImg"></div>
							  <br>
						  </div>
						  <div id="user" class="col-sm"></div>
									<div class="column">
										<h5>
											<center>Location of the furnitures 
												</center>
												</h5>
												<hr>
													<form>
														<div class="row">
															<div class="col-sm">
																<div class="input-group form-group">
																	<div class="input-group-prepend">
																		<span class="input-group-text">
																			<i class="fas fa-road"></i>
																		</span>
																	</div>
																	<input type="text" class="form-control" id="street" placeholder="Street">
																	</div>
																</div>
																<div class="col-sm">
																	<div class="input-group form-group">
																		<div class="input-group-prepend">
																			<span class="input-group-text">
																				<i class="fa fa-address-book" aria-hidden="true"></i>
																			</span>
																		</div>
																		<input type="text" class="form-control" id="building_number" placeholder="Number">
																		</div>
																	</div>
																</div>
																<div class="input-group form-group">
																	<div class="input-group-prepend">
																		<span class="input-group-text">
																			<i class="fa fa-map-pin" aria-hidden="true"></i>
																		</span>
																	</div>
																	<input type="text" class="form-control" id="postcode" placeholder="Postal code">
																	</div>
																	<div class="row">
																		<div class="col-sm">
																			<div class="input-group form-group">
																				<div class="input-group-prepend">
																					<span class="input-group-text">
																						<i class="fas fa-flag"></i>
																					</span>
																				</div>
																				<input type="text" class="form-control" id="country" placeholder="Country">
																				</div>
																			</div>
																			<div class="col-sm">
																				<div class="input-group form-group">
																					<div class="input-group-prepend">
																						<span class="input-group-text">
																							<i class="fas fa-envelope"></i>
																						</span>
																					</div>
																					<input type="text" class="form-control" id="unit_number" placeholder="Post box">
																					</div>
																				</div>
																			</div>
																			<hr>
																				<div class="row">
																					<div class="col-sm">
																						<div class="input-group form-group">
																							<div class="input-group-prepend">
																								<span class="input-group-text">
																									<i class="fas fa-city"></i>
																								</span>
																							</div>
																							<input type="text" class="form-control" id="commune" placeholder="Commune">
																							</div>
																						</div>
																						<div class="col-sm">
																							<div class="input-group form-group">
																								<div class="input-group-prepend">
																									<span class="input-group-text">
																										<i class="fas fa-info"></i>
																									</span>
																								</div>
																								<textarea class="form-control" placeholder="Label furniture"  id="label_furniture" ></textarea>
																							</div>
																						</div>
																						<div class="col-sm">
																							<div class="input-group form-group">
																								<div class="input-group-prepend">
																									<span class="input-group-text">
																										<i class="fas fa-handshake"></i>
																									</span>
																								</div>
																								<textarea class="form-control" placeholder="Time slot of availability"  id="time_slot" ></textarea>
																							</div>
																						</div>
																					</div>
																					<div class="form-group">
																						<input type="submit" value="Send request" class="btn btn-lg btn-outline-primary btn-block">
																						</div>
																					</form>
																				</div>
																			</div>
																			<div class="card-footer">
																				<div class="d-flex justify-content-center"></div>
																				<div id="messageBoardForm"></div>
																			</div>
																		</div>
																	</div>
																</div>`;


const IntroduceVisits = () => {
	const user = getUserSessionData();
	if (!user) {
		Navbar();
		RedirectUrl(`/`);
	}else{
		let page = document.querySelector("#page");
		page.innerHTML = introduceVisits;
		if(user.isBoss){

			let id = getTokenSessionDate();
			fetch(API_URL + "users/", {
				method: "GET",
				headers: {
					"Content-Type": "application/json",
					"Authorization": id,
				},
			}).then((response) => {
				if (!response.ok) {
					return response.text().then((err) => onError(err));
				} else return response.json().then((data) => onCreatePage(data));
			});
		}else{
			onCreatePage();
		}
 	}
};

const onCreatePage = (data) => {
	Sidebar(true, false);

	let users = document.getElementById("user");
	const userSession = getUserSessionData();
	if(data){
		let input = ` <div class="form-group">
		<select class="form-control" id="user_id" name="user_id">`;
		data.list.forEach(user => {
			input += `<option value="${user.id}"`;
			if(userSession.id == user.id) input += ` selected`;
			input += `>${user.username}</option>`;
		});
		input += `
			</select>
		</div>`;
		users.innerHTML = input;
	}else{
		users.innerHTML = `<input id="user_id" value="-1" hidden>`;
	}

	let introduceVisitsForm = document.querySelector("form");
	introduceVisitsForm.addEventListener("submit", onIntroduceVisits);
	let uploadImage = document.querySelector("#files");
	uploadImage.addEventListener("change", onUpload);
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

const onIntroduceVisits = (e) => {
  e.preventDefault();

  let visit = {
    street: document.getElementById("street").value,
    building_number: document.getElementById("building_number").value,
    postcode: document.getElementById("postcode").value,
    commune: document.getElementById("commune").value,
    country: document.getElementById("country").value,
    unit_number: document.getElementById("unit_number").value,
    time_slot: document.getElementById("time_slot").value,
    label_furniture: document.getElementById("label_furniture").value,
	user_id: document.getElementById("user_id").value
  };
  let id = getTokenSessionDate();
  fetch(API_URL + "visits/introduceVisits", {
    method: "POST",
    body: JSON.stringify(visit),
    headers: {
      "Content-Type": "application/json",
      Authorization: id,
    },
  }).then((response) => {
    if (!response.ok) {
      return response.text().then((err) => onError(err));
    } else return response.json().then((data) => onVisitAdded(data));
  });
};

const onVisitAdded = (data) => {
	let visitId = data.visit.id;
	//console.log(data,visitId);

	const input = document.getElementById('files');
	const formData = new FormData();
	for(let i = 0; i < input.files.length; i++){
		formData.append("photo"+i, input.files[i]);
	}

	let id = getTokenSessionDate();

	fetch(API_URL + "photos/uploadPhotosVisit", {
		method: "POST",
		body: formData,
		headers:{
			"Authorization": id,
			"visitId": visitId,
		}
	})
	.then((response) => {
		if (!response.ok) {
			return response.text().then((err) => onDeleteVisit(visitId));
		}
		else
			return response.json().then((data) => onVisitRequest());
	});
};

const onVisitRequest = () => {
  alert("Your request has been sent. An admin must now confirm your visit.");
  RedirectUrl("/");
};

const onDeleteVisit = (visitId) => {
	let id = getTokenSessionDate();
	fetch(API_URL + "visits/delete/" + visitId, {
		method: "DELETE",
		headers:{
			"Authorization": id,
		}
	})
	.then((response) => {
		return onError("Missing photos.");
	});
}

const onError = (err) => {
  let messageBoard = document.querySelector("#messageBoardForm");
  messageBoard.innerHTML = err;
};

export default IntroduceVisits;
