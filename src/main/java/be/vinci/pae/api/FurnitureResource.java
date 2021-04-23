package be.vinci.pae.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ContainerRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import be.vinci.pae.api.filters.Authorize;
import be.vinci.pae.api.filters.AuthorizeBoss;
import be.vinci.pae.api.utils.PresentationException;
import be.vinci.pae.domaine.DomaineFactory;
import be.vinci.pae.domaine.furniture.FurnitureDTO;
import be.vinci.pae.domaine.furniture.FurnitureUCC;
import be.vinci.pae.domaine.photo.PhotoDTO;
import be.vinci.pae.domaine.photo.PhotoFurnitureDTO;
import be.vinci.pae.domaine.user.UserDTO;
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
@Path("/furnitures")
public class FurnitureResource {

  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Inject
  private FurnitureUCC furnitureUCC;

  @Inject
  private DomaineFactory domaineFactory;

  @Inject
  private UserResource userRessource;

  /**
   * get all furnitures.
   * 
   * @return list of all furnitures.
   */
  @GET
  public Response allFurnitures() {
    List<FurnitureDTO> listFurnitures = new ArrayList<FurnitureDTO>();
    listFurnitures = furnitureUCC.getAll();

    return createResponseWithObjectNodeWith1PutPOJO("list", listFurnitures);
  }

  /**
   * get a clients furniture.
   * 
   * @return list of all the clients furnitures.
   */
  @GET
  @Authorize
  @Path("myFurnitures")
  public Response myFurnitures(@Context ContainerRequest request) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");
    if (currentUser == null || !currentUser.isBoss()) {
      throw new PresentationException("You dont have the permission.", Status.BAD_REQUEST);
    }
    List<FurnitureDTO> listFurnitures = new ArrayList<FurnitureDTO>();
    listFurnitures = furnitureUCC.getMyFurniture(currentUser.getID());

