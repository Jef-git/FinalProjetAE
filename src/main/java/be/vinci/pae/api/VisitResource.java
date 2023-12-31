package be.vinci.pae.api;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.jersey.server.ContainerRequest;
import com.fasterxml.jackson.databind.JsonNode;
import be.vinci.pae.api.filters.Authorize;
import be.vinci.pae.api.filters.AuthorizeBoss;
import be.vinci.pae.api.utils.PresentationException;
import be.vinci.pae.api.utils.ResponseMaker;
import be.vinci.pae.domaine.DomaineFactory;
import be.vinci.pae.domaine.address.AddressDTO;
import be.vinci.pae.domaine.photo.PhotoDTO;
import be.vinci.pae.domaine.user.UserDTO;
import be.vinci.pae.domaine.user.UserUCC;
import be.vinci.pae.domaine.visit.VisitDTO;
import be.vinci.pae.domaine.visit.VisitUCC;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
@Path("/visits")
public class VisitResource {

  @Inject
  private VisitUCC visitUcc;

  @Inject
  private UserUCC userUcc;

  @Inject
  private DomaineFactory domaineFactory;


  /**
   * Get the visit with an ID if exists or send error message.
   * 
   * @param id id of the visit.
   * @return a visit if visit exists in database and matches the id.
   */
  @GET
  @Path("/{id}")
  @AuthorizeBoss
  public Response getVisitById(@PathParam("id") int id) {
    // Check credentials.
    if (id < 1) {
      throw new PresentationException("Id cannot be under 1", Status.BAD_REQUEST);
    }

    Object[] listOfAll = this.visitUcc.getAllInfosOfVisit(id);
    PhotoResource.transformAllURLOfThePhotosIntoBase64Image((List<PhotoDTO>) listOfAll[1]);

    int i = 0;
    return ResponseMaker.createResponseWithObjectNodeWith3PutPOJO("visit", listOfAll[i++], "photos",
        listOfAll[i++], "photosVisit", listOfAll[i++]);
  }

  /**
   * introduce visit if correct parameters are sent.
   * 
   * @param json object containing user information and address.
   * @return ok if user has been inserted or an exception.
   */
  @POST
  @Path("/introduceVisits")
  @Consumes(MediaType.APPLICATION_JSON)
  @Authorize
  public Response introduceVisit(@Context ContainerRequest request, JsonNode json) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");
    if (currentUser == null) {
      throw new PresentationException("You dont have the permission.", Status.BAD_REQUEST);
    }

    checkJsonAddress(json);

    if (!json.hasNonNull("time_slot") || json.get("time_slot").asText().equals("")) {
      throw new PresentationException("time slot is needed ", Status.BAD_REQUEST);
    }
    VisitDTO visit = domaineFactory.getVisitDTO();

    LocalDateTime dateNow = LocalDateTime.now();
    visit.setRequestDate(Timestamp.valueOf(dateNow));
    visit.setTimeSlot(json.get("time_slot").asText());
    visit.setLabelFurniture(json.get("label_furniture").asText());

    AddressDTO addressDTO = domaineFactory.getAdressDTO();
    addressDTO = createFullFillAddress(addressDTO, -1, json.get("building_number").asText(),
        json.get("commune").asText(), json.get("postcode").asText(), json.get("street").asText(),
        json.get("unit_number").asText(), json.get("country").asText());

    if (currentUser.isBoss()) {
      int id = json.get("user_id").asInt();
      if (id < 1 || userUcc.getUser(id) == null) {
        throw new PresentationException("User doesn't exist.", Status.BAD_REQUEST);
      }
      visit = visitUcc.introduceVisit(visit, addressDTO, id);
    } else {
      visit = visitUcc.introduceVisit(visit, addressDTO, currentUser.getID());
    }

