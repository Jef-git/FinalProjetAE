package be.vinci.pae.domaine.address;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AddressImpl.class)
public interface AddressDTO {
  int getID();

  /**
   * Change the id of the user if is not already use in the DB.
   * 
   * @param id : not already use in the DB.
   */
  void setID(int id);

  String getStreet();

  /**
   * Change the street of the user.
   * 
   * @param street : has to be Not Null.
   */
  void setStreet(String street);

  String getBuildingNumber();

  void setBuildingNumber(String buildingNumber);

  String getUnitNumber();

  void setUnitNumber(String unitNumber);

  String getPostCode();

  void setPostCode(String postCode);

  String getCommune();

  /**
   * Change the commune of the user.
   * 
   * @param commune : as to be Not Null.
   */
  void setCommune(String commune);

  String getCountry();

  /**
   * Change the country of the user.
   * 
   * @param country : as to be Not Null.
   */
  void setCountry(String country);
}
