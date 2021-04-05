package be.vinci.pae.api;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.jersey.server.ContainerRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import be.vinci.pae.api.filters.Authorize;
import be.vinci.pae.api.filters.AuthorizeBoss;
import be.vinci.pae.api.utils.FatalException;
import be.vinci.pae.api.utils.Json;
import be.vinci.pae.api.utils.PresentationException;
import be.vinci.pae.domaine.AddressDTO;
import be.vinci.pae.domaine.DomaineFactory;
import be.vinci.pae.domaine.UserDTO;
import be.vinci.pae.domaine.UserUCC;
import be.vinci.pae.utils.Config;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
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
@Path("/users")
public class UserResource {
  // 86400s = 1 jour (86.400.000 ms).
  private static final long EXPIRATION_TIME = 86400 * 1000;

  private final Algorithm jwtAlgorithm = Algorithm.HMAC256(Config.getProperty("JWTSecret"));
  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Inject
  private UserUCC userUcc;

  @Inject
  private DomaineFactory domaineFactory;


  /**
   * Login the user if exists or send error message.
   * 
   * @param json object containing a username and password.
   * @return a user if user exists in database and matches password.
   */
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(JsonNode json) {
    // Get and check credentials
    if (json.get("username").asText().equals("") && json.get("password").asText().equals("")) {
      throw new PresentationException("Username and password needed", Status.BAD_REQUEST);
    }
    if (json.get("username").asText().equals("")) {
      throw new PresentationException("Username ", Status.BAD_REQUEST);
    }
    if (json.get("password").asText().equals("")) {
      throw new PresentationException("Password needed", Status.BAD_REQUEST);
    }


    UserDTO user = this.userUcc.login(json.get("username").asText(), json.get("password").asText());
    if (!user.isConfirmed()) {
      throw new PresentationException("This account hasn't been confirmed by an admin yet",
          Status.BAD_REQUEST);
    }
    ObjectNode node = createToken(user);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Get the user with an ID if exists or send error message.
   * 
   * @param id id of the user.
   * @return a user if user exists in database and matches the id.
   */
  @GET
  @Path("/{id}")
  @AuthorizeBoss
  public Response getUserById(@PathParam("id") int id) {
    // Check credentials.
    if (id < 1) {
      throw new PresentationException("Id cannot be under 1", Status.BAD_REQUEST);
    }

    UserDTO user = this.userUcc.getUser(id);

    ObjectNode node = createToken(user);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Get the user from an id in a token in header.
   * 
   * @param request header with the token.
   * @return a new token and the user.
   */
  @GET
  @Path("/me")
  @Authorize
  public Response getUser(@Context ContainerRequest request) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");

    if (currentUser == null) {
      throw new PresentationException("User not found", Status.BAD_REQUEST);
    }
    ObjectNode node = createToken(currentUser);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }


  /**
   * Create a token and a ObjectNode with an user.
   * 
   * @param user : the user to put in the token.
   * @return ObjectNode contains the token and the user filter.
   */
  private ObjectNode createToken(UserDTO user) {
    // Create token
    String token;
    try {
      token = JWT.create().withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
          .withIssuer("auth0").withClaim("user", user.getID()).sign(this.jwtAlgorithm);
    } catch (Exception e) {
      throw new FatalException("Unable to create token", e);
    }

    // Build response
    // load the user data from a public JSON view to filter out the private info not
    // to be returned by the API (such as password)
    UserDTO publicUser = Json.filterBossJsonView(user, UserDTO.class);
    return jsonMapper.createObjectNode().put("token", token).putPOJO("user", publicUser);
  }

  /**
   * register a user if correct parameters are sent.
   * 
   * @param json object containing user information and address.
   * @return ok if user has been inserted or an exception.
   */
  @POST
  @Path("/register")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(JsonNode json) {
    if (json.get("username").asText().equals("")) {
      throw new PresentationException("Username is needed ", Status.BAD_REQUEST);
    }
    if (json.get("email").asText().equals("")) {
      throw new PresentationException("email is needed", Status.BAD_REQUEST);
    }
    if (json.get("password").asText().equals("")) {
      throw new PresentationException("password is needed ", Status.BAD_REQUEST);
    }
    if (json.get("lastname").asText().equals("")) {
      throw new PresentationException("Lastname is needed ", Status.BAD_REQUEST);
    }
    if (json.get("firstname").asText().equals("")) {
      throw new PresentationException("Firstname is needed ", Status.BAD_REQUEST);
    }
    if (json.get("street").asText().equals("")) {
      throw new PresentationException("street is needed ", Status.BAD_REQUEST);
    }
    if (json.get("building_number").asText().equals("")) {
      throw new PresentationException("building number is needed ", Status.BAD_REQUEST);
    }
    if (json.get("postcode").asText().equals("")) {
      throw new PresentationException("postcode is needed ", Status.BAD_REQUEST);
    }
    if (json.get("commune").asText().equals("")) {
      throw new PresentationException("commune is needed ", Status.BAD_REQUEST);
    }
    if (json.get("country").asText().equals("")) {
      throw new PresentationException("country is needed ", Status.BAD_REQUEST);
    }
    if (json.get("unit_number").asText().equals("")) {
      throw new PresentationException("unit number is needed ", Status.BAD_REQUEST);
    }

    UserDTO user = domaineFactory.getUserDTO();

    user.setUserName(json.get("username").asText());
    user.setFirstName(json.get("firstname").asText());
    user.setLastName(json.get("lastname").asText());
    user.setEmail(json.get("email").asText());
    user.setPassword(json.get("password").asText());

    AddressDTO addressDTO = domaineFactory.getAdressDTO();

    addressDTO.setBuildingNumber(json.get("building_number").asText());
    addressDTO.setCommune(json.get("commune").asText());
    addressDTO.setPostCode(json.get("postcode").asText());
    addressDTO.setStreet(json.get("street").asText());
    addressDTO.setUnitNumber(json.get("unit_number").asText());
    addressDTO.setCountry(json.get("country").asText());
    LocalDateTime now = LocalDateTime.now();
    Timestamp timestamp = Timestamp.valueOf(now);
    user.setRegistrationDate(timestamp);
    userUcc.register(user, addressDTO);

    return Response.ok().build();
  }



  /**
   * get all users.
   * 
   * @return list of all users.
   */
  @GET
  @AuthorizeBoss
  public Response allUsers() {
    List<UserDTO> listUsers = new ArrayList<UserDTO>();
    listUsers = userUcc.getAll();

    ObjectNode node = jsonMapper.createObjectNode().putPOJO("list", listUsers);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * get all users.
   * 
   * @return list of all users.
   */
  @GET
  @Path("/notConfirmed")
  @AuthorizeBoss
  public Response allUsersConfirmed() {
    List<UserDTO> listUsers = new ArrayList<UserDTO>();
    listUsers = userUcc.getAllNotConfirmed();

    ObjectNode node = jsonMapper.createObjectNode().putPOJO("list", listUsers);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }


  /**
   * update confirmation.
   * 
   * @return list of all users.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @AuthorizeBoss
  public Response updateConfirmed(JsonNode json) {
    int userId = json.get("userId").asInt();
    boolean isBoss = json.get("isBoss").asBoolean();
    boolean antiqueDealer = json.get("isAntiqueDealer").asBoolean();
    boolean confirmed = json.get("isConfirmed").asBoolean();
    UserDTO user = domaineFactory.getUserDTO();
    user.setID(userId);
    user.setBoss(isBoss);
    user.setConfirmed(confirmed);
    user.setAntiqueDealer(antiqueDealer);
    this.userUcc.updateConfirmed(user);
    return Response.ok(MediaType.APPLICATION_JSON).build();
  }

}