    return ResponseMaker.createResponseWithObjectNodeWith1PutPOJO("visit", visit);
  }

  /**
   * get all visits.
   * 
   * @return list of all visits.
   */
  @GET
  @AuthorizeBoss
  public Response allVisits() {
    List<VisitDTO> listVisits = new ArrayList<VisitDTO>();
    listVisits = visitUcc.getAll();

    return ResponseMaker.createResponseWithObjectNodeWith1PutPOJO("list", listVisits);
  }

  /**
   * get all my visits.
   * 
   * @return list of all visits.
   */
  @GET
  @Path("/myVisits")
  @Authorize
  public Response allMyVisits(@Context ContainerRequest request) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");
    if (currentUser == null) {
      throw new PresentationException("User not found", Status.BAD_REQUEST);
    }

    List<VisitDTO> listVisits = new ArrayList<VisitDTO>();
    listVisits = visitUcc.getAllMyVisits(currentUser.getID());

    return ResponseMaker.createResponseWithObjectNodeWith1PutPOJO("list", listVisits);
  }

  /**
   * get all visits not confirmed.
   * 
   * @return list of all visits not confirmed with list of users.
   */
  @GET
  @Path("/notConfirmed")
  @AuthorizeBoss
  public Response allVisitsNotConfirmed() {
    List<VisitDTO> listVisits = new ArrayList<VisitDTO>();
    listVisits = visitUcc.getAllNotConfirmed();
    List<UserDTO> listUser = new ArrayList<UserDTO>();
    for (VisitDTO visit : listVisits) {
      listUser.add(userUcc.getUser(visit.getUserId()));
    }
    return ResponseMaker.createResponseWithObjectNodeWith2PutPOJO("visits", listVisits, "users",
        listUser);
  }

  /**
   * get all visits.
   * 
   * @return list of all visits.
   */
  @GET
  @Path("/confirmed")
  @AuthorizeBoss
  public Response allVisitsConfirmed() {
    List<VisitDTO> listVisits = new ArrayList<VisitDTO>();
    listVisits = visitUcc.getAllConfirmed();
    List<UserDTO> listUser = new ArrayList<UserDTO>();
    for (VisitDTO visit : listVisits) {
      listUser.add(userUcc.getUser(visit.getUserId()));
    }
    return ResponseMaker.createResponseWithObjectNodeWith2PutPOJO("visits", listVisits, "users",
        listUser);
  }

  /**
   * update confirmation.
   * 
   * @return list of all users.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @AuthorizeBoss
  public Response updateConfirmed(@Context ContainerRequest request, JsonNode json) {
    if (!json.hasNonNull("visitId") || json.get("visitId").asText().equals("")) {
      throw new PresentationException("Visit id is needed ", Status.BAD_REQUEST);
    }

    UserDTO currentUser = (UserDTO) request.getProperty("user");

    if (currentUser == null) {
      throw new PresentationException("User not found", Status.BAD_REQUEST);
    }
    VisitDTO visit = domaineFactory.getVisitDTO();
    visit = visitUcc.getVisit(json.get("visitId").asInt());

    if (json.hasNonNull("isConfirmed")) {
      boolean confirmed = json.get("isConfirmed").asBoolean();
      visit.setIsConfirmed(confirmed);
    }

    if (json.get("dateTime").asText() != "") {

      String term = json.get("dateTime").asText();
      LocalDateTime parsed = LocalDateTime.parse(term);
      visit.setDateAndHoursVisit(Timestamp.valueOf(parsed));
    }


    if (json.get("explanatoryNote").asText() != "" && json.hasNonNull("explanatoryNote")) {
      visit.setExplanatoryNote(json.get("explanatoryNote").asText());
    }

    this.visitUcc.updateConfirmed(visit);
    return Response.ok().build();
  }

  /**
   * delete the visit if it belong to the user requesting it.
   * 
   * @param id of the visit to delete
   * @param request contains the token of the user.
   * @return empty response.
   */
  @DELETE
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Authorize
  public Response delete(@PathParam("id") int id, @Context ContainerRequest request) {
    if (id < 1) {
      throw new PresentationException("Id cannot be under 1", Status.BAD_REQUEST);
    }

    UserDTO currentUser = (UserDTO) request.getProperty("user");
    if (currentUser == null) {
      throw new PresentationException("User not found", Status.BAD_REQUEST);
    }

    VisitDTO visit = visitUcc.getVisit(id);
    if (visit == null || visit.getUserId() != currentUser.getID()) {
      throw new PresentationException("You can't delete this visit.", Status.BAD_REQUEST);
    }

    visitUcc.delete(id);
    return Response.ok().build();
  }



  // ******************** Public static's Methods ********************

  /**
   * Verify json to check address variables.
   * 
   * @param json node with required objects.
   */
  public static void checkJsonAddress(JsonNode json) {
    if (!json.hasNonNull("street") || json.get("street").asText().equals("")) {
      throw new PresentationException("street is needed ", Status.BAD_REQUEST);
    }
    if (!json.hasNonNull("building_number") || json.get("building_number").asText().equals("")) {
      throw new PresentationException("building number is needed ", Status.BAD_REQUEST);
    }
    if (!json.hasNonNull("postcode") || json.get("postcode").asText().equals("")) {
      throw new PresentationException("postcode is needed ", Status.BAD_REQUEST);
    }
    if (!json.hasNonNull("commune") || json.get("commune").asText().equals("")) {
      throw new PresentationException("commune is needed ", Status.BAD_REQUEST);
    }
    if (!json.hasNonNull("country") || json.get("country").asText().equals("")) {
      throw new PresentationException("country is needed ", Status.BAD_REQUEST);
    }
    if (!json.hasNonNull("unit_number") || json.get("unit_number").asText().equals("")) {
      throw new PresentationException("unit number is needed ", Status.BAD_REQUEST);
    }
  }

  /**
   * create a full filled address.
   * 
   * @param addressDTO the address to fill.
   * @param id of the address.
   * @param buildingNumber of the address.
   * @param commune of the address.
   * @param postcode of the address.
   * @param street of the address.
   * @param unitNumber of the address.
   * @param country of the address.
   * @return the address full filled.
   */
  public static AddressDTO createFullFillAddress(AddressDTO addressDTO, int id,
      String buildingNumber, String commune, String postcode, String street, String unitNumber,
      String country) {

    addressDTO.setID(id);
    addressDTO.setBuildingNumber(buildingNumber);
    addressDTO.setCommune(commune);
    addressDTO.setPostCode(postcode);
    addressDTO.setStreet(street);
    addressDTO.setUnitNumber(unitNumber);
    addressDTO.setCountry(country);

    return addressDTO;
  }

}