    return createResponseWithObjectNodeWith1PutPOJO("list", listFurnitures);
  }

  /**
   * update a furniture.
   * 
   * @return the furniture updated.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @AuthorizeBoss
  public Response updateFurniture(@Context ContainerRequest request, JsonNode json) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");
    if (currentUser == null || !currentUser.isBoss()) {
      throw new PresentationException("You dont have the permission.", Status.BAD_REQUEST);
    }
    // System.out.println(json);
    // System.out.println(json.get("files").get(0));
    // System.out.println(json.get("formData").get("photo0"));
    // System.out.println(json.get("filesBase64").get(0));

    checkAllCredentialFurniture(json); // pourrais renvoyer le type si besoin en dessous.
    FurnitureDTO furniture = createFullFillFurniture(json);
    List<PhotoDTO> photos = createAllPhotosFullFilled(json);
    PhotoFurnitureDTO photoFurniture = createFullFillPhotoFurniture();

    furniture = furnitureUCC.update(furniture, photos, photoFurniture);

    return createResponseWithObjectNodeWith1PutPOJO("furniture", furniture);
  }

  private void checkAllCredentialFurniture(JsonNode json) {
    // Required Field.
    if (json.get("furnitureId").asText().equals("") || json.get("furnitureId").asInt() < 1) {
      throw new PresentationException("Id is needed or incorrect.", Status.BAD_REQUEST);
    }
    if (json.get("title").asText().equals("")) {
      throw new PresentationException("Title is needed.", Status.BAD_REQUEST);
    }
    if (json.get("state").asText().equals("")) {
      throw new PresentationException("State is needed.", Status.BAD_REQUEST);
    }
    if (json.get("purchasePrice").asText().equals("") || json.get("purchasePrice").asInt() <= 0) {
      throw new PresentationException("Purchase Price is needed or inccorect.", Status.BAD_REQUEST);
    }
    if (json.get("seller").asText().equals("")) {
      throw new PresentationException("Seller is needed.", Status.BAD_REQUEST);
    }
    int sellerId = json.get("seller").asInt();
    if (sellerId < 1 || userRessource.getUserById(sellerId) == null) {
      throw new PresentationException("Seller does not exist.", Status.BAD_REQUEST);
    }
    if (json.get("pickUpDate").asText().equals("")) {
      throw new PresentationException("Pick-up date is needed.", Status.BAD_REQUEST);
    }
    String timestampPattern = "^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$";
    Pattern pattern = Pattern.compile(timestampPattern, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(json.get("pickUpDate").asText());
    if (!matcher.find()) {
      throw new PresentationException("Pick-up date is not matching a Timestamp pattern.",
          Status.BAD_REQUEST);
    }
    if (json.get("type").asText().equals("") || json.get("type").asInt() < 1) {
      throw new PresentationException("Type is needed ", Status.BAD_REQUEST);
    }
    // TODO
    // int typeId = json.get("type").asInt();
    // if (typeId < 1 || getTypeById(typeId) == null) {
    // throw new BusinessException("Type does not exist ", HttpStatus.BAD_REQUEST_400);
    // }


    // Check when the furniture is in restoration.
    String state = json.get("state").asText();
    if (state.equals("ER") && !json.get("depositDate").asText().equals("")) {
      throw new PresentationException(
          "You cant have a deposit date if the state is in restoration.", Status.BAD_REQUEST);
    }


    // Check when the furniture is in the shop.
    if (json.get("depositDate").asText().equals("") && !state.equals("ER")) {
      throw new PresentationException("A deposit date is needed if is not anymore in restoration.",
          Status.BAD_REQUEST);
    }
    // TODO Verifier que depositDate est bien un timestamp.


    // Check when the furniture is put up for sale.
    if ((json.get("sellingPrice").asText().equals("")
        || json.get("sellingPrice").asText().equals("0")) && !state.equals("ER")
        && !state.equals("M")) {
      throw new PresentationException(
          "Selling Price is needed if is not anymore in restoration or in shop.",
          Status.BAD_REQUEST);
    }
    if (!json.get("sellingPrice").asText().equals("")
        && !json.get("sellingPrice").asText().equals("0")
        && (state.equals("ER") || state.equals("M"))) {
      throw new PresentationException(
          "You cant have a selling price if the state is in restoration or in shop.",
          Status.BAD_REQUEST);
    }
    if (!json.get("sellingPrice").asText().equals("") && json.get("sellingPrice").asInt() < 0) {
      throw new PresentationException("You cant have a negative selling price.",
          Status.BAD_REQUEST);
    }


    // Check when the furniture is buy.
    if ((json.get("buyer").asText().equals("") || json.get("buyer").asText().equals("0"))
        && (state.equals("V") || state.equals("EL") || state.equals("L") || state.equals("AE")
            || state.equals("E") || state.equals("R"))) {
      throw new PresentationException("Buyer is needed ", Status.BAD_REQUEST);
    }
    int buyerId = json.get("buyer").asInt();
    if (buyerId != 0 && (buyerId < 1 || userRessource.getUserById(buyerId) == null)) {
      throw new PresentationException("Buyer does not exist ", Status.BAD_REQUEST);
    }
    if (!json.get("buyer").asText().equals("") && !json.get("buyer").asText().equals("0")
        && !state.equals("V") && !state.equals("EL") && !state.equals("L") && !state.equals("AE")
        && !state.equals("E") && !state.equals("R")) {
      throw new PresentationException(
          "You cant have a buyer if the state is not (sold, on delivery, "
              + "delivered, to go, taken away, reserved).",
          Status.BAD_REQUEST);
    }
    if (!json.get("buyer").asText().equals("") && !json.get("buyer").asText().equals("0")
        && json.get("dateOfSale").asText().equals("")) {
      throw new PresentationException("A date of sale is needed if a buyer is specify.",
          Status.BAD_REQUEST);
    }
    // TODO Verifier que dateOfSale est bien un timestamp.
    // TODO Verifier que si il y a un buyer, il y a soit delivery/saleWithdrawalDate.

    // Case if delivery.
    if ((state.equals("EL") || state.equals("EL")) && (json.get("delivery").asText().equals("")
        || json.get("delivery").asText().equals("0"))) {
      throw new PresentationException(
          "Delivery is needed if the state is on delivery or delivered.", Status.BAD_REQUEST);
    }
    // TODO Verifier que delivery est bien un timestamp.

    // Case if takeaway.
    if ((state.equals("AE") || state.equals("E"))
        && json.get("furnitureDateCollection").asText().equals("")) {
      throw new PresentationException(
          "Furniture date collection is needed if the state is to go or take away.",
          Status.BAD_REQUEST);
    }
    // TODO Verifier que furnitureDateCollection est bien un timestamp.

    // Case if antique dealer.
    if (!json.get("specialSalePrice").asText().equals("")
        && json.get("specialSalePrice").asInt() < 0) {
      throw new PresentationException("You cant have a negative special sale price.",
          Status.BAD_REQUEST);
    }
    if (!json.get("specialSalePrice").asText().equals("")
        && !json.get("specialSalePrice").asText().equals("0") && !state.equals("V")
        && !state.equals("EL") && !state.equals("L") && !state.equals("AE") && !state.equals("E")
        && !state.equals("R")) {
      throw new PresentationException(
          "You cant have a special sale price if the state is not (sold, on delivery,"
              + " delivered, to go, taken away, reserved)",
          Status.BAD_REQUEST);
    }
    if (!json.get("specialSalePrice").asText().equals("")
        && !json.get("specialSalePrice").asText().equals("0")
        && (json.get("buyer").asText().equals("") || json.get("buyer").asText().equals("0"))) {
      throw new PresentationException("Buyer is needed if a special sale price is specify.",
          Status.BAD_REQUEST);
    }



    // Check if withdraw.
    if (json.get("saleWithdrawalDate").asText().equals("") && state.equals("RE")) {
      throw new PresentationException("Sale Withdrawal Date is needed if the state is withdraw.",
          Status.BAD_REQUEST);
    }
    if (!json.get("saleWithdrawalDate").asText().equals("") && !state.equals("RE")) {
      throw new PresentationException(
          "The state need to be withdraw if a sale withdrawal date is specify.",
          Status.BAD_REQUEST);
    }
    // TODO Verifier que saleWithdrawalDate est bien un timestamp.
  }

  private FurnitureDTO createFullFillFurniture(JsonNode json) {
    FurnitureDTO furniture = domaineFactory.getFurnitureDTO();

    furniture.setFurnitureId(json.get("furnitureId").asInt());
    furniture.setFurnitureTitle(json.get("title").asText());
    furniture.setType(json.get("type").asInt());
    furniture.setBuyer(json.get("buyer").asInt());
    furniture.setPurchasePrice(json.get("purchasePrice").asLong());

    Timestamp timestamp;
    if (!json.get("furnitureDateCollection").asText().equals("")) {
      timestamp = Timestamp.valueOf(json.get("furnitureDateCollection").asText());
      furniture.setFurnitureDateCollection(timestamp);
    }
    furniture.setSellingPrice(json.get("sellingPrice").asLong());
    furniture.setSpecialSalePrice(json.get("specialSalePrice").asLong());
    furniture.setDelivery(json.get("delivery").asInt());
    furniture.setState(json.get("state").asText());

    if (!json.get("depositDate").asText().equals("")) {
      timestamp = Timestamp.valueOf(json.get("depositDate").asText());
      furniture.setDepositDate(timestamp);
    }

    if (!json.get("dateOfSale").asText().equals("")) {
      timestamp = Timestamp.valueOf(json.get("dateOfSale").asText());
      furniture.setDateOfSale(timestamp);
    }

    if (!json.get("saleWithdrawalDate").asText().equals("")) {
      timestamp = Timestamp.valueOf(json.get("saleWithdrawalDate").asText());
      furniture.setSaleWithdrawalDate(timestamp);
    }
    furniture.setSeller(json.get("seller").asInt());

    if (!json.get("pickUpDate").asText().equals("")) {
      timestamp = Timestamp.valueOf(json.get("pickUpDate").asText());
      furniture.setPickUpDate(timestamp);
    }

    return furniture;
  }

  private List<PhotoDTO> createAllPhotosFullFilled(JsonNode json) {
    if (json.get("filesBase64").size() != json.get("filesName").size()) {
      throw new PresentationException(
          "The number of files is not the same then the number of names.", Status.BAD_REQUEST);
    }

    List<PhotoDTO> photos = new ArrayList<PhotoDTO>();

    int i = 0;
    while (json.get("filesBase64").get(i) != null) {
      if (json.get("filesBase64").get(i).asText().equals("")) {
        throw new PresentationException("A file base64 cannot be empty.", Status.BAD_REQUEST);
      }
      if (json.get("filesName").get(i).asText().equals("")) {
        throw new PresentationException("A name cannot be empty.", Status.BAD_REQUEST);
      }

      String picture = json.get("filesBase64").get(i).asText();
      String name = json.get("filesName").get(i).asText();
      PhotoDTO photo = domaineFactory.getPhotoDTO();

      photo.setPicture(picture);
      photo.setName(name);

      photos.add(photo);

      i++;
    }

    return photos;
  }

  private PhotoFurnitureDTO createFullFillPhotoFurniture() {
    PhotoFurnitureDTO photoFurniture = domaineFactory.getPhotoFurnitureDTO();

    photoFurniture.setVisible(false);
    photoFurniture.setFavourite(false);

    return photoFurniture;
  }

  /**
   * Get the furniture with an ID if exists or send error message.
   * 
   * @param id id of the furniture.
   * @return a furniture if furniture exists in database and matches the id.
   */
  @GET
  @Path("/{id}")
  public Response getFurnitureById(@PathParam("id") int id) {
    // Check credentials.
    if (id < 1) {
      throw new PresentationException("Id cannot be under 1", Status.BAD_REQUEST);
    }
    FurnitureDTO furniture = this.furnitureUCC.findById(id);

    return createResponseWithObjectNodeWith1PutPOJO("furniture", furniture);
  }

  /**
   * get the furniture by is id and all types and users.
   * 
   * @param id id of the furniture.
   * @return list of all types, users and the furniture where the id is the same.
   */
  @GET
  @Path("/infosUpdate/{id}")
  public Response allInfosForUpdateFurniture(@PathParam("id") int id) {
    if (id < 1) {
      throw new PresentationException("Id cannot be under 1", Status.BAD_REQUEST);
    }

    Object[] listOfAll = furnitureUCC.getAllInfosForUpdate(id);

    int i = 0;
    return createResponseWithObjectNodeWith5PutPOJO("furniture", listOfAll[i++], "types",
        listOfAll[i++], "users", listOfAll[i++], "photos", listOfAll[i++], "photosFurnitures",
        listOfAll[i++]);
  }

  /**
   * create a response with a ObjectNode with 1 putPOJO.
   * 
   * @param <E> the type of the object.
   * @param namePOJO the name of the POJO put.
   * @param object object to put.
   * @return a response.ok build with the ObjectNode inside.
   */
  private <E> Response createResponseWithObjectNodeWith1PutPOJO(String namePOJO, E object) {
    ObjectNode node = jsonMapper.createObjectNode().putPOJO(namePOJO, object);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * create a response with a ObjectNode with 3 putPOJO.
   * 
   * @param <E> the type of the first object.
   * @param <F> the type of the second object.
   * @param <G> the type of the third object.
   * @param namePOJO1 the name of the POJO put.
   * @param object1 object to put.
   * @return a response.ok build with all the ObjectNode inside.
   */
  private <E, F, G, H, I> Response createResponseWithObjectNodeWith5PutPOJO(String namePOJO1,
      E object1, String namePOJO2, F object2, String namePOJO3, G object3, String namePOJO4,
      H object4, String namePOJO5, I object5) {
    ObjectNode node =
        jsonMapper.createObjectNode().putPOJO(namePOJO1, object1).putPOJO(namePOJO2, object2)
            .putPOJO(namePOJO3, object3).putPOJO(namePOJO4, object4).putPOJO(namePOJO5, object5);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }


  @POST
  @Path("/test1")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response upload1Photo(@FormDataParam("photo0") InputStream file,
      @FormDataParam("photo0") FormDataContentDisposition fileDisposition) {
    System.out.println("Coucou1");
    System.out.println("InputStream: " + file + "\nFormDataContentDisposition: " + fileDisposition);

    String uploadedFileLocation = "C:\\Ecole Vinci\\projet-ae-groupe-05/"
        + "src/main/resources/photos/" + fileDisposition.getFileName();
    System.out.println(uploadedFileLocation);

    // save it
    writeToFile(file, uploadedFileLocation);


    // Test for return
    File f = new File(uploadedFileLocation);
    String encodstring = "data:image/png;base64," + encodeFileToBase64Binary(f);
    // System.out.println(encodstring.substring(0, 200));

    return createResponseWithObjectNodeWith1PutPOJO("furniture", encodstring);

    /*
     * File test = new File("C:\\Ecole Vinci\\projet-ae-groupe-05/src/main/resources/photos/Bahut_2.png"); System.out.println(test); return
     * createResponseWithObjectNodeWith1PutPOJO("furniture", test);
     */
  }

  @POST
  @Path("/test1B")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  // @Authorize
  public Response uploadMultiplePhotos(final FormDataMultiPart multiPart) {
    System.out.println("Coucou1B");
    System.out.println("Multipart: " + multiPart);
    System.out
        .println("InputStream: " + multiPart.getField("photo0").getValueAs(InputStream.class));
    Map<String, List<FormDataBodyPart>> fields = multiPart.getFields();

    List<String> paths = new ArrayList<>();
    System.out.println(
        "**************************************Keys presente**************************************");
    for (String keyField : fields.keySet()) {
      System.out.println(keyField);
      List<FormDataBodyPart> values = fields.get(keyField);
      for (FormDataBodyPart formDataBodyPart : values) {
        System.out.println(formDataBodyPart.getName());
        System.out.println(formDataBodyPart);
        System.out.println(formDataBodyPart.getValueAs(InputStream.class));
        String fileName =
            formDataBodyPart.getHeaders().get("Content-Disposition").get(0).split(";")[2].substring(
                11, formDataBodyPart.getHeaders().get("Content-Disposition").get(0).split(";")[2]
                    .length() - 1);
        System.out.println("Name : " + fileName);
        String uploadedFileLocation =
            "C:\\Ecole Vinci\\projet-ae-groupe-05/" + "src/main/resources/photos/" + fileName;
        System.out.println(uploadedFileLocation);

        // save it
        writeToFile(formDataBodyPart.getValueAs(InputStream.class), uploadedFileLocation);

        // Test for return
        File f = new File(uploadedFileLocation);
        String encodstring = "data:image/png;base64," + encodeFileToBase64Binary(f);
        paths.add(encodstring);
      }
    }
    System.out.println(
        "**************************************Value**************************************");
    for (List<FormDataBodyPart> value : fields.values()) {
      for (FormDataBodyPart formDataBodyPart : value) {
        System.out.println(formDataBodyPart);
        System.out.println(formDataBodyPart.getName());
        System.out.println(formDataBodyPart.getClass());
        System.out.println(formDataBodyPart.getEntity());
        System.out.println(formDataBodyPart.getHeaders().get("Content-Disposition").size());
        System.out.println("Name : "
            + formDataBodyPart.getHeaders().get("Content-Disposition").get(0).split(";")[2]
                .substring(11,
                    formDataBodyPart.getHeaders().get("Content-Disposition").get(0).split(";")[2]
                        .length() - 1));
        // System.out.println(formDataBodyPart.getValueAs(FormDataContentDisposition.class));
        for (String formDataBodyPart2 : formDataBodyPart.getHeaders().keySet()) {
          System.out.println(formDataBodyPart2);
          System.out.println(formDataBodyPart.getHeaders().get(formDataBodyPart2));
        }
      }
    }

    return createResponseWithObjectNodeWith1PutPOJO("photos", paths);
  }

  @POST
  @Path("/test2")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response test2(final FormDataMultiPart multiPart) {
    System.out.println("Coucou2");
    System.out.println("FormDataMultiPart: " + multiPart);
    System.out.println("Name of field 'photo0': " + multiPart.getField("photo0").getName());
    System.out.println("ValueAs: " + multiPart.getField("photo0").getValueAs(InputStream.class));
    System.out.println("Field: " + multiPart.getField("photo0"));
    System.out.println("Entity: " + multiPart.getField("photo0").getEntity());

    ObjectNode node = jsonMapper.createObjectNode().putPOJO("photo",
        multiPart.getField("photo0").getValueAs(InputStream.class));
    System.out.println("Node: " + node);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  @POST
  @Path("/test3")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public FormDataBodyPart test3(final FormDataMultiPart multiPart) {
    System.out.println("Coucou3");
    System.out.println("FormDataMultiPart: " + multiPart);
    System.out.println("Name of field 'photo0': " + multiPart.getField("photo0").getName());
    System.out.println("Value: " + multiPart.getField("photo0").getValueAs(InputStream.class));
    System.out.println("Value2: " + multiPart.getField("photo0"));
    System.out.println("Entity: " + multiPart.getField("photo0").getEntity());

    return multiPart.getField("photo0");
  }

  // save uploaded file to new location
  private OutputStream writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
    try {
      OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
      int read = 0;
      byte[] bytes = new byte[1024];

      out = new FileOutputStream(new File(uploadedFileLocation));
      while ((read = uploadedInputStream.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
      // System.out.println(out);
      // System.out.println(bytes);
      out.flush();
      out.close();
      return out;
    } catch (IOException e) {
      e.printStackTrace();
      throw new PresentationException("A name cannot be empty.", e, Status.BAD_REQUEST);
    }
  }

  private static String encodeFileToBase64Binary(File file) {
    String encodedfile = null;
    try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
      byte[] bytes = new byte[(int) file.length()];
      fileInputStreamReader.read(bytes);
      encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return encodedfile;
  }

  // @POST
  // @Path("/test1")
  // @Consumes("multipart/mixed")
  // public Response test1(final FormDataMultiPart multiPart) {
  // System.out.println("Coucou");
  // System.out.println(multiPart);
  // return createResponseWithObjectNodeWith1PutPOJO("furniture", 31);
  // }

  // @POST
  // @Path("/test2")
  // @Consumes(MediaType.MULTIPART_FORM_DATA)
  // public Response test2(final FormDataMultiPart multiPart) {
  // System.out.println("Coucou");
  // System.out.println(multiPart);
  // return createResponseWithObjectNodeWith1PutPOJO("furniture", 31);
  // }

  // @POST
  // @Path("/test3")
  // @Consumes(MediaType.MULTIPART_FORM_DATA)
  // public Response test3(@FormDataParam("furnitureId") int id,
  // @FormDataParam("photo0") FileDataBodyPart bean, @FormDataParam("photo1") InputStream file,
  // @FormDataParam("photo2") FormDataContentDisposition fileDisposition) {
  // System.out.println("Coucou");
  // System.out.println(id + " " + bean + " " + file + " " + fileDisposition);
  // return createResponseWithObjectNodeWith1PutPOJO("furniture", 31);
  // }

}
